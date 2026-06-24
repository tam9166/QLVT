package com.qlvt.service;

import com.qlvt.entity.Department;
import com.qlvt.entity.Material;
import com.qlvt.entity.Warehouse;
import com.qlvt.enums.ChatIntent;
import com.qlvt.repository.DepartmentRepository;
import com.qlvt.repository.WarehouseRepository;
import com.qlvt.util.VietnameseTextNormalizer;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ChatbotNlpService {
    private static final Pattern DAYS_PATTERN = Pattern.compile("(?:trong|toi|den)\\s+(\\d{1,3})\\s+ngay");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\b(\\d{1,6})\\b");
    private static final Set<String> REQUEST_QUANTITY_TRIGGERS = Set.of(
            "can", "lay", "xin", "cap", "xuat", "muon", "tao", "lap", "du", "thieu"
    );
    private static final Map<String, Integer> VIETNAMESE_NUMBER_WORDS = Map.ofEntries(
            Map.entry("mot", 1),
            Map.entry("moi", 1),
            Map.entry("hai", 2),
            Map.entry("ba", 3),
            Map.entry("bon", 4),
            Map.entry("tu", 4),
            Map.entry("nam", 5),
            Map.entry("lam", 5),
            Map.entry("sau", 6),
            Map.entry("bay", 7),
            Map.entry("tam", 8),
            Map.entry("chin", 9)
    );
    private static final Set<String> NUMBER_CONNECTORS = Set.of("le", "linh");

    private final MaterialSearchService materialSearchService;
    private final WarehouseRepository warehouseRepository;
    private final DepartmentRepository departmentRepository;

    public ChatbotNlpService(MaterialSearchService materialSearchService,
                             WarehouseRepository warehouseRepository,
                             DepartmentRepository departmentRepository) {
        this.materialSearchService = materialSearchService;
        this.warehouseRepository = warehouseRepository;
        this.departmentRepository = departmentRepository;
    }

    public ParsedQuestion parse(String message) {
        String normalized = MaterialSearchService.expandAliases(VietnameseTextNormalizer.normalizeSearchText(message));
        ChatIntent intent = detectIntent(normalized);
        List<MaterialSearchService.MaterialMatch> matches = materialSearchService.rankedMatches(message, 8);
        MaterialResolution materialResolution = resolveMaterials(normalized, matches);
        Optional<Warehouse> warehouse = resolveWarehouse(normalized);
        Optional<Department> department = resolveDepartment(normalized);
        int expiryWindowDays = expiryWindowDays(normalized);
        Integer requestedQuantity = requestedQuantity(normalized);

        return new ParsedQuestion(
                message == null ? "" : message,
                normalized,
                intent,
                materialResolution.materials(),
                materialResolution.candidates(),
                materialResolution.ambiguous(),
                warehouse.orElse(null),
                department.map(Department::getName).orElse(null),
                expiryWindowDays,
                requestedQuantity
        );
    }

    private ChatIntent detectIntent(String text) {
        if (text.isBlank() || has(text, "help", "huong dan", "tro giup", "ban lam duoc gi", "hoi nhu the nao",
                "cach hoi", "goi y", "chatbot", "tro ly", "minh hoi gi duoc", "ban ho tro gi")) {
            return ChatIntent.GENERAL_HELP;
        }
        if (has(text, "lieu dung", "cach dung thuoc", "dieu tri", "uong bao nhieu", "uong the nao",
                "uong thuoc", "thuoc nay", "tiem bao nhieu", "tiem thuoc", "bao nhieu lan",
                "tac dung phu", "chi dinh", "chan doan", "dung cho benh")) {
            return ChatIntent.UNKNOWN;
        }
        if (has(text, "phieu cua toi", "phieu cua em", "don cua toi", "yeu cau cua toi", "yeu cau toi dau",
                "trang thai phieu", "da duyet chua", "da xuat chua", "da nhan chua",
                "kho duyet chua", "truong khoa duyet chua", "phieu den dau", "phieu xu ly chua")) {
            return ChatIntent.CHECK_REQUEST_STATUS;
        }
        if (has(text, "da nhan", "da cap phat", "lich su nhan", "lich su cap phat",
                "nhan gan day", "khoa vua nhan gi", "da nhan nhung gi", "vat tu da cap")) {
            return ChatIntent.CHECK_RECEIVED_HISTORY;
        }
        if (has(text, "nen lay", "lay lo nao truoc", "lay o kho nao truoc", "xuat lo nao truoc",
                "uu tien xuat", "uu tien lay", "cap phat lo nao", "fefo", "fifo")) {
            return ChatIntent.ASK_RECOMMEND_ISSUE;
        }
        if (has(text, "vat tu nao het hang", "mon nao het hang", "het hang", "het kho")) {
            return ChatIntent.ASK_LOW_STOCK;
        }
        if (has(text, "vat tu nao sap het", "mon nao sap het", "thuoc nao sap het", "thuoc gi sap het",
                "hang nao gan het", "ton thap", "gan het", "can nhap them",
                "sap het hang", "duoi nguong", "duoi ton toi thieu")) {
            return ChatIntent.ASK_LOW_STOCK;
        }
        if (has(text, "lo nao sap het han", "hang nao sap het han", "vat tu nao sap het han",
                "sap het han", "gan het han", "het han trong", "sap het date", "gan het date",
                "qua han", "da het han")) {
            return ChatIntent.ASK_EXPIRED_OR_NEAR_EXPIRED;
        }
        if (has(text, "tao phieu", "tao phieu xuat", "tao yeu cau", "lap phieu", "lap yeu cau",
                "can xin", "muon xin", "xin cap", "de nghi cap", "cap them", "lay them",
                "lay vat tu", "lay thuoc", "linh thuoc", "xuat kho cho khoa")) {
            return ChatIntent.CREATE_REQUEST_DRAFT;
        }
        if ((text.contains("khoa") && has(text, "vat tu gi", "con gi", "co gi", "co nhung gi", "dang giu gi"))
                || has(text, "ton tai khoa", "ton o khoa", "ton khoa", "khoa con bao nhieu", "vat tu tai khoa",
                "vat tu khoa toi", "trong khoa con gi", "khoa con gi", "khoa dang giu")) {
            return ChatIntent.CHECK_DEPARTMENT_STOCK;
        }
        if (has(text, "nha cung cap", "supplier", "cong ty cung cap", "ai cung cap", "mua o dau", "nha thau", "don vi cung cap")) {
            return ChatIntent.CHECK_SUPPLIER;
        }
        if (has(text, "ngay nhap", "nhap ngay nao", "ngay ve kho", "ngay nhan hang")) {
            return ChatIntent.ASK_IMPORT_DATE;
        }
        if (has(text, "so lo", "ma lo", "batch", "lot", "lo nao", "lo gan nhat")) {
            return ChatIntent.ASK_BATCH;
        }
        if (has(text, "han dung", "hsd", "expiry", "date", "ngay het han", "con han khong", "han khi nao")) {
            return ChatIntent.ASK_EXPIRY;
        }
        if (has(text, "o dau", "de dau", "nam o dau", "vi tri", "vi tri nao", "ke nao",
                "tu do", "tu thuoc", "ngan tu", "ngan nao", "o kho nao", "kho nao")) {
            return ChatIntent.ASK_LOCATION;
        }
        if (has(text, "con khong", "con bao nhieu", "con nhieu khong", "con may", "con nhieu",
                "con cap duoc", "cap duoc bao nhieu", "con hang khong", "con du khong",
                "co du khong", "du khong", "co san khong", "cap duoc khong",
                "thieu bao nhieu", "con thieu bao nhieu", "can bo sung bao nhieu",
                "phai bo sung", "phai dat mua", "can dat mua", "dat mua them",
                "ton kho", "ton", "kha dung", "stock", "so luong con")) {
            return ChatIntent.ASK_STOCK;
        }
        if (has(text, "thay the", "tuong duong", "doi sang", "loai khac", "het thi dung gi", "vat tu tuong tu")) {
            return ChatIntent.SUGGEST_ALTERNATIVE;
        }
        return ChatIntent.SEARCH_MATERIAL;
    }

    private MaterialResolution resolveMaterials(String normalized, List<MaterialSearchService.MaterialMatch> matches) {
        if (matches.isEmpty()) {
            return new MaterialResolution(List.of(), List.of(), false);
        }

        boolean multiQuestion = normalized.contains(" va ") || normalized.contains(" voi ") || normalized.contains(" cung ");
        if (multiQuestion) {
            List<Material> materials = matches.stream()
                    .filter(match -> match.score() >= 48)
                    .map(MaterialSearchService.MaterialMatch::material)
                    .limit(4)
                    .toList();
            if (materials.size() >= 2) {
                return new MaterialResolution(materials, List.of(), false);
            }
        }

        MaterialSearchService.MaterialMatch first = matches.get(0);
        MaterialSearchService.MaterialMatch second = matches.size() > 1 ? matches.get(1) : null;
        boolean exactCode = normalized.contains(VietnameseTextNormalizer.normalizeSearchText(first.material().getCode()));
        boolean exactName = normalized.contains(VietnameseTextNormalizer.normalizeSearchText(first.material().getName()));
        boolean sizeAmbiguous = looksLikeSizeAmbiguous(normalized, matches);
        boolean preferredGenericMaterial = normalized.contains("khau trang") || normalized.contains("bo dich truyen") || normalized.contains("day truyen dich");
        if (matches.size() == 1 || exactCode || exactName || preferredGenericMaterial || (!sizeAmbiguous && first.score() >= 85) || (second != null && first.score() - second.score() >= 28)) {
            return new MaterialResolution(List.of(first.material()), List.of(), false);
        }
        return new MaterialResolution(List.of(), matches.stream().map(MaterialSearchService.MaterialMatch::material).limit(5).toList(), true);
    }

    private boolean looksLikeSizeAmbiguous(String normalized, List<MaterialSearchService.MaterialMatch> matches) {
        if (matches.size() <= 1 || normalized.matches(".*\\b(size|sz|co)\\s*[a-z0-9]+.*")) {
            return false;
        }
        long sizedCandidates = matches.stream()
                .limit(5)
                .map(match -> VietnameseTextNormalizer.normalizeSearchText(match.material().getName() + " " + match.material().getAliasText()))
                .filter(text -> text.matches(".*\\b(size|sz|co)\\s*[a-z0-9]+.*"))
                .count();
        return sizedCandidates >= 2;
    }

    private Optional<Warehouse> resolveWarehouse(String normalized) {
        return warehouseRepository.findByDeletedFalseOrderByCodeAsc().stream()
                .filter(warehouse -> containsEntity(normalized, warehouse.getCode(), warehouse.getName()))
                .findFirst();
    }

    private Optional<Department> resolveDepartment(String normalized) {
        return departmentRepository.findByDeletedFalseOrderByCodeAsc().stream()
                .filter(department -> containsEntity(normalized, department.getCode(), department.getName()))
                .findFirst();
    }

    private boolean containsEntity(String text, String code, String name) {
        String normalizedCode = VietnameseTextNormalizer.normalizeSearchText(code);
        String normalizedName = VietnameseTextNormalizer.normalizeSearchText(name);
        return (!normalizedCode.isBlank() && text.contains(normalizedCode))
                || (!normalizedName.isBlank() && text.contains(normalizedName))
                || (!normalizedName.isBlank() && normalizedName.replace("khoa ", "").length() > 2
                    && text.contains(normalizedName.replace("khoa ", "")));
    }

    private int expiryWindowDays(String text) {
        Matcher matcher = DAYS_PATTERN.matcher(text);
        if (matcher.find()) {
            return Math.max(1, Math.min(365, Integer.parseInt(matcher.group(1))));
        }
        if (has(text, "trong thang nay", "thang nay")) {
            return Math.max(1, (int) (LocalDate.now().datesUntil(LocalDate.now().with(TemporalAdjusters.lastDayOfMonth()).plusDays(1)).count()));
        }
        if (has(text, "hom nay", "ngay hom nay", "trong ngay")) {
            return 1;
        }
        if (has(text, "ngay mai", "mai")) {
            return 2;
        }
        if (has(text, "tuan nay", "trong tuan")) {
            LocalDate endOfWeek = LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
            return Math.max(1, (int) LocalDate.now().datesUntil(endOfWeek.plusDays(1)).count());
        }
        if (has(text, "tuan toi", "tuan sau")) {
            return 14;
        }
        if (has(text, "thang toi", "thang sau")) {
            return 60;
        }
        return 30;
    }

    private Integer requestedQuantity(String text) {
        if (!has(text, "can", "lay", "xin", "cap", "xuat", "du khong", "co du", "muon",
                "tao", "lap", "yeu cau", "de nghi", "thieu", "bo sung", "dat mua")) {
            return null;
        }
        String withoutSpecs = text
                .replaceAll("\\b\\d+\\s*(ml|mm|cm|g|mg|kg|l)\\b", " ")
                .replaceAll("\\b(size|co)\\s*\\d+\\b", " ");
        Matcher matcher = NUMBER_PATTERN.matcher(withoutSpecs);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return requestedQuantityFromWords(withoutSpecs);
    }

    private Integer requestedQuantityFromWords(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        List<String> tokens = List.of(text.trim().split("\\s+"));
        for (int i = 0; i < tokens.size(); i++) {
            if (!REQUEST_QUANTITY_TRIGGERS.contains(tokens.get(i))) {
                continue;
            }
            int end = Math.min(tokens.size(), i + 8);
            for (int start = i + 1; start < end; start++) {
                NumberParse parsed = parseNumberAt(tokens, start);
                if (parsed.value() != null && parsed.value() > 0) {
                    return parsed.value();
                }
            }
        }
        return null;
    }

    private NumberParse parseNumberAt(List<String> tokens, int start) {
        int value = 0;
        int current = 0;
        int index = start;
        boolean seen = false;

        while (index < tokens.size()) {
            String token = tokens.get(index);
            if (NUMBER_CONNECTORS.contains(token) && seen) {
                index++;
                continue;
            }
            Integer digit = VIETNAMESE_NUMBER_WORDS.get(token);
            if (digit != null) {
                current += digit;
                seen = true;
                index++;
                continue;
            }
            if ("muoi".equals(token) || "chuc".equals(token)) {
                current = current == 0 ? 10 : current * 10;
                seen = true;
                index++;
                continue;
            }
            if ("tram".equals(token)) {
                if (!seen) {
                    break;
                }
                current = current == 0 ? 100 : current * 100;
                value += current;
                current = 0;
                index++;
                continue;
            }
            if ("nghin".equals(token) || "ngan".equals(token)) {
                if (!seen && current == 0) {
                    break;
                }
                current = current == 0 ? 1 : current;
                value += current * 1000;
                current = 0;
                seen = true;
                index++;
                continue;
            }
            break;
        }

        if (!seen) {
            return new NumberParse(null, start);
        }
        return new NumberParse(value + current, index);
    }

    private boolean has(String text, String... keywords) {
        return VietnameseTextNormalizer.containsAnyKeyword(text, keywords);
    }

    private record MaterialResolution(List<Material> materials, List<Material> candidates, boolean ambiguous) {
    }

    private record NumberParse(Integer value, int nextIndex) {
    }

    public record ParsedQuestion(String rawMessage,
                                 String normalizedMessage,
                                 ChatIntent intent,
                                 List<Material> materials,
                                 List<Material> candidates,
                                 boolean ambiguousMaterial,
                                 Warehouse warehouse,
                                 String department,
                                 int expiryWindowDays,
                                 Integer requestedQuantity) {
        public boolean hasMaterial() {
            return !materials.isEmpty();
        }

        public Optional<Material> firstMaterial() {
            return materials.stream().findFirst();
        }

        public List<Material> targetMaterials() {
            return new ArrayList<>(materials);
        }
    }
}
