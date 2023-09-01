package com.mata.weather.live.myapplication

import androidx.annotation.Keep

@Keep
data class ItemModel(
    var id: String = "",
    var content: String = "",
    var isSelected: Boolean = false
)
