import org.gradle.testing.jacoco.plugins.JacocoTaskExtension

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.sonarqube)
    alias(libs.plugins.ktlint)
    id("maven-publish")
    id("jacoco")
}

dependencyLocking {
    lockAllConfigurations()
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
        debug {
            enableUnitTestCoverage = true
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

sonar {
    properties {
        val buildDir = project.layout.buildDirectory.get().asFile
        property("sonar.junit.reportPaths", "$buildDir/test-results/testDebugUnitTest")
        property("sonar.coverage.jacoco.xmlReportPaths", "$buildDir/reports/jacoco/testDebugUnitTestReport/testDebugUnitTestReport.xml")
    }
}

// Configuration Ktlint pour le menu Gradle
tasks.register("ktlintCheckStyle") {
    group = "verification"
    description = "Lance la vérification du style de code avec Ktlint."
    dependsOn("ktlintCheck")
}

tasks.register("ktlintFormatStyle") {
    group = "verification"
    description = "Corrige automatiquement le style de code avec Ktlint."
    dependsOn("ktlintFormat")
}

// Groupement des tâches ktlint existantes
tasks.matching { it.name.startsWith("ktlint") }.configureEach {
    group = "verification"
}

// Configuration JaCoCo pour les tests unitaires (JDK internals exclusion)
tasks.withType<Test>().configureEach {
    extensions.findByType<JacocoTaskExtension>()?.apply {
        excludes = listOf("jdk.internal.*", "sun.*", "com.sun.*", "org.jcp.*")
    }
}

tasks.register<JacocoReport>("testDebugUnitTestReport") {
    dependsOn("testDebugUnitTest")
    group = "Reporting"
    description = "Generate Jacoco coverage reports for the debug build."

    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    val fileFilter = listOf(
        "**/R.class", "**/R$*.class", "**/BuildConfig.*", "**/Manifest*.*",
        "**/*Test*.*", "android/**/*.*"
    )

    val buildDir = layout.buildDirectory.asFile.get()

    // Configuration simplifiée pour AGP 8.8
    val classTree = fileTree("$buildDir/tmp/kotlin-classes/debug") {
        exclude(fileFilter)
    }

    sourceDirectories.setFrom(files("$projectDir/src/main/java"))
    classDirectories.setFrom(files(classTree))

    executionData.setFrom(
        fileTree(buildDir) {
            include("outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec")
        }
    )
}

jacoco {
    toolVersion = libs.versions.jacoco.get().toString()
}

// Configuration spécifique Ktlint
configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
    android.set(true)
    ignoreFailures.set(false)
    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
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
    testImplementation(libs.mockk)
    testImplementation(libs.mockk.android)
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
