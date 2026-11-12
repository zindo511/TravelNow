package vn.travelnow.trip;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "trips")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Trip {

    /** Do client sinh ra và gửi lên cùng yêu cầu, không phải do server cấp phát. */
    @Id
    @Column(length = 64)
    private String tripId;

    @Column(nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private TripStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    /**
     * Optimistic lock: hai yêu cầu hủy đồng thời không thể cùng thắng.
     * Version null cũng là dấu hiệu để Spring Data biết đây là entity mới và INSERT
     * (thay vì merge đè lên bản ghi có cùng tripId).
     */
    @Version
    private Long version;

    private Trip(String tripId, String userId) {
        this.tripId = tripId;
        this.userId = userId;
        this.status = TripStatus.PENDING;
        this.createdAt = Instant.now();
    }

    static Trip pending(String tripId, String userId) {
        return new Trip(tripId, userId);
    }

    void markBooked() {
        requireStatus(TripStatus.PENDING, "xác nhận");
        this.status = TripStatus.BOOKED;
    }

    void markCancelled() {
        requireStatus(TripStatus.BOOKED, "hủy");
        this.status = TripStatus.CANCELLED;
    }

    private void requireStatus(TripStatus expected, String action) {
        if (status != expected) {
            throw new InvalidTripStateException(
                    "Không thể %s chuyến đi %s đang ở trạng thái %s".formatted(action, tripId, status));
        }
    }
}
