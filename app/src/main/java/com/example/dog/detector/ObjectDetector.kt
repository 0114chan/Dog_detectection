package com.example.dog.detector

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.RectF
import android.util.Log
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import com.example.dog.model.DogDetectionResult
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder

class DogDetector(private val context: Context) {
    private var interpreter: Interpreter? = null
    private var testImage: Bitmap? = null
    private val tag = "DogDetector"

    companion object {
        private const val IMAGE_SIZE_X = 300
        private const val IMAGE_SIZE_Y = 300
        private const val MAX_DETECTIONS = 10
        private const val MODEL_FILE = "detect.tflite"
        private const val TEST_IMAGE_FILE = "dogx.jpg"
    }

    init {
        Log.d(tag, "DogDetector 초기화 시작")
        initializeInterpreter()
    }

    private fun initializeInterpreter() {
        try {
            val modelFile = FileUtil.loadMappedFile(context, MODEL_FILE)
            val options = Interpreter.Options().apply {
                numThreads = 4
            }
            interpreter = Interpreter(modelFile, options)
            Log.d(tag, "TFLite 인터프리터 초기화 성공")

            loadTestImage()
        } catch (e: Exception) {
            Log.e(tag, "초기화 실패", e)
            interpreter = null
        }
    }

    private fun loadTestImage() {
        try {
            context.assets.open(TEST_IMAGE_FILE).use { inputStream ->
                testImage = BitmapFactory.decodeStream(inputStream)?.also { bitmap ->
                    Log.d(tag, "테스트 이미지 로드 성공: ${bitmap.width}x${bitmap.height}")
                } ?: throw IOException("이미지 디코딩 실패")
            }
        } catch (e: Exception) {
            Log.e(tag, "테스트 이미지 로드 실패", e)
        }
    }

    private fun loadImage(bitmap: Bitmap): TensorImage {
        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(IMAGE_SIZE_Y, IMAGE_SIZE_X, ResizeOp.ResizeMethod.BILINEAR))
            .build()

        val tensorImage = TensorImage.fromBitmap(bitmap)
        return imageProcessor.process(tensorImage)
    }

    fun detectDogsTest(onDetection: (List<DogDetectionResult>) -> Unit) {
        if (interpreter == null) {
            Log.e(tag, "interpreter가 null입니다. 재초기화 시도...")
            initializeInterpreter()
            if (interpreter == null) {
                Log.e(tag, "interpreter 재초기화 실패")
                onDetection(emptyList())
                return
            }
        }

        testImage?.let { bitmap ->
            try {
                val tensorImage = loadImage(bitmap)

                val outputLocations = Array(1) { Array(MAX_DETECTIONS) { FloatArray(4) } }
                val outputClasses = Array(1) { FloatArray(MAX_DETECTIONS) }
                val outputScores = Array(1) { FloatArray(MAX_DETECTIONS) }
                val numDetections = FloatArray(1)

                val inputArray = arrayOf(tensorImage.buffer)
                val outputMap = mapOf(
                    0 to outputLocations,
                    1 to outputClasses,
                    2 to outputScores,
                    3 to numDetections
                )

                interpreter?.runForMultipleInputsOutputs(inputArray, outputMap)

                val detections = mutableListOf<DogDetectionResult>()
                val detectionCount = numDetections[0].toInt()

                for (i in 0 until detectionCount) {
                    val score = outputScores[0][i]
                    val label = outputClasses[0][i].toInt()

                    // COCO 데이터셋에서 개는 클래스 ID 17
                    if (label == 17 && score > 0.5f) {
                        val location = outputLocations[0][i]
                        val boundingBox = RectF(
                            location[1] * bitmap.width,
                            location[0] * bitmap.height,
                            location[3] * bitmap.width,
                            location[2] * bitmap.height
                        )

                        detections.add(
                            DogDetectionResult(
                                confidence = score,
                                boundingBox = boundingBox,
                                label = "강아지"
                            )
                        )
                        Log.d(tag, "강아지 감지: 신뢰도 ${score * 100}%")
                    }
                }

                onDetection(detections)
            } catch (e: Exception) {
                Log.e(tag, "감지 중 오류 발생", e)
                e.printStackTrace()
                onDetection(emptyList())
            }
        } ?: run {
            Log.e(tag, "테스트 이미지가 null입니다")
            onDetection(emptyList())
        }
    }

    fun close() {
        interpreter?.close()
    }
}