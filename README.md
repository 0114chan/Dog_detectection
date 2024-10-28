# Dog_detectection
detect dog in image
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
