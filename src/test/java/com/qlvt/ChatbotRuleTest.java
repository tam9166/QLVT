package com.qlvt;

import com.qlvt.chatbot.RuleBasedAiProvider;
import com.qlvt.entity.Department;
import com.qlvt.entity.Material;
import com.qlvt.enums.ChatIntent;
import com.qlvt.repository.DepartmentRepository;
import com.qlvt.repository.MaterialRepository;
import com.qlvt.repository.WarehouseRepository;
import com.qlvt.service.ChatbotNlpService;
import com.qlvt.service.MaterialSearchService;
import com.qlvt.util.VietnameseTextNormalizer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ChatbotRuleTest {
    @Test
    void normalizerSupportsVietnameseWithAndWithoutAccent() {
        assertEquals("gang tay size m con khong", VietnameseTextNormalizer.normalizeSearchText("Găng tay size M còn không?"));
        assertTrue(VietnameseTextNormalizer.fuzzyMatch("gang tay size m", "Găng tay y tế không bột size M"));
    }

    @Test
    void ruleEngineDetectsNaturalInventoryQuestions() {
        RuleBasedAiProvider provider = new RuleBasedAiProvider();

        assertEquals(ChatIntent.CHECK_STOCK, provider.detectIntent("VT001 còn bao nhiêu"));
        assertEquals(ChatIntent.CHECK_STOCK, provider.detectIntent("VT001 còn cấp được bao nhiêu?"));
        assertEquals(ChatIntent.CHECK_STOCK, provider.detectIntent("kho còn hàng không"));
        assertEquals(ChatIntent.CHECK_LOCATION, provider.detectIntent("nó nằm ở đâu"));
        assertEquals(ChatIntent.CHECK_LOCATION, provider.detectIntent("VT001 để ở kho nào?"));
        assertEquals(ChatIntent.CHECK_BATCH, provider.detectIntent("lô ABC123 hạn dùng khi nào"));
        assertEquals(ChatIntent.CHECK_DEPARTMENT_EXPIRING_MATERIALS, provider.detectIntent("có lô nào gần hết date không?"));
        assertEquals(ChatIntent.CHECK_REQUEST_STATUS, provider.detectIntent("phiếu của tôi tới đâu rồi"));
        assertEquals(ChatIntent.CHECK_REQUEST_STATUS, provider.detectIntent("phiếu của em kho duyệt chưa?"));
        assertEquals(ChatIntent.CREATE_REQUEST_DRAFT, provider.detectIntent("mình cần xin 10 VT001"));
        assertEquals(ChatIntent.CHECK_DEPARTMENT_STOCK, provider.detectIntent("khoa tôi còn gì"));
    }

    @Test
    void ruleEngineRejectsMedicalAdvice() {
        RuleBasedAiProvider provider = new RuleBasedAiProvider();

        assertEquals(ChatIntent.UNKNOWN, provider.detectIntent("thuốc này liều dùng bao nhiêu"));
        assertEquals(ChatIntent.UNKNOWN, provider.detectIntent("uống thuốc này bao nhiêu lần một ngày?"));
        assertEquals(ChatIntent.UNKNOWN, provider.detectIntent("bệnh này điều trị thế nào"));
    }

    @Test
    void materialSearchUnderstandsAliasesAndLightTypos() {
        Material supplierNoise = material(4L, "VT028", "Mặt nạ khí dung người lớn", "", "cái");
        supplierNoise.setCategory("Cấp cứu");
        MaterialSearchService service = new MaterialSearchService(materialRepository(
                material(1L, "VT001", "Khẩu trang y tế", "mask; kt y tế", "cái"),
                material(2L, "VT002", "Bộ dịch truyền", "dây truyền dịch; bộ dây truyền", "bộ"),
                material(3L, "VT003", "Bơm tiêm 5ml", "syringe", "cái"),
                supplierNoise
        ));

        assertEquals("VT001", service.search("khau trangg con bao nhieu", 3).get(0).getCode(),
                () -> service.rankedMatches("khau trangg con bao nhieu", 3).stream()
                        .map(match -> match.material().getCode() + ":" + match.score())
                        .collect(Collectors.joining(", ")));
        assertEquals("VT002", service.search("day truyen dich nam o ke nao", 3).get(0).getCode());
        assertEquals("VT003", service.search("syringe 5ml còn không", 3).get(0).getCode());
        assertTrue(service.search("nhà cung cấp?", 3).isEmpty());
    }

    @Test
    void nlpDetectsBusinessIntentsAndMultipleMaterials() {
        MaterialSearchService searchService = new MaterialSearchService(materialRepository(
                material(1L, "VT001", "Khẩu trang y tế", "mask", "cái"),
                material(2L, "VT002", "Bộ dịch truyền", "dây truyền dịch", "bộ")
        ));
        ChatbotNlpService nlp = new ChatbotNlpService(searchService, emptyWarehouses(), emptyDepartments());

        ChatbotNlpService.ParsedQuestion stock = nlp.parse("còn bao nhiêu khẩu trang và bộ dịch truyền");
        assertEquals(ChatIntent.ASK_STOCK, stock.intent());
        assertEquals(2, stock.materials().size());

        assertEquals(ChatIntent.ASK_LOCATION, nlp.parse("khau trang o dau").intent());
        assertEquals(ChatIntent.ASK_RECOMMEND_ISSUE, nlp.parse("tôi cần lấy khẩu trang thì lấy lô nào trước").intent());
        assertEquals(ChatIntent.ASK_EXPIRED_OR_NEAR_EXPIRED, nlp.parse("có lô nào hết hạn trong 30 ngày tới không").intent());
        assertEquals(ChatIntent.GENERAL_HELP, nlp.parse("chatbot có thể làm gì").intent());
        assertEquals(ChatIntent.ASK_LOW_STOCK, nlp.parse("thuốc gì sắp hết trong kho").intent());
        assertEquals(ChatIntent.UNKNOWN, nlp.parse("thuốc này liều dùng bao nhiêu").intent());
    }

    @Test
    void nlpUnderstandsQuantityDepartmentAndNaturalTimeWindows() {
        MaterialSearchService searchService = new MaterialSearchService(materialRepository(
                material(1L, "VT001", "Khẩu trang y tế", "mask", "hộp"),
                material(2L, "VT003", "Bơm tiêm 5ml", "syringe", "cái")
        ));
        ChatbotNlpService nlp = new ChatbotNlpService(searchService, emptyWarehouses(), departments(department("KCC", "Khoa Cấp cứu")));

        ChatbotNlpService.ParsedQuestion enough = nlp.parse("cần 20 hộp khẩu trang có đủ không");
        assertEquals(ChatIntent.ASK_STOCK, enough.intent());
        assertEquals(20, enough.requestedQuantity());

        ChatbotNlpService.ParsedQuestion enoughByWords = nlp.parse("cần hai mươi hộp khẩu trang có đủ không");
        assertEquals(ChatIntent.ASK_STOCK, enoughByWords.intent());
        assertEquals(20, enoughByWords.requestedQuantity());

        ChatbotNlpService.ParsedQuestion shortage = nlp.parse("thiếu bao nhiêu nếu cần 500 hộp khẩu trang");
        assertEquals(ChatIntent.ASK_STOCK, shortage.intent());
        assertEquals(500, shortage.requestedQuantity());

        ChatbotNlpService.ParsedQuestion syringe = nlp.parse("tạo yêu cầu 30 bơm tiêm 5ml");
        assertEquals(ChatIntent.CREATE_REQUEST_DRAFT, syringe.intent());
        assertEquals(30, syringe.requestedQuantity());

        ChatbotNlpService.ParsedQuestion syringeByWords = nlp.parse("tạo yêu cầu một trăm bơm tiêm 5ml");
        assertEquals(ChatIntent.CREATE_REQUEST_DRAFT, syringeByWords.intent());
        assertEquals(100, syringeByWords.requestedQuantity());

        ChatbotNlpService.ParsedQuestion nextWeek = nlp.parse("tuần sau cần khẩu trang");
        assertNull(nextWeek.requestedQuantity());

        ChatbotNlpService.ParsedQuestion departmentStock = nlp.parse("khoa cấp cứu còn vật tư gì");
        assertEquals(ChatIntent.CHECK_DEPARTMENT_STOCK, departmentStock.intent());
        assertEquals("Khoa Cấp cứu", departmentStock.department());

        ChatbotNlpService.ParsedQuestion thisWeek = nlp.parse("lô nào sắp hết hạn tuần này");
        assertEquals(ChatIntent.ASK_EXPIRED_OR_NEAR_EXPIRED, thisWeek.intent());
        assertTrue(thisWeek.expiryWindowDays() >= 1 && thisWeek.expiryWindowDays() <= 7);
    }

    private MaterialRepository materialRepository(Material... materials) {
        MaterialRepository repository = mock(MaterialRepository.class);
        when(repository.findByDeletedFalseOrderByCodeAsc()).thenReturn(List.of(materials));
        return repository;
    }

    private WarehouseRepository emptyWarehouses() {
        WarehouseRepository repository = mock(WarehouseRepository.class);
        when(repository.findByDeletedFalseOrderByCodeAsc()).thenReturn(List.of());
        return repository;
    }

    private DepartmentRepository emptyDepartments() {
        DepartmentRepository repository = mock(DepartmentRepository.class);
        when(repository.findByDeletedFalseOrderByCodeAsc()).thenReturn(List.of());
        return repository;
    }

    private DepartmentRepository departments(Department... departments) {
        DepartmentRepository repository = mock(DepartmentRepository.class);
        when(repository.findByDeletedFalseOrderByCodeAsc()).thenReturn(List.of(departments));
        return repository;
    }

    private Material material(Long id, String code, String name, String alias, String unit) {
        Material material = new Material();
        material.setId(id);
        material.setCode(code);
        material.setName(name);
        material.setAliasText(alias);
        material.setUnit(unit);
        material.setStatus("ACTIVE");
        material.setActualQuantity(100);
        material.setMinStock(20);
        return material;
    }

    private Department department(String code, String name) {
        Department department = new Department();
        department.setCode(code);
        department.setName(name);
        return department;
    }
}
