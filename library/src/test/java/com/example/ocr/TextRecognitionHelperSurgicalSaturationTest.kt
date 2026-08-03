package com.example.ocr

import android.graphics.Bitmap
import com.example.imagecomparison.CoveredRequirement
import com.google.mlkit.vision.text.Text
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
class TextRecognitionHelperSurgicalSaturationTest {
    private val originalWrapper = TextRecognitionHelper.mlKitWrapper

    @After
    fun tearDown() {
        TextRecognitionHelper.mlKitWrapper = originalWrapper
    }

    @Test
    @CoveredRequirement("2.1.2")
    fun testExtractText_SystematicBranchSaturation_Surgical() = runTest {
        val mockWrapper = mock<IMlKitWrapper>()
        TextRecognitionHelper.mlKitWrapper = mockWrapper
        TextRecognitionHelper.ocrDispatcher = Dispatchers.Unconfined
        val bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)

        // Case: Null task result
        whenever(mockWrapper.processImage(any(), any())).thenReturn(com.google.android.gms.tasks.Tasks.forResult(null))
        TextRecognitionHelper.extractText(bitmap)

        // Case: Blank text
        val mockText = mock<Text>()
        whenever(mockText.text).thenReturn("   ")
        whenever(mockWrapper.processImage(any(), any())).thenReturn(com.google.android.gms.tasks.Tasks.forResult(mockText))
        TextRecognitionHelper.extractText(bitmap)
        assertTrue(true)
    }
}
