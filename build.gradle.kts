plugins {
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.sonarqube)
    alias(libs.plugins.ktlint) apply false
}

// Récupération sécurisée du jeton SonarCloud depuis local.properties
val localProperties = java.util.Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { localProperties.load(it) }
}
val sonarToken: String? = localProperties.getProperty("sonar.token")

// Utilisation du bloc 'sonar' (nouveau standard 5.x)
sonar {
    properties {
        property("sonar.projectKey", "brunolambinbd-hue_OcrLib")
        property("sonar.organization", "brunolambinbd-hue")
        property("sonar.host.url", "https://sonarcloud.io")
        sonarToken?.let { property("sonar.token", it) }
        property("sonar.sourceEncoding", "UTF-8")
        property("sonar.scm.provider", "git")
        property("sonar.gradle.skipCompile", "false")
        property("sonar.exclusions", "**/build/**,**/generated/**,**/.gradle/**")
        property("sonar.tests", "src/test/java,src/androidTest/java")
    }
}

apply(from = "sync-standards.gradle.kts")
