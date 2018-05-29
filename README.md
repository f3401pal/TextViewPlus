# TextViewPlus
Android TextView with performance improvement for large amount of text. 
The purpose of this library is trying to setup an example implementation for best practice of setting text on Android `TextView`.

This project is inspired by a Google IO 2018 session, `Best practices for text on Android`
https://www.youtube.com/watch?v=x-FcOX6ErdI


What actually happened when you call `TextView.setText()` is the text needs to be processed in the context of the TextView as well as any custom span instance you set on the text.
Because of that, `setText()` with a large text could be very slow specially it is normally running in UI thread.

As a solution, Android P released a new class `PreComputedText` which pre-calculate and store the view metrics. 
`PreComputedText` could be created in a background thread and later call `TextView.setText(PreComputedText)` on UI thread.
`PreComputedTextCompact` is also available.

Another problem with large text is memory allocation, when loading all text at once. The system will need to allocation a large chunk of memory at once.
As suggested by this Google IO session, we should use `RecyclerView` as the solution.

`TextViewPlus` implementation details
- split text into paragraphs (background thread)
- create `PreComputedText` for each paragraph (background thread)
- set `PreComputedText` of each paragraph in `TextView` item in `RecyclerView` (UI thread)

A general improvement on display items in `RecyclerView` is batching updates. Instead loading all the paragraphs at once, the implementation loads paragraphs in a batch (i.e. 5 paragraphs at a time).
This way users see content as soon as the first batch loaded instead of wait for complete text.

 