package com.quyt.mqttchat.presentation.feature.home.message.detail

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.quyt.mqttchat.R
import com.quyt.mqttchat.databinding.DialogMediaViewerBinding
import com.quyt.mqttchat.presentation.extensions.animateAlpha
import com.quyt.mqttchat.presentation.extensions.applyMargin
import com.quyt.mqttchat.presentation.extensions.makeVisible
import com.quyt.mqttchat.presentation.gestures.direction.SwipeDirection
import com.quyt.mqttchat.presentation.gestures.direction.SwipeDirectionDetector
import com.quyt.mqttchat.presentation.gestures.dismiss.SwipeToDismissHandler


class MediaViewerDialog : DialogFragment() {
    private lateinit var binding: DialogMediaViewerBinding
    private lateinit var transitionImageAnimator: TransitionImageAnimator
    private lateinit var swipeDismissHandler: SwipeToDismissHandler
    private lateinit var url: String
    private lateinit var directionDetector: SwipeDirectionDetector

    private var swipeDirection: SwipeDirection? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
    }

//    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//
//        val dialog = super.onCreateDialog(savedInstanceState) as Dialog
//
//        return dialog
//    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
//        dialog?.window?.setBackgroundDrawableResource(R.color.transparent)
        binding = DialogMediaViewerBinding.inflate(inflater, container, false)
        directionDetector = SwipeDirectionDetector(requireContext()) { swipeDirection = it }
        Glide.with(this).load(url).into(binding.transitionImageView)
        //
        transitionImageAnimator = createTransitionImageAnimator(null)
        swipeDismissHandler = createSwipeToDismissHandler()
        binding.rootContainer.setOnTouchListener(swipeDismissHandler)
        binding.root.setOnTouchListener { _, event ->
            dispatchTouchEvent(event)
            handleTouchIfNotScaled(event)
        }
        return binding.root
    }



    private fun handleTouchIfNotScaled(event: MotionEvent): Boolean {
        directionDetector.handleTouchEvent(event)

        return when (swipeDirection) {
            SwipeDirection.UP, SwipeDirection.DOWN -> {
//                if (isSwipeToDismissAllowed && !wasScaled && imagesPager.isIdle) {
                    swipeDismissHandler.onTouch(binding.rootContainer, event)
//                } else true
            }
//            SwipeDirection.LEFT, SwipeDirection.RIGHT -> {
//                imagesPager.dispatchTouchEvent(event)
//            }
            else -> true
        }
    }

    private fun dispatchTouchEvent(event: MotionEvent) {
        if (event.action == MotionEvent.ACTION_UP) {
            handleEventActionUp(event)
        }

        if (event.action == MotionEvent.ACTION_DOWN) {
            handleEventActionDown(event)
        }
    }

    private fun handleEventActionDown(event: MotionEvent) {
//        swipeDirection = null
//        wasScaled = false
//        imagesPager.dispatchTouchEvent(event)

        swipeDismissHandler.onTouch(binding.rootContainer, event)
//        isOverlayWasClicked = dispatchOverlayTouch(event)
    }

    private fun handleEventActionUp(event: MotionEvent) {
//        wasDoubleTapped = false
        swipeDismissHandler.onTouch(binding.rootContainer, event)
//        imagesPager.dispatchTouchEvent(event)
//        isOverlayWasClicked = dispatchOverlayTouch(event)
    }

    private fun createSwipeToDismissHandler()
            : SwipeToDismissHandler = SwipeToDismissHandler(
        swipeView = binding.dismissContainer,
        shouldAnimateDismiss = { true },
        onDismiss = { animateClose() },
        onSwipeViewMove = ::handleSwipeViewMove
    )


    private fun createTransitionImageAnimator(transitionImageView: ImageView?) =
        TransitionImageAnimator(
            externalImage = transitionImageView,
            internalImage = binding.transitionImageView,
            internalImageContainer = binding.transitionImageContainer
        )

    private fun animateClose() {
        prepareViewsForTransition()
        binding.dismissContainer.applyMargin(0, 0, 0, 0)

        transitionImageAnimator.animateClose(
            shouldDismissToBottom = true,
            onTransitionStart = { duration ->
                binding.backgroundView.animateAlpha(binding.backgroundView.alpha, 0f, duration)
//                binding.overlayView?.animateAlpha(overlayView?.alpha, 0f, duration)
            },
            onTransitionEnd = { dialog?.dismiss() })
    }

    private fun prepareViewsForTransition() {
        binding.transitionImageContainer.makeVisible()
//        imagesPager.makeGone()
    }

    private fun handleSwipeViewMove(translationY: Float, translationLimit: Int) {
        val alpha = calculateTranslationAlpha(translationY, translationLimit)
        binding.backgroundView.alpha = alpha
//        binding.overlayView?.alpha = alpha
    }

    private fun calculateTranslationAlpha(translationY: Float, translationLimit: Int): Float =
        1.0f - 1.0f / translationLimit.toFloat() / 4f * Math.abs(translationY)



    companion object {
        fun newInstance(url: String?): MediaViewerDialog {
            val fragment = MediaViewerDialog()
            fragment.url = url ?: ""
            return fragment
        }
    }
}