package com.udacity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.content.withStyledAttributes
import kotlin.properties.Delegates

private const val RADIUS_CIRCLE_LOADING = 30.0f

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0

    private val valueAnimator = ValueAnimator()

    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { p, old, new ->
        when (new) {
            ButtonState.Clicked -> {
                buttonState = ButtonState.Loading
            }
            ButtonState.Loading -> {
                valueAnimator.start()
            }
            ButtonState.Completed -> {
                valueAnimator.cancel()
                invalidate()
            }
        }
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 55.0f
        typeface = Typeface.create("", Typeface.BOLD)
    }

    private val rect = RectF()
    private val textBoundRect = Rect()

    private var progress = 0.0

    private var textButtonColor = 0
    private var buttonColor = 0
    private var buttonLoadingColor = 0
    private var buttonCircleColor = 0

    init {
        isClickable = true

        context.withStyledAttributes(attrs, R.styleable.LoadingButton) {
            textButtonColor = getColor(R.styleable.LoadingButton_textButtonColor, 0)
            buttonColor = getColor(R.styleable.LoadingButton_buttonColor, 0)
            buttonLoadingColor = getColor(R.styleable.LoadingButton_buttonLoadingColor, 0)
            buttonCircleColor = getColor(R.styleable.LoadingButton_buttonCircleColor, 0)
        }

        initLoadingAnimator()
    }

    private fun initLoadingAnimator() {
        valueAnimator.interpolator = AccelerateDecelerateInterpolator()
        valueAnimator.duration = 2000
        valueAnimator.setFloatValues(0.0f, 100.0f)
        valueAnimator.addUpdateListener {
            progress = (it.animatedValue as Float).toDouble()
            invalidate()
        }
        valueAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                buttonState = ButtonState.Completed
            }
        })
    }

    override fun performClick(): Boolean {
        super.performClick()
        if (buttonState == ButtonState.Completed)
            buttonState = ButtonState.Clicked
        return true
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        widthSize = w
        heightSize = h
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        paint.color = buttonColor

        val buttonText = when (buttonState) {
            ButtonState.Loading -> resources.getString(R.string.button_loading)
            else -> resources.getString(R.string.button_name)
        }

        canvas?.drawRect(0.0f, 0.0f, width.toFloat(), height.toFloat(), paint)

        if (buttonState == ButtonState.Loading) {
            paint.color = buttonLoadingColor
            canvas?.drawRect(
                0f, 0f,
                (widthSize * (progress / 100)).toFloat(), height.toFloat(), paint
            )
            paint.color = buttonCircleColor
            paint.getTextBounds(buttonText, 0, buttonText.length, textBoundRect)
            val centerX = measuredWidth.toFloat() / 2
            val centerY = measuredHeight.toFloat() / 2
            rect.set(
                centerX + textBoundRect.right / 2,
                centerY - RADIUS_CIRCLE_LOADING,
                centerX + textBoundRect.right / 2 + RADIUS_CIRCLE_LOADING * 2,
                centerY + RADIUS_CIRCLE_LOADING
            )
            canvas?.drawArc(
                rect,
                0f, (360 * (progress / 100)).toFloat(),
                true,
                paint
            )
        }

        paint.color = Color.WHITE
        canvas?.drawText(buttonText, (width / 2).toFloat(), ((height + 30) / 2).toFloat(), paint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }
}