package com.qlvt;

import com.qlvt.service.FileStorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileStorageServiceTest {
    @TempDir
    Path tempDir;

    @Test
    void validUploadIsStoredWithGeneratedName() {
        FileStorageService service = new FileStorageService(tempDir.toString(), 1024);
        MockMultipartFile file = new MockMultipartFile("file", "bien-ban.pdf", "application/pdf", "ok".getBytes());

        FileStorageService.StoredFile stored = service.store(file);

        assertEquals("bien-ban.pdf", stored.originalFileName());
        assertTrue(stored.storedFileName().endsWith(".pdf"));
        assertTrue(Path.of(stored.storagePath()).toFile().exists());
    }

    @Test
    void invalidExtensionIsRejected() {
        FileStorageService service = new FileStorageService(tempDir.toString(), 1024);
        MockMultipartFile file = new MockMultipartFile("file", "virus.exe", "application/octet-stream", "x".getBytes());

        assertThrows(IllegalArgumentException.class, () -> service.store(file));
    }
}
