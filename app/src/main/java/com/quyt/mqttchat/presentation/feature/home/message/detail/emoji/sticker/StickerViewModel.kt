package com.quyt.mqttchat.presentation.feature.home.message.detail.emoji.sticker

import com.quyt.mqttchat.domain.model.Sticker
import com.quyt.mqttchat.domain.model.StickerPack
import com.quyt.mqttchat.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


sealed class StickerState {
    data class Success(val data: ArrayList<Sticker>,val category : ArrayList<StickerPack>) : StickerState()
}

@HiltViewModel
class StickerViewModel @Inject constructor() : BaseViewModel<StickerState>() {

    init {
        getSticker()
    }

    fun getSticker() {
        val listStickerPack = ArrayList<StickerPack>()
        val panpaPack = ArrayList<Sticker>()
        val catPack = ArrayList<Sticker>()
        for (i in 0..119) {
            panpaPack.add(Sticker("http://quyt.ddns.net:82/api/public/dl/46ciCcEb/$i.png","Panpa"))
            catPack.add(Sticker("http://quyt.ddns.net:82/api/public/dl/fuv8dTKK/Cat/$i.png","Cat"))
        }
        listStickerPack.add(StickerPack("Panpa", panpaPack))
        listStickerPack.add(StickerPack("Cat", catPack))
        //
        val stickerData = ArrayList<Sticker>()
        listStickerPack.forEach {
            stickerData.add(Sticker("",it.name))
            stickerData.addAll(it.stickers)
        }
        uiState.postValue(StickerState.Success(stickerData,listStickerPack))
    }
}