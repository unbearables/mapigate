package com.github.unbearables.mapigate.map

/**
 * A custom View that serves as map marker. It bundles its own position and name.
 */
@Suppress("unused")
class MapMarker(var latitude: Double,
                var longitude: Double,
                val markerId: Any,
                val title: String,
                val entity: Any? = null,
                val markerIconResId: Int? = null,
                val clickable: Boolean = true,
                private val accessPoint: MapCoordinate? = null) {

    val accessLatitude: Double = latitude
        get() = accessPoint?.lat ?: field
    val accessLongitude: Double = longitude
        get() = accessPoint?.lng ?: field

    init {
        if (!(markerId is String || markerId is Number)) {
            throw IllegalArgumentException(
                "MapMarker.markerId must be String or Number, provided: ${markerId.javaClass.name}")
        }
    }
}
