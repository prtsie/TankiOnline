package com.example.tankionline.utils

import android.view.View
import com.example.tankionline.binding
import com.example.tankionline.models.Coordinate
import com.example.tankionline.models.Element

fun View.checkViewCanMoveThroughBorder(coordinate: Coordinate) =
    (coordinate.top >= 0 &&
            coordinate.top + this.height <= binding.container.height &&
            coordinate.left >= 0 &&
            coordinate.left + this.width <= binding.container.width)


fun getElementByCoordinates(coordinate: Coordinate, elementsOnContainer: List<Element>) =
    elementsOnContainer.firstOrNull { it.coordinate == coordinate }