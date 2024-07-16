package com.wx.kotlin_kapt_demo.ui.sample.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wx.kotlin_kapt_demo.data_source.kapt.ApiRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainVIewModel : ViewModel() {

    private val repository by lazy { ApiRepository() }
    val liveDataImg by lazy { MutableLiveData<String>() }

    fun requestTest() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.get899("西游记", "西游记", 1, "")
            result?.data?.takeIf {
                it.size > 0
            }?.let {
                liveDataImg.postValue(it[0].middleURL)
            }
        }
    }
}