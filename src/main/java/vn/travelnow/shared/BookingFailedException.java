package vn.travelnow.shared;

import lombok.Getter;

/**
 * Một bước trong luồng đặt chuyến đi thất bại. Là RuntimeException nên
 * transaction bao ngoài sẽ rollback toàn bộ các bước trước đó.
 */
@Getter
public class BookingFailedException extends RuntimeException {

    private final BookingStep step;

    public BookingFailedException(BookingStep step, String message) {
        super(message);
        this.step = step;
    }
}
