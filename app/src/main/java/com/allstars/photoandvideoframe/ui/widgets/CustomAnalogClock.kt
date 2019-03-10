package com.allstars.photoandvideoframe.ui.widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.support.annotation.ColorInt
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import com.allstars.photoandvideoframe.R

import java.util.Calendar

class CustomAnalogClock(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var h: Int = 0
    private var w: Int = 0

    /**
     * truncation of the heights of the clock-hands,
     * hour clock-hand will be smaller comparetively to others
     */
    private var handTruncation: Int = 0
    private var hourHandTruncation = 0

    private var smallRadius = 0
    private var mainRadius = 0
    private var tickRadius = 0

    private var centerX: Int = 0
    private var centerY: Int = 0

    private lateinit var paint: Paint

    private var handWidth: Int = 0
    private var tickLineLength: Int = 0

    private var isInit: Boolean = false  // it will be true once the clock will be initialized.

    @ColorInt
    private var outerCircleColor: Int = 0
    @ColorInt
    private var innerCircleColor: Int = 0
    @ColorInt
    private var hoursHandColor: Int = 0
    @ColorInt
    private var minutesHandColor: Int = 0
    @ColorInt
    private var secondsHandColor: Int = 0

    init {

        init(attrs)
    }

    private fun init(set: AttributeSet) {
        val ta = context.obtainStyledAttributes(set, R.styleable.CustomAnalogClock)

        outerCircleColor = ta.getColor(R.styleable.CustomAnalogClock_outer_circle_color, Color.WHITE)
        innerCircleColor = ta.getColor(R.styleable.CustomAnalogClock_inner_circle_color, Color.BLACK)
        hoursHandColor = ta.getColor(R.styleable.CustomAnalogClock_hours_hand_color, Color.WHITE)
        minutesHandColor = ta.getColor(R.styleable.CustomAnalogClock_minutes_hand_color, Color.YELLOW)
        secondsHandColor = ta.getColor(R.styleable.CustomAnalogClock_seconds_hand_color, Color.BLUE)

        ta.recycle()
    }

    override fun onDraw(canvas: Canvas) {

        /* initialize necessary values */
        if (!isInit) {
            paint = Paint()
            h = height
            w = width
            val padding = dpToPixels(8).toInt()  // spacing from the circle border
            val minAttr = Math.min(h, w)
            mainRadius = minAttr / 2
            smallRadius = (minAttr / 2 * 0.8).toInt()
            tickRadius = smallRadius - padding

            centerX = w / 2
            centerY = h / 2

            // for maintaining different heights among the clock-hands
            handTruncation = minAttr / 20
            hourHandTruncation = minAttr / 17

            handWidth = dpToPixels(2).toInt()

            tickLineLength = dpToPixels(10).toInt()

            isInit = true  // set true once initialized
        }

        /* outer circle */
        paint.reset()
        paint.color = outerCircleColor
        paint.style = Paint.Style.FILL
        paint.isAntiAlias = true
        canvas.drawCircle(centerX.toFloat(), centerY.toFloat(), mainRadius.toFloat(), paint)

        /* inner circle */
        paint.reset()
        paint.color = innerCircleColor
        paint.style = Paint.Style.FILL
        paint.isAntiAlias = true
        canvas.drawCircle(centerX.toFloat(), centerY.toFloat(), smallRadius.toFloat(), paint)

        /* clock-center */
        paint.reset()
        paint.color = outerCircleColor
        paint.style = Paint.Style.FILL
        paint.isAntiAlias = true
        canvas.drawCircle(
            centerX.toFloat(),
            centerY.toFloat(),
            12f,
            paint
        )  // the 03 clock hands will be rotated from this center point.

        paint.reset()
        paint.color = hoursHandColor
        paint.style = Paint.Style.FILL
        paint.strokeWidth = handWidth.toFloat()
        paint.isAntiAlias = true
        for (i in 1..12) {
            val di = i.toDouble()

            val angleFrom12 = di / 12.0 * 2.0 * Math.PI

            val angleFrom3 = Math.PI / 2.0 - angleFrom12

            canvas.drawLine(
                (centerX + Math.cos(angleFrom3) * tickRadius).toFloat(),
                (centerY - Math.sin(angleFrom3) * tickRadius).toFloat(),
                (centerX + Math.cos(angleFrom3) * (tickRadius - tickLineLength)).toFloat(),
                (centerY - Math.sin(angleFrom3) * (tickRadius - tickLineLength)).toFloat(),
                paint
            )
        }

        /* draw clock hands to represent the every single time */
        val calendar = Calendar.getInstance()
        var hour = calendar.get(Calendar.HOUR_OF_DAY)
        hour = if (hour > 12) hour - 12 else hour

        /* draw hours */
        drawHandLine(
            canvas,
            ((hour + calendar.get(Calendar.MINUTE) / 60f) * 5f).toDouble(),
            HandLineType.HOURS
        )
        drawHandLine(canvas, calendar.get(Calendar.MINUTE).toDouble(), HandLineType.MINUTES) // draw minutes
        drawHandLine(canvas, calendar.get(Calendar.SECOND).toDouble(), HandLineType.SECONDS) // draw seconds

        /* invalidate the appearance for next representation of time  */
        postInvalidateDelayed(500)
        invalidate()
    }

    private fun drawHandLine(canvas: Canvas, moment: Double, handLineType: HandLineType) {
        paint.reset()
        paint.strokeWidth = handWidth.toFloat()
        paint.style = Paint.Style.FILL
        paint.isAntiAlias = true
        val angle = Math.PI * moment / 30 - Math.PI / 2
        val handRadius =
            if (handLineType == HandLineType.HOURS) smallRadius - handTruncation - hourHandTruncation else smallRadius - handTruncation
        when (handLineType) {
            CustomAnalogClock.HandLineType.HOURS -> paint.color = hoursHandColor
            CustomAnalogClock.HandLineType.MINUTES -> paint.color = minutesHandColor
            CustomAnalogClock.HandLineType.SECONDS -> paint.color = secondsHandColor
        }
        canvas.drawLine(
            centerX.toFloat(),
            centerY.toFloat(),
            (w / 2 + Math.cos(angle) * handRadius).toFloat(),
            (h / 2 + Math.sin(angle) * handRadius).toFloat(),
            paint
        )
    }

    private fun dpToPixels(dp: Int): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), this.resources.displayMetrics)
    }

    internal enum class HandLineType {
        HOURS,
        MINUTES,
        SECONDS
    }

}
