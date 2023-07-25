package com.quyt.mqttchat.presentation.feature.home.message.detail

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.CountDownTimer
import android.os.Handler
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.PictureResult
import com.otaliastudios.cameraview.VideoResult
import com.otaliastudios.cameraview.controls.Mode
import com.quyt.mqttchat.R
import com.quyt.mqttchat.databinding.ItemImagePickerBinding
import com.quyt.mqttchat.databinding.LayoutCameraBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class CustomCameraView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null, 
    defStyleAttr: Int = 0
)  : RelativeLayout(context, attrs, defStyleAttr) {
    
    lateinit var binding : LayoutCameraBinding

    private var cameraViewCollapsedX = 0
    private var cameraViewCollapsedY = 0
    private var cameraViewCollapseWidth = 0
    private var cameraViewCollapseHeight = 0
    private var isCameraViewExpanded = false
    //
    private var pictureResult: PictureResult? = null
    private var videoPath: String? = null
    private var longClickJob: Job? = null
    private val longClickDuration = 500L
    private var timeActionDown: Long = 0
    private var isVideo = false
    private lateinit var countDownTimer: CountDownTimer

    init {
        initView()
    }

    private fun initView() {
        binding = LayoutCameraBinding.inflate(LayoutInflater.from(context), null, false)
        setupCameraViewItem()
        binding.root.setOnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                Toast.makeText(context, "Back pressed", Toast.LENGTH_SHORT).show()
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun setupCameraViewItem() {
        binding.cameraView.open()
        binding.vCamera.setOnClickListener {
            cameraViewCollapsedX = binding.root.x.toInt()
            cameraViewCollapsedY = binding.root.y.toInt()
            cameraViewCollapseWidth = binding.root.width
            cameraViewCollapseHeight = binding.root.height
            isCameraViewExpanded = true
            expandCameraAnimation()
        }
        //
        binding.vCapture.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    //
                    binding.cvType.setCardBackgroundColor(ContextCompat.getColor(context, R.color.white))
                    binding.cvType.visibility = View.VISIBLE
                    //
                    timeActionDown = System.currentTimeMillis()
                    longClickJob = CoroutineScope(Dispatchers.Main).launch {
                        delay(longClickDuration)
                        if (longClickJob?.isActive == true) {
                            // Start recording
                            binding.cvType.setCardBackgroundColor(ContextCompat.getColor(context, R.color.red))
                            startRecordVideo()
                        }
                    }
                }

                MotionEvent.ACTION_UP -> {
                    // User tapped
                    if (System.currentTimeMillis() - timeActionDown < 100) {
                        takePicture()
                    }
                    // Stop recording
                    binding.cvType.visibility = View.GONE
                    binding.cameraView.stopVideo()
                    longClickJob?.cancel()
                }
            }
            true
        }

        binding.cameraView.addCameraListener(object : CameraListener() {
            override fun onPictureTaken(result: PictureResult) {
                super.onPictureTaken(result)
                pictureTaken(result)
            }

            override fun onVideoTaken(result: VideoResult) {
                super.onVideoTaken(result)
                videoTaken(result)
            }

            override fun onVideoRecordingStart() {
                super.onVideoRecordingStart()
                binding.cvDuration.visibility = View.VISIBLE
                countDownTimer.start()
            }
        })

        binding.cvSend.setOnClickListener {
            sendData()
        }
        binding.ivClose.setOnClickListener {
            pictureResult = null
            videoPath = null
            updateView(isVideo = isVideo)
        }
    }

    private fun sendData() {
        if (isVideo) {
            videoPath?.let {
                val uriData = ArrayList<String>()
                uriData.add(it)
//                listener.onDataSelected(uriData)
//                dismiss()
            }
        } else {
            pictureResult?.let {
                val imageFile = File(context.cacheDir, "image-${System.currentTimeMillis()}.jpg")
                pictureResult?.toFile(imageFile) {
                    val uriData = ArrayList<String>()
                    uriData.add(imageFile.absolutePath)
//                    listener.onDataSelected(uriData)
//                    dismiss()
                }
            }
        }
    }
    private fun takePicture() {
        binding.cameraView.mode = Mode.PICTURE
        isVideo = false
        binding.cameraView.takePictureSnapshot()
    }

    private fun pictureTaken(result: PictureResult) {
        pictureResult = result
        Glide.with(context)
            .load(result.data)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                    return false
                }

                override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                    updateView(false)
                    return false
                }
            })
            .into(binding.ivImage)
    }

    private fun videoTaken(result: VideoResult) {
        updateView(false, isVideo = true)
        //
        videoPath = result.file.absolutePath
        binding.videoView.setVideoPath(videoPath)
        binding.videoView.setOnPreparedListener {
            binding.videoView.start()
        }
    }

    private fun startRecordVideo() {
        binding.cameraView.mode = Mode.VIDEO
        isVideo = true
        val videoFile = File(context.cacheDir, "video-${System.currentTimeMillis()}.mp4")
        Handler().postDelayed({
            binding.cameraView.takeVideoSnapshot(videoFile)
        }, 100)
    }

    private fun updateView(isCapturing: Boolean = true, isVideo: Boolean = false) {
        if (isCapturing) {
            binding.ivClose.visibility = View.GONE
            binding.cvSend.visibility = View.GONE
            binding.vCapture.visibility = View.VISIBLE
            binding.cameraView.visibility = View.VISIBLE
            if (isVideo) {
                binding.videoView.visibility = View.GONE
            } else {
                binding.ivImage.visibility = View.GONE
            }
        } else {
            binding.ivClose.visibility = View.VISIBLE
            binding.cvSend.visibility = View.VISIBLE
            binding.vCapture.visibility = View.GONE
            binding.cameraView.visibility = View.GONE
            if (isVideo) {
                binding.cvDuration.visibility = View.GONE
                countDownTimer.cancel()
                countDownTimer.onFinish()
                binding.videoView.visibility = View.VISIBLE
            } else {
                binding.ivImage.visibility = View.VISIBLE
            }
        }
    }

    private fun expandCameraAnimation(){
        val screenWidth = resources.displayMetrics.widthPixels
        val screenHeight = resources.displayMetrics.heightPixels + getStatusBarHeight(context)
        // Expand camera view to full screen with autoTransition
        val widthAnimator = ValueAnimator.ofInt(binding.root.width, screenWidth)
        widthAnimator.addUpdateListener { animation ->
            binding.root.layoutParams.width = animation.animatedValue as Int
            binding.root.requestLayout()
        }
        //
        val heightAnimator = ValueAnimator.ofInt(binding.root.height, screenHeight)
        heightAnimator.addUpdateListener { animation ->
            binding.root.layoutParams.height = animation.animatedValue as Int
            binding.root.requestLayout()
        }
        //
        val moveXAnimator = ValueAnimator.ofInt(binding.root.x.toInt(), 0)
        moveXAnimator.addUpdateListener { animation ->
            binding.root.x = (animation.animatedValue as Int).toFloat()
            binding.root.requestLayout()
        }
        //
        val moveYAnimator = ValueAnimator.ofInt(binding.root.y.toInt(), 0)
        moveYAnimator.addUpdateListener { animation ->
            binding.root.y = (animation.animatedValue as Int).toFloat()
            binding.root.requestLayout()
        }
        //
        val animatorSet = AnimatorSet()
        animatorSet.interpolator = DecelerateInterpolator()
        animatorSet.duration = 300
        animatorSet.doOnEnd {
            showCameraAction()
        }
        animatorSet.playTogether(widthAnimator, heightAnimator, moveXAnimator, moveYAnimator)
        animatorSet.start()
    }

    private fun collapseCameraAnimation(){
        hideCameraAction()
        val widthAnimator = ValueAnimator.ofInt(binding.root.width, cameraViewCollapseWidth)
        widthAnimator.addUpdateListener { animation ->
            binding.root.layoutParams.width = animation.animatedValue as Int
            binding.root.requestLayout()
        }
        //
        val heightAnimator = ValueAnimator.ofInt(binding.root.height, cameraViewCollapseHeight)
        heightAnimator.addUpdateListener { animation ->
            binding.root.layoutParams.height = animation.animatedValue as Int
            binding.root.requestLayout()
        }
        //
        val moveXAnimator = ValueAnimator.ofInt(binding.root.x.toInt(), cameraViewCollapsedX)
        moveXAnimator.addUpdateListener { animation ->
            binding.root.x = (animation.animatedValue as Int).toFloat()
            binding.root.requestLayout()
        }
        //
        val moveYAnimator = ValueAnimator.ofInt(binding.root.y.toInt(), cameraViewCollapsedY)
        moveYAnimator.addUpdateListener { animation ->
            binding.root.y = (animation.animatedValue as Int).toFloat()
            binding.root.requestLayout()
        }
        //
        val animatorSet = AnimatorSet()
        animatorSet.interpolator = DecelerateInterpolator()
        animatorSet.duration = 300
        animatorSet.playTogether(widthAnimator, heightAnimator, moveXAnimator, moveYAnimator)
        animatorSet.start()
    }

    private fun showCameraAction() {
        binding.rlCapture.visibility = View.VISIBLE
        binding.ivCamera.visibility = View.GONE
        binding.vCamera.visibility = View.GONE
    }

    private fun hideCameraAction() {
        binding.rlCapture.visibility = View.GONE
        binding.ivCamera.visibility = View.VISIBLE
        binding.vCamera.visibility = View.VISIBLE
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

}