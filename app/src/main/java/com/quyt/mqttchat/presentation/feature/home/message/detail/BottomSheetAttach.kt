package com.quyt.mqttchat.presentation.feature.home.message.detail

import android.app.Dialog
import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.quyt.mqttchat.databinding.BottomSheetAttachBinding
import com.quyt.mqttchat.databinding.BottomSheetAttachBindingImpl
import com.quyt.mqttchat.presentation.adapter.ImagePickerAdapter
import com.quyt.mqttchat.presentation.adapter.OnImagePickerListener

interface BottomSheetListener {
    fun onDataSelected(data: ArrayList<String>)
}

class BottomSheetAttach(private val listener : BottomSheetListener) : BottomSheetDialogFragment(), LoaderManager.LoaderCallbacks<Cursor>, OnImagePickerListener {
    private lateinit var binding: BottomSheetAttachBinding
    private val listOfAllImages = ArrayList<String>()
    private lateinit var mLoaderManager: LoaderManager
    private lateinit var imagePickerAdapter: ImagePickerAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = BottomSheetAttachBindingImpl.inflate(inflater, container, false)
        initImageRecyclerView()
        initLoadManager()
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        bottomSheetDialog.setOnShowListener {
            val dialog = it as BottomSheetDialog
            dialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout
            val containerLayout: FrameLayout = dialog.findViewById<FrameLayout>(com.google.android.material.R.id.container) as FrameLayout
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
            listener.onDataSelected(imagePickerAdapter.getSelectedImages())
            dismiss()
        }
    }

    override fun onImageSelect(imageSelected: ArrayList<String>) {
        binding.rlSend.visibility = if (imageSelected.size > 0) View.VISIBLE else View.GONE
        binding.tvSelectedCount.text = imageSelected.size.toString()
    }

    private fun stickSendButton(): View {
        val sendImageButton = binding.rlSend
        val sendImageButtonParent = sendImageButton.parent as ViewGroup
        sendImageButton.layoutParams = FrameLayout.LayoutParams(sendImageButton.layoutParams.width, sendImageButton.layoutParams.height).apply {
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
        actionView.layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
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
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
        val selection: String? = null     //Selection criteria
        val selectionArgs = arrayOf<String>()  //Selection criteria
        val sortOrder: String? = null

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
        data?.let {
            val columnIndexData = it.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
            while (it.moveToNext()) {
                listOfAllImages.add(it.getString(columnIndexData));
            }
            imagePickerAdapter.setItems(listOfAllImages)
        }
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {

    }


}