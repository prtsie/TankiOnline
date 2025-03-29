package com.example.tankionline.drawers

import android.widget.FrameLayout
import com.example.tankionline.CELL_SIZE
import com.example.tankionline.binding
import com.example.tankionline.enums.CELLS_TANK_SIZE
import com.example.tankionline.enums.Direction
import com.example.tankionline.enums.Material
import com.example.tankionline.models.Coordinate
import com.example.tankionline.models.Element
import com.example.tankionline.models.Tank
import com.example.tankionline.utils.drawElement

private const val MAX_ENEMY_COUNT = 20

class EnemyDrawer(private val container: FrameLayout, private val elements: MutableList<Element>) {
    private val respawnList: List<Coordinate>
    private var enemyCount = 0
    private var currentCoordinate: Coordinate
    private val tanks = mutableListOf<Tank>()

    init {
        respawnList = getRespawnList()
        currentCoordinate = respawnList[0]
    }

    private fun getRespawnList(): List<Coordinate> {
        val respawnList = mutableListOf<Coordinate>()
        respawnList.add(Coordinate(0, 0))
        respawnList.add(Coordinate(0,
            ((container.width - container.width % CELL_SIZE) / CELL_SIZE -
                    (container.width - container.width % CELL_SIZE) / CELL_SIZE % 2) *
        CELL_SIZE / 2 - CELL_SIZE * CELLS_TANK_SIZE))
        respawnList.add(Coordinate(0,
            (container.width - container.width % CELL_SIZE) - CELL_SIZE * CELLS_TANK_SIZE))
        return respawnList
    }

    private fun drawEnemy() {
        var index = respawnList.indexOf(currentCoordinate) + 1
        if (index == respawnList.size) {
            index = 0
        }
        currentCoordinate = respawnList[index]
        val enemyTank = Tank(
            Element(
                material = Material.ENEMY_TANK,
                coordinate = currentCoordinate
            ),
            Direction.DOWN
        )
        enemyTank.element.drawElement(container)
        elements.add(enemyTank.element)
        tanks.add(enemyTank)
    }

    fun moveEnemyTanks() {
        Thread {
            while (true) {
                removeInconsistentTanks()
                tanks.forEach {
                    it.move(it.direction, container, elements)
                }
                Thread.sleep(400)
            }
        }.start()
    }

    fun startEnemyCreation() {
        Thread {
            while (enemyCount < MAX_ENEMY_COUNT) {
                drawEnemy()
                enemyCount++
                Thread.sleep(3000)
            }
        }.start()
    }

    private fun removeInconsistentTanks() {
        tanks.removeAll(getInconsistentTanks())
    }

    private fun getInconsistentTanks(): List<Tank> {
        val removingTanks = mutableListOf<Tank>()
        val allTanksElements = elements.filter { it.material == Material.ENEMY_TANK }
        tanks.forEach {
            if (!allTanksElements.contains(it.element)) {
                removingTanks.add(it)
            }
        }
        return removingTanks
    }
}