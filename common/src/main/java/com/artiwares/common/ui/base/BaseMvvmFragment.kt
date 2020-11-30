package com.artiwares.common.ui.base

import android.os.Bundle
import android.view.View
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

abstract class BaseMvvmFragment<VM : ViewModel, V : ViewDataBinding> : BaseFragment<V>() {

    protected lateinit var mViewModel: VM

    open fun <T : ViewModel> createViewModel(fragment: Fragment, cls: Class<T>): T {
        return ViewModelProvider(fragment).get(cls)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
    }

    abstract fun initViewModel(): VM
}