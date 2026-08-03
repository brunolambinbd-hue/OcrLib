# OcrLib

## Description
Bibliothèque d'extraction de texte (OCR) basée sur Google ML Kit, optimisée pour la reconnaissance de titres et d'éditeurs sur des couvertures d'objets de collection.

## Fonctionnalités
- `TextRecognitionHelper` : Service asynchrone pour extraire des mots clés depuis un Bitmap.
- Support multi-langue via ML Kit.
- Intégration native de la traçabilité.

## Installation
1. Publier localement : `./gradlew publishToMavenLocal`
2. Ajoutez la dépendance :
```kotlin
implementation("com.example.lib:ocr:1.0.0")
```

## Dépendances
- `TraceabilityLib`
- `Google Play Services ML Kit Text Recognition`
