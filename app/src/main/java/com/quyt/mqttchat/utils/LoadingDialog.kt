package com.quyt.mqttchat.utils

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import androidx.databinding.DataBindingUtil
import com.quyt.mqttchat.R
import com.quyt.mqttchat.databinding.DialogLoadingBinding

object LoadingDialog {
    private var loadingDialog : AlertDialog? = null
    fun showLoading(context: Context) {
        if (loadingDialog == null) {
            val builder = AlertDialog.Builder(context)
            val view = DataBindingUtil.inflate<DialogLoadingBinding>(
                LayoutInflater.from(context),
                R.layout.dialog_loading,
                null,
                false
            )
            builder.setView(view.root)
            builder.setCancelable(false)
            loadingDialog = builder.create()
            loadingDialog?.window?.setBackgroundDrawableResource(R.color.transparent)
        }
        if (!loadingDialog!!.isShowing) {
            loadingDialog!!.show()
        }
    }

    fun hideLoading() {
        if (loadingDialog != null && loadingDialog!!.isShowing) {
            loadingDialog!!.dismiss()
            loadingDialog = null
        }
    }
}