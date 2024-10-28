# Dog_detectection
detect dog in image
<img width="1220" alt="화면 캡처 2024-10-28 190352" src="https://github.com/user-attachments/assets/45d42881-8224-47cf-a954-25fb5040d3f6">
<img width="1238" alt="화면 캡처 2024-10-28 190429" src="https://github.com/user-attachments/assets/1ffe41a4-9563-4990-ad2d-45a37b791d03">
<img width="1234" alt="화면 캡처 2024-10-28 191159" src="https://github.com/user-attachments/assets/57592602-b538-47ea-aecf-8383a2ef1c9b">


# DogDetector 구현 가이드

## 개요
DogDetector는 TensorFlow Lite를 사용하여 이미지에서 강아지를 감지하는 Android 클래스입니다. COCO 데이터셋을 기반으로 학습된 모델을 사용하여 이미지 내의 강아지를 식별하고 위치를 감지합니다.

## 주요 기능
- 이미지에서 강아지 감지
- 감지된 강아지의 위치 정보 (바운딩 박스) 제공
- 감지 신뢰도 점수 제공

## 기술 스택
- TensorFlow Lite Interpreter
- Android Bitmap 처리
- TensorFlow Lite Support Library

## 구현 상세

### 초기화
```kotlin
class DogDetector(private val context: Context)
```
- Context를 통해 모델 파일과 테스트 이미지에 접근
- 생성자에서 자동으로 TFLite 인터프리터 초기화

### 주요 상수
- IMAGE_SIZE_X, IMAGE_SIZE_Y: 300x300 (모델 입력 크기)
- MAX_DETECTIONS: 최대 감지 개수 (10개)
- MODEL_FILE: "detect.tflite" (모델 파일명)
- TEST_IMAGE_FILE: "dogx.jpg" (테스트 이미지 파일명)

### 핵심 메소드

1. **initializeInterpreter()**
   - TFLite 모델 파일 로드
   - 인터프리터 옵션 설정 (4개 스레드 사용)
   - 테스트 이미지 로드

2. **loadImage(bitmap: Bitmap)**
   - 입력 이미지를 모델에 맞게 전처리
   - 300x300 크기로 리사이즈
   - TensorImage 형식으로 변환

3. **detectDogsTest()**
   - 테스트 이미지에서 강아지 감지 수행
   - 결과로 DogDetectionResult 리스트 반환
   - 신뢰도 0.5 이상인 결과만 필터링

### 출력 형식
```kotlin
data class DogDetectionResult(
    val confidence: Float,    // 감지 신뢰도 (0~1)
    val boundingBox: RectF,  // 감지된 영역
    val label: String        // "강아지"
)
```

## 사용 방법

1. 인스턴스 생성
```kotlin
val dogDetector = DogDetector(context)
```

2. 감지 실행
```kotlin
dogDetector.detectDogsTest { results ->
    results.forEach { result ->
        // 감지 결과 처리
        println("신뢰도: ${result.confidence}")
        println("위치: ${result.boundingBox}")
    }
}
```

3. 리소스 해제
```kotlin
dogDetector.close()
```

## 주의사항
1. assets 폴더에 다음 파일들이 필요합니다:
   - detect.tflite (모델 파일)
   - dogx.jpg (테스트 이미지)

2. 필요한 의존성:
```gradle
dependencies {
    implementation 'org.tensorflow:tensorflow-lite:2.5.0'
    implementation 'org.tensorflow:tensorflow-lite-support:0.3.0'
}
```

3. 메모리 관리를 위해 사용 완료 후 반드시 close() 호출 필요

## 에러 처리
- 모든 예외 상황은 로그로 기록됨
- 초기화 실패 시 자동 재시도
- 감지 실패 시 빈 리스트 반환

# MainActivity 및 TestScreen 구현 가이드

## 개요
이 코드는 Jetpack Compose를 사용하여 강아지 감지 기능을 테스트하는 안드로이드 UI를 구현한 것입니다. 메인 화면에서는 테스트 이미지를 표시하고, 감지 기능을 실행할 수 있으며, 감지 결과를 실시간으로 확인할 수 있습니다.

## 주요 컴포넌트

### MainActivity
```kotlin
class MainActivity : ComponentActivity()
```
- 앱의 진입점
- Compose UI를 설정하고 TestScreen을 호출

### TestScreen Composable
```kotlin
@Composable
fun TestScreen()
```
- 메인 UI를 구성하는 Composable 함수
- ViewModel을 통한 상태 관리
- 이미지 표시, 테스트 실행, 결과 표시 기능 포함

## 화면 구성

### 1. 이미지 영역
- assets 폴더의 "dogx.jpg" 이미지를 표시
- `Image` Composable을 사용하여 화면 상단에 배치
- weight(1f)를 통해 화면의 1/3 차지

### 2. 테스트 버튼
- "강아지 감지 테스트 실행" 버튼
- 클릭 시 `viewModel.runDetection()` 호출
- 전체 너비를 차지하도록 설정

