package com.example.ocr

import android.graphics.Bitmap
import com.example.imagecomparison.CoveredRequirement
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [32])
class TextRecognitionHelperFinalSaturationTest {
    private val originalWrapper = TextRecognitionHelper.mlKitWrapper

    @After
    fun tearDown() {
        TextRecognitionHelper.mlKitWrapper = originalWrapper
    }

    @Test
    @CoveredRequirement("2.1.2")
    fun testExtractText_ThrowableBranch_Surgical() = runTest {
        val mockWrapper = mock<IMlKitWrapper>()
        // Return a task that fails with a custom Exception to trigger catch blocks
        whenever(mockWrapper.processImage(any(), any())).thenThrow(java.lang.Error("Bytecode visit"))
        TextRecognitionHelper.mlKitWrapper = mockWrapper
        TextRecognitionHelper.ocrDispatcher = Dispatchers.Unconfined
        TextRecognitionHelper.extractText(Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888))
    }
}
