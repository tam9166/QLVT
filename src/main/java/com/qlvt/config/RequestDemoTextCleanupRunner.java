package com.qlvt.config;

import com.qlvt.entity.MaterialRequest;
import com.qlvt.entity.MaterialRequestLine;
import com.qlvt.repository.MaterialRequestRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("!prod")
@Order(11)
public class RequestDemoTextCleanupRunner implements CommandLineRunner {
    private final MaterialRequestRepository requestRepository;

    public RequestDemoTextCleanupRunner(MaterialRequestRepository requestRepository) {
        this.requestRepository = requestRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        requestRepository.findAll().stream()
                .filter(request -> "YC-DEMO-001".equals(request.getCode()) || "YC-DEMO-002".equals(request.getCode()))
                .forEach(this::cleanRequest);
    }

    private void cleanRequest(MaterialRequest request) {
        if ("YC-DEMO-001".equals(request.getCode())) {
            request.setDepartment("Khoa Cấp cứu");
            request.setPriority("Ưu tiên vừa");
            request.setNote("Cấp bổ sung vật tư trực cấp cứu cuối tuần");
            request.getLines().forEach(this::cleanEmergencyLine);
        } else if ("YC-DEMO-002".equals(request.getCode())) {
            request.setDepartment("Khoa Nội tổng hợp");
            request.setPriority("Ưu tiên vừa");
            request.setNote("Trưởng khoa đã duyệt, chờ kho xử lý");
            request.getLines().forEach(line -> {
                if ("VT006".equals(line.getMaterial().getCode())) {
                    line.setReason("Bổ sung xe tiêm truyền");
                    line.setNote("Dữ liệu mẫu");
                }
            });
        }
        requestRepository.save(request);
    }

    private void cleanEmergencyLine(MaterialRequestLine line) {
        if ("VT002".equals(line.getMaterial().getCode())) {
            line.setReason("Bổ sung tủ trực cấp cứu");
        } else if ("VT001".equals(line.getMaterial().getCode())) {
            line.setReason("Phát cho khu tiếp nhận");
        }
        line.setNote("Dữ liệu mẫu");
    }
}
