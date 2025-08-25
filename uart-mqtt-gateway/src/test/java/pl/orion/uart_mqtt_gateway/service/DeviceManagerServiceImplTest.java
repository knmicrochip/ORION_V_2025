package pl.orion.uart_mqtt_gateway.service;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class DeviceManagerServiceImplTest {
    @Test
    void testRegexMatch() {
        //given
        final var allowedPortPrefix = "regex:/dev/ttyACM[0-9]";
        final var port = "/dev/ttyACM1";

        //when
        final var isAllowed = DeviceManagerServiceImpl.regexMatch(port, allowedPortPrefix);

        //then
        assertTrue(isAllowed);
    }

    @Test
    void testRegexDoesntMatch() {
        //given
        final var allowedPortPrefix = "regex:/dev/ttyACM[3-9]";
        final var port = "/dev/ttyACM0";

        //when
        final var isAllowed = DeviceManagerServiceImpl.regexMatch(port, allowedPortPrefix);

        //then
        assertTrue(!isAllowed);
    }
}
