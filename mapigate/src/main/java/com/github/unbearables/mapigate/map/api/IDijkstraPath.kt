package com.github.unbearables.mapigate.map.api

import com.github.unbearables.mapigate.map.MapCoordinate

interface IDijkstraPath {
    val fromMarkerId: Int
    val toMarkerId: Int
    val distanceInMeters: Double
    val pathList: List<MapCoordinate>?
}
