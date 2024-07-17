package com.wx.annotations


@Suppress("SupportAnnotationUsage")
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class CreateService(val interfaceApi: String, val superClass: String) {
    /**
     * interfaceApi:  接口类名字，
     *  superClass :  自动生成的类的继承的父类
     **/
}


