package com.wx.annotations


@Suppress("SupportAnnotationUsage")
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class PostBody(val json: String) {

}


