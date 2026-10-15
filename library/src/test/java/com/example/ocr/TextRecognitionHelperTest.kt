package com.parabd.lib.ocr

import android.graphics.Bitmap
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.parabd.lib.traceability.CoveredRequirement
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [TextRecognitionHelper] using MockK and BaseOcrTest.
 * This is a Pure JVM test to ensure compatibility with JaCoCo coverage.
 */
@ExperimentalCoroutinesApi
public class TextRecognitionHelperTest : BaseOcrTest() {
    private val originalWrapper: IMlKitWrapper = TextRecognitionHelper.mlKitWrapper
    private val originalFactory: () -> TextRecognizer = TextRecognitionHelper.recognizerFactory

    /**
     * Sets up test mocks and state.
     */
    public override fun setUp() {
        super.setUp()
        // Mocking Bitmap since we are in Pure JVM
        mockkStatic(Bitmap::class)

        TextRecognitionHelper.recognizerClient = mockk<TextRecognizer>(relaxed = true)
        TextRecognitionHelper.recognizerFactory = { mockk<TextRecognizer>(relaxed = true) }
    }

    /**
     * Tears down test state and restores original properties.
     */
    public override fun tearDown() {
        TextRecognitionHelper.recognizerClient = null
        TextRecognitionHelper.recognizerFactory = originalFactory
        TextRecognitionHelper.mlKitWrapper = originalWrapper
        TextRecognitionHelper.ocrDispatcher = kotlinx.coroutines.Dispatchers.Default
        super.tearDown()
    }

    /**
     * Tests that extractText returns filtered words correctly.
     */
    @Test
    @CoveredRequirement("2.3.1.2")
    public fun testExtractText_ReturnsFilteredWords(): Unit =
        runTest {
            TextRecognitionHelper.ocrDispatcher = kotlinx.coroutines.Dispatchers.Unconfined
            val bitmap: Bitmap = mockk<Bitmap>()
            val mockText: Text = mockk<Text>()
            every { mockText.text } returns "Spirou and Fantasio"

            val mockWrapper: IMlKitWrapper = mockk<IMlKitWrapper>()
            every { mockWrapper.processImage(any(), any()) } returns Tasks.forResult(mockText)
            TextRecognitionHelper.mlKitWrapper = mockWrapper

            val result: List<String> = TextRecognitionHelper.extractText(bitmap)
            assertTrue(result.contains("spirou"))
            assertTrue(result.contains("fantasio"))
            assertTrue(!result.contains("and"))
        }

    /**
     * Tests lazy initialization of the default recognizer.
     */
    @Test
    @CoveredRequirement("2.3.1.2")
    public fun testLazyInitializationOfDefaultRecognizer(): Unit =
        runTest {
            val mockDefaultRecognizer: TextRecognizer = mockk<TextRecognizer>(relaxed = true)
            TextRecognitionHelper.recognizerFactory = { mockDefaultRecognizer }
            TextRecognitionHelper.recognizerClient = null
            TextRecognitionHelper.ocrDispatcher = kotlinx.coroutines.Dispatchers.Unconfined
            val bitmap: Bitmap = mockk<Bitmap>()

            val mockWrapper: IMlKitWrapper = mockk<IMlKitWrapper>()
            every { mockWrapper.processImage(any(), any()) } returns Tasks.forResult(null)
            TextRecognitionHelper.mlKitWrapper = mockWrapper

            TextRecognitionHelper.extractText(bitmap)
            verify { mockWrapper.processImage(mockDefaultRecognizer, bitmap) }
        }

    /**
     * Tests default ML Kit wrapper coverage.
     */
    @Test
    @CoveredRequirement("2.3.1.2")
    public fun testDefaultMlKitWrapper_Coverage() {
        val bitmap: Bitmap = mockk<Bitmap>()
        val mockRecognizer: TextRecognizer = mockk<TextRecognizer>(relaxed = true)
        mockkStatic(InputImage::class)
        val mockInputImage: InputImage = mockk<InputImage>()
        every { InputImage.fromBitmap(bitmap, 0) } returns mockInputImage

        TextRecognitionHelper.mlKitWrapper.processImage(mockRecognizer, bitmap)

        verify { mockRecognizer.process(mockInputImage) }
    }

