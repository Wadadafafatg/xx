plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    // حيلة إضافية: إذا ردت مستقبلاً تضيف Baseline Profiles لتسريع تشغيل التطبيق تفعل هذا الـ Plugin
    // id("androidx.baselineprofile") 
}

android {
    namespace = "com.example.freshstart"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.freshstart"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            // 1. تفعيل ضغط وتصغير الكود (حذف الكود الزايد والمكتبات الما مستخدمة)
            isMinifyEnabled = true
            
            // 2. تفعيل حذف الصور والملفات والموارد الزايدة غير المستخدمة بالتطبيق
            isShrinkResources = true
            
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        
        debug {
            // خليه خفيف للـ Debug حتى تبني بسرعة من الهاتف وتجرب
            applicationIdSuffix = ".debug"
            isDebuggable = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
        // حيلة لكوتلن: تفعيل التحسينات الإضافية أثناء البناء
        freeCompilerArgs += listOf(
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-Xjvm-default=all"
        )
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    val bom = platform("androidx.compose:compose-bom:2024.06.00")
    implementation(bom)
    androidTestImplementation(bom)

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.activity:activity-compose:1.9.1")
    
    // واجهات Compose الأساسية
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    // مكاتب إضافية ننصح بيها للأداء السلس (اختيارية حسب حاجتك):
    // implementation("io.coil-kt:coil-compose:2.6.0") // لتحميل الصور بكفاءة عالية وبدون تعليق
    // implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4") // لإدارة الـ State والـ ViewModel بشكل محترف

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
