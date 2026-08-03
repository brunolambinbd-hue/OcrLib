plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("maven-publish")
}

android {
    namespace = "com.example.ocr"
    compileSdk = libs.versions.sdkCompile.get().toString().toInt()

    defaultConfig {
        minSdk = libs.versions.sdkMin.get().toString().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.javaVersion.get().toString())
        targetCompatibility = JavaVersion.toVersion(libs.versions.javaVersion.get().toString())
    }
    kotlinOptions {
        jvmTarget = libs.versions.javaVersion.get().toString()
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
    publishing {
        singleVariant("release")
    }
}

dependencies {
    api(libs.play.services.mlkit.text.recognition)
    api(libs.kotlinx.coroutines.play.services)
    api(libs.traceability)
    
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.truth)
    testImplementation(libs.androidx.arch.core.testing)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.test.core)
}

    afterEvaluate {
        publishing {
            publications {
                create<MavenPublication>("release") {
                    from(components["release"])
                    groupId = "com.example.lib"
                    artifactId = "ocr"
                    version = "1.0.0"
                }
            }
        }
    }