### 3. 결과 표시 영역
- LazyColumn을 사용한 스크롤 가능한 결과 목록
- 반투명 검은 배경 (alpha = 0.5f)
- 각 감지 결과마다:
  - 신뢰도 (백분율)
  - 바운딩 박스 좌표
  - 구분선

## 상태 관리
```kotlin
val detectedDogs by viewModel.detectedDogs.collectAsState()
```
- ViewModel을 통해 감지 결과 상태 관리
- Flow를 사용한 반응형 상태 업데이트

## 스타일링
- padding: 16.dp (전체 화면)
- 중앙 정렬된 컨텐츠
- 결과 텍스트: 흰색
- 구분선: 반투명 흰색 (alpha = 0.3f)

## 사용된 Compose 컴포넌트
- Column: 세로 방향 레이아웃
- Image: 비트맵 이미지 표시
- Button: 사용자 액션 처리
- LazyColumn: 스크롤 가능한 리스트
- Text: 텍스트 표시
- Divider: 구분선

## 필요한 의존성
```gradle
dependencies {
    implementation "androidx.compose.ui:ui:$compose_version"
    implementation "androidx.compose.material3:material3:$material3_version"
    implementation "androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycle_version"
}
```

## 주의사항
1. assets 폴더에 "dogx.jpg" 파일이 반드시 필요
2. TestViewModel과 TestViewModelFactory 구현 필요
3. MLKitTestTheme 정의 필요

## UI 흐름
1. 앱 실행 → TestScreen 표시
2. 테스트 이미지 로드 및 표시
3. 버튼 클릭 → 감지 실행
4. 결과를 실시간으로 목록에 표시

## 에러 처리
- 이미지 로드 실패 시 안전하게 처리 (runCatching 사용)
- null 이미지 케이스 처리

- # TestViewModel 구현 가이드

## 개요
이 코드는 강아지 감지 기능의 비즈니스 로직을 처리하는 ViewModel과 그의 Factory 클래스를 구현합니다. MVVM 아키텍처 패턴을 따르며, Kotlin Flow를 사용하여 상태를 관리합니다.

## 구성 요소

### TestViewModel
```kotlin
class TestViewModel(context: Context) : ViewModel()
```

#### 주요 속성
1. **detector**
   - DogDetector 인스턴스
   - Context를 통해 초기화

2. **상태 관리**
   ```kotlin
   private val _detectedDogs = MutableStateFlow<List<DogDetectionResult>>(emptyList())
   val detectedDogs = _detectedDogs.asStateFlow()
   ```
   - MutableStateFlow: 내부 상태 관리
   - StateFlow: UI에 노출되는 읽기 전용 상태

#### 주요 메서드
```kotlin
fun runDetection()
```
- 강아지 감지 프로세스 실행
- 결과를 Flow를 통해 UI에 전달
- 로그를 통한 실행 과정 추적

### TestViewModelFactory
```kotlin
class TestViewModelFactory(private val context: Context) : ViewModelProvider.Factory
```
- ViewModel 생성을 담당하는 Factory 클래스
- Context를 ViewModel에 전달

#### create 메서드
```kotlin
override fun <T : ViewModel> create(modelClass: Class<T>): T
```
- ViewModel 인스턴스 생성
- 타입 체크 및 캐스팅 처리
- 잘못된 ViewModel 클래스에 대한 예외 처리

## 데이터 흐름
1. UI에서 `runDetection()` 호출
2. DogDetector에서 감지 실행
3. 결과를 StateFlow를 통해 UI에 전달
4. UI가 상태 변경을 관찰하고 업데이트

## 로깅
- tag: "TestViewModel"
- 주요 로그 포인트:
  - 감지 실행 시작
  - 결과 수신 및 개수

## 사용 방법

### ViewModel 초기화
```kotlin
val viewModel: TestViewModel = viewModel(
    factory = TestViewModelFactory(context)
)
```

### 상태 관찰
```kotlin
val detectedDogs by viewModel.detectedDogs.collectAsState()
```

### 감지 실행
```kotlin
viewModel.runDetection()
```

## 필요한 의존성
```gradle
dependencies {
    implementation "androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle_version"
    implementation "org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutines_version"
}
```

## 주의사항
1. Context 메모리 누수 방지
   - ViewModel이 Context 참조를 가지고 있으므로 주의 필요
   - Application Context 사용 권장

2. StateFlow 사용
   - 초기값 필수 (emptyList())
   - UI에서는 읽기 전용으로만 접근

3. Factory 패턴
   - ViewModel 생성 시 항상 Factory 사용
   - Context 전달을 위한 필수 구현

## 에러 처리
- Factory에서 잘못된 ViewModel 클래스 요청 시 IllegalArgumentException 발생
- DogDetector 실패 시 빈 리스트 반환
- 모든 주요 동작에 대한 로깅 구현

## 아키텍처 고려사항
1. MVVM 패턴 준수
2. 단방향 데이터 흐름
3. 관심사의 분리
   - UI: MainActivity
   - 비즈니스 로직: ViewModel
   - 데이터 처리: DogDetector
