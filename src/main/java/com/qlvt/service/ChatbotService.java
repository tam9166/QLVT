package com.qlvt.service;

import com.qlvt.entity.ChatMessage;
import com.qlvt.entity.ChatSession;
import com.qlvt.entity.DepartmentStock;
import com.qlvt.entity.IssueSlip;
import com.qlvt.entity.Material;
import com.qlvt.entity.MaterialBatch;
import com.qlvt.entity.MaterialRequest;
import com.qlvt.entity.StockBalance;
import com.qlvt.entity.Warehouse;
import com.qlvt.enums.BatchStatus;
import com.qlvt.enums.ChatIntent;
import com.qlvt.enums.IssueStatus;
import com.qlvt.repository.ChatMessageRepository;
import com.qlvt.repository.ChatSessionRepository;
import com.qlvt.repository.DepartmentStockRepository;
import com.qlvt.repository.IssueSlipRepository;
import com.qlvt.repository.MaterialBatchRepository;
import com.qlvt.repository.MaterialRepository;
import com.qlvt.repository.MaterialRequestRepository;
import com.qlvt.repository.StockBalanceRepository;
import com.qlvt.util.VietnameseTextNormalizer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ChatbotService {
    private static final NumberFormat VI_NUMBER = NumberFormat.getIntegerInstance(new Locale("vi", "VN"));
    private static final String MEDICAL_SAFETY_REPLY = """
            Mình hỗ trợ phần quản lý vật tư: tra tồn, vị trí, lô, hạn dùng, phiếu và lịch sử nhận.
            Còn liều dùng, chẩn đoán hay chỉ định điều trị thì bạn cần hỏi bác sĩ hoặc dược sĩ phụ trách nhé.
            """;

    private final ChatbotNlpService nlpService;
    private final MaterialSearchService materialSearchService;
    private final MaterialRepository materialRepository;
    private final MaterialBatchRepository batchRepository;
    private final StockBalanceRepository balanceRepository;
    private final MaterialRequestRepository requestRepository;
    private final IssueSlipRepository issueSlipRepository;
    private final DepartmentStockRepository departmentStockRepository;
    private final ChatSessionRepository sessionRepository;
    private final ChatMessageRepository messageRepository;

    public ChatbotService(ChatbotNlpService nlpService,
                          MaterialSearchService materialSearchService,
                          MaterialRepository materialRepository,
                          MaterialBatchRepository batchRepository,
                          StockBalanceRepository balanceRepository,
                          MaterialRequestRepository requestRepository,
                          IssueSlipRepository issueSlipRepository,
                          DepartmentStockRepository departmentStockRepository,
                          ChatSessionRepository sessionRepository,
                          ChatMessageRepository messageRepository) {
        this.nlpService = nlpService;
        this.materialSearchService = materialSearchService;
        this.materialRepository = materialRepository;
        this.batchRepository = batchRepository;
        this.balanceRepository = balanceRepository;
        this.requestRepository = requestRepository;
        this.issueSlipRepository = issueSlipRepository;
        this.departmentStockRepository = departmentStockRepository;
        this.sessionRepository = sessionRepository;
        this.messageRepository = messageRepository;
    }

    @Transactional
    public ChatResponse answer(String question, String username, String department) {
        ChatSession session = currentSession(username);
        ChatbotNlpService.ParsedQuestion parsed = parseWithConversationContext(question, session);
        ChatResponse response = route(parsed, username, department, session.getId());

        saveMessage(session, "USER", question, parsed.intent(), response.answer());
        saveMessage(session, "BOT", response.answer(), parsed.intent(), response.answer());
        session.setUpdatedAt(LocalDateTime.now());
        sessionRepository.save(session);
        return response;
    }

    private ChatbotNlpService.ParsedQuestion parseWithConversationContext(String question, ChatSession session) {
        ChatbotNlpService.ParsedQuestion parsed = nlpService.parse(question);
        if (!shouldUseRecentMaterialContext(question, parsed)) {
            return parsed;
        }

        String enrichedQuestion = enrichWithRecentMaterial(question, session, true);
        if (enrichedQuestion.equals(question == null ? "" : question)) {
            return parsed;
        }

        ChatbotNlpService.ParsedQuestion enrichedParsed = nlpService.parse(enrichedQuestion);
        return enrichedParsed;
    }

    public List<Map<String, String>> recentHistory(String username) {
        return sessionRepository.findFirstByUserOrderByUpdatedAtDesc(username)
                .map(session -> messageRepository.findTop30BySession_IdOrderByCreatedAtAsc(session.getId()).stream()
                        .map(message -> Map.of("sender", message.getSenderType(), "message", message.getMessage() == null ? "" : message.getMessage()))
                        .toList())
                .orElse(List.of());
    }

    public void clearHistory(String username) {
        sessionRepository.findFirstByUserOrderByUpdatedAtDesc(username).ifPresent(session -> {
            messageRepository.deleteAll(messageRepository.findTop30BySession_IdOrderByCreatedAtAsc(session.getId()));
            session.setUpdatedAt(LocalDateTime.now());
            sessionRepository.save(session);
        });
    }

    private ChatResponse route(ChatbotNlpService.ParsedQuestion parsed, String username, String userDepartment, Long sessionId) {
        if (parsed.ambiguousMaterial() && requiresSpecificMaterial(parsed.intent()) && !canAnswerWithoutMaterial(parsed)) {
            return ambiguousMaterial(parsed, sessionId);
        }

        return switch (parsed.intent()) {
            case GENERAL_HELP, HELP -> simple(parsed.intent(), help(), helpSuggestions(), sessionId);
            case UNKNOWN -> simple(ChatIntent.UNKNOWN, MEDICAL_SAFETY_REPLY, List.of("Tra tồn kho", "Xem lô sắp hết hạn"), sessionId);
            case ASK_LOW_STOCK -> lowStockReport(parsed, sessionId);
            case ASK_EXPIRED_OR_NEAR_EXPIRED, CHECK_DEPARTMENT_EXPIRING_MATERIALS -> expiringReport(parsed, sessionId);
            case ASK_RECOMMEND_ISSUE -> recommendIssue(parsed, sessionId);
            case ASK_LOCATION, CHECK_LOCATION -> materialInventory(parsed, "LOCATION", sessionId);
            case ASK_EXPIRY, ASK_IMPORT_DATE, ASK_BATCH, CHECK_EXPIRY, CHECK_BATCH -> {
                ChatResponse batchResponse = batchLookup(parsed, sessionId);
                if (batchResponse != null) {
                    yield batchResponse;
                }
                yield materialInventory(parsed, "BATCH", sessionId);
            }
            case ASK_STOCK, CHECK_STOCK -> {
                if (parsed.department() != null || containsDepartmentScope(parsed.normalizedMessage())) {
                    yield departmentStock(parsed, firstNonBlank(parsed.department(), userDepartment), sessionId);
                }
                yield materialInventory(parsed, "STOCK", sessionId);
            }
            case CHECK_DEPARTMENT_STOCK -> {
                if (parsed.ambiguousMaterial() && !isGenericDepartmentStockQuestion(parsed.normalizedMessage())) {
                    yield ambiguousMaterial(parsed, sessionId);
                }
                yield departmentStock(parsed, firstNonBlank(parsed.department(), userDepartment), sessionId);
            }
            case CHECK_SUPPLIER -> checkSupplier(parsed, sessionId);
            case SUGGEST_ALTERNATIVE -> suggestAlternative(parsed, sessionId);
            case CHECK_REQUEST_STATUS -> simple(parsed.intent(), checkRequestStatus(username, userDepartment), List.of("Tạo yêu cầu cấp phát", "Xem thông báo"), sessionId);
            case CREATE_REQUEST_DRAFT -> simple(parsed.intent(), createRequestDraft(parsed), List.of("Mở /requests/new", "Kiểm tra tồn trước khi xin"), sessionId);
            case CHECK_RECEIVED_HISTORY -> simple(parsed.intent(), receivedHistory(username, userDepartment), List.of("Xem tồn tại khoa", "Xem phiếu của tôi"), sessionId);
            default -> searchMaterial(parsed, sessionId);
        };
    }

    private ChatResponse materialInventory(ChatbotNlpService.ParsedQuestion parsed, String mode, Long sessionId) {
        if (!parsed.hasMaterial()) {
            return missingMaterial(parsed.intent(), sessionId);
        }

        List<ChatItem> items = new ArrayList<>();
        StringBuilder answer = new StringBuilder();
        for (Material material : parsed.targetMaterials()) {
            List<StockBalance> balances = balancesFor(material, parsed.warehouse());
            long totalAvailable = balances.stream().mapToLong(this::available).sum();
            long totalActual = balances.stream().mapToLong(balance -> Math.max(0, balance.getActualQuantity())).sum();
            String status = stockStatus(material, totalAvailable);

            if (!answer.isEmpty()) {
                answer.append("\n\n");
            }
            answer.append(stockOpening(material, totalAvailable, status));
            appendQuantityCheck(answer, parsed, material, totalAvailable);
            if (balances.isEmpty()) {
                answer.append("\n\nMình chưa thấy tồn theo kho/kệ/lô cho vật tư này trong dữ liệu hiện tại.");
                items.add(materialOnlyItem(material, totalAvailable, status));
                continue;
            }

            answer.append("\n\nChi tiết:");
            for (StockBalance balance : balances.stream().limit(8).toList()) {
                ChatItem item = itemFromBalance(balance, totalAvailable);
                items.add(item);
                answer.append("\n* ")
                        .append(item.warehouseName()).append(": ")
                        .append(formatNumber(available(balance))).append(" ").append(item.unit())
                        .append(", ").append(item.locationName())
                        .append(", lô ").append(item.batchCode())
                        .append(", nhập ").append(nullSafe(item.importDate()))
                        .append(", HSD ").append(nullSafe(item.expiryDate()))
                        .append(statusSuffix(item.status()));
            }

            appendFefoFulfillmentPlan(answer, parsed, material, balances);
            appendModeSpecificHint(answer, mode, balances, material, totalActual);
        }
        return new ChatResponse(true, parsed.intent().name(), answer.toString(), answer.toString(), items, inventorySuggestions(parsed), sessionId);
    }

    private void appendQuantityCheck(StringBuilder answer, ChatbotNlpService.ParsedQuestion parsed, Material material, long totalAvailable) {
        Integer requestedQuantity = parsed.requestedQuantity();
        if (requestedQuantity == null || requestedQuantity <= 0) {
            return;
        }
        String scope = parsed.warehouse() == null ? "trong hệ thống" : "tại " + parsed.warehouse().getName();
        if (totalAvailable >= requestedQuantity) {
            answer.append("\nMình kiểm tra nhanh: đủ để cấp ")
                    .append(formatNumber(requestedQuantity)).append(" ").append(nullSafe(material.getUnit()))
                    .append(" ").append(scope).append(".");
        } else {
            answer.append("\nMình kiểm tra nhanh: chưa đủ ")
                    .append(formatNumber(requestedQuantity)).append(" ").append(nullSafe(material.getUnit()))
                    .append(" ").append(scope).append("; còn thiếu khoảng ")
                    .append(formatNumber(requestedQuantity - totalAvailable)).append(" ").append(nullSafe(material.getUnit()))
                    .append(".");
        }
    }

    private void appendFefoFulfillmentPlan(StringBuilder answer,
                                           ChatbotNlpService.ParsedQuestion parsed,
                                           Material material,
                                           List<StockBalance> balances) {
        Integer requestedQuantity = parsed.requestedQuantity();
        if (requestedQuantity == null || requestedQuantity <= 0) {
            return;
        }

        List<StockBalance> issuableBalances = balances.stream()
                .filter(balance -> available(balance) > 0 && isIssuable(balance))
                .toList();
        if (issuableBalances.isEmpty()) {
            answer.append("\n\nChưa có lô còn hạn/trạng thái AVAILABLE để lập kế hoạch cấp phát FEFO cho yêu cầu ")
                    .append(formatNumber(requestedQuantity)).append(" ").append(nullSafe(material.getUnit())).append(".");
            return;
        }

        long remaining = requestedQuantity;
        answer.append("\n\nKế hoạch cấp phát FEFO đề xuất:");
        for (StockBalance balance : issuableBalances) {
            if (remaining <= 0) {
                break;
            }
            long take = Math.min(remaining, available(balance));
            answer.append("\n* Lấy ").append(formatNumber(take)).append(" ").append(nullSafe(material.getUnit()))
                    .append(" từ ").append(warehouseLabel(balance)).append(" / ").append(locationLabel(balance))
                    .append(", lô ").append(batchLabel(balance))
                    .append(", HSD ").append(format(balance.getBatch().getExpiryDate()));
            remaining -= take;
        }

        long plannedQuantity = requestedQuantity - remaining;
        if (plannedQuantity >= requestedQuantity) {
            answer.append("\nTổng kế hoạch trên đủ ")
                    .append(formatNumber(requestedQuantity)).append(" ").append(nullSafe(material.getUnit()))
                    .append("; nên xuất theo đúng thứ tự FEFO để giảm rủi ro hết hạn.");
        } else {
            answer.append("\nKế hoạch hiện chỉ gom được ")
                    .append(formatNumber(plannedQuantity)).append(" ").append(nullSafe(material.getUnit()))
                    .append(", còn thiếu khoảng ")
                    .append(formatNumber(requestedQuantity - plannedQuantity)).append(" ").append(nullSafe(material.getUnit()))
                    .append(". Nên tạo đề nghị mua/bổ sung hoặc kiểm tra tồn thực tế trước khi xác nhận cấp phát.");
        }
    }

    private boolean requiresSpecificMaterial(ChatIntent intent) {
        return switch (intent) {
            case ASK_STOCK, ASK_LOCATION, ASK_EXPIRY, ASK_IMPORT_DATE, ASK_BATCH, ASK_RECOMMEND_ISSUE,
                 CHECK_STOCK, CHECK_LOCATION, CHECK_EXPIRY, CHECK_BATCH, CHECK_SUPPLIER,
                 SUGGEST_ALTERNATIVE, CREATE_REQUEST_DRAFT -> true;
            default -> false;
        };
    }

    private boolean canAnswerWithoutMaterial(ChatbotNlpService.ParsedQuestion parsed) {
        return switch (parsed.intent()) {
            case ASK_STOCK, CHECK_STOCK, CHECK_DEPARTMENT_STOCK -> isGenericDepartmentStockQuestion(parsed.normalizedMessage());
            default -> false;
        };
    }

    private boolean isGenericDepartmentStockQuestion(String normalizedMessage) {
        return containsDepartmentScope(normalizedMessage)
                && VietnameseTextNormalizer.containsAnyKeyword(normalizedMessage,
                "vat tu gi", "con gi", "dang giu gi", "co gi", "co nhung gi", "danh sach", "tat ca", "nhung vat tu");
    }

    private void appendModeSpecificHint(StringBuilder answer, String mode, List<StockBalance> balances, Material material, long totalActual) {
        Optional<StockBalance> fefo = balances.stream().filter(balance -> available(balance) > 0 && isIssuable(balance)).findFirst();
        if ("LOCATION".equals(mode)) {
            answer.append("\n\nMình đã liệt kê kho, kệ/vị trí và lô để bạn dễ tìm khi đi lấy hàng.");
        } else if ("BATCH".equals(mode)) {
            answer.append("\n\nCác dòng trên có ngày nhập và hạn dùng để bạn kiểm tra lô trước khi cấp phát.");
        }
        fefo.ifPresent(balance -> answer.append("\nGợi ý FEFO: nên ưu tiên lô ")
                .append(batchLabel(balance))
                .append(" tại ").append(warehouseLabel(balance)).append(" / ").append(locationLabel(balance))
                .append(" vì ")
                .append(balance.getBatch().getExpiryDate() == null
                        ? "lô này có ngày nhập sớm hơn trong các lô còn khả dụng."
                        : "hạn dùng gần nhất là " + format(balance.getBatch().getExpiryDate()) + "."));

        long expiredCount = balances.stream().filter(this::isExpired).count();
        if (expiredCount > 0) {
            answer.append("\nLưu ý: có ").append(expiredCount)
                    .append(" dòng tồn đã hết hạn hoặc không nên cấp phát. Vui lòng kiểm tra module Thu hồi/Hủy vật tư.");
        }
        if (totalActual == 0 && material.getAvailableQuantity() == 0) {
            answer.append("\nHiện vật tư này đang hết hàng trong hệ thống.");
        }
    }

    private ChatResponse recommendIssue(ChatbotNlpService.ParsedQuestion parsed, Long sessionId) {
        if (!parsed.hasMaterial()) {
            return globalFefoRecommendation(parsed, sessionId);
        }
        Material material = parsed.firstMaterial().orElseThrow();
        List<StockBalance> fefoBalances = parsed.warehouse() == null
                ? balanceRepository.findAvailableFefo(material.getId(), LocalDate.now())
                : balanceRepository.findAvailableFefoInWarehouse(material.getId(), parsed.warehouse().getId(), LocalDate.now());
        if (fefoBalances.isEmpty()) {
            String answer = "Mình chưa thấy lô còn khả dụng để cấp phát cho " + materialLine(material)
                    + ". Nếu cần gấp, bạn nên kiểm tra tồn thực tế hoặc tạo đề nghị mua/bổ sung.";
            return simple(ChatIntent.ASK_RECOMMEND_ISSUE, answer, List.of("Xem tồn thấp", "Tạo đề nghị mua"), sessionId);
        }

        StockBalance first = fefoBalances.get(0);
        ChatItem item = itemFromBalance(first, fefoBalances.stream().mapToLong(this::available).sum());
        String reason = first.getBatch().getExpiryDate() == null
                ? "lô này được nhập sớm nhất trong các lô không có hạn dùng rõ ràng, phù hợp nguyên tắc FIFO."
                : "lô này có hạn dùng gần nhất là " + format(first.getBatch().getExpiryDate()) + ", phù hợp nguyên tắc FEFO.";
        String answer = "Bạn nên lấy " + material.getName() + " từ "
                + item.warehouseName() + ", " + item.locationName() + ", lô " + item.batchCode()
                + " trước. Hiện lô này còn " + formatNumber(item.availableQuantity()) + " " + item.unit()
                + ".\n\nLý do: " + reason;
        return new ChatResponse(true, ChatIntent.ASK_RECOMMEND_ISSUE.name(), answer, answer, List.of(item), List.of("Xem vị trí trong kho", "Tạo yêu cầu cấp phát"), sessionId);
    }

    private ChatResponse globalFefoRecommendation(ChatbotNlpService.ParsedQuestion parsed, Long sessionId) {
        List<StockBalance> balances = balanceRepository.findAll().stream()
                .filter(balance -> parsed.warehouse() == null || balance.getWarehouse().getId().equals(parsed.warehouse().getId()))
                .filter(balance -> available(balance) > 0 && isIssuable(balance))
                .sorted(Comparator.comparing((StockBalance balance) -> balance.getBatch().getExpiryDate(), Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(balance -> balance.getBatch().getReceiptDate(), Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(balance -> balance.getMaterial().getCode())
                        .thenComparing(balance -> balance.getWarehouse().getCode())
                        .thenComparing(balance -> balance.getLocation().getCode()))
                .limit(8)
                .toList();
        if (balances.isEmpty()) {
            String scope = parsed.warehouse() == null ? "" : " táº¡i " + parsed.warehouse().getName();
            String answer = "MÃ¬nh chÆ°a tháº¥y lÃ´ cÃ²n kháº£ dá»¥ng Ä‘á»ƒ gá»£i Ã½ FEFO" + scope
                    + ". Báº¡n nÃªn kiá»ƒm tra tá»“n thá»±c táº¿, lÃ´ háº¿t háº¡n vÃ  cÃ¡c phiáº¿u Ä‘ang giá»¯ hÃ ng.";
            return simple(ChatIntent.ASK_RECOMMEND_ISSUE, answer, List.of("Xem lÃ´ sáº¯p háº¿t háº¡n", "Tra má»™t váº­t tÆ° cá»¥ thá»ƒ"), sessionId);
        }

        List<ChatItem> items = balances.stream()
                .map(balance -> itemFromBalance(balance, available(balance)))
                .toList();
        String scope = parsed.warehouse() == null ? "toÃ n há»‡ thá»‘ng" : parsed.warehouse().getName();
        StringBuilder answer = new StringBuilder("CÃ¡c lÃ´ nÃªn Æ°u tiÃªn xuáº¥t trÆ°á»›c theo FEFO trong ")
                .append(scope).append(":");
        for (ChatItem item : items) {
            answer.append("\n* ")
                    .append(item.materialCode()).append(" - ").append(item.materialName())
                    .append(" | lÃ´ ").append(item.batchCode())
                    .append(" | cÃ²n ").append(formatNumber(item.availableQuantity())).append(" ").append(item.unit())
                    .append(" | ").append(item.warehouseName()).append(" / ").append(item.locationName())
                    .append(" | HSD ").append(item.expiryDate());
        }
        answer.append("\n\nKhi cáº¥p phÃ¡t cho má»™t váº­t tÆ° cá»¥ thá»ƒ, báº¡n cÃ³ thá»ƒ há»i \"cáº§n 20 kháº©u trang thÃ¬ láº¥y lÃ´ nÃ o trÆ°á»›c\" Ä‘á»ƒ mÃ¬nh láº­p káº¿ hoáº¡ch sá»‘ lÆ°á»£ng theo tá»«ng lÃ´.");
        return new ChatResponse(true, ChatIntent.ASK_RECOMMEND_ISSUE.name(), answer.toString(), answer.toString(), items,
                List.of("Cáº§n 20 kháº©u trang láº¥y lÃ´ nÃ o?", "Xem lÃ´ sáº¯p háº¿t háº¡n"), sessionId);
    }

    private ChatResponse batchLookup(ChatbotNlpService.ParsedQuestion parsed, Long sessionId) {
        List<MaterialBatch> batches = batchRepository.findAll().stream()
                .filter(batch -> batch.getBatchNumber() != null)
                .filter(batch -> parsed.normalizedMessage().contains(VietnameseTextNormalizer.normalizeSearchText(batch.getBatchNumber())))
                .limit(5)
                .toList();
        if (batches.isEmpty()) {
            return null;
        }

        List<ChatItem> items = new ArrayList<>();
        StringBuilder answer = new StringBuilder("Mình tìm thấy thông tin lô bạn hỏi:");
        for (MaterialBatch batch : batches) {
            List<StockBalance> balances = balanceRepository.findAll().stream()
                    .filter(balance -> balance.getBatch() != null && balance.getBatch().getId().equals(batch.getId()))
                    .filter(balance -> balance.getActualQuantity() > 0 || balance.getReservedQuantity() > 0 || balance.getPendingIssueQuantity() > 0)
                    .toList();
            if (balances.isEmpty()) {
                ChatItem item = itemFromBatch(batch);
                items.add(item);
                answer.append(batchLine(item));
                continue;
            }
            for (StockBalance balance : balances) {
                ChatItem item = itemFromBalance(balance, available(balance));
                items.add(item);
                answer.append(batchLine(item));
            }
        }
        answer.append("\n\nNếu cần cấp phát, hãy ưu tiên lô còn hạn và trạng thái AVAILABLE; lô hết hạn/khóa nên xử lý ở Thu hồi/Hủy.");
        return new ChatResponse(true, parsed.intent().name(), answer.toString(), answer.toString(), items, List.of("Nên xuất lô nào trước?", "Xem lô sắp hết hạn"), sessionId);
    }

    private String batchLine(ChatItem item) {
        return "\n* " + item.materialName()
                + " | lô " + item.batchCode()
                + " | còn khả dụng " + formatNumber(item.availableQuantity()) + " " + item.unit()
                + " | " + item.warehouseName() + " / " + item.locationName()
                + " | nhập " + item.importDate()
                + " | HSD " + item.expiryDate()
                + statusSuffix(item.status());
    }

    private ChatResponse expiringReport(ChatbotNlpService.ParsedQuestion parsed, Long sessionId) {
        int days = parsed.expiryWindowDays();
        LocalDate today = LocalDate.now();
        LocalDate until = today.plusDays(days);
        List<StockBalance> balances = balanceRepository.findAll().stream()
                .filter(balance -> balance.getBatch() != null && balance.getBatch().getExpiryDate() != null)
                .filter(balance -> parsed.warehouse() == null || balance.getWarehouse().getId().equals(parsed.warehouse().getId()))
                .filter(balance -> !parsed.hasMaterial() || parsed.materials().stream().anyMatch(material -> material.getId().equals(balance.getMaterial().getId())))
                .filter(balance -> balance.getBatch().getExpiryDate().isBefore(today) || !balance.getBatch().getExpiryDate().isAfter(until))
                .filter(balance -> balance.getActualQuantity() > 0)
                .sorted(Comparator.comparing(balance -> balance.getBatch().getExpiryDate()))
                .limit(20)
                .toList();

        if (balances.isEmpty()) {
            String target = parsed.hasMaterial() ? " cho " + materialLine(parsed.firstMaterial().orElseThrow()) : "";
            String answer = "Mình kiểm tra rồi, hiện chưa thấy lô sắp hết hạn trong " + days + " ngày tới" + target + ".";
            return simple(ChatIntent.ASK_EXPIRED_OR_NEAR_EXPIRED, answer, List.of("Xem tồn thấp", "Kiểm tra vật tư khác"), sessionId);
        }

        List<ChatItem> items = balances.stream().map(balance -> itemFromBalance(balance, available(balance))).toList();
        StringBuilder answer = new StringBuilder("Mình thấy ")
                .append(items.size()).append(" lô cần chú ý trong ").append(days).append(" ngày tới:");
        for (ChatItem item : items) {
            answer.append("\n* ")
                    .append(item.materialName()).append(" | lô ").append(item.batchCode())
                    .append(" | còn ").append(formatNumber(item.availableQuantity())).append(" ").append(item.unit())
                    .append(" | ").append(item.warehouseName()).append(" / ").append(item.locationName())
                    .append(" | HSD ").append(item.expiryDate())
                    .append(statusSuffix(item.status()));
        }
        answer.append("\n\nBạn nên ưu tiên rà soát FEFO, khóa cấp phát lô hết hạn và chuyển sang Thu hồi/Hủy nếu cần.");
        return new ChatResponse(true, ChatIntent.ASK_EXPIRED_OR_NEAR_EXPIRED.name(), answer.toString(), answer.toString(), items, List.of("Xem module Lô/HSD", "Tạo cảnh báo hôm nay"), sessionId);
    }

    private ChatResponse lowStockReport(ChatbotNlpService.ParsedQuestion parsed, Long sessionId) {
        boolean outOfStockOnly = VietnameseTextNormalizer.containsAnyKeyword(parsed.normalizedMessage(), "het hang", "het kho");
        List<Material> materials = materialRepository.findByDeletedFalseOrderByCodeAsc().stream()
                .filter(material -> outOfStockOnly ? material.getAvailableQuantity() <= 0 : material.getAvailableQuantity() <= Math.max(1, material.getMinStock()))
                .limit(15)
                .toList();
        if (materials.isEmpty()) {
            String answer = outOfStockOnly
                    ? "Mình chưa thấy vật tư nào hết hàng theo số liệu hiện tại."
                    : "Mình chưa thấy vật tư nào dưới ngưỡng tồn tối thiểu theo số liệu hiện tại.";
            return simple(ChatIntent.ASK_LOW_STOCK, answer, List.of("Xem lô sắp hết hạn", "Tra một vật tư cụ thể"), sessionId);
        }

        List<ChatItem> items = materials.stream()
                .map(material -> materialOnlyItem(material, material.getAvailableQuantity(), stockStatus(material, material.getAvailableQuantity())))
                .toList();
        StringBuilder answer = new StringBuilder(outOfStockOnly
                ? "Hiện có các vật tư đang hết hàng hoặc không còn khả dụng:"
                : "Hiện có các vật tư đang tồn thấp:");
        int index = 1;
        for (Material material : materials) {
            answer.append("\n").append(index++).append(". ")
                    .append(material.getName())
                    .append(": còn ").append(formatNumber(material.getAvailableQuantity())).append(" ").append(nullSafe(material.getUnit()))
                    .append(", mức tối thiểu ").append(formatNumber(material.getMinStock())).append(" ").append(nullSafe(material.getUnit()));
        }
        answer.append("\n\nBạn nên kiểm tra tồn thực tế và tạo đề nghị mua/bổ sung cho các vật tư này.");
        return new ChatResponse(true, ChatIntent.ASK_LOW_STOCK.name(), answer.toString(), answer.toString(), items, List.of("Mở /purchases/requests", "Xem báo cáo tồn kho"), sessionId);
    }

    private ChatResponse departmentStock(ChatbotNlpService.ParsedQuestion parsed, String department, Long sessionId) {
        if (department == null || department.isBlank()) {
            String answer = "Mình chưa xác định được khoa/phòng cần tra. Bạn có thể hỏi rõ hơn, ví dụ: \"khoa cấp cứu còn bộ dịch truyền không?\".";
            return simple(ChatIntent.CHECK_DEPARTMENT_STOCK, answer, List.of("Tra tồn kho tổng", "Tạo yêu cầu cấp phát"), sessionId);
        }
        List<DepartmentStock> stocks = departmentStockRepository.findByDepartmentAndQuantityOnHandGreaterThanOrderByMaterial_CodeAscBatch_ExpiryDateAsc(department, 0);
        if (parsed.hasMaterial()) {
            stocks = stocks.stream()
                    .filter(stock -> parsed.materials().stream().anyMatch(material -> material.getId().equals(stock.getMaterial().getId())))
                    .toList();
        } else {
            stocks = stocks.stream().limit(10).toList();
        }
        if (stocks.isEmpty()) {
            String target = parsed.hasMaterial() ? " cho " + materialLine(parsed.firstMaterial().orElseThrow()) : "";
            String answer = department + " hiện chưa còn tồn" + target + " trong dữ liệu khoa/phòng.";
            return simple(ChatIntent.CHECK_DEPARTMENT_STOCK, answer, List.of("Tạo yêu cầu cấp phát", "Tra tồn kho tổng"), sessionId);
        }

        List<ChatItem> items = stocks.stream().map(this::itemFromDepartmentStock).toList();
        StringBuilder answer = new StringBuilder("Tồn tại ").append(department).append(":");
        for (ChatItem item : items) {
            answer.append("\n* ")
                    .append(item.materialName()).append(": còn ").append(formatNumber(item.availableQuantity())).append(" ").append(item.unit())
                    .append(", lô ").append(item.batchCode())
                    .append(", HSD ").append(nullSafe(item.expiryDate()))
                    .append(statusSuffix(item.status()));
        }
        return new ChatResponse(true, ChatIntent.CHECK_DEPARTMENT_STOCK.name(), answer.toString(), answer.toString(), items, List.of("Tạo yêu cầu cấp phát", "Báo vật tư hỏng/mất"), sessionId);
    }

    private ChatResponse checkSupplier(ChatbotNlpService.ParsedQuestion parsed, Long sessionId) {
        if (!parsed.hasMaterial()) {
            return missingMaterial(ChatIntent.CHECK_SUPPLIER, sessionId);
        }
        Material material = parsed.firstMaterial().orElseThrow();
        List<MaterialBatch> batches = batchRepository.findAll().stream()
                .filter(batch -> batch.getMaterial().getId().equals(material.getId()))
                .filter(batch -> batch.getSupplier() != null)
                .sorted(Comparator.comparing(MaterialBatch::getReceiptDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5)
                .toList();
        if (batches.isEmpty()) {
            return simple(ChatIntent.CHECK_SUPPLIER, "Mình chưa thấy nhà cung cấp nào được ghi nhận cho " + materialLine(material) + ".", List.of("Xem lịch sử giá nhập"), sessionId);
        }
        StringBuilder answer = new StringBuilder("Các nhà cung cấp gần đây của ").append(materialLine(material)).append(":");
        batches.forEach(batch -> answer.append("\n* ")
                .append(batch.getSupplier().getName())
                .append(", lô ").append(batch.getBatchNumber())
                .append(", nhập ngày ").append(format(batch.getReceiptDate())));
        return simple(ChatIntent.CHECK_SUPPLIER, answer.toString(), List.of("Xem lịch sử giá nhập", "Xem đơn mua"), sessionId);
    }

    private ChatResponse suggestAlternative(ChatbotNlpService.ParsedQuestion parsed, Long sessionId) {
        if (!parsed.hasMaterial()) {
            return missingMaterial(ChatIntent.SUGGEST_ALTERNATIVE, sessionId);
        }
        Material material = parsed.firstMaterial().orElseThrow();
        List<Material> alternatives = materialSearchService.alternatives(material, 5);
        if (alternatives.isEmpty()) {
            return simple(ChatIntent.SUGGEST_ALTERNATIVE, "Mình chưa thấy vật tư thay thế phù hợp cho " + materialLine(material) + " trong dữ liệu hiện tại.", List.of("Tra tồn kho vật tư khác"), sessionId);
        }
        String answer = "Có vài vật tư gần nhóm với " + materialLine(material) + ". Bạn vẫn nên kiểm tra lại trước khi dùng:\n"
                + listMaterials(alternatives);
        return simple(ChatIntent.SUGGEST_ALTERNATIVE, answer, List.of("Tra tồn từng vật tư", "Tạo yêu cầu cấp phát"), sessionId);
    }

    private ChatResponse searchMaterial(ChatbotNlpService.ParsedQuestion parsed, Long sessionId) {
        if (parsed.hasMaterial()) {
            Material material = parsed.firstMaterial().orElseThrow();
            String answer = "Mình tìm thấy vật tư này:\n" + materialSummary(material)
                    + "\n\nBạn có thể hỏi tiếp: \"" + material.getName() + " còn bao nhiêu?\" hoặc \""
                    + material.getName() + " nằm ở đâu?\"";
            return simple(ChatIntent.SEARCH_MATERIAL, answer, inventorySuggestions(parsed), sessionId);
        }
        if (!parsed.candidates().isEmpty()) {
            return ambiguousMaterial(parsed, sessionId);
        }
        String answer = """
                Mình chưa tìm thấy vật tư khớp với câu hỏi này, nên mình không tự đoán.
                Bạn thử gửi mã vật tư, tên đầy đủ hơn, hoặc một cụm dễ nhận diện hơn nhé. Ví dụ: "găng tay size M", "khẩu trang y tế", hoặc "VT002".
                """;
        return simple(ChatIntent.SEARCH_MATERIAL, answer, List.of("Còn bao nhiêu khẩu trang?", "Vật tư nào tồn thấp?"), sessionId);
    }

    private ChatResponse ambiguousMaterial(ChatbotNlpService.ParsedQuestion parsed, Long sessionId) {
        StringBuilder answer = new StringBuilder("Mình tìm thấy vài vật tư khá giống nhau, nên chưa chọn thay bạn để tránh sai dữ liệu:\n");
        int index = 1;
        for (Material material : parsed.candidates()) {
            answer.append(index++).append(". ").append(materialLine(material))
                    .append(" | còn có thể cấp ").append(formatNumber(material.getAvailableQuantity())).append(" ")
                    .append(nullSafe(material.getUnit())).append("\n");
        }
        answer.append("\nBạn nhắn lại bằng mã hoặc tên đầy đủ hơn, ví dụ: \"")
                .append(parsed.candidates().isEmpty() ? "VT002" : parsed.candidates().get(0).getCode())
                .append(" còn bao nhiêu?\".");
        List<ChatItem> items = parsed.candidates().stream()
                .map(material -> materialOnlyItem(material, material.getAvailableQuantity(), stockStatus(material, material.getAvailableQuantity())))
                .toList();
        return new ChatResponse(false, parsed.intent().name(), answer.toString().strip(), answer.toString().strip(), items, List.of("Nhập mã vật tư", "Nhập thêm size/quy cách"), sessionId);
    }

    private ChatResponse missingMaterial(ChatIntent intent, Long sessionId) {
        String answer = "Bạn muốn kiểm tra vật tư nào? Bạn có thể nhập như: \"còn bao nhiêu khẩu trang\", \"bộ dịch truyền ở kho nào\" hoặc \"bơm tiêm 5ml còn bao nhiêu\".";
        return simple(intent, answer, List.of("Còn bao nhiêu khẩu trang?", "Bộ dịch truyền nằm ở đâu?"), sessionId);
    }

    private List<StockBalance> balancesFor(Material material, Warehouse warehouse) {
        List<StockBalance> balances = balanceRepository.findByMaterial_IdOrderByWarehouse_CodeAscLocation_CodeAsc(material.getId()).stream()
                .filter(balance -> warehouse == null || balance.getWarehouse().getId().equals(warehouse.getId()))
                .filter(balance -> balance.getActualQuantity() > 0 || balance.getReservedQuantity() > 0 || balance.getPendingIssueQuantity() > 0)
                .sorted(Comparator.comparing((StockBalance balance) -> balance.getBatch().getExpiryDate(), Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(balance -> balance.getBatch().getReceiptDate(), Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(balance -> balance.getWarehouse().getCode())
                        .thenComparing(balance -> balance.getLocation().getCode()))
                .toList();
        if (!balances.isEmpty()) {
            return balances;
        }
        return warehouse == null
                ? balanceRepository.findAvailableFefo(material.getId(), LocalDate.now())
                : balanceRepository.findAvailableFefoInWarehouse(material.getId(), warehouse.getId(), LocalDate.now());
    }

    private ChatItem itemFromBalance(StockBalance balance, long totalAvailable) {
        Material material = balance.getMaterial();
        MaterialBatch batch = balance.getBatch();
        return new ChatItem(
                material.getId(),
                material.getCode(),
                material.getName(),
                (int) totalAvailable,
                nullSafe(material.getUnit()),
                warehouseLabel(balance),
                locationLabel(balance),
                batchLabel(balance),
                format(batch.getReceiptDate()),
                format(batch.getExpiryDate()),
                rowStatus(balance),
                available(balance),
                Math.max(0, balance.getActualQuantity()),
                Math.max(0, balance.getReservedQuantity()),
                daysToExpiry(batch.getExpiryDate()),
                statusNote(balance)
        );
    }

    private ChatItem itemFromDepartmentStock(DepartmentStock stock) {
        Material material = stock.getMaterial();
        MaterialBatch batch = stock.getBatch();
        return new ChatItem(
                material.getId(),
                material.getCode(),
                material.getName(),
                stock.getQuantityOnHand(),
                nullSafe(material.getUnit()),
                stock.getDepartment(),
                "Tồn tại khoa",
                batch == null ? "-" : nullSafe(batch.getBatchNumber()),
                stock.getLastReceivedAt() == null ? "-" : format(stock.getLastReceivedAt().toLocalDate()),
                batch == null ? "-" : format(batch.getExpiryDate()),
                batch != null && batch.getExpiryDate() != null && batch.getExpiryDate().isBefore(LocalDate.now()) ? "EXPIRED" : "AVAILABLE",
                stock.getQuantityOnHand(),
                stock.getQuantityOnHand(),
                0,
                batch == null ? null : daysToExpiry(batch.getExpiryDate()),
                "Tồn khoa/phòng"
        );
    }

    private ChatItem itemFromBatch(MaterialBatch batch) {
        Material material = batch.getMaterial();
        boolean issuable = batch.canIssue(LocalDate.now());
        String status = batch.getExpiryDate() != null && batch.getExpiryDate().isBefore(LocalDate.now())
                ? "EXPIRED"
                : issuable ? "AVAILABLE" : batch.getStatus().name();
        return new ChatItem(
                material.getId(),
                material.getCode(),
                material.getName(),
                batch.getQuantity(),
                nullSafe(material.getUnit()),
                batch.getWarehouse() == null ? "Kho chưa rõ" : batch.getWarehouse().getName(),
                batch.getLocation() == null ? "vị trí chưa rõ" : nullSafe(batch.getLocation().getName()),
                nullSafe(batch.getBatchNumber()),
                format(batch.getReceiptDate()),
                format(batch.getExpiryDate()),
                status,
                issuable ? batch.getQuantity() : 0,
                batch.getQuantity(),
                0,
                daysToExpiry(batch.getExpiryDate()),
                issuable ? "Có thể cấp phát" : "Cần kiểm tra trạng thái lô"
        );
    }

    private ChatItem materialOnlyItem(Material material, long totalAvailable, String status) {
        return new ChatItem(
                material.getId(), material.getCode(), material.getName(), (int) totalAvailable, nullSafe(material.getUnit()),
                "-", "-", "-", "-", "-", status, (int) totalAvailable, material.getActualQuantity(), material.getReservedQuantity(), null, ""
        );
    }

    private ChatResponse simple(ChatIntent intent, String answer, List<String> suggestions, Long sessionId) {
        return new ChatResponse(true, intent.name(), answer.strip(), answer.strip(), List.of(), suggestions, sessionId);
    }

    private ChatSession currentSession(String username) {
        return sessionRepository.findFirstByUserOrderByUpdatedAtDesc(username).orElseGet(() -> {
            ChatSession created = new ChatSession();
            created.setUser(username);
            created.setTitle("Tra cứu QLVT");
            return sessionRepository.save(created);
        });
    }

    private boolean shouldUseRecentMaterialContext(String question, ChatbotNlpService.ParsedQuestion parsed) {
        if (!requiresSpecificMaterial(parsed.intent())) {
            return false;
        }
        String normalized = VietnameseTextNormalizer.normalizeSearchText(question);
        if (parsed.hasMaterial() && directlyMentionsResolvedMaterial(normalized, parsed)) {
            return false;
        }
        return normalized.length() <= 80 || VietnameseTextNormalizer.containsAnyKeyword(normalized,
                "vat tu nay", "vat tu do", "lo nay", "lo do", "hang nay", "hang do",
                "cai nay", "cai do", "no", "muc nay", "dong nay", "vua hoi", "vua noi",
                "vay", "the", "tiep", "lay lo nao", "xuat lo nao", "o dau", "han dung",
                "hsd", "nha cung cap", "xin them", "cap them", "lay them");
    }

    private boolean directlyMentionsResolvedMaterial(String normalizedQuestion, ChatbotNlpService.ParsedQuestion parsed) {
        return parsed.materials().stream().anyMatch(material -> {
            String code = VietnameseTextNormalizer.normalizeSearchText(material.getCode());
            String name = VietnameseTextNormalizer.normalizeSearchText(material.getName());
            if ((!code.isBlank() && normalizedQuestion.contains(code))
                    || (!name.isBlank() && normalizedQuestion.contains(name))) {
                return true;
            }
            String aliases = nullSafe(material.getAliasText());
            for (String alias : aliases.split("\\s*,\\s*|\\s*;\\s*")) {
                String normalizedAlias = VietnameseTextNormalizer.normalizeSearchText(alias);
                if (!normalizedAlias.isBlank() && normalizedQuestion.contains(normalizedAlias)) {
                    return true;
                }
            }
            return false;
        });
    }

    private String enrichWithRecentMaterial(String question, ChatSession session, boolean allowImplicitFollowUp) {
        String baseQuestion = question == null ? "" : question;
        String normalized = VietnameseTextNormalizer.normalizeSearchText(question);
        boolean explicitReference = VietnameseTextNormalizer.containsAnyKeyword(normalized,
                "vat tu nay", "vat tu do", "lo nay", "lo do", "hang nay", "hang do",
                "cai nay", "cai do", "no", "muc nay", "dong nay", "vua hoi", "vua noi");
        if (!explicitReference && !allowImplicitFollowUp) {
            return baseQuestion;
        }
        List<String> recentTexts = messageRepository.findTop30BySession_IdOrderByCreatedAtAsc(session.getId()).stream()
                .sorted(Comparator.comparing(ChatMessage::getCreatedAt).reversed())
                .map(message -> nullSafe(message.getMessage()) + "\n" + nullSafe(message.getResponse()))
                .limit(10)
                .toList();
        List<MaterialBatch> batches = batchRepository.findAll();
        List<Material> materials = materialRepository.findByDeletedFalseOrderByCodeAsc();
        if (VietnameseTextNormalizer.containsAnyKeyword(normalized, "lo nay", "lo do", "batch nay", "batch do")) {
            Optional<MaterialBatch> recentBatch = recentTexts.stream()
                    .map(VietnameseTextNormalizer::normalizeSearchText)
                    .flatMap(text -> batches.stream()
                            .filter(batch -> batch.getBatchNumber() != null)
                            .filter(batch -> text.contains(VietnameseTextNormalizer.normalizeSearchText(batch.getBatchNumber()))))
                    .findFirst();
            if (recentBatch.isPresent()) {
                return baseQuestion + " " + recentBatch.get().getBatchNumber();
            }
        }
        return recentTexts.stream()
                .map(VietnameseTextNormalizer::normalizeSearchText)
                .flatMap(text -> materials.stream()
                        .filter(material -> text.contains(VietnameseTextNormalizer.normalizeSearchText(material.getCode()))
                                || text.contains(VietnameseTextNormalizer.normalizeSearchText(material.getName()))))
                .findFirst()
                .map(material -> baseQuestion + " " + material.getCode())
                .orElse(baseQuestion);
    }

    private String checkRequestStatus(String username, String department) {
        List<MaterialRequest> requests = requestRepository.findTop10ByRequesterOrderByCreatedAtDesc(username);
        if (requests.isEmpty() && department != null && !department.isBlank()) {
            requests = requestRepository.findTop10ByDepartmentOrderByCreatedAtDesc(department);
        }
        if (requests.isEmpty()) {
            return "Mình chưa thấy phiếu yêu cầu nào trong phạm vi bạn được phép xem. Nếu vừa cần xin vật tư, bạn có thể tạo yêu cầu tại /requests/new.";
        }
        StringBuilder builder = new StringBuilder("Mình thấy các phiếu gần đây như sau:");
        requests.forEach(request -> builder.append("\n* ")
                .append(request.getCode())
                .append(" | ").append(format(request.getCreatedAt()))
                .append(" | ").append(request.getStatus())
                .append(request.getRejectedReason() == null ? "" : " | lý do: " + request.getRejectedReason())
                .append(" | /requests/").append(request.getId()));
        return builder.append("\nBạn bấm vào đường dẫn phiếu để xem chi tiết từng dòng vật tư.").toString();
    }

    private String createRequestDraft(ChatbotNlpService.ParsedQuestion parsed) {
        Material material = parsed.firstMaterial().orElse(null);
        Integer quantity = parsed.requestedQuantity() == null ? extractFirstNumber(parsed.rawMessage()) : parsed.requestedQuantity();
        if (material == null || quantity == null) {
            return """
                    Mình cần thêm một chút thông tin để lập đúng yêu cầu: vật tư nào và số lượng bao nhiêu.
                    Ví dụ bạn có thể nhắn: "mình cần xin 20 hộp găng tay size M" hoặc "tạo yêu cầu 10 VT001".
                    """;
        }
        return "Mình hiểu là bạn muốn lấy "
                + formatNumber(quantity) + " " + nullSafe(material.getUnit())
                + " của " + materialLine(material)
                + ".\nBạn mở trang tạo yêu cầu tại /requests/new để kiểm tra khoa/phòng, ghi lý do và gửi duyệt.";
    }

    private String receivedHistory(String username, String department) {
        List<IssueSlip> slips = issueSlipRepository.findTop10ByMaterialRequest_RequesterAndStatusOrderByCreatedAtDesc(username, IssueStatus.RECEIVED);
        if (slips.isEmpty() && department != null && !department.isBlank()) {
            slips = issueSlipRepository.findTop10ByDepartmentAndStatusOrderByCreatedAtDesc(department, IssueStatus.RECEIVED);
        }
        if (slips.isEmpty()) {
            return "Mình chưa thấy lịch sử nhận vật tư nào trong phạm vi bạn được phép xem.";
        }
        StringBuilder builder = new StringBuilder("Các lần nhận vật tư gần đây:");
        slips.forEach(slip -> slip.getLines().forEach(line -> builder.append("\n* ")
                .append(format(slip.getReceivedAt()))
                .append(" | ").append(materialLine(line.getMaterial()))
                .append(" | SL ").append(formatNumber(line.getIssuedQuantity()))
                .append(" | phiếu ").append(slip.getIssueCode())));
        return builder.toString();
    }

    private String help() {
        return """
                Mình có thể hỗ trợ bạn tra dữ liệu QLVT bằng câu hỏi tự nhiên.
                Bạn có thể hỏi như:
                * "còn bao nhiêu khẩu trang?"
                * "bộ dịch truyền nằm ở đâu?"
                * "vật tư nào sắp hết?"
                * "có lô nào hết hạn trong 30 ngày tới không?"
                * "tôi cần lấy khẩu trang thì lấy lô nào trước?"
                * "khoa cấp cứu còn bộ dịch truyền không?"

                Mình chỉ dùng dữ liệu trong hệ thống. Nếu tên vật tư mơ hồ, mình sẽ hỏi lại thay vì tự đoán.
                """;
    }

    private String materialSummary(Material material) {
        return materialLine(material)
                + "\n* Loại: " + nullSafe(material.getCategory())
                + "\n* ĐVT: " + nullSafe(material.getUnit())
                + "\n* Tồn thực tế: " + formatNumber(material.getActualQuantity())
                + "\n* Đang giữ: " + formatNumber(material.getReservedQuantity())
                + "\n* Chờ xuất: " + formatNumber(material.getPendingIssueQuantity())
                + "\n* Có thể cấp: " + formatNumber(material.getAvailableQuantity())
                + "\n* Trạng thái: " + nullSafe(material.getStatus())
                + "\n* Trang vật tư: /materials";
    }

    private String listMaterials(List<Material> materials) {
        StringBuilder builder = new StringBuilder();
        materials.forEach(material -> builder.append("* ")
                .append(materialLine(material))
                .append(" | còn có thể cấp ").append(formatNumber(material.getAvailableQuantity())).append(" ")
                .append(nullSafe(material.getUnit()))
                .append("\n"));
        return builder.toString().trim();
    }

    private List<String> inventorySuggestions(ChatbotNlpService.ParsedQuestion parsed) {
        String materialName = parsed.firstMaterial().map(Material::getName).orElse("vật tư này");
        return List.of(
                materialName + " nằm ở đâu?",
                "Nên xuất lô nào trước?",
                "Có lô nào sắp hết hạn không?"
        );
    }

    private List<String> helpSuggestions() {
        return List.of("Còn bao nhiêu khẩu trang?", "Vật tư nào sắp hết?", "Có lô nào hết hạn trong 30 ngày tới không?");
    }

    private int available(StockBalance balance) {
        return Math.max(0, balance.getAvailableQuantity());
    }

    private boolean isIssuable(StockBalance balance) {
        MaterialBatch batch = balance.getBatch();
        return batch != null
                && batch.getStatus() == BatchStatus.AVAILABLE
                && (batch.getExpiryDate() == null || batch.getExpiryDate().isAfter(LocalDate.now()));
    }

    private boolean isExpired(StockBalance balance) {
        MaterialBatch batch = balance.getBatch();
        return batch != null && batch.getExpiryDate() != null && batch.getExpiryDate().isBefore(LocalDate.now());
    }

    private String stockOpening(Material material, long totalAvailable, String status) {
        if (totalAvailable > 0) {
            return "Có nhé. " + material.getName() + " hiện còn tổng cộng "
                    + formatNumber(totalAvailable) + " " + nullSafe(material.getUnit())
                    + " có thể cấp trong hệ thống.";
        }
        if ("LOW_STOCK".equals(status)) {
            return material.getName() + " đang dưới ngưỡng tồn tối thiểu. Hiện còn "
                    + formatNumber(totalAvailable) + " " + nullSafe(material.getUnit()) + " có thể cấp.";
        }
        return material.getName() + " hiện chưa còn số lượng khả dụng để cấp phát.";
    }

    private String stockStatus(Material material, long totalAvailable) {
        if (totalAvailable <= 0) {
            return "OUT_OF_STOCK";
        }
        if (material.getMinStock() > 0 && totalAvailable <= material.getMinStock()) {
            return "LOW_STOCK";
        }
        return "AVAILABLE";
    }

    private String rowStatus(StockBalance balance) {
        if (isExpired(balance)) {
            return "EXPIRED";
        }
        MaterialBatch batch = balance.getBatch();
        if (batch != null && batch.getExpiryDate() != null && !batch.getExpiryDate().isAfter(LocalDate.now().plusDays(30))) {
            return "NEAR_EXPIRY";
        }
        if (available(balance) <= 0) {
            return "OUT_OF_STOCK";
        }
        return "AVAILABLE";
    }

    private String statusSuffix(String status) {
        return switch (status) {
            case "EXPIRED" -> " (đã hết hạn, không nên cấp phát)";
            case "NEAR_EXPIRY" -> " (sắp hết hạn)";
            case "LOW_STOCK" -> " (tồn thấp)";
            case "OUT_OF_STOCK" -> " (hết khả dụng)";
            default -> "";
        };
    }

    private String statusNote(StockBalance balance) {
        return switch (rowStatus(balance)) {
            case "EXPIRED" -> "Đã hết hạn";
            case "NEAR_EXPIRY" -> "Sắp hết hạn";
            case "OUT_OF_STOCK" -> "Không còn khả dụng";
            default -> isIssuable(balance) ? "Có thể cấp phát" : "Cần kiểm tra trạng thái lô";
        };
    }

    private Long daysToExpiry(LocalDate expiryDate) {
        return expiryDate == null ? null : ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
    }

    private boolean containsDepartmentScope(String text) {
        return VietnameseTextNormalizer.containsAnyKeyword(text, "khoa", "phong", "tai khoa", "khoa cap cuu", "khoa toi");
    }

    private String materialLine(Material material) {
        return material.getCode() + " - " + material.getName();
    }

    private String warehouseLabel(StockBalance balance) {
        if (balance.getWarehouse() == null) {
            return "Kho chưa rõ";
        }
        String name = nullSafe(balance.getWarehouse().getName());
        if ("-".equals(name)) {
            return "Kho chưa rõ";
        }
        return VietnameseTextNormalizer.normalizeSearchText(name).startsWith("kho ") ? name : "Kho " + name;
    }

    private String locationLabel(StockBalance balance) {
        if (balance.getLocation() == null) {
            return "vị trí chưa rõ";
        }
        return nullSafe(balance.getLocation().getName());
    }

    private String batchLabel(StockBalance balance) {
        if (balance.getBatch() == null) {
            return "chưa rõ";
        }
        return nullSafe(balance.getBatch().getBatchNumber());
    }

    private void saveMessage(ChatSession session, String sender, String message, ChatIntent intent, String response) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSession(session);
        chatMessage.setSenderType(sender);
        chatMessage.setMessage(message);
        chatMessage.setIntent(persistedIntent(intent));
        chatMessage.setResponse(response);
        messageRepository.save(chatMessage);
    }

    private ChatIntent persistedIntent(ChatIntent intent) {
        return switch (intent) {
            case ASK_STOCK, CHECK_DEPARTMENT_STOCK -> ChatIntent.CHECK_STOCK;
            case ASK_LOCATION -> ChatIntent.CHECK_LOCATION;
            case ASK_EXPIRY, ASK_IMPORT_DATE, ASK_EXPIRED_OR_NEAR_EXPIRED -> ChatIntent.CHECK_EXPIRY;
            case ASK_BATCH, ASK_RECOMMEND_ISSUE -> ChatIntent.CHECK_BATCH;
            case ASK_LOW_STOCK -> ChatIntent.CHECK_DEPARTMENT_EXPIRING_MATERIALS;
            case GENERAL_HELP -> ChatIntent.HELP;
            default -> intent;
        };
    }

    private Integer extractFirstNumber(String question) {
        Matcher matcher = Pattern.compile("\\d+").matcher(question == null ? "" : question);
        return matcher.find() ? Integer.parseInt(matcher.group()) : null;
    }

    private String firstNonBlank(String left, String right) {
        return left != null && !left.isBlank() ? left : right;
    }

    private String format(LocalDate value) {
        return value == null ? "-" : value.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    private String format(LocalDateTime value) {
        return value == null ? "-" : value.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    private String formatNumber(long value) {
        return VI_NUMBER.format(value);
    }

    private String nullSafe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    public record ChatResponse(boolean success,
                               String intent,
                               String answer,
                               String message,
                               List<ChatItem> items,
                               List<String> suggestions,
                               Long sessionId) {
    }

    public record ChatItem(Long materialId,
                           String materialCode,
                           String materialName,
                           Integer totalQuantity,
                           String unit,
                           String warehouseName,
                           String locationName,
                           String batchCode,
                           String importDate,
                           String expiryDate,
                           String status,
                           Integer availableQuantity,
                           Integer actualQuantity,
                           Integer reservedQuantity,
                           Long daysToExpiry,
                           String note) {
    }
}
