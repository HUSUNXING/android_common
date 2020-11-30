package com.artiwares.common.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment

abstract class BaseFragment<V : ViewDataBinding> : Fragment() {

    protected lateinit var mBinding: V



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initParam()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = DataBindingUtil.inflate(layoutInflater, getLayoutId(), container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onFragmentCreated(view, savedInstanceState)
    }

    //检测 宿主 是否还存活
    open fun isAlive(): Boolean {
        return !(isRemoving || isDetached || activity == null)
    }

    protected open fun initParam() {

    }

    abstract fun onFragmentCreated(view: View, savedInstanceState: Bundle?)

    @LayoutRes
    abstract fun getLayoutId(): Int



}