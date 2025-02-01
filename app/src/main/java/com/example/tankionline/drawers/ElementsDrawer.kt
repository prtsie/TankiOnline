package com.example.tankionline.drawers

import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.example.tankionline.CELL_SIZE
import com.example.tankionline.R
import com.example.tankionline.binding
import com.example.tankionline.enums.Direction
import com.example.tankionline.enums.Material
import com.example.tankionline.models.Coordinate
import com.example.tankionline.models.Element

class ElementsDrawer(val container: FrameLayout) {
    var currentMaterial = Material.EMPTY
    private val elementsOnContainer = mutableListOf<Element>()

    fun onTouchContainer(x: Float, y: Float) {
        val topMargin = y.toInt() - (y.toInt() % CELL_SIZE)
        val leftMargin = x.toInt() - (x.toInt() % CELL_SIZE)
        val coordinate = Coordinate(topMargin, leftMargin)
        if (currentMaterial == Material.EMPTY) {
            eraseView(coordinate)
        } else {
            drawOrReplaceView(coordinate)
        }
    }

    private fun drawOrReplaceView(coordinate: Coordinate) {
        val viewOnCoordinate = getElementByCoordinates(coordinate)
        if (viewOnCoordinate == null) {
            drawView(coordinate)
            return
        }
        if (viewOnCoordinate.material != currentMaterial) {
            replaceView(coordinate)
        }
    }

    private fun eraseView(coordinate: Coordinate) {
        val elemetOnCoordinate = getElementByCoordinates(coordinate)
        if (elemetOnCoordinate != null) {
            val erasingView = container.findViewById<View>(elemetOnCoordinate.viewId)
            container.removeView(erasingView)
            elementsOnContainer.remove(elemetOnCoordinate)
        }
    }

    private fun replaceView(coordinate: Coordinate) {
        eraseView(coordinate)
        drawView(coordinate)
    }

    fun drawView(coordinate: Coordinate) {
        val view = ImageView(container.context)
        val layoutParams = FrameLayout.LayoutParams(CELL_SIZE, CELL_SIZE)
        when (currentMaterial) {
            Material.EMPTY -> {}
            Material.BRICK -> view.setImageResource(R.drawable.brick)
            Material.CONCRETE -> view.setImageResource(R.drawable.concrete)
            Material.GRASS -> view.setImageResource(R.drawable.grass)
        }
        layoutParams.topMargin = coordinate.top
        layoutParams.leftMargin = coordinate.left
        val viewId = View.generateViewId()
        view.id = viewId
        view.layoutParams = layoutParams
        container.addView(view)
        elementsOnContainer.add(Element(viewId, currentMaterial, coordinate))
    }

    fun move(myTank: View, direction: Direction) {
        val layoutParams = myTank.layoutParams as FrameLayout.LayoutParams
        val currentCoordiante = Coordinate(layoutParams.topMargin, layoutParams.leftMargin)
        when (direction) {
            Direction.UP -> {
                myTank.rotation = 0f
                (myTank.layoutParams as FrameLayout.LayoutParams).topMargin -= CELL_SIZE
            }
            Direction.DOWN -> {
                myTank.rotation = 180f
                (myTank.layoutParams as FrameLayout.LayoutParams).topMargin += CELL_SIZE
            }
            Direction.LEFT -> {
                myTank.rotation = 270f
                (myTank.layoutParams as FrameLayout.LayoutParams).leftMargin -= CELL_SIZE
            }
            Direction.RIGHT -> {
                myTank.rotation = 90f
                (myTank.layoutParams as FrameLayout.LayoutParams).leftMargin += CELL_SIZE
            }
        }

        val nextCoordinate = Coordinate(layoutParams.topMargin, layoutParams.leftMargin)
        if (checkTankCanMoveThroughBorder(
            nextCoordinate,
            myTank
        ) && checkTankCanMoveThroughMaterial(nextCoordinate)) {
            binding.container.removeView(myTank)
            binding.container.addView(myTank)
        } else {
            (myTank.layoutParams as FrameLayout.LayoutParams).topMargin = currentCoordiante.top
            (myTank.layoutParams as FrameLayout.LayoutParams).leftMargin = currentCoordiante.left
        }
    }

    private fun getElementByCoordinates(coordinate: Coordinate) =
        elementsOnContainer.firstOrNull { it.coordinate == coordinate }

    private fun checkTankCanMoveThroughMaterial(coordinate: Coordinate): Boolean {
        getTankCoordinates(coordinate).forEach {
            val element =  getElementByCoordinates(it)
            if (element != null && !element.material.tankCanGoThrough) {
                return false
            }
        }
        return true
    }

    private fun checkTankCanMoveThroughBorder(coordinate: Coordinate, myTank: View) =
        (coordinate.top >= 0 &&
            coordinate.top + myTank.height <= binding.container.height &&
            coordinate.left >= 0 &&
            coordinate.left + myTank.width <= binding.container.width)

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