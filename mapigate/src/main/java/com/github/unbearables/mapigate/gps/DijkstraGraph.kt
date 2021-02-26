package com.github.unbearables.mapigate.gps

import com.github.unbearables.mapigate.map.MapMarker

data class Edge(val from: MapMarker, val to: MapMarker, val value: Double)

data class DijkstraResult(val markers: List<MapMarker>, val distanceStepMap: Map<Any, Double>,
                          val totalDistance: Double)

/**
 * Non-directed Djikstra graph
 */
class DijkstraGraph {
    private val edges = mutableSetOf<Edge>()
    private val vertices = mutableSetOf<MapMarker>()

    fun addLink(pair: Pair<MapMarker, MapMarker>, value: Double) {
        val (from, to) = pair
        vertices += from
        vertices += to

        edges.add(Edge(from, to, value))
        edges.add(Edge(to, from, value))
    }

    /**
     * Finds shortest path in djikstra graph
     */
    fun shortestPath(from: MapMarker, target: MapMarker): DijkstraResult {
        val unvisitedSet = vertices.toSet().toMutableSet() // clone set
        val dists = vertices.map { it.markerId to Double.POSITIVE_INFINITY }.toMap().toMutableMap()
        val paths = mutableMapOf<MapMarker, List<MapMarker>>()
        dists[from.markerId] = 0.0 // 0 to itself
        var curr = from // start from itself

        while (unvisitedSet.isNotEmpty() && unvisitedSet.contains(target)) {
            adjacentVertices(curr).forEach {
                adj -> calcAdjacent(curr, adj, dists, paths)
            }
            unvisitedSet.remove(curr)

            if (curr.markerId == target.markerId
                    || unvisitedSet.all { dists[it.markerId]!!.isInfinite() }) {
                break
            }
            if (unvisitedSet.isNotEmpty()) {
                curr = unvisitedSet.minByOrNull { dists[it.markerId]!! }!!
            }
        }

        if (paths.containsKey(target)) {
            return dijkstraResult(target, paths[target]!!, dists)
        }
        return DijkstraResult(emptyList(), emptyMap(), 0.0) // no result
    }

    private fun calcAdjacent(curr: MapMarker, adj: MapMarker, dists: MutableMap<Any, Double>,
                             paths: MutableMap<MapMarker, List<MapMarker>>) {
        val dist = getDistance(curr, adj)
        if (dists[curr.markerId]!! + dist < dists[adj.markerId]!!) {
            dists[adj.markerId] = dists[curr.markerId]!! + dist
            paths[adj] = paths.getOrDefault(curr, listOf(curr)) + listOf(adj)
        }
    }

    private fun dijkstraResult(target: MapMarker, markers: List<MapMarker>,
            dists: MutableMap<Any, Double>): DijkstraResult {
        val distStepMap = mutableMapOf<Any, Double>()
        var lastDist = 0.0
        var prevMarkerId: Any? = null
        for ((i, m) in markers.withIndex()) {
            if (i != 0) {
                val distStep = dists[m.markerId]!!
                distStepMap[prevMarkerId!!] = distStep - lastDist
                lastDist = distStep
            }
            prevMarkerId = m.markerId
        }
        return DijkstraResult(markers, distStepMap, dists[target.markerId]!!)
    }

    private fun adjacentVertices(marker: MapMarker): Set<MapMarker> {
        return edges
                .filter { it.from.markerId == marker.markerId }
                .map { it.to }
                .toSet()
    }

    private fun getDistance(from: MapMarker, to: MapMarker): Double {
        return edges
                .filter { it.from.markerId == from.markerId && it.to.markerId == to.markerId }
                .map { it.value }
                .first()
    }
}
