package com.github.unbearables.mapigate.map

import android.animation.ObjectAnimator
import android.graphics.*
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.peterlaurence.mapview.MapView
import com.peterlaurence.mapview.MapViewConfiguration
import com.peterlaurence.mapview.ReferentialData
import com.peterlaurence.mapview.api.*
import com.peterlaurence.mapview.core.TileStreamProvider
import com.peterlaurence.mapview.markers.MarkerTapListener
import com.peterlaurence.mapview.paths.PathPoint
import com.peterlaurence.mapview.paths.PathView
import com.peterlaurence.mapview.paths.addPathView
import com.peterlaurence.mapview.paths.toFloatArray
import com.peterlaurence.mapview.util.AngleDegree
import com.github.unbearables.mapigate.R
import com.github.unbearables.mapigate.gps.*
import com.github.unbearables.mapigate.map.api.IDijkstraPath
import com.github.unbearables.mapigate.map.api.MapigateEventListener
import com.github.unbearables.mapigate.map.config.MapConfiguration
import com.github.unbearables.mapigate.map.config.PathStyle
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

@Suppress("unused")
abstract class MapigateFragment : Fragment(), MapigateEventListener {

    private lateinit var parentView: ViewGroup

    // GUI layout
    private lateinit var myLocationBtn: FloatingActionButton
    // bottom sheet
    private var bottomSheetInit: Boolean = false
    private lateinit var bottomSheet: RelativeLayout
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var bottomSheetContent: RelativeLayout
    private lateinit var bottomSheetDragger: ImageView
    private lateinit var headerIcon: ImageView
    private lateinit var headerName: TextView
    private lateinit var directionButton: FloatingActionButton
    private lateinit var sumDistanceText: TextView

    // Map objects
    private lateinit var mapView: MapView
    private lateinit var mapRefOwner: RefOwner
    private var currPosMarker: MapMarkerView? = null
    private val currPositionMarkerId = UUID.randomUUID().toString()
    private lateinit var currPosRefOwner: RefOwner
    private var markerViewMap: MutableMap<Any, MapMarkerView> = mutableMapOf()
    // map - dijkstra
    private lateinit var pathView: PathView
    private lateinit var dijkstraGraph: DijkstraGraph
    private var dijkstraReady: Boolean = false
    // map - path
    private var pathMap: MutableMap<Int, MutableMap<Int, List<MapCoordinate>>> = HashMap()
    private lateinit var defaultPathStyle: PathStyle

