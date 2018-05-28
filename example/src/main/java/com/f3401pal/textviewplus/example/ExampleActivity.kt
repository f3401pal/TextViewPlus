package com.f3401pal.textviewplus.example

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.f3401pal.textviewplus.Paragraph
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.FlowableEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_example.*

private const val PARAGRAPH_BUFFER_SIZE = 5

class ExampleActivity : AppCompatActivity() {

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
        setContentView(R.layout.activity_example)
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