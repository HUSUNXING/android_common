package com.artiwares.common.ui.base

import android.os.Bundle
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

abstract class BaseMvvmActivity<VM : ViewModel, V : ViewDataBinding> : BaseActivity<V>() {

    protected lateinit var mViewModel: VM

    override fun initViews(savedInstanceState: Bundle?) {
        super.initViews(savedInstanceState)
        mViewModel = initViewModel()
    }

    /**
     * 初始化ViewModel
     *
     * @return 继承BaseViewModel的ViewModel
     */
    abstract fun initViewModel(): VM

    /**
     * 创建ViewModel
     */
    open fun <T : ViewModel> createViewModel(activity: FragmentActivity, cls: Class<T>): T {
        return ViewModelProvider(activity).get(cls)
    }

}