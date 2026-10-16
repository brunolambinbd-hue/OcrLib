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

sonarqube {
    properties {
        property("sonar.projectKey", "brunolambinbd-hue_OcrLib")
        property("sonar.organization", "brunolambinbd-hue")
        property("sonar.host.url", "https://sonarcloud.io")

        // Utilisation du jeton si présent dans local.properties
        sonarToken?.let { property("sonar.token", it) }

        property("sonar.projectVersion", "1.0.0")
        property("sonar.branch.name", "main")
        property("sonar.language", "kotlin")
        property("sonar.kotlin.version", libs.versions.kotlin.get())

        // Configuration de l'analyse
        property("sonar.sourceEncoding", "UTF-8")
        property("sonar.scm.provider", "git")
        property("sonar.gradle.skipCompile", "false")

        // Exclusions globales
        property(
            "sonar.exclusions",
            "**/build/**, **/generated/**, **/.gradle/**, **/*Binding.java, **/*_Impl.java, **/R.java, **/BuildConfig.java",
        )

        // 1. Évite l'erreur 403 de téléchargement JRE sur Windows
        property("sonar.scanner.skipJreProvisioning", "true")

        // 2. Ignore les faux-positifs d'adresses IP (S1313) sur les IDs d'exigences (ex: "2.3.1.9")
        property("sonar.issue.ignore.multicriteria", "e1,e2")
        property("sonar.issue.ignore.multicriteria.e1.ruleKey", "kotlin:S1313")
        property("sonar.issue.ignore.multicriteria.e1.resourceKey", "**/*.kt")
        property("sonar.issue.ignore.multicriteria.e2.ruleKey", "kotlin:S6474")
        property("sonar.issue.ignore.multicriteria.e2.resourceKey", "**/*.gradle.kts")
    }
}

tasks.register("runSonarAnalysis") {
    group = "Reporting"
    description = "Exécute les tests (Couverture), le Lint et télécharge le rapport vers SonarCloud."
    dependsOn(":library:testDebugUnitTestReport")
    dependsOn(":library:lintDebug")
    finalizedBy("sonar")
    doLast {
        println("\n🚀 CYCLE SONAR : Rapports générés. Lancement de l'upload vers SonarCloud...")
    }
}

subprojects {
    dependencyLocking {
        lockAllConfigurations()
    }
    apply(plugin = "org.sonarqube")
    sonarqube {
        properties {
            val buildDir = project.layout.buildDirectory.get().asFile
            // Chaque module indexe son code et ses rapports
            property("sonar.sources", "src/main/java")
            property("sonar.tests", "src/test/java")
            property("sonar.coverage.jacoco.xmlReportPaths", "$buildDir/reports/jacoco/testDebugUnitTestReport/testDebugUnitTestReport.xml")
            property("sonar.junit.reportPaths", "$buildDir/test-results/testDebugUnitTest")
            property("sonar.androidLint.reportPaths", "$buildDir/reports/lint-results-debug.xml")
        }
    }
}

apply(from = "sync-standards.gradle.kts")
