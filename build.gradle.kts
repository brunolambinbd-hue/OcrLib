plugins {
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.ktlint) apply false
}

dependencyLocking {
    lockAllConfigurations()
}

configurations.all {
    resolutionStrategy.activateDependencyLocking()
}

// Dummy configuration to force a root-level gradle.lockfile
configurations.create("dummyConfig")
dependencies {
    "dummyConfig"("org.jetbrains.kotlin:kotlin-stdlib:2.0.21")
}

subprojects {
    dependencyLocking {
        lockAllConfigurations()
    }
}

apply(from = "sync-standards.gradle.kts")
