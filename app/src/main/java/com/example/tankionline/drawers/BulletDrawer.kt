package com.example.tankionline.drawers

import android.app.Activity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.example.tankionline.CELL_SIZE
import com.example.tankionline.R
import com.example.tankionline.enums.Direction
import com.example.tankionline.models.Coordinate
import com.example.tankionline.utils.checkViewCanMoveThroughBorder

private const val BULLET_WIDTH = 15
private const val BULLET_HEIGHT = 15

class BulletDrawer(val container: FrameLayout) {
    fun makeBulletMove(myTank: View, currentDirection: Direction) {
        Thread {
            val bullet = createBullet(myTank, currentDirection)
            while (bullet.checkViewCanMoveThroughBorder(Coordinate(bullet.top, bullet.left))) {
                when (currentDirection) {
                    Direction.UP -> (bullet.layoutParams as FrameLayout.LayoutParams).topMargin -= BULLET_HEIGHT
                    Direction.DOWN -> (bullet.layoutParams as FrameLayout.LayoutParams).topMargin += BULLET_HEIGHT
                    Direction.LEFT -> (bullet.layoutParams as FrameLayout.LayoutParams).leftMargin -= BULLET_HEIGHT
                    Direction.RIGHT -> (bullet.layoutParams as FrameLayout.LayoutParams).leftMargin += BULLET_HEIGHT
                }
                Thread.sleep(30)
                (container.context as Activity).runOnUiThread {
                    container.removeView(bullet)
                    container.addView(bullet)
                }
                (container.context as Activity).runOnUiThread {
                    container.removeView(bullet)
                }
            }
        }.start()
    }

    private fun createBullet(myTank: View, currentDirection: Direction): ImageView {
        return ImageView(container.context)
            .apply {
                this.setImageResource(R.drawable.bullet)
                this.layoutParams = FrameLayout.LayoutParams(BULLET_WIDTH, BULLET_HEIGHT)
                val bulletCoordinate = getBulletCoordinates(this, myTank, currentDirection)
                (this.layoutParams as FrameLayout.LayoutParams).topMargin = bulletCoordinate.top
                (this.layoutParams as FrameLayout.LayoutParams).leftMargin = bulletCoordinate.left
                this.rotation = currentDirection.rotation
            }
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