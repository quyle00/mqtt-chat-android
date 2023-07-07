package com.quyt.mqttchat.presentation.feature.home.message.detail

import android.app.Dialog
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.otaliastudios.cameraview.CameraView
import com.quyt.mqttchat.databinding.BottomSheetAttachBinding
import com.quyt.mqttchat.databinding.BottomSheetAttachBindingImpl
import com.quyt.mqttchat.databinding.ItemCameraBinding
import com.quyt.mqttchat.presentation.adapter.ImagePickerAdapter
import com.quyt.mqttchat.presentation.adapter.MediaModel
import com.quyt.mqttchat.presentation.adapter.MediaType
import com.quyt.mqttchat.presentation.adapter.OnImagePickerListener

interface OnBottomSheetListener {
    fun onDataSelected(data: ArrayList<String>)
}

class BottomSheetAttach(private val listener: OnBottomSheetListener) : BottomSheetDialogFragment(),
    LoaderManager.LoaderCallbacks<Cursor>, OnImagePickerListener, OnDialogCameraListener{
    private lateinit var binding: BottomSheetAttachBinding
    private val listMedia = ArrayList<MediaModel>()
    private lateinit var mLoaderManager: LoaderManager
    private lateinit var imagePickerAdapter: ImagePickerAdapter
    private var isFromSetting = false
    private var hasMediaPermissionGranted = false
    private lateinit var cameraView : CameraView
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


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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
            val containerLayout: FrameLayout = dialog.findViewById<FrameLayout>(
                com.google.android.material.R.id.container
            ) as FrameLayout
            containerLayout.addView(stickyBottomActionView(), containerLayout.childCount)
            containerLayout.addView(stickSendButton(), containerLayout.childCount)
        }
        return bottomSheetDialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bottomSheetBehavior = BottomSheetBehavior.from(binding.root.parent as View)
        bottomSheetBehavior.peekHeight = 1000
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
        MaterialAlertDialogBuilder(requireContext(), R.style.MaterialAlertDialog_Material3)
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

        MaterialAlertDialogBuilder(requireContext(), R.style.MaterialAlertDialog_Material3).setTitle("Alert")
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
                if (linearLayoutManager.findFirstCompletelyVisibleItemPosition() == 0) {
                    val cameraItemViewHolder = (binding.rvImage.findViewHolderForAdapterPosition(0) as ImagePickerAdapter.CameraViewHolder)
                    setupCameraViewItem(cameraItemViewHolder.binding)
                }
            }
        })
    }

    private fun setupCameraViewItem(itemBinding: ItemCameraBinding) {
        cameraView = itemBinding.cameraView
        itemBinding.cameraView.setLifecycleOwner(this)
        itemBinding.vCamera.setOnClickListener {
            cameraView.close()
            val cameraDialogFragment = CameraDialogFragment(this)
            cameraDialogFragment.show(childFragmentManager, "CameraDialogFragment")
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
        listMedia.add(MediaModel("", MediaType.CAMERA))
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

}
