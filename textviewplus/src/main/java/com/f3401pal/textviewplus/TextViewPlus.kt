package com.f3401pal.textviewplus

import android.content.Context
import android.os.Build
import android.os.Looper
import android.text.Layout.JUSTIFICATION_MODE_INTER_WORD
import android.text.Spannable
import android.text.SpannableString
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.UiThread
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.text.PrecomputedTextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*
import java.util.concurrent.Executors


class TextViewPlus : RecyclerView {

    private val adapter = Adapter(context)
    val preComputeText = createPreComputeText(context)

    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)
    constructor(context: Context, attributeSet: AttributeSet, style: Int) : super(context, attributeSet, style)

    init {
        layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        setAdapter(adapter)
    }

    fun setTextAsync(fileName: String) {
        val precomputedText = mutableListOf<Paragraph>().apply {
            preComputeText(fileName, { this.add(it) })
        }
        handler.post {
            setText(precomputedText)
        }
    }

    @UiThread
    fun appendText(vararg p: Paragraph) {
        adapter.data.addAll(p)
        adapter.notifyItemRangeInserted(0, p.size)
    }

    @UiThread
    fun appendText(p: List<Paragraph>) {
        val insertPosition = adapter.data.size
        adapter.data.addAll(p)
        adapter.notifyItemRangeInserted(insertPosition, insertPosition + p.size)
    }

    @UiThread
    fun setText(textData: List<Paragraph>) {
        adapter.data.clear()
        adapter.data.addAll(textData)
        adapter.notifyDataSetChanged()
    }

    companion object {

        private fun createPreComputeText(context: Context): (fileName: String, onNext: (Paragraph) -> Unit) -> Unit {
            return { fileName, onNext ->
                if (Objects.equals(Looper.myLooper(), Looper.getMainLooper())) {
                    throw RuntimeException("Running async task on UI thread")
                }
                val input = context.assets.open(fileName)
                input.bufferedReader().useLines { lines ->
                    lines.forEach { line ->
                        if(line.isNotBlank()) {
                            Paragraph(line).also {
                                onNext(it)
                            }
                        }
                    }
                }
                input.close()
            }
        }
    }

}

data class Paragraph(val raw: String)

private class Adapter(context: Context) : RecyclerView.Adapter<ViewHolder>() {

    private val inflater = LayoutInflater.from(context)
    private val spannableFactory = SimpleSpannableFactory()
    private val executor = Executors.newCachedThreadPool()

    internal val data = mutableListOf<Paragraph>()

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
        holder.textView.setTextFuture(PrecomputedTextCompat
                .getTextFuture(data[position].raw, holder.textView.textMetricsParamsCompat, executor))
    }

}

private class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    internal val textView: AppCompatTextView = view.findViewById(R.id.textView)

}

private class SimpleSpannableFactory : Spannable.Factory() {

    override fun newSpannable(source: CharSequence?): Spannable {
        return source?.let {
            it as? SpannableString ?: super.newSpannable(source)
        } ?: super.newSpannable(source)
    }
}