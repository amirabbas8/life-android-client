package net.saoshyant.Life.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import ir.adad.client.Adad
import net.saoshyant.Life.R
import net.saoshyant.Life.app.DatabaseHandler
import java.util.*

class Main : Activity() {
    internal var y = 2018
    internal var m = 10
    internal var d = 15

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Adad.initialize(applicationContext)
        setContentView(R.layout.main)
        val lytExDate = findViewById<RelativeLayout>(R.id.exdatela)
        val download = findViewById<Button>(R.id.download)
        download.setOnClickListener { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://saoshyant.net/Life/appdownload/Life.apk"))) }

        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        val hi = findViewById<TextView>(R.id.hi)


        val mainActivityIntent = Intent(applicationContext, MainActivity::class.java)

        //Intent countryIntent = new Intent(getApplicationContext(), SelectCountry.class);

        val LoginIntent = Intent(applicationContext, LoginActivity::class.java)

        val db1 = DatabaseHandler(applicationContext)
        val user: HashMap<String, String>
        if (db1.rowCount > 0) {
            user = db1.userDetails

            val firstPage = user["firstPage"]
            hi.setText(String.format("Salam\n%s", user["realname"]))
            when (firstPage) {
                "home" -> {

                    startActivity(mainActivityIntent)

                    finish()
                }
                else -> {

                    startActivity(LoginIntent)

                    finish()
                }
            }

        } else {

            startActivity(LoginIntent)

            finish()


        }
    }


}
