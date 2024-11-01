package com.example.dog.detector

import android.content.Context
import android.graphics.*
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
        private const val TEST_IMAGE_FILE = "mm.jpg"
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

    fun detectDogsTest(onDetection: (List<DogDetectionResult>, Bitmap) -> Unit) {
        if (interpreter == null) {
            Log.e(tag, "interpreter가 null입니다. 재초기화 시도...")
            initializeInterpreter()
            if (interpreter == null) {
                Log.e(tag, "interpreter 재초기화 실패")
                onDetection(emptyList(), testImage ?: return)
                return
            }
        }

        testImage?.let { bitmap ->
            // 원본 이미지를 수정 가능한 비트맵으로 변환
            val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
            val canvas = Canvas(mutableBitmap)

            // 바운딩 박스를 그리기 위한 Paint 객체 설정
            val boxPaint = Paint().apply {
                color = Color.RED
                style = Paint.Style.STROKE
                strokeWidth = 8f
                isAntiAlias = true
            }

            // 텍스트를 위한 Paint 객체
            val textPaint = Paint().apply {
                color = Color.RED
                textSize = 48f
                isFakeBoldText = true
                isAntiAlias = true
            }

            // 텍스트 배경을 위한 Paint 객체
            val textBackgroundPaint = Paint().apply {
                color = Color.WHITE
                alpha = 180
                style = Paint.Style.FILL
            }

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

                    if (label in listOf(15, 16, 17) && score > 0.4f) {
                        // 15(새), 16(고양이), 17(강아지)
                        val location = outputLocations[0][i]
                        val boundingBox = RectF(
                            location[1] * bitmap.width,
                            location[0] * bitmap.height,
                            location[3] * bitmap.width,
                            location[2] * bitmap.height
                        )

                        // 바운딩 박스 그리기
                        canvas.drawRect(boundingBox, boxPaint)

                        // 신뢰도 텍스트 준비
                        val text = String.format("강아지: %.1f%%", score * 100)
                        val textBounds = Rect()
                        textPaint.getTextBounds(text, 0, text.length, textBounds)

                        // 텍스트 배경 그리기
                        val textBackground = RectF(
                            boundingBox.left,
                            boundingBox.top - textBounds.height() - 20f,
                            boundingBox.left + textBounds.width() + 20f,
                            boundingBox.top
                        )
                        canvas.drawRect(textBackground, textBackgroundPaint)

                        // 텍스트 그리기
                        canvas.drawText(
                            text,
                            boundingBox.left + 10f,
                            boundingBox.top - 10f,
                            textPaint
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

                onDetection(detections, mutableBitmap)
            } catch (e: Exception) {
                Log.e(tag, "감지 중 오류 발생", e)
                e.printStackTrace()
                onDetection(emptyList(), bitmap)
            }
        } ?: run {
            Log.e(tag, "테스트 이미지가 null입니다")
            onDetection(emptyList(), Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888))
        }
    }

    fun close() {
        interpreter?.close()
    }
}