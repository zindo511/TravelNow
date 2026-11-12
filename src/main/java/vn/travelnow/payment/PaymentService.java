package vn.travelnow.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

/** Cổng thanh toán giả lập: "thu tiền" và "hoàn tiền" chỉ là thao tác ghi database. */
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository repository;

    @Transactional(propagation = Propagation.MANDATORY)
    public Payment charge(String tripId, BigDecimal amount) {
        String confirmationCode = "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return repository.save(new Payment(tripId, amount, confirmationCode));
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void refund(String tripId) {
        repository.findByTripId(tripId)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy thanh toán của chuyến đi " + tripId))
                .refund();
    }

    @Transactional(readOnly = true)
    public Optional<Payment> findByTripId(String tripId) {
        return repository.findByTripId(tripId);
    }
}
