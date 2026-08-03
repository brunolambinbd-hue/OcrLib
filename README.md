# OcrLib

A standalone Android library for text recognition using Google ML Kit.

## Setup

Add the library to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.example.lib:ocr:1.0.0")
}
```

Ensure you have `mavenLocal()` in your repositories if you are using a local build.

## Usage

```kotlin
val recognizer: ITextRecognizer = TextRecognitionHelper
val words = recognizer.extractText(bitmap)
```

## Features
- ML Kit Text Recognition integration.
- Coroutines support.
- Built-in word filtering (removes small words and pure numeric strings).
- Requirement traceability via annotations.
