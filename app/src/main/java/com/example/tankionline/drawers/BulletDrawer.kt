package com.example.tankionline.drawers

import android.app.Activity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.example.tankionline.CELL_SIZE
import com.example.tankionline.GameCore.isPlaying
import com.example.tankionline.R
import com.example.tankionline.enums.Direction
import com.example.tankionline.enums.Material
import com.example.tankionline.models.Bullet
import com.example.tankionline.models.Coordinate
import com.example.tankionline.models.Element
import com.example.tankionline.models.Tank
import com.example.tankionline.utils.checkViewCanMoveThroughBorder
import com.example.tankionline.utils.getElementByCoordinates
import com.example.tankionline.utils.getTankByCoordinates
import com.example.tankionline.utils.getViewCoordinate
import com.example.tankionline.utils.runOnUiThread

private const val BULLET_WIDTH = 15
private const val BULLET_HEIGHT = 15

class BulletDrawer(
    private val container: FrameLayout,
    private val elements: MutableList<Element>,
    private val enemyDrawer: EnemyDrawer
) {

    init {
        moveAllBullets()
    }

    private val allBullets = mutableListOf<Bullet>()

    fun addNewBulletForTank(tank: Tank) {
        val view = container.findViewById<View>(tank.element.viewId) ?: return
        if (tank.alreadyHasBullet()) return
        allBullets.add(Bullet(createBullet(view, tank.direction), tank.direction, tank))
    }

    private fun Tank.alreadyHasBullet(): Boolean =
        allBullets.firstOrNull { it.tank == this } != null

    private fun moveAllBullets() {
        Thread({
            while (true) {
                if (!isPlaying()) {
                    continue
                }
                interactWithAllBullets()
                Thread.sleep(30)
            }
        }).start()
    }

    private fun interactWithAllBullets() {
        allBullets.toList().forEach { bullet ->
            val view = bullet.view
            if (bullet.canBulletGoFurther()) {
                val params = view.layoutParams as FrameLayout.LayoutParams
                when (bullet.direction) {
                    Direction.UP -> params.topMargin -= BULLET_HEIGHT
                    Direction.DOWN -> params.topMargin += BULLET_HEIGHT
                    Direction.LEFT -> params.leftMargin -= BULLET_WIDTH
                    Direction.RIGHT -> params.leftMargin += BULLET_WIDTH
                }
                chooseBehaviorInTermsOfDirections(bullet)
                container.runOnUiThread {
                    container.removeView(view)
                    container.addView(view)
                }
            }
            else {
                stopBullet(bullet)
            }
            bullet.stopIntersectingBullets()
        }
        removeInconsistentBullets()
    }

    private fun removeInconsistentBullets() {
        val removingList = allBullets.filter { !it.canMoveFurther }
        removingList.forEach {
            stopBullet(it)
            container.runOnUiThread {
                container.removeView(it.view)
            }
        }
        allBullets.removeAll(removingList)
    }

    private fun Bullet.stopIntersectingBullets() {
        val bulletCoordinate = this.view.getViewCoordinate()
        for (bulletInList in allBullets) {
            val coordinateList = bulletInList.view.getViewCoordinate()
            if (this == bulletInList) {
                continue
            }
            if (coordinateList == bulletCoordinate) {
                stopBullet(this)
                stopBullet(bulletInList)
                return
            }
        }
    }

    private fun Bullet.canBulletGoFurther() =
        this.view.checkViewCanMoveThroughBorder(this.view.getViewCoordinate()) && this.canMoveFurther

    private fun chooseBehaviorInTermsOfDirections(bullet: Bullet){
        when (bullet.direction) {
            Direction.UP, Direction.DOWN -> {
                compareCollections(getCoordinatesForTopOrBottomDirection(bullet), bullet)
            }
            Direction.LEFT, Direction.RIGHT -> {
                compareCollections(getCoordinatesForLeftOrRightDirection(bullet), bullet)
            }
        }
    }

    private fun compareCollections(detectedCoordinatesList: List<Coordinate>, bullet: Bullet){
        for (coordinate in detectedCoordinatesList) {
            var element = getElementByCoordinates(coordinate, elements)
            if (element == null) {
                element = getTankByCoordinates(coordinate, enemyDrawer.tanks)
            }
            if (element == bullet.tank.element) {
                continue
            }
            removeElementsAndStopBullet(element, bullet)
        }
    }

    private fun removeElementsAndStopBullet(element: Element?, bullet: Bullet){
        if (element != null) {
            if (element.material.tankCanGoThrough) {
                return
            }
            if (bullet.tank.element.material == Material.ENEMY_TANK && element.material == Material.ENEMY_TANK) {
                stopBullet(bullet)
                return
            }
            if (element.material.simpleBulletCanDestroy) {
                stopBullet(bullet)
                removeView(element)
                elements.remove(element)
                removeTank(element)
            } else {
                stopBullet(bullet)
            }
        }
    }

    private fun removeTank(element: Element) {
        val tanksElements = enemyDrawer.tanks.map { it.element }
        val tankIndex = tanksElements.indexOf(element)
        enemyDrawer.removeTank(tankIndex)
    }

    private fun stopBullet(bullet: Bullet) {
        bullet.canMoveFurther = false
    }

    private fun removeView(element: Element?) {
        val activity = container.context as Activity
        activity.runOnUiThread {
            container.removeView(activity.findViewById(element!!.viewId))
        }
    }

    private fun getCoordinatesForTopOrBottomDirection(bullet: Bullet): List<Coordinate> {
        val bulletCoordinate = bullet.view.getViewCoordinate()
        val leftCell = bulletCoordinate.left - bulletCoordinate.left % CELL_SIZE
        val rightCell = leftCell + CELL_SIZE
        val topCoordinate = bulletCoordinate.top - bulletCoordinate.top % CELL_SIZE
        return listOf(
            Coordinate(topCoordinate, leftCell),
            Coordinate(topCoordinate, rightCell)
        )
    }

    private fun getCoordinatesForLeftOrRightDirection(bullet: Bullet): List<Coordinate> {
        val bulletCoordinate = bullet.view.getViewCoordinate()
        val topCell = bulletCoordinate.top - bulletCoordinate.top % CELL_SIZE
        val bottomCell = topCell + CELL_SIZE
        val leftCoordinate = bulletCoordinate.left - bulletCoordinate.left % CELL_SIZE
        return listOf(
            Coordinate(topCell, leftCoordinate),
            Coordinate(bottomCell, leftCoordinate)
        )
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