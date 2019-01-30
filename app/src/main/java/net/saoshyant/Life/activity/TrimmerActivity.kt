package net.saoshyant.Life.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity

import net.saoshyant.Life.R

import life.knowledge4.videotrimmer.K4LVideoTrimmer
import life.knowledge4.videotrimmer.interfaces.OnTrimVideoListener

class TrimmerActivity : AppCompatActivity(), OnTrimVideoListener {

    private var mVideoTrimmer: K4LVideoTrimmer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trimmer)

        val extraIntent = intent
        var path = ""

        if (extraIntent != null) {
            path = extraIntent.getStringExtra("EXTRA_VIDEO_PATH")
        }


        mVideoTrimmer = findViewById(R.id.timeLine)
        if (mVideoTrimmer != null) {
            mVideoTrimmer!!.setMaxDuration(60)
            mVideoTrimmer!!.setOnTrimVideoListener(this)
            //mVideoTrimmer.setDestinationPath("/storage/emulated/0/DCIM/CameraCustom/");
            mVideoTrimmer!!.setVideoURI(Uri.parse(path))
        }
    }

    override fun getResult(uri: Uri) {

        val intent = Intent()
        intent.data = uri
        setResult(Activity.RESULT_OK, intent)
        finish()

    }

    override fun cancelAction() {
        mVideoTrimmer!!.destroy()
        finish()
    }
}
