package com.github.unbearables.mapigate

import com.github.unbearables.mapigate.gps.findNearestMarker
import com.github.unbearables.mapigate.gps.haversineDistanceKm
import com.github.unbearables.mapigate.map.MapMarker
import org.junit.Test

import org.junit.Assert.*

class GPSCalculationsTest {
    @Test
    fun haversineDistanceKmTest() {
        // Prague
        val lat1 = 50.1303061
        val lng1 = 14.3734206

        // Zlin
        val lat2 = 49.2265544
        val lng2 = 17.6670739

        assertEquals(haversineDistanceKm(lat1, lng1, lat2, lng2), 257.4, 1.0)
        assertEquals(haversineDistanceKm(lat1, lng1, lat2, lng2),haversineDistanceKm(lat2, lng2, lat1, lng1), 0.0)
    }

    @Test
    fun findNearestNodeTest() {
        val marker1 = MapMarker(1.0, 1.0, 1, "")
        val marker2 = MapMarker(2.0, 2.0, 2, "")
        val marker3 = MapMarker(3.0, 3.0, 3, "")

        assertEquals(findNearestMarker(listOf(marker1), 1.0, 1.0), marker1)
        assertEquals(findNearestMarker(listOf(marker1, marker2, marker3), 4.0, 4.0), marker3)
    }
}
