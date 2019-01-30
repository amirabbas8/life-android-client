package net.saoshyant.Life.activity

import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.os.PowerManager
import android.provider.Settings
import android.support.v7.app.AppCompatActivity
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast

import com.pili.pldroid.player.AVOptions
import com.pili.pldroid.player.PLMediaPlayer

import net.saoshyant.Life.R

import java.io.IOException
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

class VideoPlayer : AppCompatActivity() {
    private var mSurfaceView: SurfaceView? = null
    private var mSurfaceWidth = 0
    private var mSurfaceHeight = 0
    private var mMediaPlayer: PLMediaPlayer? = null
    private var loadingLl_videoPlay: LinearLayout? = null
    private var mAVOptions: AVOptions? = null
    private var mTouchAction: Int = 0
    private var mSurfaceYDisplayRange: Int = 0
    private var mTouchY: Float = 0.toFloat()
    private var mTouchX: Float = 0.toFloat()
    private var mVol: Float = 0.toFloat()
    private var audioManager: AudioManager? = null
    private var maxVolume: Int = 0
    private var mIsFirstBrightnessGesture = true
    private var mToast: Toast? = null
    private var timeTv: TextView? = null
    private var totalTv: TextView? = null
    private var playBtn: ImageButton? = null
    private var seekBar: SeekBar? = null
    private var mHandler: Handler? = null
    private var videoName: String? = null

    private val mCallback = object : SurfaceHolder.Callback {

        override fun surfaceCreated(holder: SurfaceHolder) {
            prepare()
        }

        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            mSurfaceWidth = width
            mSurfaceHeight = height
        }

