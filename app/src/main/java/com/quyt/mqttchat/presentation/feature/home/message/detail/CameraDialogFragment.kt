package com.quyt.mqttchat.presentation.feature.home.message.detail

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
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
import com.quyt.mqttchat.databinding.DialogCameraBinding
import com.quyt.mqttchat.domain.model.Media
import com.quyt.mqttchat.domain.model.MediaType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File


interface OnDialogCameraListener {
    fun onSendImage(data: ArrayList<Media>)
}


class CameraDialogFragment(private var listener: OnDialogCameraListener) : DialogFragment() {

    private lateinit var binding: DialogCameraBinding
    private var pictureResult: PictureResult? = null
    private var videoPath: String? = null
    private var longClickJob: Job? = null
    private val longClickDuration = 500L
    private var timeActionDown: Long = 0
    private var isVideo = false
    private lateinit var countDownTimer: CountDownTimer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        dialog?.window?.setBackgroundDrawableResource(R.color.transparent)
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_camera, container, true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCameraView()
        binding.ivClose.setOnClickListener {
            pictureResult = null
            videoPath = null
            updateView(isVideo = isVideo)
        }
        binding.cvSend.setOnClickListener {
            sendData()
        }
        setupTimer()
    }

    private fun setupTimer(){
        countDownTimer = object : CountDownTimer(100000, 1000) {
            var count = 0
            override fun onTick(millisUntilFinished: Long) {
                count++
                // Text with format 00:00
                binding.tvDuration.text =   String.format("%02d:%02d", count / 60, count % 60)
            }

            override fun onFinish() {
                count = 0
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupCameraView() {
        binding.cameraView.setLifecycleOwner(this)
        binding.vCapture.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    //
                    binding.cvType.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
                    binding.cvType.visibility = View.VISIBLE
                    //
                    timeActionDown = System.currentTimeMillis()
                    longClickJob = CoroutineScope(Dispatchers.Main).launch {
                        delay(longClickDuration)
                        if (longClickJob?.isActive == true) {
                            // Start recording
                            binding.cvType.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.red))
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
    }

    private fun sendData() {
        if (isVideo) {
            videoPath?.let {
                val uriData = ArrayList<Media>()
                uriData.add(Media(localUri = it, type = MediaType.VIDEO.value))
                listener.onSendImage(uriData)
                dismiss()
            }
        } else {
            pictureResult?.let {
                val imageFile = File(requireContext().cacheDir, "image-${System.currentTimeMillis()}.jpg")
                pictureResult?.toFile(imageFile) {
                    val uriData = ArrayList<Media>()
                    uriData.add(Media(localUri = imageFile.absolutePath, type = MediaType.IMAGE.value))
                    listener.onSendImage(uriData)
                    dismiss()
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
        Glide.with(this@CameraDialogFragment)
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
        Log.d("CAMERADEBUG", "Start record video")
        binding.cameraView.mode = Mode.VIDEO
        isVideo = true
        val videoFile = File(requireContext().cacheDir, "video-${System.currentTimeMillis()}.mp4")
        lifecycleScope.launch {
            delay(100)
            binding.cameraView.takeVideoSnapshot(videoFile)
        }
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


    companion object {
//        fun newInstance(url: String?): CameraDialog {
//            val fragment = CameraDialog()
//            fragment.url = url ?: ""
//            return fragment
//        }
    }
}