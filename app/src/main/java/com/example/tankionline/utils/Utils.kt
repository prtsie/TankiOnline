package com.example.tankionline.utils

import android.view.View
import com.example.tankionline.CELL_SIZE
import com.example.tankionline.binding
import com.example.tankionline.models.Coordinate
import com.example.tankionline.models.Element

fun View.checkViewCanMoveThroughBorder(coordinate: Coordinate) =
    (coordinate.top >= 0 &&
            coordinate.top + this.height <= binding.container.height &&
            coordinate.left >= 0 &&
            coordinate.left + this.width <= binding.container.width)


fun getElementByCoordinates(coordinate: Coordinate, elementsOnContainer: List<Element>): Element? {
    for (element in elementsOnContainer) {
        for (height in 0 until element.height) {
            for (width in 0 until element.width) {
                val searchingCoordinate = Coordinate(
                    element.coordinate.top + height * CELL_SIZE,
                    element.coordinate.left + width * CELL_SIZE
                )
                if (coordinate == searchingCoordinate) {
                    return element
                }
            }
        }
    }
    return null
}