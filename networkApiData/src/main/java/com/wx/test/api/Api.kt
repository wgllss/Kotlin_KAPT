package com.wx.test.api

import com.wx.annotations.PostBody
import com.wx.test.api.data.BaiduDataBean
import com.wx.test.api.data.BaseResponse
import okhttp3.RequestBody
import retrofit2.http.*

interface Api {

    @GET("search/acjson?tn=resultjson_com&logid=12307192414549550342&ipn=rj&ct=201326592&is=&fp=result&fr=&cg=star&rn=30")
    suspend fun get899(@Query("word") word: String, @Query("queryWord") queryWord: String, @Query("pn") pn: Int, @Query("gsm") gsm: String): BaseResponse<ArrayList<BaiduDataBean>>

    @FormUrlEncoded
    @POST("https://www.wanandroid.com/user/register")
    suspend fun register(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("repassword") repassword: String
    ): String


    @POST("https://www.wanandroid.com/user/register")
    suspend fun testPostBody(@Body body: RequestBody): String

    @PostBody("{\"ID\":\"Long\",\"name\":\"String\"}")
    @POST("https://www.wanandroid.com/user/register")
    suspend fun testPostBody222(@Body body: RequestBody): String
}