package vn.edu.fpt.bookstore.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp", "gif");
    private final Path uploadPath;

    public FileStorageService(@Value("${app.upload-dir}") String uploadDir) {
        this.uploadPath = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    public String storeBookCover(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "cover" : file.getOriginalFilename());
        String extension = extensionOf(original);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Ảnh bìa chỉ chấp nhận JPG, PNG, WEBP hoặc GIF");
        }
        try {
            Files.createDirectories(uploadPath);
            String filename = UUID.randomUUID() + "." + extension;
            Path target = uploadPath.resolve(filename).normalize();
            if (!target.startsWith(uploadPath)) {
                throw new IllegalArgumentException("Tên file không hợp lệ");
            }
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/" + filename;
        } catch (IOException e) {
            throw new IllegalStateException("Không thể lưu ảnh bìa", e);
        }
    }

    private String extensionOf(String filename) {
        int index = filename.lastIndexOf('.');
        return index < 0 ? "" : filename.substring(index + 1).toLowerCase(Locale.ROOT);
    }
}
