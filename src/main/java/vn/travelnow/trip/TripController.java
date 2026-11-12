package vn.travelnow.trip;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TripDetails book(@Valid @RequestBody BookTripRequest request) {
        return tripService.bookTrip(request);
    }

    @GetMapping("/{tripId}")
    public TripDetails get(@PathVariable String tripId) {
        return tripService.getTrip(tripId);
    }

    @PostMapping("/{tripId}/cancel")
    public TripDetails cancel(@PathVariable String tripId) {
        return tripService.cancelTrip(tripId);
    }
}
