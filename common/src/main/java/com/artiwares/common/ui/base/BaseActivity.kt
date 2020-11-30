package com.artiwares.common.ui.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

abstract class BaseActivity<V : ViewDataBinding> : AppCompatActivity() {

    private lateinit var mBinding: V

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //页面接受的参数方法
        initParam()
        initViews(savedInstanceState)
        //页面数据初始化方法
        initData()
    }

    protected open fun initData() {

    }

    protected open fun initParam() {

    }

    open fun initViews(savedInstanceState: Bundle?) {
        mBinding = DataBindingUtil.setContentView(this, initLayoutRes(savedInstanceState))
    }

    abstract fun initLayoutRes(savedInstanceState: Bundle?): Int


}