package com.wx.annotations


@Suppress("SupportAnnotationUsage")
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class CreateService(val interfaceApi: String, val superClass: String){

}


