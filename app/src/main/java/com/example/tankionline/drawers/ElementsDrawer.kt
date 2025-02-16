package com.example.tankionline.drawers

import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.example.tankionline.CELL_SIZE
import com.example.tankionline.R
import com.example.tankionline.enums.Material
import com.example.tankionline.models.Coordinate
import com.example.tankionline.models.Element
import com.example.tankionline.utils.getElementByCoordinates

class ElementsDrawer(val container: FrameLayout) {
    var currentMaterial = Material.EMPTY
    val elementsOnContainer = mutableListOf<Element>()

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
        val viewOnCoordinate = getElementByCoordinates(coordinate, elementsOnContainer)
        if (viewOnCoordinate == null) {
            selectMaterial(coordinate)
            return
        }
        if (viewOnCoordinate.material != currentMaterial) {
            replaceView(coordinate)
        }
    }

    private fun eraseView(coordinate: Coordinate) {
        val elemetOnCoordinate = getElementByCoordinates(coordinate, elementsOnContainer)
        if (elemetOnCoordinate != null) {
            val erasingView = container.findViewById<View>(elemetOnCoordinate.viewId)
            container.removeView(erasingView)
            elementsOnContainer.remove(elemetOnCoordinate)
        }
    }

    private fun replaceView(coordinate: Coordinate) {
        eraseView(coordinate)
        selectMaterial(coordinate)
    }

    fun selectMaterial(coordinate: Coordinate) {
        when (currentMaterial) {
            Material.EMPTY -> {}
            Material.BRICK -> drawView(R.drawable.brick, coordinate)
            Material.CONCRETE -> drawView(R.drawable.concrete, coordinate)
            Material.GRASS -> drawView(R.drawable.grass, coordinate)
            Material.EAGLE -> {
                removeExistingEagle()
                drawView(R.drawable.eagle, coordinate, 4, 3)
            }
        }
    }

    fun removeExistingEagle() {
        elementsOnContainer.firstOrNull {  it.material == Material.EAGLE }?.coordinate?.let {
            eraseView(it)
        }
    }

    private fun drawView(
        @DrawableRes image: Int,
        coordinate: Coordinate,
        width: Int = 1,
        height: Int = 1
    ) {
        val view = ImageView(container.context)
        val layoutParams = FrameLayout.LayoutParams(width * CELL_SIZE, height * CELL_SIZE)
        view.setImageResource(image)
        layoutParams.topMargin = coordinate.top
        layoutParams.leftMargin = coordinate.left
        val viewId = View.generateViewId()
        view.id = viewId
        view.layoutParams = layoutParams
        container.addView(view)
        elementsOnContainer.add(Element(viewId, currentMaterial, coordinate, width, height))
    }
}