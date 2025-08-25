package pl.orion.uart_mqtt_gateway.model;

public record ErrorPayload(String application, String error, String message) {
}
