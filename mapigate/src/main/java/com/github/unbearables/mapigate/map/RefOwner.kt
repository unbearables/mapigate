package com.github.unbearables.mapigate.map

import com.peterlaurence.mapview.ReferentialData
import com.peterlaurence.mapview.ReferentialOwner
import com.peterlaurence.mapview.util.AngleDegree

/**
 * Extends ReferentialOwner by var angleDegree
 */
interface RefOwner : ReferentialOwner {
    override var referentialData: ReferentialData
    var angleDegree: AngleDegree
}
