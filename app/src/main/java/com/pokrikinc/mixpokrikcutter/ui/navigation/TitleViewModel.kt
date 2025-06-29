package com.pokrikinc.mixpokrikcutter.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

const val defaultTitle = "MixCutter"

class TitleViewModel : ViewModel() {
    private val _title = MutableStateFlow(defaultTitle)
    val title: StateFlow<String> = _title

    fun setTitle(newTitle: String) {
        viewModelScope.launch {
            _title.value = newTitle
        }
    }

}