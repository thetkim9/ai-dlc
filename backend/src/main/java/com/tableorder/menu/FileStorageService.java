package com.tableorder.menu;

import com.tableorder.common.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024; // 5MB

    private final String uploadDir;

    public FileStorageService(@Value("${upload.dir}") String uploadDir) {
        this.uploadDir = uploadDir;
    }

    public String saveFile(Long storeId, MultipartFile file) {
        validateFile(file);

        String ext = getExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID() + "." + ext;
        Path dir = Paths.get(uploadDir, storeId.toString());

        try {
            Files.createDirectories(dir);
            Files.write(dir.resolve(filename), file.getBytes());
        } catch (IOException e) {
            throw ApiException.badRequest("파일 저장에 실패했습니다.");
        }

        return "/uploads/" + storeId + "/" + filename;
    }

    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) return;
        // /uploads/{storeId}/{filename} 형태에서 실제 경로 추출
        String relativePath = fileUrl.replaceFirst("^/uploads/", "");
        Path filePath = Paths.get(uploadDir, relativePath);
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException ignored) {
            // 파일 삭제 실패는 무시 (이미 없는 경우 등)
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw ApiException.badRequest("파일이 비어있습니다.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw ApiException.badRequest("파일 크기는 5MB를 초과할 수 없습니다.");
        }
        String ext = getExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(ext.toLowerCase())) {
            throw ApiException.badRequest("허용되지 않는 파일 형식입니다. (jpg, jpeg, png, webp만 허용)");
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw ApiException.badRequest("파일 확장자를 확인할 수 없습니다.");
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}
