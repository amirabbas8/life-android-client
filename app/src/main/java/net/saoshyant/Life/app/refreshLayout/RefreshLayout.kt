package net.saoshyant.Life.app.refreshLayout

import android.content.Context
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.FrameLayout
import net.saoshyant.Life.R


class RefreshLayout(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
    private val RELEASE_MAX_HEIGHT = 150
    private val HEADER_HEIGHT = 100
    private val FOOTER_HEIGHT = 100
    private val ANIM_DURATION = 300
    private var mChildView: View? = null
    private var mMyRefreshLayoutHeader: RefreshLayoutHeader? = null
    private var mMyRefreshLayoutFooter: RefreshLayoutFooter? = null
    private var isRefreshing: Boolean = false
    private var mTouchY: Float = 0.toFloat()
    private var mCurrentY: Float = 0.toFloat()
    private var mDamp: Float = 0.toFloat()
    var isNeedLoadMore = true
    private var isLoadMoreing: Boolean = false
    private var mHeaderImageAnimListResId: Int = 0
    private var mFooterPullText: String? = null
    private var mFooterLoadmoreingText: String? = null
    var onRefreshListener: OnRefreshListener? = null

    init {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet) {
        if (isInEditMode) {
            return
        }

        if (childCount > 1) {
            throw RuntimeException("can only have one child widget")
        }
        mDamp = RELEASE_MAX_HEIGHT * RELEASE_MAX_HEIGHT / 600f
        val typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.RefreshLayout)
        mHeaderImageAnimListResId = typedArray.getResourceId(R.styleable.RefreshLayout_headerAnimDrawbleList, -1)
        mFooterPullText = typedArray.getString(R.styleable.RefreshLayout_footerPullText)
        mFooterLoadmoreingText = typedArray.getString(R.styleable.RefreshLayout_footerLoadMoreingText)
        typedArray.recycle()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        val context = context

        mChildView = getChildAt(0)

        if (mChildView == null) {
            return
        }
        mMyRefreshLayoutHeader = RefreshLayoutHeader(context, mHeaderImageAnimListResId)
        val headerLayoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        headerLayoutParams.gravity = Gravity.TOP
        mMyRefreshLayoutHeader!!.visibility = View.GONE
        addView(mMyRefreshLayoutHeader, 0, headerLayoutParams)

        mMyRefreshLayoutFooter = RefreshLayoutFooter(context, mFooterPullText!!, mFooterLoadmoreingText!!)
        val footerLayoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        footerLayoutParams.gravity = Gravity.BOTTOM
        mMyRefreshLayoutFooter!!.visibility = View.GONE
        addView(mMyRefreshLayoutFooter, 0, footerLayoutParams)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (isRefreshing || isLoadMoreing) return true
        var intercept = false
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                mTouchY = ev.y
                mCurrentY = mTouchY
                intercept = false
            }
            MotionEvent.ACTION_MOVE -> {
                val currentY = ev.y
                val dy = currentY - mTouchY
                if (dy > 0 && !canChildScrollUp()) {
                    intercept = true
                } else if (dy < 0 && !canChildScrollDown() && isNeedLoadMore) {
                    intercept = true
                } else {
                    intercept = false
                }
            }

            MotionEvent.ACTION_POINTER_UP -> intercept = false
        }
        return intercept
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        if (isRefreshing || isLoadMoreing) {
            return super.onTouchEvent(e)
        }
        when (e.action) {
            MotionEvent.ACTION_MOVE -> {
                mCurrentY = e.y
                val distance = mCurrentY - mTouchY
                val dy = Math.sqrt((mDamp * Math.abs(distance)).toDouble())
                //                LogUtil.e("distance:"+distance);
                if (mChildView != null) {
                    if (distance > 0 && !canChildScrollUp()) {
                        mMyRefreshLayoutHeader!!.onPull(dy.toFloat())
                        ViewCompat.setTranslationY(mChildView!!, dy.toFloat())
                    } else if (distance < 0 && !canChildScrollUp()) {
                        mMyRefreshLayoutHeader!!.onPull(0f)
                        ViewCompat.setTranslationY(mChildView!!, 0f)
                    } else if (distance < 0 && !canChildScrollDown() && isNeedLoadMore) {
                        mMyRefreshLayoutFooter!!.onPull(-dy.toFloat())
                        ViewCompat.setTranslationY(mChildView!!, -dy.toFloat())
                    } else if (distance > 0 && !canChildScrollDown() && isNeedLoadMore) {
                        mMyRefreshLayoutFooter!!.onPull(0f)
                        ViewCompat.setTranslationY(mChildView!!, 0f)
                    }
                }

                return true
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                if (mChildView != null) {
                    //下拉
                    if (ViewCompat.getY(mChildView!!) > 0) {
                        if (ViewCompat.getY(mChildView!!) >= HEADER_HEIGHT) {
                            createAnimatorTranslationY(mChildView!!, HEADER_HEIGHT.toFloat(), mMyRefreshLayoutHeader)
                            isRefreshing = true
                            if (onRefreshListener != null) {
                                onRefreshListener!!.onRefresh()
                                mMyRefreshLayoutHeader!!.onStartRefreshing()
                            }
                        } else {
                            createAnimatorTranslationY(mChildView!!, 0f, mMyRefreshLayoutHeader)
                        }
                    } else {
                        if (Math.abs(ViewCompat.getY(mChildView!!)) >= FOOTER_HEIGHT) {
                            createAnimatorTranslationY(mChildView!!, (-FOOTER_HEIGHT).toFloat(), mMyRefreshLayoutFooter)
                            isLoadMoreing = true
                            if (onRefreshListener != null) {
                                onRefreshListener!!.onLoadMore()
                                mMyRefreshLayoutFooter!!.onStartLoadMore()
                            }
                        } else {
                            createAnimatorTranslationY(mChildView!!, 0f, mMyRefreshLayoutFooter)
                        }
                    }
                }
                return true
            }
        }
        return super.onTouchEvent(e)
    }

    fun createAnimatorTranslationY(v: View, h: Float, headerOrFooter: IRefreshHeaderOrFooter?) {
        val animation = object : Animation() {
            override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                val dy = interpolatedTime * (h - v.translationY) + v.translationY
                v.translationY = dy
                headerOrFooter!!.onPull(dy)
            }
        }
        animation.interpolator = AccelerateInterpolator()
        animation.duration = ANIM_DURATION.toLong()
        v.startAnimation(animation)

    }

    fun setRefreshing(refreshing: Boolean) {
        if (isRefreshing == refreshing) {
            return
        }
        if (refreshing) {
            this.post { startRefreshing() }
        } else {
            this.post { finishRefreshing() }
        }
    }

    private fun finishRefreshing() {
        if (mChildView != null) {

            val animation = object : Animation() {
                override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                    val dy = (1 - interpolatedTime) * HEADER_HEIGHT
                    mChildView!!.translationY = dy
                    mMyRefreshLayoutHeader!!.onPull(dy)
                }
            }
            animation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {

                }

                override fun onAnimationEnd(animation: Animation) {
                    mMyRefreshLayoutHeader!!.onFinishRefreshing()
                }

                override fun onAnimationRepeat(animation: Animation) {

                }
            })
            animation.interpolator = AccelerateInterpolator()
            animation.duration = ANIM_DURATION.toLong()
            mChildView!!.startAnimation(animation)

        }
        isRefreshing = false
    }

    private fun finishLoadMoreing() {
        if (mChildView != null) {
            mMyRefreshLayoutFooter!!.onFinishLoadMore()
            val animation = object : Animation() {
                override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                    val dy = (1 - interpolatedTime) * FOOTER_HEIGHT
                    mChildView!!.translationY = -dy
                    mMyRefreshLayoutFooter!!.onPull(-dy)
                }
            }
            animation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {

                }

                override fun onAnimationEnd(animation: Animation) {
                    mMyRefreshLayoutFooter!!.onFinishLoadMore()
                }

                override fun onAnimationRepeat(animation: Animation) {

                }
            })
            animation.interpolator = AccelerateInterpolator()
            animation.duration = ANIM_DURATION.toLong()
            mChildView!!.startAnimation(animation)

        }
        isLoadMoreing = false
    }

    private fun startRefreshing() {
        isRefreshing = true
        if (mChildView != null) {
            val animation = object : Animation() {
                override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                    val dy = interpolatedTime * HEADER_HEIGHT
                    mChildView!!.translationY = dy
                    mMyRefreshLayoutHeader!!.onPull(dy)
                }
            }
            animation.interpolator = AccelerateInterpolator()
            animation.duration = ANIM_DURATION.toLong()
            animation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {

                }

                override fun onAnimationEnd(animation: Animation) {
                    mMyRefreshLayoutHeader!!.onStartRefreshing()
                }

                override fun onAnimationRepeat(animation: Animation) {

                }
            })
            mChildView!!.startAnimation(animation)

        }
        if (onRefreshListener != null) {
            onRefreshListener!!.onRefresh()
        }

    }

    private fun startLoadMoreing() {
        isLoadMoreing = true
        if (mChildView != null) {
            val animation = object : Animation() {
                override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                    val dy = interpolatedTime * FOOTER_HEIGHT
                    mChildView!!.translationY = -dy
                    mMyRefreshLayoutFooter!!.onPull(dy)
                }
            }
            animation.interpolator = AccelerateInterpolator()
            animation.duration = ANIM_DURATION.toLong()
            animation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {

                }

                override fun onAnimationEnd(animation: Animation) {
                    mMyRefreshLayoutFooter!!.onStartLoadMore()
                }

                override fun onAnimationRepeat(animation: Animation) {

                }
            })
            mChildView!!.startAnimation(animation)

        }
        if (onRefreshListener != null) {
            onRefreshListener!!.onRefresh()
        }

    }

    fun setLoadMoreing(loadMoreing: Boolean) {
        if (isLoadMoreing == loadMoreing) {
            return
        }
        if (loadMoreing) {
            this.post { startLoadMoreing() }
        } else {
            this.post { finishLoadMoreing() }
        }
    }

    fun canChildScrollUp(): Boolean {
        return if (mChildView == null) {
            false
        } else ViewCompat.canScrollVertically(mChildView!!, -1)
    }

    fun canChildScrollDown(): Boolean {
        return if (mChildView == null) {
            false
        } else ViewCompat.canScrollVertically(mChildView!!, 1)
        //        if (Build.VERSION.SDK_INT < 14) {
        //            if (mChildView instanceof AbsListView) {
        //                final AbsListView absListView = (AbsListView) mChildView;
        //                if (absListView.getChildCount() > 0) {
        //                    int lastChildBottom = absListView.getChildAt(absListView.getChildCount() - 1).getBottom();
        //                    return absListView.getLastVisiblePosition() == absListView.getAdapter().getCount() - 1 && lastChildBottom <= absListView.getMeasuredHeight();
        //                } else {
        //                    return false;
        //                }
        //
        //            } else {
        //                return ViewCompat.canScrollVertically(mChildView, 1) || mChildView.getScrollY() > 0;
        //            }
        //        } else {
        //  }
    }

    interface OnRefreshListener {
        fun onRefresh()

        fun onLoadMore()
    }

}
