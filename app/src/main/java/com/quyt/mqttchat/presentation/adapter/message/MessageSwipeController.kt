package com.quyt.mqttchat.presentation.adapter.message

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.RecyclerView
import com.quyt.mqttchat.R
import com.quyt.mqttchat.presentation.adapter.message.viewHolder.MyMessageViewHolder
import com.quyt.mqttchat.presentation.adapter.message.viewHolder.OtherMessageViewHolder
import com.quyt.mqttchat.utils.DimenUtils

class MessageSwipeController(private val context: Context, private val showReplyUI: (Int) -> Unit) :
    Callback() {

    private lateinit var imageDrawable: Drawable
    private lateinit var shareRound: Drawable

    private var currentItemViewHolder: RecyclerView.ViewHolder? = null
    private lateinit var mView: View
    private var dX = 0f

    private var replyButtonProgress: Float = 0.toFloat()
    private var lastReplyButtonAnimationTime: Long = 0
    private var swipeBack = false
    private var isVibrate = false
    private var startTracking = false
    private val triggerValue = 50

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        mView = when (viewHolder) {
            is MyMessageViewHolder -> viewHolder.binding.rlMessage
            is OtherMessageViewHolder -> viewHolder.binding.rlMessage
            else -> viewHolder.itemView
        }
        imageDrawable = ContextCompat.getDrawable(context, R.drawable.ic_reply_24)!!
        shareRound = ContextCompat.getDrawable(context, R.drawable.ic_circle)!!
        return makeMovementFlags(ACTION_STATE_IDLE, LEFT)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

    override fun convertToAbsoluteDirection(flags: Int, layoutDirection: Int): Int {
        if (swipeBack) {
            swipeBack = false
            return 0
        }
        return super.convertToAbsoluteDirection(flags, layoutDirection)
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        if (actionState == ACTION_STATE_SWIPE) {
            setTouchListener(recyclerView, viewHolder)
        }

        if (mView.translationX < pxToDp(130) || dX < this.dX) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            this.dX = dX
            startTracking = true
        }
        currentItemViewHolder = viewHolder
        drawReplyButton(c, viewHolder)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setTouchListener(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        recyclerView.setOnTouchListener { _, event ->
            swipeBack = event.action == MotionEvent.ACTION_CANCEL || event.action == MotionEvent.ACTION_UP
            if (swipeBack) {
                if (Math.abs(mView.translationX) >= pxToDp(triggerValue)) {
                    showReplyUI(viewHolder.absoluteAdapterPosition)
                }
            }
            false
        }
    }
    private fun drawReplyButton(canvas: Canvas, viewHolder: RecyclerView.ViewHolder) {
        if (currentItemViewHolder == null) {
            return
        }
        val translationX = mView.translationX
        val newTime = System.currentTimeMillis()
        val dt = Math.min(17, newTime - lastReplyButtonAnimationTime)
        lastReplyButtonAnimationTime = newTime
        val showing = translationX <= -pxToDp(30)
        if (showing) {
            if (replyButtonProgress < 1.0f) {
                replyButtonProgress += dt / 180.0f
                if (replyButtonProgress > 1.0f) {
                    replyButtonProgress = 1.0f
                } else {
                    mView.invalidate()
                }
            }
        } else if (translationX >= 0.0f) { // Revert swipe
            replyButtonProgress = 0f
            startTracking = false
            isVibrate = false
        } else {
            if (replyButtonProgress > 0.0f) {
                replyButtonProgress -= dt / 180.0f
                if (replyButtonProgress < 0.1f) {
                    replyButtonProgress = 0f
                } else {
                    mView.invalidate()
                }
            }
        }
        val alpha: Int
        val scale: Float
        if (showing) {
            scale = if (replyButtonProgress <= 0.8f) {
                1.2f * (replyButtonProgress / 0.8f)
            } else {
                1.2f - 0.2f * ((replyButtonProgress - 0.8f) / 0.2f)
            }
            alpha = Math.min(255f, 255 * (replyButtonProgress / 0.8f)).toInt()
        } else {
            scale = replyButtonProgress
            alpha = Math.min(255f, 255 * replyButtonProgress).toInt()
        }
        shareRound.alpha = alpha

        imageDrawable.alpha = alpha
        if (startTracking) {
            if (!isVibrate && mView.translationX <= -pxToDp(triggerValue)) {
                mView.performHapticFeedback(
                    HapticFeedbackConstants.KEYBOARD_TAP,
                    HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
                )
                isVibrate = true
            }
        }
        val leftMargin = 5
        val x = mView.right + (mView.translationX / 2).toInt() + leftMargin

        val y = (mView.top + mView.measuredHeight / 2).toFloat()
        if (viewHolder is MyMessageViewHolder) {
            shareRound.colorFilter = PorterDuffColorFilter(
                ContextCompat.getColor(context, R.color.colorPrimary),
                PorterDuff.Mode.MULTIPLY
            )
        }else{
            shareRound.colorFilter = PorterDuffColorFilter(
                ContextCompat.getColor(context, R.color.normal_text),
                PorterDuff.Mode.MULTIPLY
            )
        }
        imageDrawable.colorFilter = PorterDuffColorFilter(
            ContextCompat.getColor(context, R.color.white),
            PorterDuff.Mode.MULTIPLY
        )

        shareRound.setBounds(
            (x - pxToDp(18) * scale).toInt(),
            (y - pxToDp(18) * scale).toInt(),
            (x + pxToDp(18) * scale).toInt(),
            (y + pxToDp(18) * scale).toInt()
        )
        shareRound.draw(canvas)
        // + 20 is margin left of item
        imageDrawable.setBounds(
            (x - pxToDp(12) * scale).toInt(),
            (y - pxToDp(11) * scale).toInt(),
            (x + pxToDp(12) * scale).toInt(),
            (y + pxToDp(10) * scale).toInt()
        )
        imageDrawable.draw(canvas)
        shareRound.alpha = 255
        imageDrawable.alpha = 255
    }

    private fun pxToDp(pixel: Int): Int {
        return DimenUtils.dp(pixel.toFloat(), context)
    }
}
