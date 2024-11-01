package com.example.dog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dog.ui.theme.MLKitTestTheme
import android.graphics.BitmapFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MLKitTestTheme {
                TestScreen()
            }
        }
    }
}
@Composable
fun TestScreen() {
    val context = LocalContext.current
    val viewModel: TestViewModel = viewModel(
        factory = TestViewModelFactory(context)
    )

    val detectedDogs by viewModel.detectedDogs.collectAsState()
    val processedImage by viewModel.processedImage.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 처리된 이미지 표시
        processedImage?.let { bitmap ->
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Processed Image",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
        }

        // 테스트 버튼
        Button(
            onClick = { viewModel.runDetection() },
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text("동물 감지 테스트 실행")
        }

        // 감지 결과 표시
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.5f))
        ) {
            items(detectedDogs) { detection ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Text(
                        text = "${detection.label} 감지! 신뢰도: ${(detection.confidence * 100).toInt()}%",
                        color = Color.White
                    )
                    Text(
                        text = "위치: ${detection.boundingBox}",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                    Divider(color = Color.White.copy(alpha = 0.3f))
                }
            }
        }
    }
}