        override fun surfaceDestroyed(holder: SurfaceHolder) {
            release()
        }
    }

    private val runnable = object : Runnable {
        override fun run() {
            if (mMediaPlayer != null && mMediaPlayer!!.isPlaying) {
                timeTv!!.text = millisToString(mMediaPlayer!!.currentPosition)
                seekBar!!.progress = mMediaPlayer!!.currentPosition.toInt()
            }
            mHandler!!.postDelayed(this, 1000)
        }
    }

    private val mOnPreparedListener = PLMediaPlayer.OnPreparedListener {
        if (mMediaPlayer != null) {
            play()
            mHandler!!.post(runnable)
            timeTv!!.text = millisToString(mMediaPlayer!!.currentPosition)
            totalTv!!.text = millisToString(mMediaPlayer!!.duration)
            seekBar!!.max = mMediaPlayer!!.duration.toInt()
            seekBar!!.progress = mMediaPlayer!!.currentPosition.toInt()
        }
    }

    private val mOnInfoListener = PLMediaPlayer.OnInfoListener { mp, what, extra ->
        when (what) {
            PLMediaPlayer.MEDIA_INFO_BUFFERING_START -> loadingLl_videoPlay!!.visibility = View.VISIBLE
            PLMediaPlayer.MEDIA_INFO_BUFFERING_END, PLMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START -> loadingLl_videoPlay!!.visibility = View.GONE
            else -> {
            }
        }
        true
    }

    private val mSeekListener = object : SeekBar.OnSeekBarChangeListener {

        override fun onStartTrackingTouch(seekBar: SeekBar) {}

        override fun onStopTrackingTouch(seekBar: SeekBar) {}

        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            if (fromUser) {
                mMediaPlayer!!.seekTo(progress.toLong())
                timeTv!!.text = millisToString(progress.toLong())
            }
        }
    }

    private val mOnVideoSizeChangedListener = PLMediaPlayer.OnVideoSizeChangedListener { mp, width, height ->
        var width = width
        var height = height
        // resize the display window to fit the screen
        if (width != 0 && height != 0) {
            val ratioW = width.toFloat() / mSurfaceWidth.toFloat()
            val ratioH = height.toFloat() / mSurfaceHeight.toFloat()
            val ratio = Math.max(ratioW, ratioH)
            width = Math.ceil((width.toFloat() / ratio).toDouble()).toInt()
            height = Math.ceil((height.toFloat() / ratio).toDouble()).toInt()
            val layout = FrameLayout.LayoutParams(width, height)
            layout.gravity = Gravity.CENTER
            mSurfaceView!!.layoutParams = layout
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = intent.extras
        videoName = bundle!!.getString("videoName")
        title = getString(R.string.player)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.video_player)
        mHandler = Handler()
        mSurfaceView = findViewById(R.id.SurfaceView) as SurfaceView
        mSurfaceView!!.holder.addCallback(mCallback)
        loadingLl_videoPlay = findViewById(R.id.loadingLl_videoPlay) as LinearLayout
        timeTv = findViewById(R.id.timeTv) as TextView
        seekBar = findViewById(R.id.seekBar) as SeekBar
        totalTv = findViewById(R.id.totalTv) as TextView
        playBtn = findViewById(R.id.playBtn) as ImageButton
        mAVOptions = AVOptions()
        mAVOptions!!.setInteger(AVOptions.KEY_PREPARE_TIMEOUT, 10 * 1000)
        mAVOptions!!.setInteger(AVOptions.KEY_GET_AV_FRAME_TIMEOUT, 10 * 1000)
        mAVOptions!!.setInteger(AVOptions.KEY_LIVE_STREAMING, 0)
        mAVOptions!!.setInteger(AVOptions.KEY_MEDIACODEC, 0)
        mAVOptions!!.setInteger(AVOptions.KEY_START_ON_PREPARED, 1)
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager!!.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
        maxVolume = audioManager!!.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
    }

    override fun onDestroy() {
        super.onDestroy()
        release()
        if (audioManager != null) {
            audioManager!!.abandonAudioFocus(null)
        }
    }

    override fun onResume() {
        super.onResume()
        if (mMediaPlayer != null) {
            play()
        }
    }

    private fun play() {
        if (!mMediaPlayer!!.isPlaying) {
            playBtn!!.setImageResource(R.drawable.ic_pause)
            mMediaPlayer!!.start()
        }
    }

    override fun onPause() {
        super.onPause()
        pause()
    }

    private fun pause() {
        if (mMediaPlayer!!.isPlaying) {
            playBtn!!.setImageResource(R.drawable.ic_play)
            mMediaPlayer!!.pause()
        }
    }

    fun onClickPlay(v: View) {
        if (mMediaPlayer != null) {
            if (mMediaPlayer!!.isPlaying) {
                pause()
            } else {
                play()
            }
        }
    }

    private fun prepare() {
        if (mMediaPlayer != null) {
            mMediaPlayer!!.setDisplay(mSurfaceView!!.holder)
            return
        }
        try {
            mMediaPlayer = PLMediaPlayer(mAVOptions)
            mMediaPlayer!!.setOnPreparedListener(mOnPreparedListener)
            mMediaPlayer!!.setOnInfoListener(mOnInfoListener)
            mMediaPlayer!!.setScreenOnWhilePlaying(true)
            mMediaPlayer!!.setOnVideoSizeChangedListener(mOnVideoSizeChangedListener)
            seekBar!!.setOnSeekBarChangeListener(mSeekListener)
            // set replay if completed
            // mMediaPlayer.setLooping(true);
            mMediaPlayer!!.setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
            mMediaPlayer!!.dataSource = "http://saoshyant.net/video/" + videoName!!
            //            mMediaPlayer.setDataSource("file://");
            mMediaPlayer!!.setDisplay(mSurfaceView!!.holder)
            mMediaPlayer!!.prepareAsync()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val screen = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(screen)
        if (mSurfaceYDisplayRange == 0)
            mSurfaceYDisplayRange = Math.min(screen.widthPixels,
                    screen.heightPixels)
        val y_changed = event.rawY - mTouchY
        val x_changed = event.rawX - mTouchX

        // coef is the gradient's move to determine a neutral zone
        val coef = Math.abs(y_changed / x_changed)
        val xgesturesize = x_changed / screen.xdpi * 2.54f

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // Audio
                mTouchY = event.rawY
                mVol = audioManager!!.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()
                mTouchAction = TOUCH_NONE
                // Seek
                mTouchX = event.rawX
            }
            MotionEvent.ACTION_MOVE -> {
                // No volume/brightness action if coef < 2
                if (coef > 2) {
                    // Volume (Up or Down - Right side)
                    if (mTouchX > screen.widthPixels / 2) {
                        doVolumeTouch(y_changed)
                    }
                    // Brightness (Up or Down - Left side)
                    if (mTouchX < screen.widthPixels / 2) {
                        doBrightnessTouch(y_changed)
                    }
                    // Extend the overlay for a little while, so that it doesn't
                    // disappear on the user if more adjustment is needed. This
                    // is because on devices with soft navigation (e.g. Galaxy
                    // Nexus), gestures can't be made without activating the UI.
                }
                // Seek (Right or Left move)
                doSeekTouch(coef, xgesturesize, false)
            }
            MotionEvent.ACTION_UP ->
                // Seek
                doSeekTouch(coef, xgesturesize, true)
        }
        return mTouchAction != TOUCH_NONE
    }

    private fun doSeekTouch(coef: Float, gesturesize: Float, seek: Boolean) {
        // No seek action if coef > 0.5 and gesturesize < 1cm
        if (coef > 0.5 || Math.abs(gesturesize) < 1)
            return
        if (mTouchAction != TOUCH_NONE && mTouchAction != TOUCH_SEEK)
            return
        mTouchAction = TOUCH_SEEK
        val length = mMediaPlayer!!.duration
        val time = mMediaPlayer!!.currentPosition

        // Size of the jump, 10 minutes max (600000), with a bi-cubic
        // progression, for a 8cm gesture
        var jump = (Math.signum(gesturesize) * (600000 * Math.pow(
                (gesturesize / 8).toDouble(), 4.0) + 3000)).toInt()

        // Adjust the jump
        if (jump > 0 && time + jump > length)
            jump = (length - time).toInt()
        if (jump < 0 && time + jump < 0)
            jump = (-time).toInt()

        // Jump !
        if (seek && length > 0)
            mMediaPlayer!!.seekTo(time + jump)

        if (length > 0)
        // Show the jump's size
            toastData(String.format("%s%s (%s)", if (jump >= 0) "+" else "", millisToString(jump.toLong()), millisToString(time + jump)))
    }

    private fun doVolumeTouch(y_changed: Float) {
        if (mTouchAction != TOUCH_NONE && mTouchAction != TOUCH_VOLUME)
            return
        val delta = -(y_changed / mSurfaceYDisplayRange * maxVolume).toInt()
        val vol = Math.min(Math.max(mVol + delta, 0f), maxVolume.toFloat()).toInt()
        if (delta != 0) {
            audioManager!!.setStreamVolume(AudioManager.STREAM_MUSIC, vol, 0)
            getAudioValue()
        }
    }

    private fun doBrightnessTouch(y_changed: Float) {
        if (mTouchAction != TOUCH_NONE && mTouchAction != TOUCH_BRIGHTNESS)
            return
        if (mIsFirstBrightnessGesture)
            initBrightnessTouch()
        mTouchAction = TOUCH_BRIGHTNESS

        // Set delta : 0.07f is arbitrary for now, it possibly will change in
        // the future
        val delta = -y_changed / mSurfaceYDisplayRange * 0.07f

        // Estimate and adjust Brightness
        val lp = window.attributes
        lp.screenBrightness = Math.min(
                Math.max(lp.screenBrightness + delta, 0.01f), 1f)

        // Set Brightness
        window.attributes = lp
        toastData(getString(R.string.brightness) + '\u00A0'.toString() + Math.round(lp.screenBrightness * 15))
    }

    private fun initBrightnessTouch() {
        var brightnessTemp = 0.01f
        // Initialize the layoutParams screen brightness
        try {
            brightnessTemp = Settings.System.getInt(
                    contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS) / 255.0f
        } catch (e: Settings.SettingNotFoundException) {
            e.printStackTrace()
        }

        val lp = window.attributes
        lp.screenBrightness = brightnessTemp
        window.attributes = lp
        mIsFirstBrightnessGesture = false
    }

    private fun millisToString(millis: Long): String {
        var millis = millis
        millis = Math.abs(millis)
        millis /= 1000
        val sec = (millis % 60).toInt()
        millis /= 60
        val min = (millis % 60).toInt()
        millis /= 60
        val hours = millis.toInt()
        val time: String
        val format = NumberFormat.getInstance(Locale.US) as DecimalFormat
        format.applyPattern("00")
        if (millis > 0)
            time = hours.toString() + ":" + format.format(min.toLong()) + ":" + format.format(sec.toLong())
        else
            time = min.toString() + ":" + format.format(sec.toLong())
        return time
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                mHandler!!.postDelayed({ getAudioValue() }, 100)
                return false
            }

            KeyEvent.KEYCODE_VOLUME_UP -> {
                mHandler!!.postDelayed({ getAudioValue() }, 100)
                return false
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun getAudioValue() {
        val currentVolume = audioManager!!.getStreamVolume(AudioManager.STREAM_MUSIC)
        toastData(getString(R.string.volume) + '\u00A0'.toString() + currentVolume * 100 / maxVolume + " %")
    }

    fun release() {
        if (mMediaPlayer != null) {
            mMediaPlayer!!.stop()
            mMediaPlayer!!.release()
            mMediaPlayer = null
        }
    }

    fun toastData(text: String) {
        if (mToast == null) {
            mToast = Toast.makeText(this, text, Toast.LENGTH_LONG)
        } else {
            // 当toast对象存在时，修改文本和显示的时间，不要重新创建对象，多个Toast只显示最后的个toast的时
            mToast!!.setText(text)
            mToast!!.duration = Toast.LENGTH_LONG
        }
        mToast!!.show()
    }

    companion object {
        // Touch Events
        private val TOUCH_NONE = 0
        private val TOUCH_VOLUME = 1
        private val TOUCH_BRIGHTNESS = 2
        private val TOUCH_SEEK = 3
    }
}
