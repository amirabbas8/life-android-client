package net.saoshyant.Life.activity

import android.app.Activity
import android.content.res.Configuration
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.method.ScrollingMovementMethod
import android.widget.TextView

import net.saoshyant.Life.R


class Policies : AppCompatActivity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.policies)
        setTitle(R.string.policies)
        val policies = findViewById(R.id.policies) as TextView
        policies.movementMethod = ScrollingMovementMethod()


    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }


}
