package com.example.dog.model

import android.graphics.RectF

data class DogDetectionResult(
    val confidence: Float,
    val boundingBox: RectF,
    val label: String
)