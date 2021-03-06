package com.github.unbearables.mapigate.map.api

import com.github.unbearables.mapigate.map.MapCoordinate

interface IMapPath {
    val fromMarkerId: Int
    val toMarkerId: Int
    val distanceInMeters: Double
    val pathList: List<MapCoordinate>?
}
