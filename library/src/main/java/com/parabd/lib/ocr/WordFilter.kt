@file:Suppress("RedundantVisibilityModifier")
package com.parabd.lib.ocr

import com.parabd.lib.traceability.SatisfiesRequirement
import java.util.Locale

/**
 * Utility for filtering and cleaning words from raw text.
 */
public object WordFilter {

    /**
     * Filters and cleans a string into a list of significant words.
     *
     * Requirement 2.3.2: Removes words with 3 or fewer characters and pure numeric strings.
     *
     * @param rawText The raw text string to filter.
     * @return A list of significant lowercase words.
     */
    @SatisfiesRequirement("2.3.2.2")
    public fun filterWords(rawText: String?): List<String> {
        if (rawText.isNullOrBlank()) return emptyList()

        return rawText
            .split("[^\\p{L}\\p{N}]+".toRegex())
            .map { it.trim().lowercase(Locale.ROOT) }
            .filter { cleanWord ->
                cleanWord.length > 3 && !cleanWord.all { it.isDigit() }
            }
    }
}



