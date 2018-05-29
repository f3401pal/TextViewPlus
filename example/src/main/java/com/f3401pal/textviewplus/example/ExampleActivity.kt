package com.f3401pal.textviewplus.example

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Layout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.PrecomputedTextCompat
import com.f3401pal.textviewplus.Paragraph
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.FlowableEmitter
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_example.*
import kotlinx.android.synthetic.main.activity_textview.*
import kotlinx.android.synthetic.main.activity_textview_plus.*

private const val PARAGRAPH_BUFFER_SIZE = 5

class ExampleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_example)

        launchTextViewPlusActivity.setOnClickListener{ startActivity(Intent(this, TextViewPlusActivity::class.java)) }
        launchTextViewActivity.setOnClickListener{ startActivity(Intent(this, TextViewActivity::class.java)) }
        launchPreComputedTextActivity.setOnClickListener{ startActivity(Intent(this, PreComputedTextActivity::class.java)) }
    }
}

class TextViewPlusActivity : AppCompatActivity() {

    private val computeText: Flowable<Paragraph> by lazy {
        Flowable.defer {
            Flowable.create({ emitter: FlowableEmitter<Paragraph> ->
                textViewPlus.preComputeText("pride.txt", { emitter.onNext(it) })
                emitter.onComplete()
            }, BackpressureStrategy.BUFFER)
        }.subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread()).cache()
    }
    private var disposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_textview_plus)
    }

    override fun onStart() {
        super.onStart()
        disposable = computeText.buffer(PARAGRAPH_BUFFER_SIZE).subscribe({ batch ->
            textViewPlus.appendText(batch)
        })
    }

    override fun onStop() {
        disposable?.dispose()
        textViewPlus.setText(emptyList())
        super.onStop()
    }
}

class TextViewActivity: AppCompatActivity() {

    private val loadText by lazy {
        Single.defer {
            val input = assets.open("pride.txt")
            val text = input.bufferedReader().readText()
            input.close()
            Single.just(text)
        }.subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread()).cache()
    }
    private var disposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_textview)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            textView.justificationMode = Layout.JUSTIFICATION_MODE_INTER_WORD
        }
    }

    override fun onStart() {
        super.onStart()
        disposable = loadText.subscribe{ text ->
            textView.text = text
        }
    }

    override fun onStop() {
        disposable?.dispose()
        super.onStop()
    }
}

class PreComputedTextActivity: AppCompatActivity() {

    private val loadText by lazy {
        Single.defer {
            val input = assets.open("pride.txt")
            val text = input.bufferedReader().readText()
            input.close()
            val params = PrecomputedTextCompat.Params.Builder(textView.paint).build()
            Single.just(PrecomputedTextCompat.create(text, params))
        }.subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread()).cache()
    }
    private var disposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_textview)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            textView.justificationMode = Layout.JUSTIFICATION_MODE_INTER_WORD
        }
    }

    override fun onStart() {
        super.onStart()
        disposable = loadText.subscribe{ text ->
            textView.text = text
        }
    }

    override fun onStop() {
        disposable?.dispose()
        super.onStop()
    }
}