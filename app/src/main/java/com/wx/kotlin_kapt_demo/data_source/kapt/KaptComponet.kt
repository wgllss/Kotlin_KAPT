package com.wx.kotlin_kapt_demo.data_source.kapt

import com.wx.annotations.CreateService

/**
 * interfaceApi:  接口类名字，
 *  superClass :  自动生成的类的继承的父类
 **/
@CreateService(interfaceApi = "com.wx.test.api.Api", superClass = "com.wx.kotlin_kapt_demo.data_source.repository.BaseRepository")
class KaptComponet {
}