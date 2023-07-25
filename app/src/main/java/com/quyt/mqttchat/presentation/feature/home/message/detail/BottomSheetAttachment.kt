package com.quyt.mqttchat.presentation.feature.home.message.detail

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.animation.addListener
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.PictureResult
import com.otaliastudios.cameraview.VideoResult
import com.otaliastudios.cameraview.controls.Mode
import com.quyt.mqttchat.R
import com.quyt.mqttchat.databinding.BottomSheetAttachBinding
import com.quyt.mqttchat.databinding.BottomSheetAttachBindingImpl
import com.quyt.mqttchat.databinding.ItemImagePickerBinding
import com.quyt.mqttchat.databinding.LayoutCameraBinding
import com.quyt.mqttchat.presentation.adapter.ImagePickerAdapter
import com.quyt.mqttchat.presentation.adapter.MediaModel
import com.quyt.mqttchat.presentation.adapter.MediaType
import com.quyt.mqttchat.presentation.adapter.OnImagePickerListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

interface OnBottomSheetListener {
    fun onDataSelected(data: ArrayList<String>)
}

class BottomSheetAttach(private val listener: OnBottomSheetListener) : BottomSheetDialogFragment(),
    LoaderManager.LoaderCallbacks<Cursor>, OnImagePickerListener, OnDialogCameraListener {
    private lateinit var binding: BottomSheetAttachBinding
    private val listMedia = ArrayList<MediaModel>()
    private lateinit var mLoaderManager: LoaderManager
    private lateinit var imagePickerAdapter: ImagePickerAdapter
    private var isFromSetting = false
    private var hasMediaPermissionGranted = false
    private lateinit var cameraBinding: LayoutCameraBinding
    private lateinit var containerLayout: FrameLayout
    private var cameraViewCollapsedX = 0
    private var cameraViewCollapsedY = 0
    private var cameraViewCollapseWidth = 0
    private var cameraViewCollapseHeight = 0
    private var isCameraViewExpanded = false
    private val mediaPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permission ->
        // check is all permission is granted
        hasMediaPermissionGranted = permission.entries.all { it.value }
        val mediaPermission = if (Build.VERSION.SDK_INT >= 33) {
            android.Manifest.permission.READ_MEDIA_IMAGES
        } else {
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        }
        if (!hasMediaPermissionGranted) {
            if (shouldShowRequestPermissionRationale(mediaPermission)) {
                showMediaPermissionRationale()
            } else {
                showSettingDialog()
            }
        } else {
            initLoadManager()
        }
    }
    //
    private var pictureResult: PictureResult? = null
    private var videoPath: String? = null
    private var longClickJob: Job? = null
    private val longClickDuration = 500L
    private var timeActionDown: Long = 0
    private var isVideo = false
    private lateinit var countDownTimer: CountDownTimer

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = BottomSheetAttachBindingImpl.inflate(inflater, container, false)
        initImageRecyclerView()
        checkMediaPermission()
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        bottomSheetDialog.setOnShowListener {
            val dialog = it as BottomSheetDialog
            dialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout
            containerLayout = dialog.findViewById<FrameLayout>(com.google.android.material.R.id.container) as FrameLayout
            containerLayout.addView(stickyBottomActionView(), containerLayout.childCount)
            containerLayout.addView(stickSendButton(), containerLayout.childCount)
        }

        return bottomSheetDialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bottomSheetBehavior = BottomSheetBehavior.from(binding.root.parent as View)
        bottomSheetBehavior.peekHeight = 1000
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {}
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                val linearLayoutManager = binding.rvImage.layoutManager as LinearLayoutManager
                val cameraItemView = linearLayoutManager.findViewByPosition(0)
                if (cameraItemView != null) {
                    cameraBinding.root.y = getViewYOnScreen(view).toFloat() + getStatusBarHeight(requireContext())
                }
            }
        })
        dialog?.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP && isCameraViewExpanded) {
                if (pictureResult != null || videoPath != null){
                    pictureResult = null
                    videoPath = null
                    updateView(isVideo = isVideo)
                }else{
                    collapseCameraAnimation()
                }
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }
        binding.rlSend.setOnClickListener {
            listener.onDataSelected(imagePickerAdapter.getSelectedMediaUri())
            dismiss()
        }
    }

    override fun onResume() {
        super.onResume()
        if (isFromSetting) {
            isFromSetting = false
            checkMediaPermission()
        }
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        val uri = MediaStore.Files.getContentUri("external") // Lấy URI chung cho cả ảnh và video
        val projection = arrayOf(
            MediaStore.MediaColumns.DATA,
            MediaStore.Files.FileColumns.MEDIA_TYPE,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME
        )
        val selection = "${MediaStore.Files.FileColumns.MEDIA_TYPE}=? OR ${MediaStore.Files.FileColumns.MEDIA_TYPE}=?" // Tiêu chí tìm kiếm
        val selectionArgs = arrayOf(
            MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(), // Media type cho ảnh
            MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString() // Media type cho video
        )
        val sortOrder = "${MediaStore.MediaColumns.DATE_MODIFIED} DESC"

        return CursorLoader(
            requireActivity(),
            uri,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
        listMedia.add(MediaModel("", MediaType.IMAGE))
        data?.let {
            val columnIndexData = it.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
            val columnIndexMediaType = it.getColumnIndexOrThrow(
                MediaStore.Files.FileColumns.MEDIA_TYPE
            )
            while (it.moveToNext()) {
                val mediaType = it.getInt(columnIndexMediaType)
                val mediaData = it.getString(columnIndexData)
                if (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE) {
                    listMedia.add(MediaModel(mediaData, MediaType.IMAGE))
                } else if (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) {
                    listMedia.add(MediaModel(mediaData, MediaType.VIDEO))
                }
            }
            imagePickerAdapter.setItems(listMedia)
        }
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
    }

    override fun onMediaSelected(mediaSelected: ArrayList<String>) {
        binding.rlSend.visibility = if (mediaSelected.size > 0) View.VISIBLE else View.GONE
        binding.tvSelectedCount.text = mediaSelected.size.toString()
    }


    override fun onSendImage(data: ArrayList<String>) {
        listener.onDataSelected(data)
        dismiss()
    }

    private fun checkMediaPermission() {
        if (Build.VERSION.SDK_INT >= 33) {
            hasMediaPermissionGranted = requireActivity().checkSelfPermission(android.Manifest.permission.READ_MEDIA_IMAGES) == android.content.pm.PackageManager.PERMISSION_GRANTED
            if (!hasMediaPermissionGranted) {
                mediaPermissionLauncher.launch(
                    arrayOf(
                        android.Manifest.permission.READ_MEDIA_IMAGES,
                        android.Manifest.permission.READ_MEDIA_VIDEO
                    )
                )
            } else {
                initLoadManager()
            }
        } else {
            hasMediaPermissionGranted = requireActivity().checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == android.content.pm.PackageManager.PERMISSION_GRANTED
            if (!hasMediaPermissionGranted) {
                mediaPermissionLauncher.launch(
                    arrayOf(
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    )
                )
            } else {
                initLoadManager()
            }
        }
    }

    private fun showSettingDialog() {
        MaterialAlertDialogBuilder(requireContext(), com.google.android.material.R.style.MaterialAlertDialog_Material3)
            .setTitle("Media Permission")
            .setMessage("Media permission is required to show media files, Please allow media permission from setting. if you deny the media permission, you will not be able to send media files")
            .setPositiveButton("Go to settings") { _, _ ->
                isFromSetting = true
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.parse("package:${requireActivity().packageName}")
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showMediaPermissionRationale() {

        MaterialAlertDialogBuilder(requireContext(), com.google.android.material.R.style.MaterialAlertDialog_Material3).setTitle("Alert")
            .setMessage("Media permission is required to show media files")
            .setPositiveButton("Ok") { _, _ ->
                if (Build.VERSION.SDK_INT >= 33) {
                    mediaPermissionLauncher.launch(
                        arrayOf(
                            android.Manifest.permission.READ_MEDIA_IMAGES,
                            android.Manifest.permission.READ_MEDIA_VIDEO
                        )
                    )
                } else {
                    mediaPermissionLauncher.launch(
                        arrayOf(
                            android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        )
                    )
                }
            }.setNegativeButton("Cancel", null).show()
    }

    private fun stickSendButton(): View {
        val sendImageButton = binding.rlSend
        val sendImageButtonParent = sendImageButton.parent as ViewGroup
        sendImageButton.layoutParams = FrameLayout.LayoutParams(
            sendImageButton.layoutParams.width,
            sendImageButton.layoutParams.height
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.END
            bottomMargin = 120
            marginEnd = 20
        }
        sendImageButtonParent.removeView(sendImageButton)
        return sendImageButton
    }

    private fun stickyBottomActionView(): View {
        val actionView = binding.llType
        val actionViewParent = actionView.parent as ViewGroup
        actionView.layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.BOTTOM
        }
        actionViewParent.removeView(actionView)
        return actionView
    }

    private fun initLoadManager() {
        mLoaderManager = LoaderManager.getInstance(this)
        mLoaderManager.initLoader(1, null, this)
    }

    private fun initImageRecyclerView() {
        imagePickerAdapter = ImagePickerAdapter(this)
        binding.rvImage.adapter = imagePickerAdapter
        binding.rvImage.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.rvImage.addItemDecoration(GridSpacingItemDecoration(3, 10, true))
        (binding.rvImage.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        // Set up for camera view
        binding.rvImage.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager
                if (linearLayoutManager.findFirstCompletelyVisibleItemPosition() == 0 && !::cameraBinding.isInitialized) {
                    val viewHolder = (binding.rvImage.findViewHolderForAdapterPosition(0) as ImagePickerAdapter.ImagePickerViewHolder)
                    Handler().postDelayed({
                        setupCameraViewItem(viewHolder.binding)
                    }, 10)
                }
            }
        })
    }

    private fun getViewXOnScreen(view: View): Int {
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
    private fun setupCameraViewItem(itemBinding: ItemImagePickerBinding) {
        cameraBinding = LayoutCameraBinding.inflate(LayoutInflater.from(requireContext()))
        cameraBinding.cameraView.setLifecycleOwner(viewLifecycleOwner)
        cameraBinding.root.x = getViewXOnScreen(itemBinding.root).toFloat()
        cameraBinding.root.y = getViewYOnScreen(itemBinding.root).toFloat()
        cameraBinding.root.layoutParams = FrameLayout.LayoutParams(
            itemBinding.root.width,
            itemBinding.root.height
        )
        containerLayout.addView(cameraBinding.root)
        cameraBinding.vCamera.setOnClickListener {
            cameraViewCollapsedX = cameraBinding.root.x.toInt()
            cameraViewCollapsedY = cameraBinding.root.y.toInt()
            cameraViewCollapseWidth = cameraBinding.root.width
            cameraViewCollapseHeight = cameraBinding.root.height
            isCameraViewExpanded = true
            expandCameraAnimation()
        }
        setupTimer()
        cameraBinding.vCapture.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    //
                    cameraBinding.cvType.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
                    cameraBinding.cvType.visibility = View.VISIBLE
                    //
                    timeActionDown = System.currentTimeMillis()
                    longClickJob = CoroutineScope(Dispatchers.Main).launch {
                        delay(longClickDuration)
                        if (longClickJob?.isActive == true) {
                            // Start recording
                            cameraBinding.cvType.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.red))
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
                    cameraBinding.cvType.visibility = View.GONE
                    cameraBinding.cameraView.stopVideo()
                    longClickJob?.cancel()
                }
            }
            true
        }
        cameraBinding.cameraView.addCameraListener(object : CameraListener() {
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
                cameraBinding.cvDuration.visibility = View.VISIBLE
                countDownTimer.start()
            }
        })
        cameraBinding.cvSend.setOnClickListener {
            sendData()
        }
        cameraBinding.ivClose.setOnClickListener {
            pictureResult = null
            videoPath = null
            updateView(isVideo = isVideo)
        }
    }

    private fun setupTimer(){
        countDownTimer = object : CountDownTimer(100000, 1000) {
            var count = 0
            override fun onTick(millisUntilFinished: Long) {
                count++
                // Text with format 00:00
                cameraBinding.tvDuration.text =   String.format("%02d:%02d", count / 60, count % 60)
            }

            override fun onFinish() {
                count = 0
            }
        }
    }

    private fun sendData() {
        if (isVideo) {
            videoPath?.let {
                val uriData = ArrayList<String>()
                uriData.add(it)
                listener.onDataSelected(uriData)
                dismiss()
            }
        } else {
            pictureResult?.let {
                val imageFile = File(requireContext().cacheDir, "image-${System.currentTimeMillis()}.jpg")
                pictureResult?.toFile(imageFile) {
                    val uriData = ArrayList<String>()
                    uriData.add(imageFile.absolutePath)
                    listener.onDataSelected(uriData)
                    dismiss()
                }
            }
        }
    }
    private fun takePicture() {
        cameraBinding.cameraView.mode = Mode.PICTURE
        isVideo = false
        cameraBinding.cameraView.takePictureSnapshot()
    }

    private fun pictureTaken(result: PictureResult) {
        pictureResult = result
        Glide.with(requireContext())
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
            .into(cameraBinding.ivImage)
    }

    private fun videoTaken(result: VideoResult) {
        updateView(false, isVideo = true)
        //
        videoPath = result.file.absolutePath
        cameraBinding.videoView.setVideoPath(videoPath)
        cameraBinding.videoView.setOnPreparedListener {
            cameraBinding.videoView.start()
        }
    }

    private fun startRecordVideo() {
        cameraBinding.cameraView.mode = Mode.VIDEO
        isVideo = true
        val videoFile = File(requireContext().cacheDir, "video-${System.currentTimeMillis()}.mp4")
        lifecycleScope.launch {
            delay(100)
            cameraBinding.cameraView.takeVideoSnapshot(videoFile)
        }
    }

    private fun updateView(isCapturing: Boolean = true, isVideo: Boolean = false) {
        if (isCapturing) {
            cameraBinding.ivClose.visibility = View.GONE
            cameraBinding.cvSend.visibility = View.GONE
            cameraBinding.vCapture.visibility = View.VISIBLE
            cameraBinding.cameraView.visibility = View.VISIBLE
            if (isVideo) {
                cameraBinding.videoView.visibility = View.GONE
            } else {
                cameraBinding.ivImage.visibility = View.GONE
            }
        } else {
            cameraBinding.ivClose.visibility = View.VISIBLE
            cameraBinding.cvSend.visibility = View.VISIBLE
            cameraBinding.vCapture.visibility = View.GONE
            cameraBinding.cameraView.visibility = View.GONE
            if (isVideo) {
                cameraBinding.cvDuration.visibility = View.GONE
                countDownTimer.cancel()
                countDownTimer.onFinish()
                cameraBinding.videoView.visibility = View.VISIBLE
            } else {
                cameraBinding.ivImage.visibility = View.VISIBLE
            }
        }
    }

    private fun expandCameraAnimation(){
        val screenWidth = resources.displayMetrics.widthPixels
        val screenHeight = resources.displayMetrics.heightPixels + getStatusBarHeight(requireContext())
        // Expand camera view to full screen with autoTransition
        val widthAnimator = ValueAnimator.ofInt(cameraBinding.root.width, screenWidth)
        widthAnimator.addUpdateListener { animation ->
            cameraBinding.root.layoutParams.width = animation.animatedValue as Int
            cameraBinding.root.requestLayout()
        }
        //
        val heightAnimator = ValueAnimator.ofInt(cameraBinding.root.height, screenHeight)
        heightAnimator.addUpdateListener { animation ->
            cameraBinding.root.layoutParams.height = animation.animatedValue as Int
            cameraBinding.root.requestLayout()
        }
        //
        val moveXAnimator = ValueAnimator.ofInt(cameraBinding.root.x.toInt(), 0)
        moveXAnimator.addUpdateListener { animation ->
            cameraBinding.root.x = (animation.animatedValue as Int).toFloat()
            cameraBinding.root.requestLayout()
        }
        //
        val moveYAnimator = ValueAnimator.ofInt(cameraBinding.root.y.toInt(), 0)
        moveYAnimator.addUpdateListener { animation ->
            cameraBinding.root.y = (animation.animatedValue as Int).toFloat()
            cameraBinding.root.requestLayout()
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
        val widthAnimator = ValueAnimator.ofInt(cameraBinding.root.width, cameraViewCollapseWidth)
        widthAnimator.addUpdateListener { animation ->
            cameraBinding.root.layoutParams.width = animation.animatedValue as Int
            cameraBinding.root.requestLayout()
        }
        //
        val heightAnimator = ValueAnimator.ofInt(cameraBinding.root.height, cameraViewCollapseHeight)
        heightAnimator.addUpdateListener { animation ->
            cameraBinding.root.layoutParams.height = animation.animatedValue as Int
            cameraBinding.root.requestLayout()
        }
        //
        val moveXAnimator = ValueAnimator.ofInt(cameraBinding.root.x.toInt(), cameraViewCollapsedX)
        moveXAnimator.addUpdateListener { animation ->
            cameraBinding.root.x = (animation.animatedValue as Int).toFloat()
            cameraBinding.root.requestLayout()
        }
        //
        val moveYAnimator = ValueAnimator.ofInt(cameraBinding.root.y.toInt(), cameraViewCollapsedY)
        moveYAnimator.addUpdateListener { animation ->
            cameraBinding.root.y = (animation.animatedValue as Int).toFloat()
            cameraBinding.root.requestLayout()
        }
        //
        val animatorSet = AnimatorSet()
        animatorSet.interpolator = DecelerateInterpolator()
        animatorSet.duration = 300
        animatorSet.playTogether(widthAnimator, heightAnimator, moveXAnimator, moveYAnimator)
        animatorSet.start()
    }

    private fun showCameraAction() {
        cameraBinding.rlCapture.visibility = View.VISIBLE
        cameraBinding.ivCamera.visibility = View.GONE
        cameraBinding.vCamera.visibility = View.GONE
    }

    private fun hideCameraAction() {
        cameraBinding.rlCapture.visibility = View.GONE
        cameraBinding.ivCamera.visibility = View.VISIBLE
        cameraBinding.vCamera.visibility = View.VISIBLE
    }

}
