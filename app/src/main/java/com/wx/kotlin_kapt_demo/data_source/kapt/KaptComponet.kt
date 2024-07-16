package com.wx.kotlin_kapt_demo.data_source.kapt

import com.wx.annotations.CreateService

@CreateService(interfaceApi = "com.wx.test.api.Api", superClass = "com.wx.kotlin_kapt_demo.data_source.repository.BaseRepository")
class KaptComponet {
}