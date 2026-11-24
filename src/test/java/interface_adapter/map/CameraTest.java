package interface_adapter.map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CameraTest {
    @Test
    void centersTheTargetInTheMiddleOfTheMap() {
        assertEquals(500 - 480, Camera.offset(500, 960, 2000), 1e-9);
    }

    @Test
    void clampsAtTheStartOfTheMap() {
        assertEquals(0, Camera.offset(100, 960, 2000), 1e-9);
    }

    @Test
    void clampsAtTheEndOfTheMap() {
        assertEquals(2000 - 960, Camera.offset(1950, 960, 2000), 1e-9);
    }

    @Test
    void centersAMapSmallerThanTheView() {
        assertEquals(-(960 - 600) / 2.0, Camera.offset(300, 960, 600), 1e-9);
    }
}
