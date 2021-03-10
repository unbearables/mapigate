[![](https://jitpack.io/v/Unbearables/mapigate.svg)](https://jitpack.io/#Unbearables/mapigate)
# Mapigate

Map library for displaying markers and finding shortest path amongst them using Dijkstra's algorithm.

Map implementation is done using efficient Kotlin library [MapView](https://github.com/peterLaurence/MapView) by [peterLaurence](https://github.com/peterLaurence)

## Installation
Install using gradle with desired version (latest is recommended):
```gradle
repositories {
  ...
  maven { url 'https://jitpack.io' }
}

dependencies {
  implementation 'com.github.unbearables:mapigate:{mapigate-version}'
}
```

## Usage

Easy ready to use implementation using `MapigateFragment`:

```kotlin
class MapFragment: MapigateFragment() {

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
      savedInstanceState: Bundle?): View? {
    return super.onCreateView(inflater, container, savedInstanceState).also {
      val markers = mutableListOf<MapMarker>()
      // fill markers list
      populateMap(markers)

      val dijkstraPaths = mutableListOf<IDjikstraPath>()
      // fill markers list
      initDijkstra(dijkstraPaths)
    }
  }

  override fun configureMap(): MapConfiguration {
    val topLat, rightLng, bottomLat, leftLng = ... // your coordinate bounds

    return MapConfiguration(topLat, rightLng, bottomLat, leftLng, 3815, 3085, 5, 256, 4f)
  }
}
``` 

## About

This project is result of a diploma thesis
