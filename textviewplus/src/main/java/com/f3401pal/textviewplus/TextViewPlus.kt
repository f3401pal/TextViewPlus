package com.f3401pal.textviewplus

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

class TextViewPlus : RecyclerView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)
    constructor(context: Context, attributeSet: AttributeSet, style: Int) : super(context, attributeSet, style)

    fun setTextAsync(input: InputStream) {
        val bufferedReader = BufferedReader(InputStreamReader(input, "UTF-8"))

    }

}