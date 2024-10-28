plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.dog"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.dog"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    sourceSets {
        getByName("main") {
            assets.srcDirs(file("src/main/assets"))
        }
    }

    aaptOptions {
        noCompress += listOf("tflite", "lite")  // lite 확장자도 추가
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            // TFLite 관련 중복 파일 제외
            pickFirsts += "META-INF/INDEX.LIST"
            pickFirsts += "META-INF/DEPENDENCIES"
        }
    }
}

dependencies {
    // 기존 의존성
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // ML Kit 관련
    implementation("com.google.mlkit:object-detection:17.0.2")
    implementation("com.google.mlkit:common:18.10.0")

    // CameraX
    val cameraxVersion = "1.3.0"  // CameraX 버전 업데이트
    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")

    // TensorFlow Lite
    val tensorflowVersion = "0.4.4"  // TFLite 버전 업데이트
    implementation("org.tensorflow:tensorflow-lite-task-vision:$tensorflowVersion")
    implementation("org.tensorflow:tensorflow-lite-support:$tensorflowVersion")
    implementation("org.tensorflow:tensorflow-lite-metadata:$tensorflowVersion")  // 메타데이터 지원 추가
    implementation ("org.tensorflow:tensorflow-lite:2.9.0")
    implementation ("org.tensorflow:tensorflow-lite-support:0.4.4")
    // Compose
    implementation("androidx.compose.ui:ui:1.5.0")
    implementation("androidx.compose.material:material:1.5.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")

    // 테스트 관련
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}