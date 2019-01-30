package net.saoshyant.Life.app.refreshLayout

import android.content.Context
import android.text.TextUtils
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView


class RefreshLayoutFooter(context: Context, private val mFooterPullText: String, private val mFooterLoadmoreingText: String) : FrameLayout(context), IRefreshFooter {
    private var mTvLoadMore: TextView? = null

    init {
        init()
    }

    private fun init() {
        mTvLoadMore = TextView(context)
        if (!TextUtils.isEmpty(mFooterPullText)) {
            mTvLoadMore?.text = mFooterPullText
        } else {
            mTvLoadMore!!.text = "Pull to refresh…"
        }
        val layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 100)
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL
        mTvLoadMore!!.gravity = Gravity.CENTER
        addView(mTvLoadMore, layoutParams)
    }


    override fun onStartLoadMore() {
        if (!TextUtils.isEmpty(mFooterLoadmoreingText)) {
            mTvLoadMore!!.text = mFooterLoadmoreingText
        } else {
            mTvLoadMore!!.text = "loading"
        }
        val layoutParams = mTvLoadMore!!.layoutParams as FrameLayout.LayoutParams
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL + Gravity.TOP
        mTvLoadMore!!.requestLayout()
    }


    override fun onPull(dy: Float) {
        visibility = ViewGroup.VISIBLE
        layoutParams.height = -dy.toInt()
        requestLayout()
    }

    override fun onFinishLoadMore() {
        if (!TextUtils.isEmpty(mFooterPullText)) {
            mTvLoadMore!!.text = mFooterPullText
        } else {
            mTvLoadMore!!.text = "Pull to refresh…"
        }
    }


}
