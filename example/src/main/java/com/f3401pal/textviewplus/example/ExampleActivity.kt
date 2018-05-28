package com.f3401pal.textviewplus.example

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_example.*
import java.util.concurrent.Executors
import java.util.concurrent.Future

class ExampleActivity : AppCompatActivity() {

    private val executor = Executors.newSingleThreadExecutor()

    private var future: Future<Any>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_example)
    }

    override fun onStart() {
        super.onStart()
        future = executor.submit<Any> {
            textViewPlus.setTextAsync(assets.open("pride.txt"))
        }
    }

    override fun onStop() {
        super.onStop()
        future?.cancel(true)
    }
}