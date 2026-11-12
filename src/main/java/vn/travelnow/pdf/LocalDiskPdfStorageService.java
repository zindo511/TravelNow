package vn.travelnow.pdf;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/** Ghi PDF ra đĩa local: zero setup, nhưng file gắn chặt với máy đang chạy process. */
@Service
public class LocalDiskPdfStorageService implements PdfStorageService {

    private final Path baseDir;

    public LocalDiskPdfStorageService(@Value("${travelnow.pdf.dir:./pdfs}") String dir) {
        this.baseDir = Path.of(dir).toAbsolutePath().normalize();
    }

    @Override
    public void store(String key, byte[] content) {
        try {
            Files.createDirectories(baseDir);
            Files.write(resolve(key), content);
        } catch (IOException e) {
            throw new UncheckedIOException("Không ghi được PDF " + key, e);
        }
    }

    @Override
    public Optional<byte[]> load(String key) {
        Path file = resolve(key);
        if (!Files.isRegularFile(file)) {
            return Optional.empty();
        }
        try {
            return Optional.of(Files.readAllBytes(file));
        } catch (IOException e) {
            throw new UncheckedIOException("Không đọc được PDF " + key, e);
        }
    }

    private Path resolve(String key) {
        // Khóa đến từ tripId do client cung cấp: không cho phép thoát khỏi thư mục gốc.
        Path file = baseDir.resolve(key + ".pdf").normalize();
        if (!file.startsWith(baseDir)) {
            throw new IllegalArgumentException("Khóa PDF không hợp lệ: " + key);
        }
        return file;
    }
}
