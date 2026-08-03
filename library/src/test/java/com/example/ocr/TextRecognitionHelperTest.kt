package com.example.ocr

import android.graphics.Bitmap
import com.example.imagecomparison.CoveredRequirement
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognizer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.*
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.check
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests for [TextRecognitionHelper].
 */
@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [32])
class TextRecognitionHelperTest {
    private val originalWrapper = TextRecognitionHelper.mlKitWrapper
    private val originalFactory = TextRecognitionHelper.recognizerFactory

    @Before
    fun setup() {
        TextRecognitionHelper.recognizerClient = mock<TextRecognizer>()
        TextRecognitionHelper.recognizerFactory = { mock<TextRecognizer>() }
        com.google.mlkit.common.sdkinternal.MlKitContext.initializeIfNeeded(
            androidx.test.core.app.ApplicationProvider.getApplicationContext()
        )
    }

    @After
    fun tearDown() {
        TextRecognitionHelper.recognizerClient = null
        TextRecognitionHelper.recognizerFactory = originalFactory
        TextRecognitionHelper.mlKitWrapper = originalWrapper
        TextRecognitionHelper.ocrDispatcher = kotlinx.coroutines.Dispatchers.Default
    }

    @Test
    @CoveredRequirement("2.3.1.9")
    @CoveredRequirement("2.3.2.9")
    fun testExtractText_ReturnsFilteredWords() = runTest {
        TextRecognitionHelper.ocrDispatcher = kotlinx.coroutines.Dispatchers.Unconfined
        val bitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)
        val mockText = mock<Text>()
        whenever(mockText.text).thenReturn("Spirou and Fantasio")
        val mockWrapper = mock<IMlKitWrapper>()
        whenever(mockWrapper.processImage(any(), any())).thenReturn(Tasks.forResult(mockText))
        TextRecognitionHelper.mlKitWrapper = mockWrapper

        val result = TextRecognitionHelper.extractText(bitmap)
        assertTrue(result.contains("spirou"))
        assertTrue(result.contains("fantasio"))
        assertTrue(!result.contains("and"))
    }

    @Test
    @CoveredRequirement("2.3.1.9")
    fun testDefaultMlKitWrapper_Works() {
        val bitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)
        val mockRecognizer = mock<TextRecognizer>()
        val mockTask = mock<com.google.android.gms.tasks.Task<Text>>()
        whenever(mockRecognizer.process(any<com.google.mlkit.vision.common.InputImage>())).thenReturn(mockTask)
        TextRecognitionHelper.mlKitWrapper = originalWrapper
        val resultTask = TextRecognitionHelper.mlKitWrapper.processImage(mockRecognizer, bitmap)
        assertThat(resultTask, `is`(mockTask))
        verify(mockRecognizer).process(any<com.google.mlkit.vision.common.InputImage>())
    }

    @Test
    @CoveredRequirement("2.3.1.9")
    fun testLazyInitializationOfDefaultRecognizer() = runTest {
        val mockDefaultRecognizer = mock<TextRecognizer>()
        TextRecognitionHelper.recognizerFactory = { mockDefaultRecognizer }
        TextRecognitionHelper.recognizerClient = null
        TextRecognitionHelper.ocrDispatcher = kotlinx.coroutines.Dispatchers.Unconfined
        val bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        val mockWrapper = mock<IMlKitWrapper>()
        whenever(mockWrapper.processImage(any(), any())).thenReturn(Tasks.forResult(null))
        TextRecognitionHelper.mlKitWrapper = mockWrapper

        TextRecognitionHelper.extractText(bitmap)
        verify(mockWrapper).processImage(eq(mockDefaultRecognizer), eq(bitmap))
    }

    @Test
    @CoveredRequirement("2.3.2.9")
    fun testFilterWords_FilteringRules() {
        val input = "Spirou 12345 and Fantasio"
        val result = TextRecognitionHelper.filterWords(input)
        assertTrue(result.contains("spirou"))
        assertTrue(result.contains("fantasio"))
        assertTrue(!result.contains("12345"))
        assertTrue(!result.contains("and"))
    }

    @Test
    @CoveredRequirement("2.3.1.9")
    fun testInternalProperties_DebugAccess() {
        val mockRecognizer = mock<TextRecognizer>()
        TextRecognitionHelper.recognizerClient = mockRecognizer
        org.junit.Assert.assertEquals(mockRecognizer, TextRecognitionHelper.recognizerClient)
    }
}
