package com.quyt.mqttchat.presentation.feature.home.message.detail.emoji.gif

import com.quyt.mqttchat.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
sealed class GifState {
}
@HiltViewModel
class GifViewModel @Inject constructor() : BaseViewModel<GifState>() {
}