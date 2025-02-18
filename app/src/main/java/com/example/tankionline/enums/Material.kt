package com.example.tankionline.enums

import com.example.tankionline.R

const val CELLS_SIMPLE_ELEMENT = 1
const val CELLS_EAGLE_WIDTH = 4
const val CELLS_EAGLE_HEIGHT = 3
const val CELLS_TANK_SIZE = 2

enum class Material(
    val tankCanGoThrough: Boolean,
    val bulletCanGoThrough: Boolean,
    val simpleBulletCanDestroy: Boolean,
    val elementsAmountOnScreen: Int,
    val width: Int,
    val height: Int,
    val image: Int,
    val visibleInEditMode: Boolean) {
    EMPTY(true, true, true, 0, 0, 0, 0, false),
    BRICK(false, false, true, 0, CELLS_SIMPLE_ELEMENT, CELLS_SIMPLE_ELEMENT, R.drawable.brick, false),
    CONCRETE(false, false, false, 0, CELLS_SIMPLE_ELEMENT, CELLS_SIMPLE_ELEMENT, R.drawable.concrete, false),
    GRASS(true, true, false, 0, CELLS_SIMPLE_ELEMENT, CELLS_SIMPLE_ELEMENT, R.drawable.grass, false),
    EAGLE(false, false, true, 1, CELLS_EAGLE_WIDTH, CELLS_EAGLE_HEIGHT, R.drawable.eagle, false),
    ENEMY_TANK_RESPAWN(true, true, false, 3, CELLS_TANK_SIZE, CELLS_TANK_SIZE, R.drawable.enemy_tank, true),
    PLAYER_TANK_RESPAWN(true, true, false, 1, CELLS_TANK_SIZE, CELLS_TANK_SIZE, R.drawable.tank, true)
}