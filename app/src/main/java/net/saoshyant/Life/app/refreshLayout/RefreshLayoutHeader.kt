package net.saoshyant.Life.app.refreshLayout

import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView

import net.saoshyant.Life.R

class RefreshLayoutHeader(context: Context, imageResId: Int) : FrameLayout(context), IRefreshHeder {

    private var mAnimationDrawable: AnimationDrawable? = null
    private val mIvHeight = 100f
    private var mIv: ImageView? = null

    init {
        init(imageResId)
    }

    private fun init(imageResId: Int) {
        mIv = ImageView(context)
        if (imageResId == -1) {
            mIv!!.setImageResource(R.drawable.sun_refreshing)
        } else {
            mIv!!.setImageResource(imageResId)
        }
        mIv!!.setPadding(0, 10, 0, 10)
        mIv!!.scaleType = ImageView.ScaleType.CENTER_INSIDE
        val layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, mIvHeight.toInt())
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL
        addView(mIv, layoutParams)
        mAnimationDrawable = mIv!!.drawable as AnimationDrawable
        mAnimationDrawable!!.stop()
    }

    override fun onStartRefreshing() {
        mAnimationDrawable!!.start()
    }

    override fun onPull(dy: Float) {
        visibility = View.VISIBLE
        if (dy < mIvHeight) {
            mIv!!.layoutParams.height = dy.toInt()
            mIv!!.requestLayout()
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            requestLayout()
        } else {
            layoutParams.height = dy.toInt()
            requestLayout()
            mIv!!.translationY = dy - mIvHeight
        }
    }

    override fun onFinishRefreshing() {
        mAnimationDrawable!!.stop()
    }

}
