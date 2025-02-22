package com.example.tankionline.models

import android.view.View
import com.example.tankionline.enums.Material

data class Element constructor(
    val viewId: Int = View.generateViewId(),
    val material: Material,
    var coordinate: Coordinate,
    val width: Int,
    val height: Int
)
