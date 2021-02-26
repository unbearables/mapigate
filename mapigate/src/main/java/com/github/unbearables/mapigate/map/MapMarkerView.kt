package com.github.unbearables.mapigate.map

import android.content.Context
import androidx.appcompat.widget.AppCompatImageView
import com.github.unbearables.mapigate.R

class MapMarkerView(context: Context, val mapMarker: MapMarker): AppCompatImageView(context) {
    init {
        if (mapMarker.markerIconResId != null) {
            setImageResource(mapMarker.markerIconResId)
        } else {
            setImageResource(R.drawable.map_default_marker)
        }
    }
}