    // current state helper
    private var clickedMarker: MapMarker? = null
    private var targetMarkerId: Any? = null
    private var distTextToTarget: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_map, container, false).also {
            parentView = it as ViewGroup
            myLocationBtn = it.findViewById(R.id.mapigateMyLocationButton)
            bottomSheet = it.findViewById(R.id.mapigateBSheet)

            myLocationBtn.setOnClickListener {
                if (currPosMarker != null) {
                    mapView.moveToMarker(currPosMarker!!, true)
                }
            }
            myLocationBtn.setOnLongClickListener {
                if (currPosMarker != null) {
                    mapView.moveToMarker(currPosMarker!!, true)
                    if (mapRefOwner.angleDegree != 0f) {
                        resetRotation()
                    }
                }
                true
            }

            initMapView()?.addToFragment()
            setupBottomSheet()
        }
    }

    private fun initBottomSheet() {
        if (bottomSheetInit) {
            return
        }
        bottomSheetInit = true

        bottomSheetDragger = bottomSheet.findViewById(R.id.mapigateBSheetDragger)
        bottomSheetContent = bottomSheet.findViewById(R.id.mapigateBSheetContent)
        headerIcon = bottomSheet.findViewById(R.id.mapigateHeaderIcon)
        headerName = bottomSheet.findViewById(R.id.mapigateHeaderName)
        directionButton = bottomSheet.findViewById(R.id.mapigateDirButton)
        sumDistanceText = bottomSheet.findViewById(R.id.mapigateSumDistText)

        val moveToClickedMarker = View.OnClickListener {
            mapView.moveToMarker(currPosMarker!!, true)
        }
        headerIcon.setOnClickListener(moveToClickedMarker)
        headerName.setOnClickListener(moveToClickedMarker)

        directionButton.setOnClickListener {
            if (currPosMarker != null && dijkstraReady) {
                val currLat = currPosMarker!!.mapMarker.latitude
                val currLng = currPosMarker!!.mapMarker.longitude

                val nearestNode = findNearestMarker(markerViewMap.values.map { m -> m.mapMarker }, currLat, currLng)
                val haversineDistance = haversineDistanceKm(
                        currLat, currLng, nearestNode.latitude, nearestNode.longitude)
                val dijsktraResult = findAndDrawShortestPath(nearestNode)

                targetMarkerId = clickedMarker!!.markerId
                distTextToTarget = DistanceUtil.prettifyMeters((haversineDistance * 1000) + dijsktraResult.totalDistance)
                sumDistanceText.text = distTextToTarget

                onShortestPathFind(dijsktraResult.markers, dijsktraResult.distanceStepMap,
                        haversineDistance, dijsktraResult.totalDistance)

                mapView.moveToMarker(currPosMarker!!, 1f, true)

                halfExpandBottomSheet()
            }
            else {
                Toast.makeText(activity, "Unavailable", Toast.LENGTH_SHORT).show()
            }
        }

        initBottomSheetLayout(bottomSheetContent)
    }

    private fun setupBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        bottomSheetDragger.setImageResource(R.drawable.map_sheet_dragger_down)
                    }
                    else -> {
                        bottomSheetDragger.setImageResource(R.drawable.map_sheet_dragger_up)
                    }
                }

                onBottomSheetStateChange(newState)
            }
            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        }
        bottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback)
        bottomSheetBehavior.isFitToContents = false
        bottomSheetBehavior.saveFlags = BottomSheetBehavior.SAVE_ALL
        bottomSheetBehavior.halfExpandedRatio = 0.4f
        bottomSheetBehavior.peekHeight = 90
        bottomSheetBehavior.isHideable = true
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun halfExpandBottomSheet() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
        onBottomSheetStateChange(BottomSheetBehavior.STATE_HALF_EXPANDED)
    }

    private fun initMapView(): MapView? {
        val context = requireContext()
        mapView = MapView(context)

        val mapConfig = configureMap()

        val tileStreamProvider = TileStreamProvider { row, col, zoomLvl ->
                try {
                    val tilePath = if (mapConfig.assetPathToMapTiles.endsWith("/")) {
                        mapConfig.assetPathToMapTiles
                    } else {
                        mapConfig.assetPathToMapTiles.plus("/")
                    }
                    context.assets.open("$tilePath$zoomLvl/$row/$col.jpg")
                } catch (e: Exception) {
                    null
                }
            }

        mapView.configure(
                MapViewConfiguration(
                        mapConfig.levelCount, mapConfig.fullWidth,
                        mapConfig.fullHeight, mapConfig.tileSize, tileStreamProvider
                ).setMaxScale(mapConfig.maxScale).enableRotation(mapConfig.enableRotation)
        )

        mapView.defineBounds(mapConfig.leftLongitude, mapConfig.topLatitude,
                mapConfig.rightLongitude, mapConfig.bottomLatitude)

        mapRefOwner = object : RefOwner {
            override var referentialData: ReferentialData = ReferentialData(mapConfig.enableRotation, scale = 1f)
                set(value) {
                    field = value
                    angleDegree = value.angle
                }

            override var angleDegree: AngleDegree = 0f
        }
        mapView.addReferentialOwner(mapRefOwner)

        currPosRefOwner = object : RefOwner {
            override var referentialData: ReferentialData = ReferentialData(mapConfig.enableRotation, scale = 1f)
                set(value) {
                    field = value
                    rotateMaker()
                }

            override var angleDegree: AngleDegree = 0f
                set(value) {
                    field = value
                    rotateMaker()
                }

            private fun rotateMaker() {
                currPosMarker?.rotation = angleDegree + referentialData.angle
            }
        }
        mapView.addReferentialOwner(currPosRefOwner)

        mapView.setMarkerTapListener(object : MarkerTapListener {
            override fun onMarkerTap(view: View, x: Int, y: Int) {
                if (view is MapMarkerView && view.mapMarker.clickable) {
                    if (view.mapMarker.markerId == currPositionMarkerId) {
                        onCurrentPositionMarkerMarkerTap(x, y)
                    } else {
                        initBottomSheet()

                        onMapMarkerTap(view.mapMarker)

                        halfExpandBottomSheet()
                        mapView.moveToMarker(view, true)

                        clickedMarker = view.mapMarker
                        headerName.text = clickedMarker!!.title
                        clickedMarker!!.markerIconResId?.let { headerIcon.setImageResource(it) }
                        sumDistanceText.text = distTextToTarget
                            ?.takeIf { clickedMarker!!.markerId == targetMarkerId } ?: ""
                    }
                }
            }
        })

        pathView = PathView(context)
        mapView.addPathView(pathView)
        initDefaultPathStyle()

        return mapView
    }

    private fun initDefaultPathStyle() {
        val paint = Paint()
        paint.color = Color.parseColor(DEFAULT_PATH_COLOR)
        paint.style = Paint.Style.STROKE
        paint.isAntiAlias = true
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND
        defaultPathStyle = PathStyle(paint, 15f)
    }

    private fun findAndDrawShortestPath(nearestNode: MapMarker): DijkstraResult {
        val pathPoints = mutableListOf<PathPoint>()
        if (currPosMarker != null) {
            pathPoints.add(PathPoint(currPosMarker!!.mapMarker.longitude, currPosMarker!!.mapMarker.latitude))
        }

        val dijkstraResult: DijkstraResult
        val markerList: List<MapMarker>
        if (nearestNode.markerId == clickedMarker!!.markerId) {
            markerList = listOf(nearestNode)
            dijkstraResult = DijkstraResult(markerList, emptyMap(), 0.0)
        } else {
            dijkstraResult = dijkstraGraph.shortestPath(nearestNode, clickedMarker as MapMarker)
            markerList = dijkstraResult.markers
        }

        drawPath(markerList, pathPoints)

        return dijkstraResult
    }

    private fun drawPath(markerList: List<MapMarker>, pathPoints: MutableList<PathPoint>) {
        val lastIndex = markerList.size - 1
        for ((i, n) in markerList.withIndex()) {
            pathPoints.add(PathPoint(n.accessLongitude, n.accessLatitude))
            if (i != lastIndex && pathMap.containsKey(n.markerId)) {
                val pathCoordinates = pathMap[n.markerId]!![markerList[1 + i].markerId]
                if (pathCoordinates != null) {
                    for (pc in pathCoordinates) {
                        pathPoints.add(PathPoint(pc.lng, pc.lat))
                    }
                }
            }
        }
        pathView.visibility = View.VISIBLE
        val pathStyle = stylePath() ?: defaultPathStyle
        pathView.updatePaths(listOfNotNull(pathPoints.toFloatArray(mapView))
                .map {
                    object : PathView.DrawablePath {
                        override val visible: Boolean = true
                        override var path: FloatArray = it
                        override var paint: Paint? = pathStyle.paint
                        override val width: Float? = pathStyle.width
                    }
                })
    }

    private fun MapView.addToFragment() = apply {
        id = R.id.mapigate_mapview_id
        isSaveEnabled = true
        parentView.addView(this, 0)
    }

    private fun MapView.addCurrPositionMarker(lat: Double, lng: Double): MapMarkerView {
        val mView = MapMarkerView(requireContext(),
            MapMarker(lat, lng, currPositionMarkerId, "", null, R.drawable.map_marker_curr_position)) // TODO customize icon
        addMarker(mView, lng , lat, -0.5f, -0.5f)
        return mView
    }

    private fun resetRotation() {
        val initAngle = mapRefOwner.angleDegree
        val wrapper = object {
            fun setAngle(angle: Float) {
                mapView.setAngle(angle)
            }

            fun getAngle(): Float {
                return if (initAngle > 180f) mapRefOwner.angleDegree - 360 else mapRefOwner.angleDegree
            }
        }
        ObjectAnimator.ofFloat(wrapper, "angle", 0f).apply {
            interpolator = DecelerateInterpolator()
            duration = 1000
            start()
        }
    }

    private fun clearMapMarkers() {
        clickedMarker = null
        markerViewMap.clear()
        pathView.visibility = View.INVISIBLE

        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        onBottomSheetStateChange(BottomSheetBehavior.STATE_HIDDEN)
    }

    /**
     * Mapigate API
     */

    fun populateMap(markers: MutableCollection<MapMarker>, clearOldMarkers: Boolean = false) {
        if (clearOldMarkers) {
            clearMapMarkers()
        }

        for (m in markers) {
            val mView = MapMarkerView(requireContext(), m)
            markerViewMap[m.markerId] = mView
            mapView.addMarker(mView, m.longitude, m.latitude, -0.5f, -0.5f)
        }
    }

    fun updateCurrentPosition(location: Location) {
        updateCurrentPosition(location.longitude, location.latitude, location.bearing)
    }

    fun updateCurrentPosition(lng: Double, lat: Double, bearing: Float = 0f) {
        if (currPosMarker == null) {
            currPosMarker = mapView.addCurrPositionMarker(lng, lat)
        }
        mapView.moveMarker(currPosMarker!!, lng, lat)
        currPosMarker!!.mapMarker.latitude = lat
        currPosMarker!!.mapMarker.longitude = lng
        currPosRefOwner.angleDegree = bearing
    }

    fun initDijkstra(dijkstraPaths: List<IDijkstraPath>) {
        dijkstraGraph = DijkstraGraph()

        for (dp in dijkstraPaths) {
            if (markerViewMap.containsKey(dp.fromMarkerId) && markerViewMap.containsKey(dp.toMarkerId)) {
                dijkstraGraph.addLink(
                    markerViewMap[dp.fromMarkerId]!!.mapMarker to markerViewMap[dp.toMarkerId]!!.mapMarker,
                     dp.distanceInMeters)

                if (dp.pathList != null) {
                    val fromMap = pathMap.getOrPut(dp.fromMarkerId) { HashMap() }
                    fromMap[dp.toMarkerId] = dp.pathList!!
                    val toMap = pathMap.getOrPut(dp.toMarkerId) { HashMap() }
                    toMap[dp.fromMarkerId] = ArrayList(dp.pathList!!).reversed()
                }
            }
        }

        dijkstraReady = true
    }

    fun moveToMarkerById(markerId: Any, destScale: Float = 1f, animate: Boolean = true): Boolean {
        if (markerViewMap.containsKey(markerId)) {
            mapView.moveToMarker(markerViewMap[markerId]!!, destScale, animate)
            return true
        }
        return false
    }

    fun moveToCurrentPositionMarker(destScale: Float = 1f, animate: Boolean = true) {
        if (currPosMarker != null) {
            mapView.moveToMarker(currPosMarker!!, destScale, animate)
        }
    }

    fun changePathVisibility(visibility: Int) {
        pathView.visibility = visibility
    }

    /**
     * Mandatory configuration
     */
    abstract fun configureMap(): MapConfiguration

    /** Defaulting style path */
    open fun stylePath(): PathStyle? = null
}

private const val DEFAULT_PATH_COLOR = "#1460aa"
