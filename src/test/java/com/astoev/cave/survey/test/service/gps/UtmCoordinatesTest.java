package com.astoev.cave.survey.test.service.gps;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.astoev.cave.survey.service.gps.UtmCoordinate;

import org.junit.jupiter.api.Test;


public class UtmCoordinatesTest {

    @Test
    public void testWgs84ToUtm() {

        UtmCoordinate coordinate = new UtmCoordinate(42.811522, 23.378906);
        assertEquals(694497, coordinate.getEasting(), 1);
        assertEquals(4742630, coordinate.getNorthing(), 1);
        assertEquals(34, coordinate.getZone());
        assertEquals('T', coordinate.getLetter());

        coordinate = new UtmCoordinate(42.123, 23.234);
        assertEquals(684663, coordinate.getEasting(), 1);
        assertEquals(4665848, coordinate.getNorthing(), 1);
        assertEquals(34, coordinate.getZone());
        assertEquals('T', coordinate.getLetter());
    }
}
