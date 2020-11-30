package com.artiwares.common.ui.view.refresh

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.artiwares.common.utils.DisplayUtil

abstract class OverView : FrameLayout {


    /**
     * 获取状态
     *
     * @return 状态
     */
    /**
     * 设置状态
     *
     * @param state 状态
     */
    var state = RefreshState.STATE_INIT

    /**
     * 触发下拉刷新 需要的最小高度
     */
    var mPullRefreshHeight = 0

    /**
     * 最小阻尼
     */
    var minDamp = 1.6f

    /**
     * 最大阻尼
     */
    var maxDamp = 2.2f

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context!!, attrs, defStyle
    ) {
        preInit()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    ) {
        preInit()
    }

    constructor(context: Context?) : super(context!!) {
        preInit()
    }

    protected fun preInit() {
        mPullRefreshHeight = DisplayUtil.dp2px(66f, resources)
        init()
    }

    /**
     * 初始化
     */
    abstract fun init()

    abstract fun onScroll(scrollY: Int, pullRefreshHeight: Int)

    /**
     * 显示Overlay
     */
    abstract fun onVisible()

    /**
     * 超过Overlay，释放就会加载
     */
    abstract fun onOver()

    /**
     * 开始加载
     */
    abstract fun onRefresh()

    /**
     * 加载完成
     */
    abstract fun onFinish()
}