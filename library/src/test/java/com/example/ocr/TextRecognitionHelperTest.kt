package com.example.ocr

import android.graphics.Bitmap
import com.example.imagecomparison.CoveredRequirement
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
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
class TextRecognitionHelperTest : BaseOcrTest() {
    private val originalWrapper = TextRecognitionHelper.mlKitWrapper
    private val originalFactory = TextRecognitionHelper.recognizerFactory

    override fun setUp() {
        super.setUp()
        // Mocking Bitmap since we are in Pure JVM
        mockkStatic(Bitmap::class)
        
        TextRecognitionHelper.recognizerClient = mockk<TextRecognizer>(relaxed = true)
        TextRecognitionHelper.recognizerFactory = { mockk<TextRecognizer>(relaxed = true) }
    }

    override fun tearDown() {
        TextRecognitionHelper.recognizerClient = null
        TextRecognitionHelper.recognizerFactory = originalFactory
        TextRecognitionHelper.mlKitWrapper = originalWrapper
        TextRecognitionHelper.ocrDispatcher = kotlinx.coroutines.Dispatchers.Default
        super.tearDown()
    }

    @Test
    @CoveredRequirement("2.3.1.9")
    fun testExtractText_ReturnsFilteredWords() = runTest {
        TextRecognitionHelper.ocrDispatcher = kotlinx.coroutines.Dispatchers.Unconfined
        val bitmap = mockk<Bitmap>()
        val mockText = mockk<Text>()
        every { mockText.text } returns "Spirou and Fantasio"
        
        val mockWrapper = mockk<IMlKitWrapper>()
        every { mockWrapper.processImage(any(), any()) } returns Tasks.forResult(mockText)
        TextRecognitionHelper.mlKitWrapper = mockWrapper

        val result = TextRecognitionHelper.extractText(bitmap)
        assertTrue(result.contains("spirou"))
        assertTrue(result.contains("fantasio"))
        assertTrue(!result.contains("and"))
    }

    @Test
    @CoveredRequirement("2.3.1.9")
    fun testLazyInitializationOfDefaultRecognizer() = runTest {
        val mockDefaultRecognizer = mockk<TextRecognizer>(relaxed = true)
        TextRecognitionHelper.recognizerFactory = { mockDefaultRecognizer }
        TextRecognitionHelper.recognizerClient = null
        TextRecognitionHelper.ocrDispatcher = kotlinx.coroutines.Dispatchers.Unconfined
        val bitmap = mockk<Bitmap>()
        
        val mockWrapper = mockk<IMlKitWrapper>()
        every { mockWrapper.processImage(any(), any()) } returns Tasks.forResult(null)
        TextRecognitionHelper.mlKitWrapper = mockWrapper

        TextRecognitionHelper.extractText(bitmap)
        verify { mockWrapper.processImage(mockDefaultRecognizer, bitmap) }
    }

    @Test
    @CoveredRequirement("2.1.2")
    fun testDefaultMlKitWrapper_Coverage() {
        val bitmap = mockk<Bitmap>()
        val mockRecognizer = mockk<TextRecognizer>(relaxed = true)
        mockkStatic(InputImage::class)
        val mockInputImage = mockk<InputImage>()
        every { InputImage.fromBitmap(bitmap, 0) } returns mockInputImage
        
        TextRecognitionHelper.mlKitWrapper.processImage(mockRecognizer, bitmap)
        
        verify { mockRecognizer.process(mockInputImage) }
    }

    @Test
    @CoveredRequirement("2.3.1.9")
    fun testExtractText_EmptyOrBlankResults() = runTest {
        TextRecognitionHelper.ocrDispatcher = kotlinx.coroutines.Dispatchers.Unconfined
        val bitmap = mockk<Bitmap>()
        val mockWrapper = mockk<IMlKitWrapper>()
        TextRecognitionHelper.mlKitWrapper = mockWrapper

        // Case result is null (Task returns null)
        every { mockWrapper.processImage(any(), any()) } returns Tasks.forResult(null)
        assertTrue(TextRecognitionHelper.extractText(bitmap).isEmpty())

        // Case text is null or empty
        val mockText = mockk<Text>()
        every { mockText.text } returns ""
        every { mockWrapper.processImage(any(), any()) } returns Tasks.forResult(mockText)
        assertTrue(TextRecognitionHelper.extractText(bitmap).isEmpty())

        // Case text is blank
        every { mockText.text } returns "   "
        assertTrue(TextRecognitionHelper.extractText(bitmap).isEmpty())
    }

    @Test
    @CoveredRequirement("2.3.1.9")
    fun testExtractText_HandlesException() = runTest {
        TextRecognitionHelper.ocrDispatcher = kotlinx.coroutines.Dispatchers.Unconfined
        val bitmap = mockk<Bitmap>()
        val mockWrapper = mockk<IMlKitWrapper>()
        every { mockWrapper.processImage(any(), any()) } throws RuntimeException("OCR Error")
        TextRecognitionHelper.mlKitWrapper = mockWrapper

        // Test with Log.isLoggable = true
        every { android.util.Log.isLoggable(any(), android.util.Log.ERROR) } returns true
        assertTrue(TextRecognitionHelper.extractText(bitmap).isEmpty())

        // Test with Log.isLoggable = false
        every { android.util.Log.isLoggable(any(), android.util.Log.ERROR) } returns false
        assertTrue(TextRecognitionHelper.extractText(bitmap).isEmpty())
    }

    @Test
    @CoveredRequirement("2.3.1.9")
    fun testExtractText_HandlesThrowable() = runTest {
        TextRecognitionHelper.ocrDispatcher = kotlinx.coroutines.Dispatchers.Unconfined
        val bitmap = mockk<Bitmap>()
        val mockWrapper = mockk<IMlKitWrapper>()
        every { mockWrapper.processImage(any(), any()) } throws OutOfMemoryError("Fake OOM")
        TextRecognitionHelper.mlKitWrapper = mockWrapper

        val result = TextRecognitionHelper.extractText(bitmap)
        assertTrue(result.isEmpty())
    }

    @Test
    @CoveredRequirement("2.3.1.9")
    fun testInternalProperties_DebugAccess() {
        val mockRecognizer = mockk<TextRecognizer>()
        TextRecognitionHelper.recognizerClient = mockRecognizer
        assertEquals(mockRecognizer, TextRecognitionHelper.recognizerClient)
        
        TextRecognitionHelper.ocrDispatcher = kotlinx.coroutines.Dispatchers.IO
        assertEquals(kotlinx.coroutines.Dispatchers.IO, TextRecognitionHelper.ocrDispatcher)
    }

    @Test
    @CoveredRequirement("2.3.1.9")
    fun testGetDefaultClient_And_Factory() {
        mockkStatic(TextRecognition::class)
        val mockRecognizer = mockk<TextRecognizer>()
        every { TextRecognition.getClient(any()) } returns mockRecognizer
        
        // Directly test getDefaultClient
        val method = TextRecognitionHelper::class.java.getDeclaredMethod("getDefaultClient")
        method.isAccessible = true
        assertEquals(mockRecognizer, method.invoke(TextRecognitionHelper))
        
        // Test default recognizerFactory
        assertEquals(mockRecognizer, originalFactory.invoke())
    }
}
