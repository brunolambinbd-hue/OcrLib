@file:Suppress("RedundantVisibilityModifier")

package com.parabd.lib.ocr

import android.graphics.Bitmap
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.parabd.lib.traceability.SatisfiesRequirement
import com.parabd.lib.traceability.utils.LogManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Interface defining the contract for text recognition services.
 */
public interface ITextRecognizer {
    /**
     * Extracts text from a bitmap.
     *
     * @param bitmap The image to process.
     * @return A list of extracted words.
     */
    public suspend fun extractText(bitmap: Bitmap): List<String>
}

/**
 * Interface for ML Kit interactions to allow mocking in tests.
 */
public interface IMlKitWrapper {
    /**
     * Processes a bitmap using the provided TextRecognizer.
     *
     * @param recognizer The ML Kit recognizer.
     * @param bitmap The image to analyze.
     * @return A Task containing the extracted text.
     */
    public fun processImage(
        recognizer: TextRecognizer,
        bitmap: Bitmap,
    ): Task<Text>
}

/**
 * Utility to recognize text from a Bitmap using Google ML Kit.
 */
@SatisfiesRequirement("2.3.1.9")
public object TextRecognitionHelper : ITextRecognizer {
    private const val TAG = "TextRecognitionHelper"

    /** The actual ML Kit recognizer instance. Can be swapped in tests. */
    internal var recognizerClient: TextRecognizer? = null

    /** Wrapper for ML Kit calls. Can be swapped in tests. */
    internal var mlKitWrapper: IMlKitWrapper =
        object : IMlKitWrapper {
            @SatisfiesRequirement("2.3.1.9")
            override fun processImage(
                recognizer: TextRecognizer,
                bitmap: Bitmap,
            ): Task<Text> {
                val image = InputImage.fromBitmap(bitmap, 0)
                return recognizer.process(image)
            }
        }

    /** Factory for the default recognizer. */
    internal var recognizerFactory: () -> TextRecognizer = { getDefaultClient() }

    /**
     * Logic to create the default ML Kit client.
     */
    @SatisfiesRequirement("2.3.1.9")
    private fun getDefaultClient(): TextRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    /** The active recognizer instance. */
    private val recognizer: TextRecognizer
        get() = recognizerClient ?: recognizerFactory()

    /** Dispatcher used for OCR operations. */
    internal var ocrDispatcher: kotlinx.coroutines.CoroutineDispatcher = Dispatchers.Default

    /**
     * Extracts text from a bitmap using the default OCR dispatcher.
     *
     * @param bitmap The image to process.
     * @return A list of extracted words.
     */
    @SatisfiesRequirement("2.3.1.9")
    public override suspend fun extractText(bitmap: Bitmap): List<String> = extractText(bitmap, ocrDispatcher)

    /**
     * Internal implementation of text extraction with custom dispatcher support.
     *
     * @param bitmap The image to analyze.
     * @param dispatcher The coroutine dispatcher to use.
     * @return A list of filtered words.
     */
    @SatisfiesRequirement("2.3.1.9")
    private suspend fun extractText(
        bitmap: Bitmap,
        dispatcher: kotlinx.coroutines.CoroutineDispatcher,
    ): List<String> =
        withContext(dispatcher) {
            try {
                val result = mlKitWrapper.processImage(recognizer, bitmap).await()
                if (result.text.isBlank()) return@withContext emptyList()

                filterWords(result.text)
            } catch (e: Exception) {
                LogManager.e(TAG, "Error during OCR process", e)
                emptyList()
            } catch (
                @Suppress("TooGenericExceptionCaught") _: Throwable,
            ) {
                emptyList()
            }
        }

    @SatisfiesRequirement("2.3.2.9")
    public fun filterWords(rawText: String?): List<String> = WordFilter.filterWords(rawText)
}
