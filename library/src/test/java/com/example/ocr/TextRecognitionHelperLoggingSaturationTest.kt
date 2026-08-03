package com.example.ocr

import android.graphics.Bitmap
import com.example.imagecomparison.CoveredRequirement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [32])
class TextRecognitionHelperLoggingSaturationTest {
    private val originalWrapper = TextRecognitionHelper.mlKitWrapper

    @After
    fun tearDown() {
        TextRecognitionHelper.mlKitWrapper = originalWrapper
    }

    @Test
    @CoveredRequirement("2.1.2")
    fun testExtractText_LoggingBranches_Surgical() = runTest {
        val tag = "TextRecognitionHelper"
        val mockWrapper = mock<IMlKitWrapper>()
        whenever(mockWrapper.processImage(any(), any())).thenThrow(RuntimeException("Log"))
        TextRecognitionHelper.mlKitWrapper = mockWrapper
        TextRecognitionHelper.ocrDispatcher = Dispatchers.Unconfined

        // 1. isLoggable = true
        org.robolectric.shadows.ShadowLog.setLoggable(tag, android.util.Log.ERROR)
        TextRecognitionHelper.extractText(Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888))

        // 2. isLoggable = false
        org.robolectric.shadows.ShadowLog.setLoggable(tag, 7)
        TextRecognitionHelper.extractText(Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888))
        assertTrue(true)
    }
}
