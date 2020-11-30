package com.artiwares.common.ui.view.refresh

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.Scroller

open class CtRefreshLayout : FrameLayout, IRefresh {

    private var mState: RefreshState? = null
    private var mGestureDetector: GestureDetector? = null
    private var mAutoScroller: AutoScroller? = null
    private var mIRefreshListener: IRefresh.IRefreshListener? = null
    private var mOverView: OverView? = null
    private var mLastY = 0

    //刷新时是否禁止滚动
    private var disableRefreshScroll = false

    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context!!, attrs, defStyle) {
        init()
    }

    constructor(context: Context?) : super(context!!) {
        init()
    }

    private fun init() {
        mGestureDetector = GestureDetector(context, ctGestureDetector)
        mAutoScroller = AutoScroller()
    }

    override fun setDisableRefreshScroll(disableRefreshScroll: Boolean) {
        this.disableRefreshScroll = disableRefreshScroll
    }

    override fun refreshFinished() {
        val head = getChildAt(0)
        Log.i(this.javaClass.simpleName, "refreshFinished head-bottom:" + head.bottom)
        mOverView?.onFinish()
        mOverView?.state = RefreshState.STATE_INIT
        val bottom = head.bottom
        if (bottom > 0) {
            //下over pull 200，height 100
            //  bottom  =100 ,height 100
            recover(bottom)
        }
        mState = RefreshState.STATE_INIT
    }

    override fun setRefreshListener(iRefreshListener: IRefresh.IRefreshListener?) {
        mIRefreshListener = iRefreshListener
    }


    /**
     * 设置下拉刷新的视图
     *
     * @param overView
     */
    override fun setRefreshOverView(overView: OverView?) {
        if (mOverView != null) {
            removeView(mOverView)
        }
        mOverView = overView
        val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        addView(mOverView, 0, params)
    }

    private var ctGestureDetector: CtGestureDetector = CtGestureDetector()

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        //事件分发处理
        if (!mAutoScroller!!.isFinished) {
            return false
        }
        val head = getChildAt(0)
        if (ev.action == MotionEvent.ACTION_UP || ev.action == MotionEvent.ACTION_CANCEL || ev.action == MotionEvent.ACTION_POINTER_INDEX_MASK) { //松开手
            if (head.bottom > 0) {
                if (mState !== RefreshState.STATE_REFRESH) { //非正在刷新
                    recover(head.bottom)
                    return false
                }
            }
            mLastY = 0
        }
        val consumed = mGestureDetector!!.onTouchEvent(ev)
        Log.i(TAG, "gesture consumed：$consumed")
        if ((consumed || mState !== RefreshState.STATE_INIT && mState !== RefreshState.STATE_REFRESH) && head.bottom != 0) {
            ev.action = MotionEvent.ACTION_CANCEL //让父类接受不到真实的事件
            return super.dispatchTouchEvent(ev)
        }
        return if (consumed) {
            true
        } else {
            super.dispatchTouchEvent(ev)
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        //定义head和child的排列位置
        val head = getChildAt(0)
        val child = getChildAt(1)
        if (head != null && child != null) {
            Log.i(TAG, "onLayout head-height:" + head.measuredHeight)
            val childTop = child.top
            if (mState === RefreshState.STATE_REFRESH) {
                head.layout(
                    0,
                    mOverView!!.mPullRefreshHeight - head.measuredHeight,
                    right,
                    mOverView!!.mPullRefreshHeight
                )
                child.layout(
                    0,
                    mOverView!!.mPullRefreshHeight,
                    right,
                    mOverView!!.mPullRefreshHeight + child.measuredHeight
                )
            } else {
                //left,top,right,bottom
                head.layout(0, childTop - head.measuredHeight, right, childTop)
                child.layout(0, childTop, right, childTop + child.measuredHeight)
            }
            var other: View
            for (i in 2 until childCount) {
                other = getChildAt(i)
                other.layout(0, top, right, bottom)
            }
            Log.i(TAG, "onLayout head-bottom:" + head.bottom)
        }
    }

    private fun recover(dis: Int) { //dis =200  200-100
        if (mIRefreshListener != null && dis > mOverView!!.mPullRefreshHeight) {
            mAutoScroller!!.recover(dis - mOverView!!.mPullRefreshHeight)
            mState = RefreshState.STATE_OVER_RELEASE
        } else {
            mAutoScroller!!.recover(dis)
        }
    }

    /**
     * 根据偏移量移动header与child
     *
     * @param offsetY 偏移量
     * @param nonAuto 是否非自动滚动触发
     * @return
     */
    private fun moveDown(offsetY: Int, nonAuto: Boolean): Boolean {
        Log.i("111", "changeState:$nonAuto")
        val head = getChildAt(0)
        val child = getChildAt(1)
        val childTop = child.top + offsetY
        Log.i(
            "-----",
            "moveDown head-bottom:" + head.bottom + ",child.getTop():" + child.top + ",offsetY:" + offsetY
        )
        if (childTop <= 0) { //异常情况的补充
            Log.i(TAG, "childTop<=0,mState$mState")
            val offsetY1 = -child.top
            //移动head与child的位置，到原始位置
            head.offsetTopAndBottom(offsetY1)
            child.offsetTopAndBottom(offsetY1)
            if (mState !== RefreshState.STATE_REFRESH) {
                mState = RefreshState.STATE_INIT
            }
        } else if (mState === RefreshState.STATE_REFRESH && childTop > mOverView!!.mPullRefreshHeight) {
            //如果正在下拉刷新中，禁止继续下拉
            return false
        } else if (childTop <= mOverView!!.mPullRefreshHeight) { //还没超出设定的刷新距离
            if (mOverView!!.state !== RefreshState.STATE_VISIBLE && nonAuto) { //头部开始显示
                mOverView!!.onVisible()
                mOverView!!.state = RefreshState.STATE_VISIBLE
                mState = RefreshState.STATE_VISIBLE
            }
            head.offsetTopAndBottom(offsetY)
            child.offsetTopAndBottom(offsetY)
            if (childTop == mOverView!!.mPullRefreshHeight && mState === RefreshState.STATE_OVER_RELEASE) {
                Log.i(TAG, "refresh，childTop：$childTop")
                refresh()
            }
        } else {
            if (mOverView!!.state !== RefreshState.STATE_OVER && nonAuto) {
                //超出刷新位置
                mOverView!!.onOver()
                mOverView!!.state = RefreshState.STATE_OVER
            }
            head.offsetTopAndBottom(offsetY)
            child.offsetTopAndBottom(offsetY)
        }
        if (mOverView != null) {
            mOverView!!.onScroll(head.bottom, mOverView!!.mPullRefreshHeight)
        }
        return true
    }

    /**
     * 刷新
     */
    private fun refresh() {
        if (mIRefreshListener != null) {
            mState = RefreshState.STATE_REFRESH
            mOverView!!.onRefresh()
            mOverView!!.state = RefreshState.STATE_REFRESH
            mIRefreshListener?.onRefresh()
        }
    }

    /**
     * 借助Scroller实现视图的自动滚动
     * https://juejin.im/post/5c7f4f0351882562ed516ab6
     */
    private inner class AutoScroller : Runnable {

        private val mScroller: Scroller = Scroller(context, LinearInterpolator())
        private var mLastY = 0
        var isFinished: Boolean = true

        override fun run() {
            if (mScroller.computeScrollOffset()) { //还未滚动完成
                moveDown(mLastY - mScroller.currY, false)
                mLastY = mScroller.currY
                post(this)
            } else {
                removeCallbacks(this)
                isFinished = true
            }
        }

        fun recover(dis: Int) {
            if (dis <= 0) {
                return
            }
            removeCallbacks(this)
            mLastY = 0
            isFinished = false
            mScroller.startScroll(0, 0, 0, dis, 300)
            post(this)
        }
    }

    companion object {
        private val TAG = CtRefreshLayout::class.java.simpleName
    }
}