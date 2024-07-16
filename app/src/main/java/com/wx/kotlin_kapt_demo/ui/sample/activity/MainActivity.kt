package com.wx.kotlin_kapt_demo.ui.sample.activity

import android.os.Bundle
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.button.MaterialButton
import com.wx.kotlin_kapt_demo.R
import com.wx.kotlin_kapt_demo.ui.sample.viewmodel.MainVIewModel

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private val viewModel by lazy { viewModels<MainVIewModel>().value }

    private lateinit var img: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        findViewById<MaterialButton>(R.id.btn_request).setOnClickListener {
            viewModel.requestTest()
        }

        img = findViewById(R.id.img_net)

        viewModel.liveDataImg.observe(this) {
            Glide.with(this@MainActivity).load(it)
//                .placeholder(R.drawable.ic)
                .skipMemoryCache(false)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .into(img)
        }
    }

}