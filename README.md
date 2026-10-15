# OcrLib

## Overview
`OcrLib` is an Android library providing Optical Character Recognition (OCR) capabilities powered by **Google ML Kit Text Recognition**. It is designed for efficient text extraction from bitmaps (such as book or collectible covers) and includes advanced text filtering and strict requirements traceability.

## Key Features
- **Asynchronous OCR (`Req 2.3.1.2`):** Built with Kotlin Coroutines (`extractText(Bitmap)`) using Google ML Kit.
- **Smart Word Filtering (`Req 2.3.2.2`):** Automatically filters out short words ($\le$ 3 characters) and pure numeric tokens via `WordFilter`.
- **Traceability Integration:** Fully annotated with `@SatisfiesRequirement` matching specifications in [REQUIREMENTS.md](REQUIREMENTS.md).
- **Testable Architecture:** Modular interfaces (`ITextRecognizer`, `IMlKitWrapper`) enabling robust unit and Robolectric testing.

## Installation
1. Publish locally to Maven:
   ```bash
   ./gradlew publishToMavenLocal
   ```
2. Add the dependency in your `build.gradle.kts`:
   ```kotlin
   implementation("com.parabd.lib:ocr:1.0.0")
   ```

## Usage Example
```kotlin
val bitmap: Bitmap = ... // Your image bitmap
val words: List<String> = TextRecognitionHelper.extractText(bitmap)
```

## Requirements Traceability
For a detailed mapping of functional requirements implemented in this library, please refer to [REQUIREMENTS.md](REQUIREMENTS.md).
