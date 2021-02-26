package com.github.unbearables.mapigate.gps

class DistanceUtil {

    companion object {
        @JvmStatic
        fun prettifyKilometers(km: Number, addSpace: Boolean = false): String {
            val kmDouble = km.toDouble()
            return if (kmDouble < 1) niceM((kmDouble * 1000).toInt(), addSpace) else niceKm(kmDouble, addSpace)
        }

        @JvmStatic
        fun prettifyMeters(m: Number, addSpace: Boolean = false): String {
            val mInt = m.toInt()
            return if (mInt < 1000) niceM(mInt, addSpace) else niceKm(mInt / 1000.0, addSpace)
        }

        private fun niceKm(km: Double, spaceBetween: Boolean): String {
            return "%.2f".format(km)
                    .plus(" ".takeIf { spaceBetween } ?: "")
                    .plus("km")
        }

        private fun niceM(m: Int, spaceBetween: Boolean): String {
            return m.toString()
                    .plus(" ".takeIf { spaceBetween } ?: "")
                    .plus("m")
        }
    }
}
