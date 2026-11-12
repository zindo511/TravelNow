package vn.travelnow.car;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CarRequest(
        @NotBlank String carType,
        @NotNull LocalDate pickupDate,
        @NotNull LocalDate returnDate
) {
}
