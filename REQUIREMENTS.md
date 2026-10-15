# Requirements Traceability - OcrLib

This document lists the functional requirements implemented and satisfied by `OcrLib`, mapped using `@SatisfiesRequirement` annotations.

## Chapter 2: Analysis and OCR (`2.3`)

### Requirement 2.3.1.2: Image Text Recognition
- **Description:** The system must provide asynchronous text extraction from image bitmaps using Google ML Kit.
- **Implemented By:** 
  - `TextRecognitionHelper` (Class & Methods)
  - `ITextRecognizer` interface
  - `IMlKitWrapper` interface

### Requirement 2.3.2.2: Text Filtering and Cleaning
- **Description:** The system must filter out insignificant words from extracted OCR text (removing words with 3 or fewer characters and pure numeric strings).
- **Implemented By:**
  - `WordFilter.filterWords(String?)`
  - `TextRecognitionHelper.filterWords(String?)`
