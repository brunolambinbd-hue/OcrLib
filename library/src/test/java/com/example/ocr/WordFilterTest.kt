package com.parabd.lib.ocr

import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [WordFilter] to reach 100% coverage.
 */
@Suppress("RedundantVisibilityModifier")
public class WordFilterTest {
    /**
     * Tests word filtering rules (removing short words and numeric strings).
     */
    @Test
    @CoveredRequirement("2.3.2.2")
    public fun testFilterWords_FilteringRules() {
        val input = "Spirou 12345 and Fantasio"
        val result = WordFilter.filterWords(input)
        assertTrue(result.contains("spirou"))
        assertTrue(result.contains("fantasio"))
        assertTrue("Numeric strings should be filtered out", !result.contains("12345"))
        assertTrue("Short words should be filtered out", !result.contains("and"))
    }

    /**
     * Tests edge cases like null, empty, or blank input.
     */
    @Test
    @CoveredRequirement("2.3.2.2")
    public fun testFilterWords_EdgeCases() {
        assertTrue("Null input should return empty list", WordFilter.filterWords(null).isEmpty())
        assertTrue("Empty input should return empty list", WordFilter.filterWords("").isEmpty())
        assertTrue("Blank input should return empty list", WordFilter.filterWords("   ").isEmpty())
    }

    /**
     * Tests input with only numeric tokens and special characters.
     */
    @Test
    @CoveredRequirement("2.3.2.2")
    public fun testFilterWords_NumericAndSpecialChars() {
        val input = "!!! 123 4567 89 !!!"
        val result = WordFilter.filterWords(input)
        assertTrue("Should be empty for only short or numeric tokens", result.isEmpty())
    }
}
