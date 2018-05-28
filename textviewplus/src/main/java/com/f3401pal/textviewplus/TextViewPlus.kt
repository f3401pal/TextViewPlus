package com.f3401pal.textviewplus

import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.os.Looper
import android.text.Layout.JUSTIFICATION_MODE_INTER_WORD
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.text.PrecomputedTextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.InputStream
import java.util.*


class TextViewPlus : RecyclerView {

    private val adapter = Adapter(context)

    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)
    constructor(context: Context, attributeSet: AttributeSet, style: Int) : super(context, attributeSet, style)

    init {
        layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        setAdapter(adapter)
    }

    fun setTextAsync(input: InputStream) {
        if (Objects.equals(Looper.myLooper(), Looper.getMainLooper())) {
            throw RuntimeException("Running async task on UI thread")
        }
        val data = mutableListOf<Paragraph>()
        val pBuffer = StringBuffer()
        input.bufferedReader().useLines { lines ->
            lines.forEach { line ->
                if(line.isBlank()) {
                    data.add(Paragraph(pBuffer.toString()))
                    pBuffer.setLength(0)
                } else {
                    pBuffer.append(line)
                }
            }
        }
        input.close()

        handler.post {
            adapter.data = data.toTypedArray()
            adapter.notifyDataSetChanged()
        }
    }

}

private data class Paragraph(val raw: String) {

    internal val precomputedText = PrecomputedTextCompat.create(raw, params)

    companion object {
        private val params: PrecomputedTextCompat.Params by lazy {
            PrecomputedTextCompat.Params.Builder(TextPaint().apply {
                typeface = Typeface.DEFAULT
            }).build()
        }
    }
}

private class Adapter(context: Context) : RecyclerView.Adapter<ViewHolder>() {

    private val inflater = LayoutInflater.from(context)
    private val spannableFactory = SimpleSpannableFactory()

    internal var data = emptyArray<Paragraph>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val viewHolder = ViewHolder(inflater.inflate(R.layout.item_text_view, parent, false))
        viewHolder.textView.setSpannableFactory(spannableFactory)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            viewHolder.textView.justificationMode = JUSTIFICATION_MODE_INTER_WORD
        }
        return viewHolder
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.text = data[position].precomputedText
    }

}

private class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    internal val textView: TextView = view.findViewById(R.id.textView)

}

private class SimpleSpannableFactory : Spannable.Factory() {

    override fun newSpannable(source: CharSequence?): Spannable {
        return source?.let {
            it as? SpannableString ?: super.newSpannable(source)
        } ?: super.newSpannable(source)
    }
}