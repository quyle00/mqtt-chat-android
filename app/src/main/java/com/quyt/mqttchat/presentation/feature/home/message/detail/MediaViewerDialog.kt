package com.quyt.mqttchat.presentation.feature.home.message.detail

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import androidx.core.animation.addListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.quyt.mqttchat.R
import com.quyt.mqttchat.databinding.DialogMediaViewerBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs


class MediaViewerDialog() : DialogFragment() {

    private lateinit var binding: DialogMediaViewerBinding
    private lateinit var externalImageView: ImageView
    private lateinit var url: String
    private var originalWidth = 0
    private var originalHeight = 0
    private var originalX = 0
    private var originalY = 0
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
        initValue()
        animateOpen()
        handleSwipeToDismiss()
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return object : Dialog(requireContext(), theme) {
            @Deprecated("Deprecated in Java")
            override fun onBackPressed() {
                animateClose()
            }
        }
    }

    private fun loadMedia() {
        if (getFileExtensionFromUrl(url) == "mp4") {
            binding.pbLoading.visibility = View.VISIBLE
            binding.ivInternalImage.visibility = View.GONE
            binding.vvVideo.visibility = View.VISIBLE
            binding.vvVideo.setVideoPath(url)
            binding.vvVideo.setOnPreparedListener {
                binding.pbLoading.visibility = View.GONE
                binding.vvVideo.start()
            }
        }
    }

    private fun getFileExtensionFromUrl(url: String): String {
        val fileUrl = url.substringAfterLast("/")
        val fileExtension = fileUrl.substringAfterLast(".", "")
        return fileExtension.ifEmpty { "" }
    }


    private fun initValue() {
        originalX = getViewXOnScreen(externalImageView)
        originalY = getViewYOnScreen(externalImageView)
        originalWidth = externalImageView.width
        originalHeight = externalImageView.height
    }

    private fun animateOpen() {
        externalImageView.animate().alpha(0f).setDuration(50).start()
        binding.ivInternalImage.x = originalX.toFloat()
        binding.ivInternalImage.y = originalY.toFloat()
        binding.ivInternalImage.layoutParams.let {
            it.width = originalWidth
            it.height = originalHeight
        }
        binding.ivInternalImage.setImageDrawable(externalImageView.drawable)
        //
        val originalWidth = binding.ivInternalImage.layoutParams.width
        val originalHeight = binding.ivInternalImage.layoutParams.height
        //
        val screenWidth = resources.displayMetrics.widthPixels
        val screenHeight = resources.displayMetrics.heightPixels
        //
        val widthAnimator = ValueAnimator.ofInt(originalWidth, screenWidth)
        widthAnimator.addUpdateListener { animation ->
            val layoutParams = binding.ivInternalImage.layoutParams
            layoutParams.width = animation.animatedValue as Int
            layoutParams.height = (originalHeight * layoutParams.width.toFloat() / originalWidth).toInt()
            binding.ivInternalImage.layoutParams = layoutParams
            binding.ivInternalImage.requestLayout()
        }
        //
        val newHeight = (originalHeight * screenWidth.toFloat() / originalWidth).toInt()
        val middlePos = screenHeight / 2 - newHeight / 2
        val moveYAnimator = ValueAnimator.ofInt(binding.ivInternalImage.y.toInt(), middlePos)
        moveYAnimator.addUpdateListener { animation ->
            binding.ivInternalImage.y = (animation.animatedValue as Int).toFloat()
            binding.ivInternalImage.requestLayout()
        }
        //
        val moveXAnimator = ValueAnimator.ofInt(binding.ivInternalImage.x.toInt(), 0)
        moveXAnimator.addUpdateListener { animation ->
            binding.ivInternalImage.x = (animation.animatedValue as Int).toFloat()
            binding.ivInternalImage.requestLayout()
        }
        //
        val alphaAnimator = ValueAnimator.ofFloat(0.3f, 1f)
        alphaAnimator.addUpdateListener { animation ->
            binding.vBg.alpha = animation.animatedValue as Float
            binding.vBg.requestLayout()
        }
        val animatorSet = AnimatorSet()
        animatorSet.interpolator = DecelerateInterpolator()
        animatorSet.duration = 300
        animatorSet.playTogether(widthAnimator, moveXAnimator, moveYAnimator, alphaAnimator)
        animatorSet.addListener(onEnd = {
            loadMedia()
        })
        animatorSet.start()
    }

    private fun animateClose() {
        val widthAnimator = ValueAnimator.ofInt(binding.ivInternalImage.layoutParams.width, originalWidth)
        widthAnimator.addUpdateListener { animation ->
            binding.ivInternalImage.layoutParams.width = animation.animatedValue as Int
            binding.ivInternalImage.requestLayout()
        }
        //
        val heightAnimator = ValueAnimator.ofInt(binding.ivInternalImage.layoutParams.height, originalHeight)
        heightAnimator.addUpdateListener { animation ->
            binding.ivInternalImage.layoutParams.height = animation.animatedValue as Int
            binding.ivInternalImage.requestLayout()
        }
        //
        val moveXAnimator = ValueAnimator.ofInt(binding.ivInternalImage.x.toInt(), originalX)
        moveXAnimator.addUpdateListener { animation ->
            binding.ivInternalImage.x = (animation.animatedValue as Int).toFloat()
            binding.ivInternalImage.requestLayout()
        }
        //
        val moveYAnimator = ValueAnimator.ofInt(binding.ivInternalImage.y.toInt(), (originalY - binding.dismissContainer.y).toInt())
        moveYAnimator.addUpdateListener { animation ->
            binding.ivInternalImage.y = (animation.animatedValue as Int).toFloat()
            binding.ivInternalImage.requestLayout()
        }
        //
        val alphaAnimator = ValueAnimator.ofFloat(binding.vBg.alpha, 0f)
        alphaAnimator.addUpdateListener { animation ->
            binding.vBg.alpha = animation.animatedValue as Float
            binding.vBg.requestLayout()
        }
        val animatorSet = AnimatorSet()
        animatorSet.interpolator = DecelerateInterpolator()
        animatorSet.duration = 300
        animatorSet.playTogether(widthAnimator, heightAnimator, moveXAnimator, moveYAnimator, alphaAnimator)
        animatorSet.addListener(onEnd = {
            lifecycleScope.launch {
                externalImageView.alpha = 1f
                delay(50)
                dismiss()
            }
        })
        animatorSet.start()
    }

    fun getViewXOnScreen(view: View): Int {
        val rect = Rect()
        val coordinates = IntArray(2)
        view.getGlobalVisibleRect(rect)
        view.getLocationOnScreen(coordinates)

        return coordinates[0]
    }

    private fun getViewYOnScreen(view: View): Int {
        val rect = Rect()
        val coordinates = IntArray(2)
        view.getGlobalVisibleRect(rect)
        view.getLocationOnScreen(coordinates)

        val statusBarHeight = getStatusBarHeight(view.context)

        return coordinates[1] - statusBarHeight
    }


    private fun getStatusBarHeight(context: Context): Int {
        val resources = context.resources
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            resources.getDimensionPixelSize(resourceId)
        } else {
            0
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun handleSwipeToDismiss() {
        binding.flContainer.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    val hitRect = Rect().also { binding.flTransitionContainer.getHitRect(it) }
                    if (hitRect.contains(event.x.toInt(), event.y.toInt())) {
                        isTracking = true
                    }
                    startY = event.y
                    return@setOnTouchListener true
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (isTracking) {
                        isTracking = false
                        onTrackingEnd()
                    }
                    return@setOnTouchListener true
                }

                MotionEvent.ACTION_MOVE -> {
                    if (isTracking) {
                        val translationY = event.y - startY
                        binding.dismissContainer.translationY = translationY
                        changeBgAlpha(translationY, translationLimit)
                    }
                    return@setOnTouchListener true
                }

                else -> {
                    return@setOnTouchListener false
                }
            }
        }
    }

    private fun onTrackingEnd() {
        when {
            binding.dismissContainer.translationY < -translationLimit -> {
                val oldY = binding.ivInternalImage.y
                binding.dismissContainer.y = 0f
                binding.ivInternalImage.y = oldY - getViewYOnScreen(binding.ivInternalImage)
                animateClose()
            }

            binding.dismissContainer.translationY > translationLimit -> {
                val oldY = binding.ivInternalImage.y
                binding.dismissContainer.y = 0f
                binding.ivInternalImage.y = oldY + getViewYOnScreen(binding.ivInternalImage)
                animateClose()
            }

            else -> rollBackAnimation()
        }

    }

    private fun rollBackAnimation() {
        binding.dismissContainer.animate()
            .translationY(0f)
            .setDuration(200L)
            .setInterpolator(AccelerateInterpolator())
            .setUpdateListener {
                changeBgAlpha(binding.dismissContainer.translationY, translationLimit)
            }
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    //remove the update listener, otherwise it will be saved on the next animation execution:
                    binding.dismissContainer.animate().setUpdateListener(null)
                }
            })
            .start()
    }

    private fun changeBgAlpha(translationY: Float, translationLimit: Int) {
        val alpha = calculateTranslationAlpha(translationY, translationLimit)
        binding.vBg.alpha = alpha
    }

    private fun calculateTranslationAlpha(translationY: Float, translationLimit: Int): Float {
        return 1.0f - 1.0f / translationLimit.toFloat() / 2f * abs(translationY)
    }


    companion object {
        fun newInstance(imageView: ImageView, url: String): MediaViewerDialog {
            val fragment = MediaViewerDialog()
            fragment.externalImageView = imageView
            fragment.url = url
            return fragment
        }
    }
}