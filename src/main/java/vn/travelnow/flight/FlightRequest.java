package vn.travelnow.flight;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record FlightRequest(
        @NotBlank String origin,
        @NotBlank String destination,
        @NotNull LocalDate departureDate,
        @NotNull LocalDate arrivalDate
) {
}
