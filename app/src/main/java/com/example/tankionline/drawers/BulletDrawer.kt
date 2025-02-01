package com.example.tankionline.drawers

import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.example.tankionline.CELL_SIZE
import com.example.tankionline.R
import com.example.tankionline.enums.Direction
import com.example.tankionline.models.Coordinate

class BulletDrawer(val container: FrameLayout) {
    fun drawBullet(myTank: View, currentDirection: Direction) {
        val bullet = ImageView(container.context)
            .apply {
                this.setImageResource(R.drawable.bullet)
                this.layoutParams = FrameLayout.LayoutParams(BULLET_WIDTH, BULLET_HEIGHT)
                val bulletCoordinate = getBulletCoordinates(this, myTank, currentDirection)
                (this.layoutParams as FrameLayout.LayoutParams).topMargin = bulletCoordinate.top
                (this.layoutParams as FrameLayout.LayoutParams).leftMargin = bulletCoordinate.left
                this.rotation = currentDirection.rotation
            }
        container.addView(bullet)
    }

    private fun getBulletCoordinates(bullet: ImageView, myTank: View, currentDirection: Direction): Coordinate {
        val tankLeftTopCoordinate = Coordinate(myTank.top, myTank.left)
        when (currentDirection) {
            Direction.UP -> return Coordinate(
            tankLeftTopCoordinate.top - bullet.layoutParams.height,
            getDistanceToMiddleOfTank(tankLeftTopCoordinate.left, bullet.layoutParams.width))

            Direction.DOWN -> return Coordinate(
                tankLeftTopCoordinate.top + myTank.layoutParams.height,
                getDistanceToMiddleOfTank(tankLeftTopCoordinate.left, bullet.layoutParams.width))

            Direction.LEFT -> return Coordinate(
                getDistanceToMiddleOfTank(tankLeftTopCoordinate.top, bullet.layoutParams.height),
                tankLeftTopCoordinate.left - bullet.layoutParams.width)

            Direction.RIGHT -> return Coordinate(
                getDistanceToMiddleOfTank(tankLeftTopCoordinate.top, bullet.layoutParams.height),
                tankLeftTopCoordinate.left + myTank.layoutParams.width)
        }
    }

    private fun getDistanceToMiddleOfTank(startCoordinate: Int, bulletSize: Int): Int {
        return startCoordinate + (CELL_SIZE - bulletSize / 2)
    }
}