package net.saoshyant.Life.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import net.saoshyant.Life.R


class About : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.about)
        setTitle(net.saoshyant.Life.R.string.about)

    }

}