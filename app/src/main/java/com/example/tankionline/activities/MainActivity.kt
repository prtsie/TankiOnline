package com.example.tankionline.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.KeyEvent.KEYCODE_DPAD_DOWN
import android.view.KeyEvent.KEYCODE_DPAD_LEFT
import android.view.KeyEvent.KEYCODE_DPAD_RIGHT
import android.view.KeyEvent.KEYCODE_DPAD_UP
import android.view.KeyEvent.KEYCODE_SPACE
import android.view.Menu
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.tankionline.GameCore
import com.example.tankionline.LevelStorage
import com.example.tankionline.R
import com.example.tankionline.databinding.ActivityMainBinding
import com.example.tankionline.drawers.BulletDrawer
import com.example.tankionline.drawers.ElementsDrawer
import com.example.tankionline.drawers.EnemyDrawer
import com.example.tankionline.drawers.GridDrawer
import com.example.tankionline.enums.Direction
import com.example.tankionline.enums.Direction.DOWN
import com.example.tankionline.enums.Direction.LEFT
import com.example.tankionline.enums.Direction.RIGHT
import com.example.tankionline.enums.Direction.UP
import com.example.tankionline.enums.Material
import com.example.tankionline.models.Coordinate
import com.example.tankionline.models.Element
import com.example.tankionline.models.Tank
import com.example.tankionline.sounds.MainSoundPlayer
import com.example.tankionline.utils.ProgressIndicator

const val CELL_SIZE = 50

lateinit var binding: ActivityMainBinding

class MainActivity : AppCompatActivity(), ProgressIndicator {
    private var editMode = false
    private lateinit var item: MenuItem

    private lateinit var playerTank: Tank
    private lateinit var eagle: Element

    private var gameStarted = false

    private val bulletDrawer by lazy {
        BulletDrawer(
            binding.container,
            elementsDrawer.elementsOnContainer,
            enemyDrawer,
            soundManager,
            gameCore
        )
    }

    private val gameCore by lazy {
        GameCore(this)
    }

    private val soundManager by lazy {
        MainSoundPlayer(this, this)
    }

    private fun createTank(elementWidth: Int, elementHeight: Int): Tank {
        playerTank = Tank(
            Element(
                material = Material.PLAYER_TANK,
                coordinate = getPlayerTankCoordinate(elementWidth, elementHeight)
            ), UP,
            enemyDrawer
        )
        return playerTank
    }

    private fun createEagle(elementWidth: Int, elementHeight: Int): Element {
        eagle = Element(
            material = Material.EAGLE,
            coordinate = getEagleCoordinate(elementWidth, elementHeight)
        )
        return eagle
    }

    private fun getPlayerTankCoordinate(width: Int, height: Int) = Coordinate(
        top = (height - height % 2)
        - (height - height % 2) % CELL_SIZE
        - Material.PLAYER_TANK.height * CELL_SIZE,
        left = (width - width % (2 * CELL_SIZE)) / 2
        - Material.EAGLE.width / 2 * CELL_SIZE
        - Material.PLAYER_TANK.width * CELL_SIZE
    )

    private fun getEagleCoordinate(width: Int, height: Int) = Coordinate(
        top = (height - height % 2)
                - (height - height % 2) % CELL_SIZE
                - Material.EAGLE.height * CELL_SIZE,
        left = (width - width % (2 * CELL_SIZE)) / 2
                - Material.EAGLE.width / 2 * CELL_SIZE
    )

    private val gridDrawer by lazy {
        GridDrawer(binding.container)
    }

    private val elementsDrawer by lazy {
        ElementsDrawer(binding.container)
    }

    private val levelStorage by lazy {
        LevelStorage(this)
    }

    private val enemyDrawer by lazy {
        EnemyDrawer(binding.container, elementsDrawer.elementsOnContainer, soundManager, gameCore)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Menu"

        binding.editorClear.setOnClickListener { elementsDrawer.currentMaterial =  Material.EMPTY }
        binding.editorBrick.setOnClickListener { elementsDrawer.currentMaterial =  Material.BRICK }
        binding.editorGrass.setOnClickListener { elementsDrawer.currentMaterial =  Material.GRASS }
        binding.editorConcrete.setOnClickListener { elementsDrawer.currentMaterial =  Material.CONCRETE }
        binding.container.setOnTouchListener { _, event ->
            if (!editMode) {
                return@setOnTouchListener true
            }
            elementsDrawer.onTouchContainer(event.x, event.y)
            return@setOnTouchListener true
        }
        elementsDrawer.drawElementsList(levelStorage.loadLevel())
        hideSettings()
        countWidthHeight()
    }

