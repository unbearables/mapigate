package com.github.unbearables.mapigate.map.config

data class MapConfiguration(
        val topLatitude: Double, val rightLongitude: Double,
        val bottomLatitude: Double, val leftLongitude: Double,
        val fullWidth: Int, val fullHeight: Int, val levelCount: Int, val tileSize: Int,
        val maxScale: Float, val enableRotation: Boolean = true,
        val assetPathToMapTiles: String = "tiles/", val currPositionMarkerResId: Int? = null)
