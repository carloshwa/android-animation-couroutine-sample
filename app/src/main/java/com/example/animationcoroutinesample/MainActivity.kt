package com.example.animationcoroutinesample

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.animation_text
import kotlinx.android.synthetic.main.activity_main.go_button
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

class MainActivity : AppCompatActivity() {
  private val animationScope: CoroutineScope by lazy { MainScope() }
  private var animationJob: Job? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    go_button.setOnClickListener { animate() }
  }

  private fun animate() {
    animationJob?.cancel()

    animationJob = animationScope.launch {
      animateText(0, 100)

      animation_text.text = "Wait for it..."
      delay(1000)

      animateText(100, 0)
    }
  }

  private suspend fun animateText(from: Int, to: Int) {
    ValueAnimator.ofInt(from, to).apply {
      duration = 1500
      addUpdateListener { animation ->
        animation_text.text = animation.animatedValue.toString()
      }
      startAnimationSuspending()
    }
  }

  override fun onStop() {
    super.onStop()
    animationJob?.cancel()
  }

  override fun onDestroy() {
    super.onDestroy()
    animationScope.cancel()
  }
}

suspend fun Animator.startAnimationSuspending() =
  suspendCancellableCoroutine<Unit> { continuation ->
    continuation.invokeOnCancellation { cancel() }

    addListener(object : AnimatorListenerAdapter() {
      override fun onAnimationEnd(animation: Animator?) {
        if (continuation.isActive) continuation.resume(Unit) { }
      }

      override fun onAnimationCancel(animation: Animator?) {
        onAnimationEnd(animation)
      }
    })

    start()
  }

