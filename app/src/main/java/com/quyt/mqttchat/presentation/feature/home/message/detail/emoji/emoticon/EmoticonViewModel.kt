package com.quyt.mqttchat.presentation.feature.home.message.detail.emoji.emoticon

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.quyt.mqttchat.domain.model.Emoji
import com.quyt.mqttchat.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


sealed class EmoticonState {
    class Success(val data: ArrayList<Emoji>) : EmoticonState()
}
@HiltViewModel
class EmoticonViewModel @Inject constructor() : BaseViewModel<EmoticonState>() {

    fun getEmoji(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val emojis = readEmojiFromJson(context)
            // Add header
            emojis.add(0, Emoji(emojis[0].category))
            for (i in 1 until emojis.size) {
                if (emojis[i].category != emojis[i - 1].category) {
                    emojis.add(i, Emoji(emojis[i].category))
                }
            }
            uiState.postValue(EmoticonState.Success(emojis))
        }
    }

    private fun readEmojiFromJson(context : Context): ArrayList<Emoji> {
        var json: ArrayList<Emoji>? = null
        try {
            val inputStream = context.assets.open("emoji.json")
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            val jsonStr = String(buffer, Charsets.UTF_8)
            json = Gson().fromJson(jsonStr, object : TypeToken<ArrayList<Emoji>>() {}.type)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return json ?: arrayListOf()
    }

}