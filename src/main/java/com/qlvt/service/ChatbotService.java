package com.qlvt.service;

import com.qlvt.chatbot.AiProvider;
import com.qlvt.entity.ChatMessage;
import com.qlvt.entity.ChatSession;
import com.qlvt.entity.DepartmentStock;
import com.qlvt.entity.IssueSlip;
import com.qlvt.entity.Material;
import com.qlvt.entity.MaterialBatch;
import com.qlvt.entity.MaterialRequest;
import com.qlvt.entity.StockBalance;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ChatbotService {
    private static final String MEDICAL_SAFETY_REPLY = """
            Mình hỗ trợ phần quản lý vật tư: tra tồn, vị trí, lô, hạn dùng, phiếu và lịch sử nhận.
            Còn liều dùng, chẩn đoán hay chỉ định điều trị thì bạn cần hỏi bác sĩ hoặc dược sĩ phụ trách nhé.
            """;

    private final AiProvider aiProvider;
    private final MaterialSearchService materialSearchService;
    private final MaterialRepository materialRepository;
    private final MaterialBatchRepository batchRepository;
    private final StockBalanceRepository balanceRepository;
    private final MaterialRequestRepository requestRepository;
    private final IssueSlipRepository issueSlipRepository;
    private final DepartmentStockRepository departmentStockRepository;
    private final ChatSessionRepository sessionRepository;
    private final ChatMessageRepository messageRepository;

    public ChatbotService(AiProvider aiProvider,
                          MaterialSearchService materialSearchService,
                          MaterialRepository materialRepository,
                          MaterialBatchRepository batchRepository,
                          StockBalanceRepository balanceRepository,
                          MaterialRequestRepository requestRepository,
                          IssueSlipRepository issueSlipRepository,
                          DepartmentStockRepository departmentStockRepository,
                          ChatSessionRepository sessionRepository,
                          ChatMessageRepository messageRepository) {
        this.aiProvider = aiProvider;
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
        ChatSession session = sessionRepository.findFirstByUserOrderByUpdatedAtDesc(username).orElseGet(() -> {
            ChatSession created = new ChatSession();
            created.setUser(username);
            created.setTitle("Tra cứu QLVT");
            return sessionRepository.save(created);
        });

        String socialReply = socialReply(question);
        ChatIntent intent;
        String answer;
        if (socialReply != null) {
            intent = ChatIntent.HELP;
            answer = socialReply;
        } else {
            String contextualQuestion = enrichWithRecentMaterial(question, session);
            intent = aiProvider.detectIntent(contextualQuestion);
            answer = switch (intent) {
                case HELP -> help();
                case UNKNOWN -> MEDICAL_SAFETY_REPLY;
                case CHECK_STOCK -> checkStock(contextualQuestion);
                case CHECK_LOCATION -> checkLocation(contextualQuestion);
                case CHECK_EXPIRY, CHECK_BATCH -> checkExpiryOrBatch(contextualQuestion);
                case CHECK_SUPPLIER -> checkSupplier(contextualQuestion);
                case SUGGEST_ALTERNATIVE -> suggestAlternative(contextualQuestion);
                case CHECK_REQUEST_STATUS -> checkRequestStatus(username, department);
                case CREATE_REQUEST_DRAFT -> createRequestDraft(contextualQuestion);
                case CHECK_RECEIVED_HISTORY -> receivedHistory(username, department);
                case CHECK_DEPARTMENT_STOCK -> departmentStock(contextualQuestion, department);
                case CHECK_DEPARTMENT_EXPIRING_MATERIALS -> expiringMaterials();
                case HELP_CREATE_REQUEST -> createRequestHelp();
                default -> searchMaterial(contextualQuestion);
            };
        }

        saveMessage(session, "USER", question, intent, answer);
        saveMessage(session, "BOT", answer, intent, answer);
        session.setUpdatedAt(LocalDateTime.now());
        sessionRepository.save(session);
        return new ChatResponse(answer.strip(), intent.name(), session.getId());
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

    private String socialReply(String question) {
        String text = VietnameseTextNormalizer.normalizeSearchText(question);
        if (text.isBlank()) {
            return """
                    Mình đây. Bạn muốn mình kiểm tra vật tư nào?
                    Bạn có thể hỏi kiểu: "găng tay size M còn bao nhiêu", "lô nào sắp hết hạn", hoặc "phiếu của tôi đến đâu rồi".
                    """;
        }
        if (text.equals("xin chao") || text.equals("chao") || text.equals("hello") || text.equals("hi") || text.equals("alo")) {
            return """
                    Chào bạn, mình đang sẵn sàng tra dữ liệu QLVT.
                    Bạn cứ hỏi tự nhiên, ví dụ: "VT001 còn bao nhiêu?", "vật tư này ở kho nào?", hoặc "các lô sắp hết hạn".
                    """;
        }
        if (VietnameseTextNormalizer.containsAnyKeyword(text, "cam on", "thanks", "thank you", "ok roi", "duoc roi")) {
            return "Không có gì. Khi cần kiểm tra vật tư, tồn kho, lô/HSD hoặc phiếu xuất nhập, bạn cứ nhắn mình nhé.";
        }
        return null;
    }

    private String enrichWithRecentMaterial(String question, ChatSession session) {
        String normalized = VietnameseTextNormalizer.normalizeSearchText(question);
        if (!VietnameseTextNormalizer.containsAnyKeyword(normalized, "no", "vat tu nay", "lo nay", "hang nay", "cai nay")) {
            return question;
        }
        String recentText = messageRepository.findTop30BySession_IdOrderByCreatedAtAsc(session.getId()).stream()
                .sorted(Comparator.comparing(ChatMessage::getCreatedAt).reversed())
                .map(message -> message.getMessage() + "\n" + nullSafe(message.getResponse()))
                .limit(10)
                .reduce("", (left, right) -> left + "\n" + right);
        return materialRepository.findByDeletedFalseOrderByCodeAsc().stream()
                .filter(material -> recentText.contains(material.getCode()))
                .findFirst()
                .map(material -> question + " " + material.getCode())
                .orElse(question);
    }

    private String searchMaterial(String question) {
        List<Material> matches = materialSearchService.search(question, 5);
        if (matches.isEmpty()) {
            return """
                    Mình chưa tìm thấy vật tư khớp với nội dung bạn nhập.
                    Bạn thử nhập mã vật tư, tên đầy đủ hơn, hoặc một phần tên dễ nhận diện hơn nhé. Ví dụ: "găng tay size M" hoặc "VT002".
                    """;
        }
        if (matches.size() > 1) {
            return "Mình thấy vài vật tư khá giống nhau. Bạn muốn xem loại nào?\n"
                    + numberedMaterials(matches)
                    + "\nBạn có thể nhắn lại bằng mã vật tư, ví dụ: VT002.";
        }
        return "Mình tìm thấy vật tư này:\n" + materialSummary(matches.get(0));
    }

    private String checkStock(String question) {
        Material material = singleMaterial(question);
        if (material == null) {
            return "Mình chưa xác định được bạn muốn kiểm tra tồn của vật tư nào. Bạn gửi giúp mình mã hoặc tên vật tư nhé.";
        }

        List<StockBalance> balances = balanceRepository.findByMaterial_IdOrderByWarehouse_CodeAscLocation_CodeAsc(material.getId());
        int totalAvailable = balances.isEmpty()
                ? material.getAvailableQuantity()
                : balances.stream().mapToInt(balance -> Math.max(0, balance.getAvailableQuantity())).sum();
        StringBuilder builder = new StringBuilder("Mình kiểm tra được như sau:\n")
                .append(materialLine(material))
                .append("\n- Tổng còn có thể cấp: ").append(totalAvailable).append(" ").append(nullSafe(material.getUnit()));

        if (balances.isEmpty()) {
            return builder.append("\n\nHiện chưa có số dư theo kho/vị trí cho vật tư này.").toString();
        }

        builder.append("\n\nTheo từng kho/vị trí:");
        balances.forEach(balance -> builder.append("\n- ")
                .append(balance.getWarehouse().getName()).append(" / ")
                .append(balance.getLocation().getName()).append(" / lô ")
                .append(balance.getBatch().getBatchNumber())
                .append(": còn ").append(balance.getAvailableQuantity()).append(" ")
                .append(nullSafe(material.getUnit()))
                .append(" (thực tế ").append(balance.getActualQuantity())
                .append(", đang giữ ").append(balance.getReservedQuantity()).append(")"));
        return builder.toString();
    }

    private String checkLocation(String question) {
        Material material = singleMaterial(question);
        if (material == null) {
            return "Mình chưa biết bạn muốn tìm vị trí của vật tư nào. Bạn gửi mã hoặc tên vật tư giúp mình nhé.";
        }
        List<StockBalance> balances = balanceRepository.findByMaterial_IdOrderByWarehouse_CodeAscLocation_CodeAsc(material.getId());
        if (balances.isEmpty()) {
            return "Mình chưa thấy vị trí tồn nào cho " + materialLine(material) + " trong dữ liệu hiện tại.";
        }
        StringBuilder builder = new StringBuilder("Mình thấy ")
                .append(materialLine(material))
                .append(" đang nằm ở:");
        balances.forEach(balance -> builder.append("\n- Kho ")
                .append(balance.getWarehouse().getName())
                .append(", vị trí ").append(balance.getLocation().getName())
                .append(", lô ").append(balance.getBatch().getBatchNumber())
                .append(", còn ").append(balance.getAvailableQuantity()).append(" ")
                .append(nullSafe(material.getUnit())));
        return builder.toString();
    }

    private String checkExpiryOrBatch(String question) {
        String text = VietnameseTextNormalizer.normalizeSearchText(question);
        Optional<MaterialBatch> batchByNumber = batchRepository.findAll().stream()
                .filter(batch -> text.contains(VietnameseTextNormalizer.normalizeSearchText(batch.getBatchNumber())))
                .findFirst();
        MaterialBatch batch = batchByNumber.orElseGet(() -> {
            Material material = singleMaterial(question);
            return material == null ? null : batchRepository.findIssuableBatchesFefo(material.getId(), LocalDate.now()).stream().findFirst().orElse(null);
        });
        if (batch == null) {
            return "Mình chưa tìm thấy lô phù hợp. Bạn gửi thêm số lô hoặc mã vật tư để mình kiểm tra chính xác hơn nhé.";
        }

        String warning = "";
        if (batch.getExpiryDate() != null) {
            if (batch.getExpiryDate().isBefore(LocalDate.now())) {
                warning = "\nLưu ý: lô này đã quá hạn, không nên cấp phát.";
            } else if (batch.getExpiryDate().isBefore(LocalDate.now().plusDays(90))) {
                warning = "\nLưu ý: lô này sắp hết hạn, nên ưu tiên kiểm tra trước khi cấp.";
            }
        }

        return "Mình tìm được thông tin lô này:\n"
                + "- Lô: " + batch.getBatchNumber()
                + "\n- Vật tư: " + materialLine(batch.getMaterial())
                + "\n- Ngày nhập: " + format(batch.getReceiptDate())
                + "\n- Ngày sản xuất: " + format(batch.getManufactureDate())
                + "\n- Hạn dùng: " + format(batch.getExpiryDate())
                + "\n- Số lượng còn: " + batch.getQuantity() + " " + nullSafe(batch.getMaterial().getUnit())
                + "\n- Trạng thái: " + batch.getStatus()
                + warning;
    }

    private String checkSupplier(String question) {
        Material material = singleMaterial(question);
        if (material == null) {
            return "Mình chưa xác định được vật tư hoặc lô cần kiểm tra nhà cung cấp. Bạn gửi mã vật tư hoặc số lô giúp mình nhé.";
        }
        List<MaterialBatch> batches = batchRepository.findAll().stream()
                .filter(batch -> batch.getMaterial().getId().equals(material.getId()))
                .filter(batch -> batch.getSupplier() != null)
                .sorted(Comparator.comparing(MaterialBatch::getReceiptDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5)
                .toList();
        if (batches.isEmpty()) {
            return "Mình chưa thấy nhà cung cấp nào được ghi nhận cho " + materialLine(material) + ".";
        }
        StringBuilder builder = new StringBuilder("Các nhà cung cấp gần đây của ")
                .append(materialLine(material)).append(":");
        batches.forEach(batch -> builder.append("\n- ")
                .append(batch.getSupplier().getName())
                .append(", lô ").append(batch.getBatchNumber())
                .append(", nhập ngày ").append(format(batch.getReceiptDate())));
        return builder.toString();
    }

    private String suggestAlternative(String question) {
        Material material = singleMaterial(question);
        if (material == null) {
            return "Mình chưa biết bạn muốn tìm vật tư thay thế cho món nào. Bạn gửi mã hoặc tên vật tư giúp mình nhé.";
        }
        List<Material> alternatives = materialSearchService.alternatives(material, 5);
        if (alternatives.isEmpty()) {
            return "Mình chưa thấy vật tư thay thế phù hợp cho " + materialLine(material) + " trong dữ liệu hiện tại.";
        }
        return "Có vài vật tư gần nhóm với " + materialLine(material) + ". Bạn vẫn nên kiểm tra lại trước khi dùng:\n"
                + listMaterials(alternatives);
    }

    private String checkRequestStatus(String username, String department) {
        List<MaterialRequest> requests = requestRepository.findTop10ByRequesterOrderByCreatedAtDesc(username);
        if (requests.isEmpty() && department != null && !department.isBlank()) {
            requests = requestRepository.findTop10ByDepartmentOrderByCreatedAtDesc(department);
        }
        if (requests.isEmpty()) {
            return "Mình chưa thấy phiếu yêu cầu nào trong phạm vi bạn được phép xem.";
        }
        StringBuilder builder = new StringBuilder("Mình thấy các phiếu gần đây như sau:");
        requests.forEach(request -> builder.append("\n- ")
                .append(request.getCode())
                .append(" | ").append(format(request.getCreatedAt()))
                .append(" | ").append(request.getStatus())
                .append(request.getRejectedReason() == null ? "" : " | lý do: " + request.getRejectedReason())
                .append(" | /requests/").append(request.getId()));
        return builder.append("\nBạn bấm vào đường dẫn phiếu để xem chi tiết từng dòng vật tư.").toString();
    }

    private String createRequestDraft(String question) {
        Material material = singleMaterial(question);
        Integer quantity = extractFirstNumber(question);
        if (material == null || quantity == null) {
            return """
                    Mình cần thêm một chút thông tin để mở đúng phiếu: vật tư nào và số lượng bao nhiêu.
                    Ví dụ bạn có thể nhắn: "tạo phiếu xuất 20 hộp găng tay size M".
                    """;
        }
        return "Mình hiểu là bạn muốn lấy "
                + quantity + " " + nullSafe(material.getUnit())
                + " của " + materialLine(material)
                + ".\nBạn mở trang tạo phiếu xuất ở đây để kiểm tra kho nhận và gửi phiếu: /issues/new";
    }

    private String createRequestHelp() {
        return """
                Bạn có thể nói tự nhiên kiểu:
                - "tạo phiếu xuất 20 hộp găng tay size M"
                - "VT001 còn bao nhiêu"
                - "lô nào sắp hết hạn"
                Mình sẽ tra dữ liệu và dẫn bạn tới đúng trang xử lý.
                """;
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
        slips.forEach(slip -> slip.getLines().forEach(line -> builder.append("\n- ")
                .append(format(slip.getReceivedAt()))
                .append(" | ").append(materialLine(line.getMaterial()))
                .append(" | SL ").append(line.getIssuedQuantity())
                .append(" | phiếu ").append(slip.getIssueCode())));
        return builder.toString();
    }

    private String expiringMaterials() {
        List<StockBalance> balances = balanceRepository.findTop20ByBatch_ExpiryDateBetweenOrderByBatch_ExpiryDateAsc(LocalDate.now(), LocalDate.now().plusDays(90));
        if (balances.isEmpty()) {
            return "Mình kiểm tra rồi, hiện chưa có lô nào sắp hết hạn trong 90 ngày tới.";
        }
        StringBuilder builder = new StringBuilder("Các lô cần chú ý trong 90 ngày tới:");
        balances.forEach(balance -> builder.append("\n- ")
                .append(materialLine(balance.getMaterial()))
                .append(" | lô ").append(balance.getBatch().getBatchNumber())
                .append(" | HSD ").append(format(balance.getBatch().getExpiryDate()))
                .append(" | còn ").append(balance.getAvailableQuantity()).append(" ").append(nullSafe(balance.getMaterial().getUnit()))
                .append(" | ").append(balance.getWarehouse().getName()).append(" / ").append(balance.getLocation().getName()));
        return builder.toString();
    }

    private String help() {
        return """
                Mình có thể hỗ trợ bạn tra dữ liệu QLVT bằng câu hỏi tự nhiên.
                Bạn có thể hỏi:
                - "VT001 còn bao nhiêu?"
                - "găng tay size M nằm ở đâu?"
                - "lô LO-001 hạn dùng khi nào?"
                - "phiếu của tôi đến đâu rồi?"
                - "vật tư nào sắp hết hạn?"
                Mình chỉ dùng dữ liệu trong hệ thống, nên câu trả lời sẽ bám theo database hiện tại.
                """;
    }

    private String departmentStock(String question, String department) {
        if (department == null || department.isBlank()) {
            return "Tài khoản của bạn chưa được gán khoa/phòng, nên mình chưa tra được tồn tại khoa.";
        }
        List<DepartmentStock> stocks = departmentStockRepository.findByDepartmentAndQuantityOnHandGreaterThanOrderByMaterial_CodeAscBatch_ExpiryDateAsc(department, 0);
        if (stocks.isEmpty()) {
            return "Mình chưa thấy tồn vật tư đang giữ tại " + department + ".";
        }
        Material material = singleMaterial(question);
        if (material != null) {
            stocks = stocks.stream().filter(stock -> stock.getMaterial().getId().equals(material.getId())).toList();
            if (stocks.isEmpty()) {
                return department + " hiện chưa còn tồn cho " + materialLine(material) + ".";
            }
        } else {
            stocks = stocks.stream().limit(10).toList();
        }
        StringBuilder builder = new StringBuilder("Tồn tại ").append(department).append(":");
        stocks.forEach(stock -> builder.append("\n- ")
                .append(materialLine(stock.getMaterial()))
                .append(" | lô ").append(stock.getBatch().getBatchNumber())
                .append(" | còn ").append(stock.getQuantityOnHand()).append(" ").append(nullSafe(stock.getMaterial().getUnit()))
                .append(" | HSD ").append(format(stock.getBatch().getExpiryDate())));
        return builder.toString();
    }

    private Material singleMaterial(String question) {
        List<Material> matches = materialSearchService.search(question, 3);
        return matches.size() == 1 ? matches.get(0) : matches.stream().findFirst().orElse(null);
    }

    private String materialSummary(Material material) {
        return materialLine(material)
                + "\n- Loại: " + nullSafe(material.getCategory())
                + "\n- ĐVT: " + nullSafe(material.getUnit())
                + "\n- Tồn thực tế: " + material.getActualQuantity()
                + "\n- Đang giữ: " + material.getReservedQuantity()
                + "\n- Chờ xuất: " + material.getPendingIssueQuantity()
                + "\n- Có thể cấp: " + material.getAvailableQuantity()
                + "\n- Trạng thái: " + nullSafe(material.getStatus())
                + "\n- Trang vật tư: /materials";
    }

    private String listMaterials(List<Material> materials) {
        StringBuilder builder = new StringBuilder();
        materials.forEach(material -> builder.append("- ")
                .append(materialLine(material))
                .append(" | còn có thể cấp ").append(material.getAvailableQuantity()).append(" ")
                .append(nullSafe(material.getUnit()))
                .append("\n"));
        return builder.toString().trim();
    }

    private String numberedMaterials(List<Material> materials) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < materials.size(); i++) {
            Material material = materials.get(i);
            builder.append(i + 1).append(". ")
                    .append(materialLine(material))
                    .append(" | còn có thể cấp ").append(material.getAvailableQuantity()).append(" ")
                    .append(nullSafe(material.getUnit()))
                    .append("\n");
        }
        return builder.toString().trim();
    }

    private String materialLine(Material material) {
        return material.getCode() + " - " + material.getName();
    }

    private void saveMessage(ChatSession session, String sender, String message, ChatIntent intent, String response) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSession(session);
        chatMessage.setSenderType(sender);
        chatMessage.setMessage(message);
        chatMessage.setIntent(intent);
        chatMessage.setResponse(response);
        messageRepository.save(chatMessage);
    }

    private Integer extractFirstNumber(String question) {
        Matcher matcher = Pattern.compile("\\d+").matcher(question);
        return matcher.find() ? Integer.parseInt(matcher.group()) : null;
    }

    private String format(LocalDate value) {
        return value == null ? "-" : value.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    private String format(LocalDateTime value) {
        return value == null ? "-" : value.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    private String nullSafe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    public record ChatResponse(String answer, String intent, Long sessionId) {
    }
}
