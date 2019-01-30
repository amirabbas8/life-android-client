package net.saoshyant.Life.activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import net.saoshyant.Life.R
import net.saoshyant.Life.app.DatabaseHandler
import net.saoshyant.Life.app.MyApplication
import org.json.JSONException
import org.json.JSONObject
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import kotlin.experimental.and

class LoginActivity : AppCompatActivity() {

    private var usernameText: EditText? = null
    private var passwordText: EditText? = null
    private var loginButton: Button? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        usernameText = findViewById(R.id.input_username)
        passwordText = findViewById(R.id.input_password)
        loginButton = findViewById(R.id.btn_login)
        val signUpLink = findViewById<TextView>(R.id.link_signup)

        usernameText!!.requestFocus()
        loginButton!!.setOnClickListener { login() }

        YoYo.with(Techniques.Pulse).duration(700).repeat(0).playOn(signUpLink)
        signUpLink.setOnClickListener {
            // Start the Signup activity
            val intent = Intent(applicationContext, SignUpActivity::class.java)
            startActivity(intent)
            finish()
            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out)
        }
    }

    fun login() {

        if (validate()) {
            loginButton!!.isEnabled = false
            processLogin()
        }


    }


    private fun processLogin() {
//        val progressDialog = ProgressBar(this@LoginActivity, null,
//                R.style.AppTheme_Dark_Dialog)
//        progressDialog.isIndeterminate = true
//        progressDialog.visibility = View.VISIBLE

        val password = passwordText!!.text.toString() + "albag"

        var md: MessageDigest? = null
        try {
            md = MessageDigest.getInstance("MD5")
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }

        if (md != null) {
            md.update(password.toByteArray())
        }

        var byteData = ByteArray(0)
        if (md != null) {
            byteData = md.digest()
        }

        //convert the byte to hex format method 1
        val sb = StringBuilder()
        for (aByteData in byteData) {
            sb.append(Integer.toString((aByteData and 0xff.toByte()) + 0x100, 16).substring(1))
        }


        val params = HashMap<String, String>()
        params["tag"] = "login"
        params["password"] = sb.toString()
        params["username"] = usernameText!!.text.toString()
        params["iid"] = ""
//        InstanceID.getInstance(applicationContext).id
        val strReq = object : StringRequest(Request.Method.POST, "http://saoshyant.net/Life/login.php", Response.Listener { response ->
            //response from the server
            // Log.d(PhoneLogin2.class.getSimpleName(), response);
            try {
                val responseObj = JSONObject(response)
                val feedArray = responseObj.getJSONArray("user")
                val feedObj = feedArray.get(0) as JSONObject

                if (feedObj.getString("success") != null) {
                    val res = feedObj.getString("success")

                    if (Integer.parseInt(res) == 1) {

                        val db = DatabaseHandler(applicationContext)
                        db.resetTables()
                        db.addUser(feedObj.getString("id"), feedObj.getString("code"), feedObj.getString("name"), "home", feedObj.getString("profileImage"))


                        startActivity(Intent(applicationContext, Main::class.java))
                        finish()
                    } else if (Integer.parseInt(res) == 3) {

                        Toast.makeText(applicationContext, R.string.no_account, Toast.LENGTH_SHORT).show()
//                        progressDialog.visibility=Gon
                        loginButton!!.isEnabled = true
                    } else {
                        Toast.makeText(applicationContext, R.string.errorproblem, Toast.LENGTH_SHORT).show()
//                        progressDialog.dismiss()
                        loginButton!!.isEnabled = true
                    }


                }

            } catch (e: JSONException) {
                Toast.makeText(applicationContext, R.string.errorproblem, Toast.LENGTH_SHORT).show()
//                progressDialog.dismiss()
                loginButton!!.isEnabled = true
                // Toast.makeText(getApplicationContext(), "Error2: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        },
                Response.ErrorListener {
                    //  Log.e(TAG, "Error1: " + error.getMessage());
                    Toast.makeText(applicationContext, R.string.errorconection, Toast.LENGTH_SHORT).show()
//                    progressDialog.dismiss()
                    loginButton!!.isEnabled = true
                }
        ) {

            /* Passing user parameters to our server
             * @return*/
            override fun getParams(): Map<String, String> {
                // Log.e(TAG, "Posting params: " + params.toString());
                return params
            }
        }
        // Adding request to request queue
        MyApplication.instance!!.addToRequestQueue(strReq)
    }


    fun validate(): Boolean {
        var valid = true

        val username = usernameText!!.text.toString()
        val password = passwordText!!.text.toString()

        if (username.isEmpty() || username.length < 4 || username.length > 20) {
            usernameText!!.error = getString(R.string.at_least)
            valid = false
        } else {
            usernameText!!.error = null
        }


        if (!username.matches("[A-Za-z0-9_]+".toRegex())) {
            usernameText!!.error = getString(R.string.invalid_username)
            valid = false
        }

        if (password.isEmpty() || password.length < 4 || password.length > 10) {
            passwordText!!.error = getString(R.string.between_characters)
            valid = false
        } else {
            passwordText!!.error = null
        }

        return valid
    }
}
