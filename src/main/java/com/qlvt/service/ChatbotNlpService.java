package com.qlvt.service;

import com.qlvt.entity.Department;
import com.qlvt.entity.Material;
import com.qlvt.entity.Warehouse;
import com.qlvt.enums.ChatIntent;
import com.qlvt.repository.DepartmentRepository;
import com.qlvt.repository.WarehouseRepository;
import com.qlvt.util.VietnameseTextNormalizer;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ChatbotNlpService {
    private static final Pattern DAYS_PATTERN = Pattern.compile("(?:trong|toi|den)\\s+(\\d{1,3})\\s+ngay");

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

        return new ParsedQuestion(
                message == null ? "" : message,
                normalized,
                intent,
                materialResolution.materials(),
                materialResolution.candidates(),
                materialResolution.ambiguous(),
                warehouse.orElse(null),
                department.map(Department::getName).orElse(null),
                expiryWindowDays
        );
    }

    private ChatIntent detectIntent(String text) {
        if (text.isBlank() || has(text, "help", "huong dan", "tro giup", "ban lam duoc gi", "hoi nhu the nao",
                "cach hoi", "goi y", "chatbot", "tro ly", "minh hoi gi duoc", "ban ho tro gi")) {
            return ChatIntent.GENERAL_HELP;
        }
        if (has(text, "lieu dung", "cach dung thuoc", "dieu tri", "uong bao nhieu", "uong the nao",
                "uong thuoc", "thuoc nay", "tiem bao nhieu", "tiem thuoc", "bao nhieu lan",
                "tac dung phu", "chi dinh", "chan doan", "thuoc gi", "dung cho benh")) {
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
        if (has(text, "vat tu nao sap het", "mon nao sap het", "ton thap", "gan het", "can nhap them",
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
        if (has(text, "ton tai khoa", "ton o khoa", "ton khoa", "khoa con bao nhieu", "vat tu tai khoa",
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
                "co san khong", "cap duoc khong", "ton kho", "ton", "kha dung", "stock", "so luong con")) {
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
        return 30;
    }

    private boolean has(String text, String... keywords) {
        return VietnameseTextNormalizer.containsAnyKeyword(text, keywords);
    }

    private record MaterialResolution(List<Material> materials, List<Material> candidates, boolean ambiguous) {
    }

    public record ParsedQuestion(String rawMessage,
                                 String normalizedMessage,
                                 ChatIntent intent,
                                 List<Material> materials,
                                 List<Material> candidates,
                                 boolean ambiguousMaterial,
                                 Warehouse warehouse,
                                 String department,
                                 int expiryWindowDays) {
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
