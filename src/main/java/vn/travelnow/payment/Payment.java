package vn.travelnow.payment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** Một giao dịch thanh toán cho cả chuyến đi. */
@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String tripId;

    @Column(nullable = false, precision = 15, scale = 0)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private PaymentStatus status;

    /** Mã xác nhận từ nhà cung cấp thanh toán (giả lập). */
    @Column(nullable = false, length = 32)
    private String confirmationCode;

    Payment(String tripId, BigDecimal amount, String confirmationCode) {
        this.tripId = tripId;
        this.amount = amount;
        this.confirmationCode = confirmationCode;
        this.status = PaymentStatus.PAID;
    }

    void refund() {
        this.status = PaymentStatus.REFUNDED;
    }
}
