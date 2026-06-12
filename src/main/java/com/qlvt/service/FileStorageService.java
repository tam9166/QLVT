package com.qlvt.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "jpg", "jpeg", "png", "docx", "xlsx");
    private static final long DEFAULT_MAX_SIZE = 10 * 1024 * 1024;

    private final Path uploadDir;
    private final long maxFileSize;

    public FileStorageService(@Value("${app.upload.dir:uploads}") String uploadDir,
                              @Value("${app.upload.max-file-size:10485760}") long maxFileSize) {
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.maxFileSize = maxFileSize <= 0 ? DEFAULT_MAX_SIZE : maxFileSize;
    }

    public StoredFile store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn file cần tải lên");
        }
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("File vượt quá dung lượng cho phép");
        }
        String originalName = file.getOriginalFilename() == null ? "file" : Paths.get(file.getOriginalFilename()).getFileName().toString();
        String extension = extension(originalName);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Loại file không được phép. Chỉ hỗ trợ pdf, jpg, jpeg, png, docx, xlsx");
        }
        try {
            Files.createDirectories(uploadDir);
            String storedName = UUID.randomUUID() + "." + extension;
            Path target = uploadDir.resolve(storedName).normalize();
            file.transferTo(target);
            return new StoredFile(originalName, storedName, target.toString(), file.getContentType() == null ? "application/octet-stream" : file.getContentType(), file.getSize());
        } catch (IOException exception) {
            throw new IllegalStateException("Không thể lưu file tải lên", exception);
        }
    }

    public Resource load(String storagePath) {
        try {
            Path path = Paths.get(storagePath).toAbsolutePath().normalize();
            if (!path.startsWith(uploadDir)) {
                throw new IllegalStateException("Đường dẫn file không hợp lệ");
            }
            Resource resource = new UrlResource(path.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new IllegalStateException("Không thể đọc file đính kèm");
            }
            return resource;
        } catch (MalformedURLException exception) {
            throw new IllegalStateException("Đường dẫn file không hợp lệ", exception);
        }
    }

    private String extension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    public record StoredFile(String originalFileName, String storedFileName, String storagePath, String contentType, long fileSize) {}
}
