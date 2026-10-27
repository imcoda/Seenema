package ink.c0d4.seenema.presentation.gallery

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Matrix
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.min

/** A fitted image at rest, with bounded pinch, drag and animated double-tap zoom. */
class ZoomImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    AppCompatImageView(context, attrs) {
    private var transform: Matrix? = null
    private var zoomAnimation: ValueAnimator? = null
    var zoom = 1f
        private set
    var onImageTap: (() -> Unit)? = null
    private val pinch = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            zoomAnimation?.cancel()
            parent?.requestDisallowInterceptTouchEvent(true)
            return true
        }
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scaleTo((zoom * detector.scaleFactor).coerceIn(1f, 4f), detector.focusX, detector.focusY)
            return true
        }
    })
    private val gestures = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent) = true
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean { performClick(); return true }
        override fun onDoubleTap(e: MotionEvent): Boolean {
            animateZoom(if (zoom > 1.05f) 1f else 2.5f, e.x, e.y)
            return true
        }
        override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            if (zoom > 1.01f && !pinch.isInProgress) {
                transform?.postTranslate(-distanceX, -distanceY)
                clampAndApply()
                return true
            }
            return false
        }
    })

    init { scaleType = ScaleType.MATRIX; transform = Matrix(); isClickable = true }
    override fun setImageDrawable(drawable: Drawable?) { super.setImageDrawable(drawable); fit() }
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) { super.onSizeChanged(w, h, oldw, oldh); fit() }
    private fun fit() {
        val matrix = transform ?: return
        val image = drawable ?: return
        if (width <= 0 || height <= 0 || image.intrinsicWidth <= 0 || image.intrinsicHeight <= 0) return
        zoomAnimation?.cancel()
        zoom = 1f
        val scale = min(width.toFloat() / image.intrinsicWidth, height.toFloat() / image.intrinsicHeight)
        matrix.setScale(scale, scale)
        matrix.postTranslate((width - image.intrinsicWidth * scale) / 2, (height - image.intrinsicHeight * scale) / 2)
        imageMatrix = matrix
    }
    private fun scaleTo(value: Float, x: Float, y: Float) {
        transform?.postScale(value / zoom, value / zoom, x, y)
        zoom = value
        clampAndApply()
    }
    private fun clampAndApply() {
        val matrix = transform ?: return
        val image = drawable ?: return
        val rect = RectF(0f, 0f, image.intrinsicWidth.toFloat(), image.intrinsicHeight.toFloat())
        matrix.mapRect(rect)
        val dx = if (rect.width() <= width) (width - rect.width()) / 2 - rect.left
            else if (rect.left > 0) -rect.left else if (rect.right < width) width - rect.right else 0f
        val dy = if (rect.height() <= height) (height - rect.height()) / 2 - rect.top
            else if (rect.top > 0) -rect.top else if (rect.bottom < height) height - rect.bottom else 0f
        matrix.postTranslate(dx, dy)
        imageMatrix = matrix
    }
    fun resetZoom() = animateZoom(1f, width / 2f, height / 2f)
    private fun animateZoom(target: Float, x: Float, y: Float) {
        zoomAnimation?.cancel()
        zoomAnimation = ValueAnimator.ofFloat(zoom, target).apply {
            duration = 240
            interpolator = DecelerateInterpolator()
            addUpdateListener { scaleTo(it.animatedValue as Float, x, y) }
            start()
        }
    }
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.actionMasked == MotionEvent.ACTION_DOWN) {
            zoomAnimation?.cancel()
            parent?.requestDisallowInterceptTouchEvent(zoom > 1.01f)
        }
        pinch.onTouchEvent(event)
        gestures.onTouchEvent(event)
        if (event.actionMasked == MotionEvent.ACTION_UP || event.actionMasked == MotionEvent.ACTION_CANCEL)
            parent?.requestDisallowInterceptTouchEvent(false)
        else if (event.pointerCount > 1 || pinch.isInProgress || zoom > 1.01f) parent?.requestDisallowInterceptTouchEvent(true)
        return true
    }
    override fun performClick(): Boolean { super.performClick(); onImageTap?.invoke(); return true }
    override fun onDetachedFromWindow() { zoomAnimation?.cancel(); super.onDetachedFromWindow() }
}
