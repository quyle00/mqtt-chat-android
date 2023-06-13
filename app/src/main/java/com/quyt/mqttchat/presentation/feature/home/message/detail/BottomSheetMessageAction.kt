package com.quyt.mqttchat.presentation.feature.home.message.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.quyt.mqttchat.databinding.BottomSheetMessageActionBinding

class BottomSheetMessageAction : BottomSheetDialogFragment() {

    private lateinit var binding: BottomSheetMessageActionBinding
    fun action(): LiveData<Action> = action
    private val action: MutableLiveData<Action> = MutableLiveData()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomSheetMessageActionBinding.inflate(inflater, container, false)
        handleAction()
        return binding.root
    }

    private fun handleAction() {
        binding.apply {
            llReply.setOnClickListener {
                dismiss()
                action.postValue(Action.REPLY)
            }
            llCopy.setOnClickListener {
                dismiss()
                action.postValue(Action.COPY)
            }
            llEdit.setOnClickListener {
                dismiss()
                action.postValue(Action.EDIT)
            }
            llDelete.setOnClickListener {
                dismiss()
                action.postValue(Action.DELETE)
            }
        }
    }

    enum class Action {
        REPLY, COPY, EDIT, DELETE
    }
}