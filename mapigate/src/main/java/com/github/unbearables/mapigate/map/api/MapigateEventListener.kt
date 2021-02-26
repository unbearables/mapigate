package com.github.unbearables.mapigate.map.api

import android.widget.RelativeLayout
import com.github.unbearables.mapigate.map.MapMarker

interface MapigateEventListener {

    fun onMapMarkerTap(marker: MapMarker) { }

    fun onCurrentPositionMarkerMarkerTap(lat: Int, lng: Int) { }

    fun initBottomSheetLayout(bottomSheetContentLayout: RelativeLayout) { }

    fun onBottomSheetStateChange(state: Int) { }

    fun onShortestPathFind(markerList: List<MapMarker>, distanceStepMap: Map<Any, Double>,
                           distanceToFirst: Double, dijsktraDistance: Double) { }
}
