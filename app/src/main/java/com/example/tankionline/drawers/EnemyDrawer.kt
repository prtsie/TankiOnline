package com.example.tankionline.drawers

import android.widget.FrameLayout
import com.example.tankionline.CELL_SIZE
import com.example.tankionline.GameCore
import com.example.tankionline.SoundManager
import com.example.tankionline.enums.CELLS_TANK_SIZE
import com.example.tankionline.enums.Direction
import com.example.tankionline.enums.Material
import com.example.tankionline.models.Coordinate
import com.example.tankionline.models.Element
import com.example.tankionline.models.Tank
import com.example.tankionline.utils.checkIfChanceBiggerThanRandom
import com.example.tankionline.utils.drawElement

private const val MAX_ENEMY_COUNT = 20

class EnemyDrawer(
    private val container: FrameLayout,
    private val elements: MutableList<Element>,
    private val soundManager: SoundManager,
    private val gameCore: GameCore
) {
    private val respawnList: List<Coordinate>
    private var enemyCount = 0
    private var currentCoordinate: Coordinate
    val tanks = mutableListOf<Tank>()
    lateinit var bulletDrawer: BulletDrawer
    private var gameStarted = false

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
            Direction.DOWN,
            this
        )
        enemyTank.element.drawElement(container)
        tanks.add(enemyTank)
    }

    private fun moveEnemyTanks() {
        Thread({
            while (true) {
                if (!gameCore.isPlaying()) {
                    continue
                }
                goThroughAllTanks()
                Thread.sleep(400)
            }
        }).start()
    }

    private fun goThroughAllTanks() {
        if (tanks.isNotEmpty()) {
            soundManager.tankMove()
        } else {
            soundManager.tankStop()
        }
        tanks.toList().forEach {
            it.move(it.direction, container, elements)
            if(checkIfChanceBiggerThanRandom(10)) {
                bulletDrawer.addNewBulletForTank(it)
            }
        }
    }

    fun startEnemyCreation() {
        if (gameStarted) {
            return
        }
        gameStarted = true
        Thread {
            while (enemyCount < MAX_ENEMY_COUNT) {
                if (!gameCore.isPlaying()) {
                    continue
                }
                drawEnemy()
                enemyCount++
                Thread.sleep(3000)
            }
        }.start()
        moveEnemyTanks()
    }

    fun removeTank(tankIndex: Int) {
        tanks.removeAt(tankIndex)
    }
}