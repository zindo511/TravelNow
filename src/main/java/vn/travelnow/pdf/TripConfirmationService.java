package vn.travelnow.pdf;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import vn.travelnow.trip.TripBookedEvent;
import vn.travelnow.trip.TripDetails;
import vn.travelnow.trip.TripService;

@Service
@RequiredArgsConstructor
@Slf4j
public class TripConfirmationService {

    private final TripService tripService;
    private final TripConfirmationPdfGenerator generator;
    private final PdfStorageService storage;

    /**
     * Chạy sau khi transaction đặt chuyến đã commit; rollback thì không bao giờ được gọi.
     * Ghi file nằm ngoài database transaction: lỗi ở đây không làm thanh toán thất bại
     * và không được dẫn đến một lần thu tiền mới, nên chỉ ghi log.
     * Nếu process chết giữa commit và bước này thì file bị bỏ sót — xem getConfirmation.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTripBooked(TripBookedEvent event) {
        TripDetails trip = event.trip();
        try {
            storage.store(trip.tripId(), generator.generate(trip));
        } catch (RuntimeException e) {
            log.error("Chuyến đi {} đã BOOKED nhưng tạo PDF xác nhận thất bại", trip.tripId(), e);
        }
    }

    /**
     * Trả về PDF xác nhận. PDF dựng lại được từ dữ liệu đã lưu, nên nếu file bị thiếu
     * (bước sau commit lỗi) thì tạo lại tại đây.
     */
    public byte[] getConfirmation(String tripId) {
        TripDetails trip = tripService.getTrip(tripId);
        return storage.load(tripId).orElseGet(() -> {
            byte[] pdf = generator.generate(trip);
            storage.store(tripId, pdf);
            return pdf;
        });
    }
}
