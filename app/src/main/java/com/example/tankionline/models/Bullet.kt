package com.example.tankionline.models

import android.view.View
import com.example.tankionline.enums.Direction

data class Bullet(
    val view: View,
    val direction: Direction,
    val tank: Tank,
    var canMoveFurther: Boolean = true)