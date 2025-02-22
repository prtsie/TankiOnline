package com.example.tankionline.models

import android.view.View
import android.widget.FrameLayout
import com.example.tankionline.CELL_SIZE
import com.example.tankionline.binding
import com.example.tankionline.enums.Direction
import com.example.tankionline.utils.checkViewCanMoveThroughBorder
import com.example.tankionline.utils.getElementByCoordinates

class Tank(val element: Element, var direction: Direction) {
    fun move(direction: Direction, container: FrameLayout, elementsOnContainer: List<Element>) {
        val view = container.findViewById<View>(element.viewId) ?: return
        val currentCoordinate = getTankCurrentCoordinate(view)
        this.direction = direction
        view.rotation = direction.rotation
        val nextCoordinate = getTankNextCoordinate(view)
        if (view.checkViewCanMoveThroughBorder(
                nextCoordinate
            ) && element.checkTankCanMoveThroughMaterial(nextCoordinate, elementsOnContainer)) {
            binding.container.removeView(view)
            binding.container.addView(view)
            element.coordinate = nextCoordinate
        } else {
            element.coordinate = currentCoordinate
            (view.layoutParams as FrameLayout.LayoutParams).topMargin = currentCoordinate.top
            (view.layoutParams as FrameLayout.LayoutParams).leftMargin = currentCoordinate.left
        }
    }

    private fun getTankCurrentCoordinate(tank: View): Coordinate {
        return Coordinate(
            (tank.layoutParams as FrameLayout.LayoutParams).topMargin,
            (tank.layoutParams as FrameLayout.LayoutParams).leftMargin
        )
    }

    private fun getTankNextCoordinate(view: View): Coordinate {
        val layoutParams = view.layoutParams as FrameLayout.LayoutParams
        when (direction) {
            Direction.UP -> {
                (view.layoutParams as FrameLayout.LayoutParams).topMargin -= CELL_SIZE
            }

            Direction.DOWN -> {
                (view.layoutParams as FrameLayout.LayoutParams).topMargin += CELL_SIZE
            }

            Direction.LEFT -> {
                (view.layoutParams as FrameLayout.LayoutParams).leftMargin -= CELL_SIZE
            }

            Direction.RIGHT -> {
                (view.layoutParams as FrameLayout.LayoutParams).leftMargin += CELL_SIZE
            }
        }

        return Coordinate(layoutParams.topMargin, layoutParams.leftMargin)
    }

    private fun Element.checkTankCanMoveThroughMaterial(coordinate: Coordinate, elementsOnContainer: List<Element>): Boolean {
        for (anyCoordinate in getTankCoordinates(coordinate)) {
            val element = getElementByCoordinates(anyCoordinate, elementsOnContainer)
            if (element != null && !element.material.tankCanGoThrough) {
                if (this == element) {
                    continue
                }
                return false
            }
        }
        return true
    }

    private fun getTankCoordinates(topLeftCoordinate: Coordinate): List<Coordinate> {
        val coordinateList = mutableListOf<Coordinate>()
        coordinateList.add(topLeftCoordinate)
        coordinateList.add(Coordinate(topLeftCoordinate.top + CELL_SIZE, topLeftCoordinate.left))
        coordinateList.add(Coordinate(topLeftCoordinate.top, topLeftCoordinate.left + CELL_SIZE))
        coordinateList.add(
            Coordinate(
                topLeftCoordinate.top + CELL_SIZE,
                topLeftCoordinate.left + CELL_SIZE
            )
        )
        return coordinateList
    }
}