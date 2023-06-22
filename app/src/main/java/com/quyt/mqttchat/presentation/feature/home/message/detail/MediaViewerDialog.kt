package com.quyt.mqttchat.presentation.feature.home.message.detail

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.bumptech.glide.Glide
import com.quyt.mqttchat.R
import com.quyt.mqttchat.databinding.DialogMediaViewerBinding
import com.quyt.mqttchat.presentation.extensions.addListener
import com.quyt.mqttchat.presentation.extensions.applyMargin
import com.quyt.mqttchat.presentation.extensions.globalVisibleRect
import com.quyt.mqttchat.presentation.extensions.isRectVisible
import com.quyt.mqttchat.presentation.extensions.localVisibleRect
import com.quyt.mqttchat.presentation.extensions.makeViewMatchParent
import com.quyt.mqttchat.presentation.extensions.postApply
import com.quyt.mqttchat.presentation.extensions.postDelayed
import com.quyt.mqttchat.presentation.extensions.requestNewSize
import kotlin.math.abs


class MediaViewerDialog : DialogFragment() {

    private lateinit var binding: DialogMediaViewerBinding
    private lateinit var url: String
    private var transitionView: ImageView? = null
    private var translationLimit = 300
    private var isTracking = false
    private var startY: Float = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        dialog?.window?.setBackgroundDrawableResource(R.color.transparent)
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_media_viewer, container, true)
        binding.transitionImageView.drawable?.let {
            binding.transitionImageView.setImageBitmap(it.toBitmap())
        }
        dialog?.setOnShowListener {
            transitionView?.let {
                if (transitionView.isRectVisible) {
                    with(transitionView.localVisibleRect) {
                        binding.transitionImageView.requestNewSize(it.width, it.height)
                        binding.transitionImageView.applyMargin(top = -top, start = -left)
                    }
                    with(transitionView.globalVisibleRect) {
                        binding.transitionImageContainer.requestNewSize(width(), height())
                        binding.transitionImageContainer.applyMargin(left, top, right, bottom)
                    }
                }

                // resetRootTranslation()
                val internalRoot = binding.transitionImageContainer.parent as ViewGroup
                internalRoot.animate()
                    .translationY(0f)
                    .setDuration(300)
                    .start()
                //
                internalRoot.postApply {
                    //ain't nothing but a kludge to prevent blinking when transition is starting
                    transitionView?.postDelayed(50) { visibility = View.INVISIBLE }

                    TransitionManager.beginDelayedTransition(
                        internalRoot,
                        AutoTransition()
                            .setDuration(300)
                            .setInterpolator(DecelerateInterpolator())
                            .addListener(onTransitionEnd = { })
                    )

                    binding.transitionImageContainer.makeViewMatchParent()
                    binding.transitionImageView.makeViewMatchParent()

                    binding.transitionImageContainer.requestLayout()
                }
            }
        }
        loadMedia()
        handleSwipeToDismiss()
        return binding.root
    }

    private fun loadMedia() {
        if (getFileExtensionFromUrl(url) == "mp4") {
            binding.transitionImageView.visibility = View.GONE
            binding.vvVideo.visibility = View.VISIBLE
            binding.vvVideo.setVideoPath(url)
            binding.vvVideo.start()
        } else {
            binding.transitionImageView.visibility = View.VISIBLE
            binding.vvVideo.visibility = View.GONE
            Glide.with(binding.root.context)
                .load(url)
                .error(
                    ContextCompat.getDrawable(
                        binding.root.context,
                        R.drawable.ic_launcher_background
                    )
                )
                .into(binding.transitionImageView)
        }
    }

    private fun getFileExtensionFromUrl(url: String): String {
        val fileUrl = url.substringAfterLast("/")
        val fileExtension = fileUrl.substringAfterLast(".", "")
        return fileExtension.ifEmpty { "" }
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun handleSwipeToDismiss() {
        binding.rootContainer.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    val hitRect = Rect().also { binding.dismissContainer.getHitRect(it) }
                    if (hitRect.contains(event.x.toInt(), event.y.toInt())) {
                        isTracking = true
                    }
                    startY = event.y
                    return@setOnTouchListener true
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (isTracking) {
                        isTracking = false
                        onTrackingEnd(view.height)
                    }
                    return@setOnTouchListener true
                }

                MotionEvent.ACTION_MOVE -> {
                    if (isTracking) {
                        val translationY = event.y - startY
                        binding.dismissContainer.translationY = translationY
                        handleSwipeViewMove(translationY, translationLimit)
                    }
                    return@setOnTouchListener true
                }

                else -> {
                    return@setOnTouchListener false
                }
            }
        }
    }

    private fun onTrackingEnd(parentHeight: Int) {
        val animateTo = when {
            binding.dismissContainer.translationY < -translationLimit -> -parentHeight.toFloat()
            binding.dismissContainer.translationY > translationLimit -> parentHeight.toFloat()
            else -> 0f
        }
        animateTranslation(animateTo)
    }

    private fun animateTranslation(translationTo: Float) {
        binding.dismissContainer.animate()
            .translationY(translationTo)
            .setDuration(200L)
            .setInterpolator(AccelerateInterpolator())
            .setUpdateListener {
                handleSwipeViewMove(binding.dismissContainer.translationY, translationLimit)
            }
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    if (translationTo != 0f) {
                        dialog?.dismiss()
                    }
                    //remove the update listener, otherwise it will be saved on the next animation execution:
                    binding.dismissContainer.animate().setUpdateListener(null)
                }
            })
            .start()
    }

    private fun handleSwipeViewMove(translationY: Float, translationLimit: Int) {
        Log.d("DebugTAG", "TranslationY: $translationY")
        Log.d("DebugTAG", "TranslationLimit: $translationLimit")
        val alpha = calculateTranslationAlpha(translationY, translationLimit)
        Log.d("DebugTAG", alpha.toString())
        binding.backgroundView.alpha = alpha
    }

    private fun calculateTranslationAlpha(translationY: Float, translationLimit: Int): Float {
        return 1.0f - 1.0f / translationLimit.toFloat() / 2f * abs(translationY)
    }

    companion object {
        fun newInstance(url: String?, transitionView: ImageView): MediaViewerDialog {
            val fragment = MediaViewerDialog()
            fragment.url = url ?: ""
            fragment.transitionView = transitionView
            return fragment
        }
    }
}