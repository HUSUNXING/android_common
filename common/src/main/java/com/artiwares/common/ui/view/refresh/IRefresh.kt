package com.artiwares.common.ui.view.refresh

interface IRefresh {

    /**
     * 刷新时是否禁止滚动
     *
     * @param disableRefreshScroll 否禁止滚动
     */
    fun setDisableRefreshScroll(disableRefreshScroll: Boolean)

    /**
     * 刷新完成
     */
    fun refreshFinished()

    /**
     * 设置下拉刷新的监听器
     *
     * @param iRefreshListener 刷新的监听器
     */
    fun setRefreshListener(iRefreshListener: IRefreshListener?)

    /**
     * 设置下拉刷新的视图
     *
     * @param overView 下拉刷新的视图
     */
    fun setRefreshOverView(overView: OverView?)

    interface IRefreshListener {
        fun onRefresh()
        fun enableRefresh(): Boolean
    }

}