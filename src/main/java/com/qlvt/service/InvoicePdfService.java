package com.qlvt.service;

import com.qlvt.entity.IssueSlip;
import com.qlvt.entity.IssueSlipLine;
import com.qlvt.entity.Receipt;
import com.qlvt.entity.ReceiptLine;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class InvoicePdfService {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public byte[] receiptInvoice(Receipt receipt) {
        List<String> lines = new ArrayList<>();
        lines.add("HÓA ĐƠN NHẬP KHO");
        lines.add("Mã phiếu: " + receipt.getReceiptCode());
        lines.add("Ngày nhập: " + (receipt.getReceiptDate() == null ? "-" : receipt.getReceiptDate().format(DATE)));
        lines.add("Kho: " + value(receipt.getWarehouse() == null ? null : receipt.getWarehouse().getName()));
        lines.add("Nhà cung cấp: " + value(receipt.getSupplier() == null ? null : receipt.getSupplier().getName()));
        lines.add("Trạng thái: " + value(receipt.getStatus()));
        lines.add("Người tạo: " + value(receipt.getCreatedBy()));
        lines.add("Người xác nhận: " + value(receipt.getConfirmedBy()));
        lines.add("");
        lines.add("CHI TIẾT VẬT TƯ");

        BigDecimal total = BigDecimal.ZERO;
        int index = 1;
        for (ReceiptLine line : receipt.getLines()) {
            BigDecimal unitPrice = line.getUnitPrice() == null ? BigDecimal.ZERO : line.getUnitPrice();
            BigDecimal amount = unitPrice.multiply(BigDecimal.valueOf(line.getQuantity()));
            total = total.add(amount);
            lines.add(index++ + ". " + line.getMaterial().getCode() + " - " + line.getMaterial().getName());
            lines.add("   Lô: " + line.getBatchNumber()
                    + " | Vị trí: " + (line.getLocation() == null ? "-" : line.getLocation().getCode())
                    + " | HSD: " + (line.getExpiryDate() == null ? "-" : line.getExpiryDate().format(DATE)));
            lines.add("   Số lượng: " + line.getQuantity() + " | Đơn giá: " + unitPrice + " | Thành tiền: " + amount);
        }
        lines.add("");
        lines.add("Tổng tiền ước tính: " + total);
        lines.add("Ghi chú: " + value(receipt.getNote()));
        return simplePdf(lines);
    }

    public byte[] issueInvoice(IssueSlip issue) {
        List<String> lines = new ArrayList<>();
        lines.add("HÓA ĐƠN XUẤT KHO");
        lines.add("Mã phiếu: " + issue.getIssueCode());
        lines.add("Yêu cầu: " + (issue.getMaterialRequest() == null ? "-" : issue.getMaterialRequest().getCode()));
        lines.add("Khoa nhận: " + value(issue.getDepartment()));
        lines.add("Kho xuất: " + value(issue.getWarehouse() == null ? null : issue.getWarehouse().getName()));
        lines.add("Trạng thái: " + value(issue.getStatus()));
        lines.add("Người tạo: " + value(issue.getCreatedBy()));
        lines.add("Người xuất: " + value(issue.getIssuedBy()));
        lines.add("Ngày xuất: " + (issue.getIssuedAt() == null ? "-" : issue.getIssuedAt().format(DATE_TIME)));
        lines.add("");
        lines.add("CHI TIẾT VẬT TƯ");

        int index = 1;
        for (IssueSlipLine line : issue.getLines()) {
            lines.add(index++ + ". " + line.getMaterial().getCode() + " - " + line.getMaterial().getName());
            lines.add("   Yêu cầu: " + line.getRequestedQuantity()
                    + " | Duyệt: " + line.getApprovedQuantity()
                    + " | Đã xuất: " + line.getIssuedQuantity());
            if (line.getAllocations().isEmpty()) {
                lines.add("   Lô xuất: -");
            } else {
                line.getAllocations().forEach(allocation -> lines.add("   Lô: " + allocation.getBatch().getBatchNumber()
                        + " | Vị trí: " + allocation.getLocation().getCode()
                        + " | Số lượng: " + allocation.getQuantity()));
            }
        }
        lines.add("");
        lines.add("Ghi chú: " + value(issue.getNote()));
        return simplePdf(lines);
    }

    private byte[] simplePdf(List<String> rawLines) {
        List<String> lines = rawLines.stream().map(this::toPdfText).toList();
        StringBuilder content = new StringBuilder("BT\n/F1 11 Tf\n50 790 Td\n14 TL\n");
        for (String line : lines) {
            content.append("(").append(escape(line)).append(") Tj\nT*\n");
        }
        content.append("ET\n");
        byte[] contentBytes = content.toString().getBytes(StandardCharsets.US_ASCII);

        List<byte[]> objects = List.of(
                "<< /Type /Catalog /Pages 2 0 R >>".getBytes(StandardCharsets.US_ASCII),
                "<< /Type /Pages /Kids [3 0 R] /Count 1 >>".getBytes(StandardCharsets.US_ASCII),
                "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >>".getBytes(StandardCharsets.US_ASCII),
                "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>".getBytes(StandardCharsets.US_ASCII),
                ("<< /Length " + contentBytes.length + " >>\nstream\n" + content + "endstream").getBytes(StandardCharsets.US_ASCII)
        );

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        write(out, "%PDF-1.4\n");
        List<Integer> offsets = new ArrayList<>();
        for (int i = 0; i < objects.size(); i++) {
            offsets.add(out.size());
            write(out, (i + 1) + " 0 obj\n");
            out.writeBytes(objects.get(i));
            write(out, "\nendobj\n");
        }
        int xref = out.size();
        write(out, "xref\n0 " + (objects.size() + 1) + "\n");
        write(out, "0000000000 65535 f \n");
        for (Integer offset : offsets) {
            write(out, String.format("%010d 00000 n \n", offset));
        }
        write(out, "trailer\n<< /Size " + (objects.size() + 1) + " /Root 1 0 R >>\nstartxref\n" + xref + "\n%%EOF");
        return out.toByteArray();
    }

    private void write(ByteArrayOutputStream out, String value) {
        out.writeBytes(value.getBytes(StandardCharsets.US_ASCII));
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }

    private String value(Object value) {
        return value == null ? "-" : value.toString();
    }

    private String toPdfText(String value) {
        if (value == null) {
            return "-";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('đ', 'd')
                .replace('Đ', 'D');
        return normalized.replaceAll("[^\\x20-\\x7E]", "?");
    }
}
