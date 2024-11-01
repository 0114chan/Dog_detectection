package com.example.dog

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.dog.detector.DogDetector
import com.example.dog.model.DogDetectionResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class TestViewModel(context: Context) : ViewModel() {
    private val detector = DogDetector(context)
    private val tag = "TestViewModel"

    private val _detectedDogs = MutableStateFlow<List<DogDetectionResult>>(emptyList())
    val detectedDogs = _detectedDogs.asStateFlow()

    private val _processedImage = MutableStateFlow<Bitmap?>(null)
    val processedImage = _processedImage.asStateFlow()

    fun runDetection() {
        Log.d(tag, "감지 실행 시작")
        detector.detectDogsTest { dogs, processedBitmap ->
            Log.d(tag, "감지 결과 수신: ${dogs.size}개")
            _detectedDogs.value = dogs
            _processedImage.value = processedBitmap
        }
    }

    override fun onCleared() {
        super.onCleared()
        detector.close()
    }
}

class TestViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TestViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TestViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}