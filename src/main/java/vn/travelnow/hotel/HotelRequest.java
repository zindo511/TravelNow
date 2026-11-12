package vn.travelnow.hotel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record HotelRequest(
        @NotBlank String hotelName,
        @NotNull LocalDate checkIn,
        @NotNull LocalDate checkOut
) {
}