    private fun countWidthHeight() {
        val frameLayout = binding.container
        frameLayout.viewTreeObserver
            .addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    frameLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    val elementWidth = frameLayout.width
                    val elementHeight =  frameLayout.height

                    playerTank = createTank(elementWidth, elementHeight)
                    eagle = createEagle(elementWidth, elementHeight)

                    elementsDrawer.drawElementsList(listOf(playerTank.element, eagle))
                    enemyDrawer.bulletDrawer = bulletDrawer
                }
            })
    }

    private fun switchEditMode() {
        editMode = !editMode
        if (editMode) {
            showSettings()
        }
        else {
            hideSettings()
        }
    }

    private fun showSettings() {
        gridDrawer.drawGrid()
        binding.materialsContainer.visibility = VISIBLE
    }

    private fun hideSettings() {
        gridDrawer.removeGrid()
        binding.materialsContainer.visibility = INVISIBLE
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.settings, menu)
        item = menu!!.findItem(R.id.menu_play)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_settings -> {
                switchEditMode()
                return true
            }
            R.id.menu_save -> {
                levelStorage.saveLevel(elementsDrawer.elementsOnContainer)
                return true
            }
            R.id.menu_play -> {
                if (editMode) {
                    return true
                }
                showIntro()
                if (soundManager.areSoundsReady()) {
                    gameCore.startOrPauseTheGame()
                    if (gameCore.isPlaying()) {
                        resumeTheGame()
                    } else {
                        pauseTheGame()
                    }
                }

                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun resumeTheGame() {
        item.icon = ContextCompat.getDrawable(this, R.drawable.baseline_play_arrow_24)
    }

    private fun showIntro() {
        if (gameStarted) {
            return
        }
        gameStarted = true
        soundManager.loadSounds()
    }

    private fun pauseTheGame() {
        item.icon = ContextCompat.getDrawable(this, R.drawable.baseline_play_arrow_24)
        gameCore.pauseTheGame()
        soundManager.pauseSounds()
    }

    override fun onPause() {
        super.onPause()
        pauseTheGame()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (!gameCore.isPlaying()) {
            return super.onKeyDown(keyCode, event)
        }
        when (keyCode) {
            KEYCODE_DPAD_UP -> onButtonPressed(UP)
            KEYCODE_DPAD_DOWN -> onButtonPressed(DOWN)
            KEYCODE_DPAD_LEFT -> onButtonPressed(LEFT)
            KEYCODE_DPAD_RIGHT -> onButtonPressed(RIGHT)
            KEYCODE_SPACE -> bulletDrawer.addNewBulletForTank(playerTank)
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (!gameCore.isPlaying()) {
            return super.onKeyUp(keyCode, event)
        }
        when (keyCode) {
            KEYCODE_DPAD_UP, KEYCODE_DPAD_LEFT,
            KEYCODE_DPAD_DOWN, KEYCODE_DPAD_RIGHT -> onButtonReleased()
        }
        return super.onKeyUp(keyCode, event)
    }

    private fun onButtonPressed(direction: Direction) {
        soundManager.tankMove()
        playerTank.move(direction, binding.container, elementsDrawer.elementsOnContainer)
    }

    private fun onButtonReleased() {
        if (enemyDrawer.tanks.isEmpty()) {
            soundManager.tankStop()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == SCORE_REQUEST_CODE) {
            recreate()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun showProgress() {
        binding.container.visibility = INVISIBLE
        binding.totalContainer.setBackgroundResource(R.color.gray)
        binding.initTitle.visibility = VISIBLE
    }

    override fun dismissProgress() {
        Thread.sleep(3000L)
        binding.container.visibility = VISIBLE
        binding.totalContainer.setBackgroundResource(R.color.black)
        binding.initTitle.visibility = GONE
        enemyDrawer.startEnemyCreation()
        soundManager.playIntroMusic()
        resumeTheGame()
    }
}