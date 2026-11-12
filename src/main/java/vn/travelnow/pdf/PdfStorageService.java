package vn.travelnow.pdf;

import java.util.Optional;

/**
 * Ranh giới giữa use case và hạ tầng lưu trữ file. Hiện tại là đĩa local; sau này
 * có thể là object storage (S3). Adapter mới vẫn phải tự xử lý khác biệt về
 * semantics — lỗi mạng, retry, phân quyền, tính nhất quán — interface không xóa được chúng.
 */
public interface PdfStorageService {

    /** Lưu (hoặc ghi đè) file PDF theo khóa. */
    void store(String key, byte[] content);

    Optional<byte[]> load(String key);
}
