package com.github.unbearables.mapigate.gps

import com.github.unbearables.mapigate.map.MapMarker
import kotlin.math.*

const val EARTH_RADIUS = 6371

fun haversineDistanceKm(lat1: Double, lng1: Double, lat2: Double, lng2: Double,
      radius: Int = EARTH_RADIUS): Double {
    val diffLat = Math.toRadians(lat2 - lat1)
    val diffLng = Math.toRadians(lng2 - lng1)

    val a = sin(diffLat / 2).pow(2) + sin(diffLng / 2).pow(2) * cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2))
    return 2 * radius * asin(sqrt(a))
}

fun findNearestMarker(markers: List<MapMarker>, currLat: Double, currLng: Double): MapMarker {
    lateinit var closestMarker: MapMarker
    var minDistance = Double.POSITIVE_INFINITY
    for (m in markers) {
        val distance = sqrt((currLat - m.accessLatitude).pow(2)
                + (currLng - m.accessLongitude).pow(2)) // pythagore
        if (minDistance > distance) {
            minDistance = distance
            closestMarker = m
        }
    }
    return closestMarker
}
