package pl.orion.rover_controller_service.chassis.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Arrays;

public record ChassisInboundPayload(
    String eventType,
    ChassisPayload payload
) {
    public record ChassisPayload(
        @JsonProperty("stick") double[] stick,
        @JsonProperty("button_x") boolean buttonX,
        @JsonProperty("button_y") boolean buttonY,
        @JsonProperty("button_a") boolean buttonA,
        @JsonProperty("button_b") boolean buttonB,
        @JsonProperty("rotate") double[] rotate
    ) {
        @Override
        public String toString() {
            return "ChassisPayload{" +
                "stick=" + Arrays.toString(stick) +
                ", buttonX=" + buttonX +
                ", buttonY=" + buttonY +
                ", buttonA=" + buttonA +
                ", buttonB=" + buttonB +
                ", rotate=" + Arrays.toString(rotate) +
                '}';
        }
    }
}
