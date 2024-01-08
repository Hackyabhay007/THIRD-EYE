    /*
     * Copyright 2022 The TensorFlow Authors. All Rights Reserved.
     *
     * Licensed under the Apache License, Version 2.0 (the "License");
     * you may not use this file except in compliance with the License.
     * You may obtain a copy of the License at
     *
     *       http://www.apache.org/licenses/LICENSE-2.0
     *
     * Unless required by applicable law or agreed to in writing, software
     * distributed under the License is distributed on an "AS IS" BASIS,
     * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     * See the License for the specific language governing permissions and
     * limitations under the License.
     */
    
    package org.tensorflow.lite.examples.objectdetection
    
    import android.content.Context
    import android.graphics.Canvas
    import android.graphics.Color
    import android.graphics.Paint
    import android.graphics.Rect
    import android.graphics.RectF
    import android.os.Build
    import android.speech.tts.TextToSpeech
    import android.util.AttributeSet
    import android.view.View
    import androidx.core.content.ContextCompat
    import java.util.LinkedList
    import kotlin.math.max
    import org.tensorflow.lite.task.vision.detector.Detection
    import java.util.Locale
    
    class OverlayView(context: Context?, attrs: AttributeSet?) : View(context, attrs) , TextToSpeech.OnInitListener {
    
        private var results: List<Detection> = LinkedList<Detection>()
        private var boxPaint = Paint()
        private var textBackgroundPaint = Paint()
        private var textPaint = Paint()
    
        private var scaleFactor: Float = 1f
    
        private var bounds = Rect()
        private var textToSpeech: TextToSpeech? = null
    
        init {
            initPaints()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                textToSpeech = TextToSpeech(context, this)
            } else {
                textToSpeech = TextToSpeech(context, this@OverlayView)
            }
        }
    
        fun clear() {
            textPaint.reset()
            textBackgroundPaint.reset()
            boxPaint.reset()
            invalidate()
            initPaints()
            textToSpeech?.stop()
            textToSpeech?.shutdown()
    
        }
    
        private fun initPaints() {
            textBackgroundPaint.color = Color.BLACK
            textBackgroundPaint.style = Paint.Style.FILL
            textBackgroundPaint.textSize = 50f
            textPaint.color = Color.WHITE
            textPaint.style = Paint.Style.FILL
            textPaint.textSize = 50f
            boxPaint.color = ContextCompat.getColor(context!!, R.color.bounding_box_color)
            boxPaint.strokeWidth = 8F
            boxPaint.style = Paint.Style.STROKE
        }
    
    
        override fun draw(canvas: Canvas) {
            super.draw(canvas)
    
            for (result in results) {
                val boundingBox = result.boundingBox
    
                val top = boundingBox.top * scaleFactor
                val bottom = boundingBox.bottom * scaleFactor
                val left = boundingBox.left * scaleFactor
                val right = boundingBox.right * scaleFactor
    
                // Draw bounding box around detected objects
                val drawableRect = RectF(left, top, right, bottom)
                canvas.drawRect(drawableRect, boxPaint)
    
                // Create text to display alongside detected objects
                val drawableText =
                    result.categories[0].label + " " +
                            String.format("%.2f", result.categories[0].score)
    
    
                // Check if accuracy is high
                val accuracyThreshold = 0.7
                if (result.categories[0].score >= accuracyThreshold) {
                    // Speak the detected object label
                    speakText(result.categories[0].label)
                }
    
                // Draw rect behind display text
                textBackgroundPaint.getTextBounds(drawableText, 0, drawableText.length, bounds)
                val textWidth = bounds.width()
                val textHeight = bounds.height()
                canvas.drawRect(
                    left,
                    top,
                    left + textWidth + Companion.BOUNDING_RECT_TEXT_PADDING,
                    top + textHeight + Companion.BOUNDING_RECT_TEXT_PADDING,
                    textBackgroundPaint
                )
    
                // Draw text for detected object
                canvas.drawText(drawableText, left, top + bounds.height(), textPaint)
            }
        }
    
    
        private var utteranceCounter = 0
    
        private var lastSpokenText: String? = null
    
        private fun speakText(text: String) {
            if (text != lastSpokenText) {
                val utteranceId = "${this.hashCode()}${utteranceCounter++}"
                val fullText = "aaapkeee saamneee  $text hai"
                textToSpeech?.speak(fullText, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
                lastSpokenText = text
            }
        }
    
    
    
        fun setResults(
          detectionResults: MutableList<Detection>,
          imageHeight: Int,
          imageWidth: Int,
        ) {
            results = detectionResults
    
            // PreviewView is in FILL_START mode. So we need to scale up the bounding box to match with
            // the size that the captured images will be displayed.
            scaleFactor = max(width * 1f / imageWidth, height * 1f / imageHeight)
        }
    
        companion object {
            private const val BOUNDING_RECT_TEXT_PADDING = 8
        }
    
        override fun onInit(status: Int) {
            if (status == TextToSpeech.SUCCESS) {
                val defaultLocale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    context.resources.configuration.locales.get(0)
                } else {
                    @Suppress("DEPRECATION")
                    context.resources.configuration.locale
                }
    
                textToSpeech?.language = defaultLocale
                val locale = Locale("hi", "IN")  // Use "hi" for Hindi and "IN" for India
                textToSpeech?.language = locale
            }
        }
    
    }
