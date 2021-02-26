package com.github.unbearables.mapigate

import com.github.unbearables.mapigate.gps.DijkstraGraph
import com.github.unbearables.mapigate.map.MapMarker
import org.junit.Test

import org.junit.Assert.*

class DijkstraGraphTest {
    @Test
    fun shortestPathTest() {
        val dijkstraGraph = DijkstraGraph()
        val m1 = MapMarker(1.0, 1.0, 1, "1")
        val m2 = MapMarker(2.0, 2.0, 2, "2")
        val m3 = MapMarker(5.0, 5.0, 3, "3")
        val m4 = MapMarker(3.0, 3.0, 4, "4")

        dijkstraGraph.addLink(m1 to m2, 1.0)
        dijkstraGraph.addLink(m1 to m3, 5.0)
        dijkstraGraph.addLink(m2 to m3, 1.0)
        dijkstraGraph.addLink(m2 to m4, 3.0)
        dijkstraGraph.addLink(m3 to m4, 1.0)

        val result = dijkstraGraph.shortestPath(m1, m4)
        assertEquals(result.totalDistance, 3.0, 0.0)
        assertArrayEquals(result.markers.toTypedArray(), arrayOf(m1, m2, m3, m4))
    }
}