    /**
     * Tests extractText with empty or blank results.
     */
    @Test
    @CoveredRequirement("2.3.1.2")
    public fun testExtractText_EmptyOrBlankResults(): Unit =
        runTest {
            TextRecognitionHelper.ocrDispatcher = kotlinx.coroutines.Dispatchers.Unconfined
            val bitmap: Bitmap = mockk<Bitmap>()
            val mockWrapper: IMlKitWrapper = mockk<IMlKitWrapper>()
            TextRecognitionHelper.mlKitWrapper = mockWrapper

            // Case result is null (Task returns null)
            every { mockWrapper.processImage(any(), any()) } returns Tasks.forResult(null)
            assertTrue(TextRecognitionHelper.extractText(bitmap).isEmpty())

            // Case text is null or empty
            val mockText: Text = mockk<Text>()
            every { mockText.text } returns ""
            every { mockWrapper.processImage(any(), any()) } returns Tasks.forResult(mockText)
            assertTrue(TextRecognitionHelper.extractText(bitmap).isEmpty())

            // Case text is blank
            every { mockText.text } returns "   "
            assertTrue(TextRecognitionHelper.extractText(bitmap).isEmpty())
        }

    /**
     * Tests extractText exception handling.
     */
    @Test
    @CoveredRequirement("2.3.1.2")
    public fun testExtractText_HandlesException(): Unit =
        runTest {
            TextRecognitionHelper.ocrDispatcher = kotlinx.coroutines.Dispatchers.Unconfined
            val bitmap: Bitmap = mockk<Bitmap>()
            val mockWrapper: IMlKitWrapper = mockk<IMlKitWrapper>()
            every { mockWrapper.processImage(any(), any()) } throws RuntimeException("OCR Error")
            TextRecognitionHelper.mlKitWrapper = mockWrapper

            // Test with Log.isLoggable = true
            every { android.util.Log.isLoggable(any(), android.util.Log.ERROR) } returns true
            assertTrue(TextRecognitionHelper.extractText(bitmap).isEmpty())

            // Test with Log.isLoggable = false
            every { android.util.Log.isLoggable(any(), android.util.Log.ERROR) } returns false
            assertTrue(TextRecognitionHelper.extractText(bitmap).isEmpty())
        }

    /**
     * Tests extractText throwable handling.
     */
    @Test
    @CoveredRequirement("2.3.1.2")
    public fun testExtractText_HandlesThrowable(): Unit =
        runTest {
            TextRecognitionHelper.ocrDispatcher = kotlinx.coroutines.Dispatchers.Unconfined
            val bitmap: Bitmap = mockk<Bitmap>()
            val mockWrapper: IMlKitWrapper = mockk<IMlKitWrapper>()
            every { mockWrapper.processImage(any(), any()) } throws OutOfMemoryError("Fake OOM")
            TextRecognitionHelper.mlKitWrapper = mockWrapper

            val result: List<String> = TextRecognitionHelper.extractText(bitmap)
            assertTrue(result.isEmpty())
        }

    /**
     * Tests internal properties debug access.
     */
    @Test
    @CoveredRequirement("2.3.1.2")
    public fun testInternalProperties_DebugAccess() {
        val mockRecognizer: TextRecognizer = mockk<TextRecognizer>()
        TextRecognitionHelper.recognizerClient = mockRecognizer
        assertEquals(mockRecognizer, TextRecognitionHelper.recognizerClient)

        TextRecognitionHelper.ocrDispatcher = kotlinx.coroutines.Dispatchers.IO
        assertEquals(kotlinx.coroutines.Dispatchers.IO, TextRecognitionHelper.ocrDispatcher)
    }

    /**
     * Tests default client creation and factory.
     */
    @Test
    @CoveredRequirement("2.3.1.2")
    public fun testGetDefaultClient_And_Factory() {
        mockkStatic(TextRecognition::class)
        val mockRecognizer: TextRecognizer = mockk<TextRecognizer>()
        every { TextRecognition.getClient(any()) } returns mockRecognizer

        // Directly test getDefaultClient
        val method = TextRecognitionHelper::class.java.getDeclaredMethod("getDefaultClient")
        method.isAccessible = true
        assertEquals(mockRecognizer, method.invoke(TextRecognitionHelper))

        // Test default recognizerFactory
        assertEquals(mockRecognizer, originalFactory.invoke())
    }
}
