package net.saoshyant.Life.activity

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import net.saoshyant.Life.R
import net.saoshyant.Life.app.DatabaseHandler
import net.saoshyant.Life.app.MyApplication
import org.json.JSONException
import org.json.JSONObject
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import kotlin.experimental.and

class update3 : AppCompatActivity() {

    private var usernameText: EditText? = null
    private var mobileText: EditText? = null
    private var passwordText: EditText? = null
    private var signUpButton: Button? = null
    private var id: String? = null
    private var code: String? = null
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        val bundle = intent.extras
        id = bundle!!.getString("id")
        code = bundle.getString("code")
        usernameText = findViewById(R.id.input_username)
        mobileText = findViewById(R.id.input_mobile)
        passwordText = findViewById(R.id.input_password)
        signUpButton = findViewById(R.id.btn_signup)
        val loginLink = findViewById<TextView>(R.id.link_login)
        loginLink.visibility = View.GONE
        signUpButton!!.text = "بروز رسانی اطلاعات"
        mobileText!!.setText(bundle.getString("phone"))
        usernameText!!.setText(bundle.getString("username"))

        usernameText!!.requestFocus()
        signUpButton!!.setOnClickListener { signUp() }

        loginLink.setOnClickListener {
            // Finish the registration screen and return to the Login activity
            val intent = Intent(applicationContext, LoginActivity::class.java)
            startActivity(intent)
            finish()
            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out)
        }
    }

    fun signUp() {

        if (validate()) {

            signUpButton!!.isEnabled = false
            ProcessLogin()
        }


    }

    private fun ProcessLogin() {
        val progressDialog = ProgressDialog(this@update3, R.style.AppTheme_Dark_Dialog)
        progressDialog.isIndeterminate = true
        progressDialog.setMessage("Creating Account...")
        progressDialog.show()
        val password = passwordText!!.text.toString()


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
        //System.out.println("Digest(in hex format):: " + sb.toString());


        val params = HashMap<String, String>()
        params["tag"] = "update"
        params["id"] = id!!
        params["password"] = sb.toString()
        params["phone"] = mobileText!!.text.toString()
        params["username"] = usernameText!!.text.toString()
        params["iid"] = ""
//        InstanceID.getInstance(applicationContext).id

        //    Log.d(PhoneLogin2.class.getSimpleName(), params.toString());
        val strReq = object : StringRequest(Request.Method.POST, "http://saoshyant.net/Life/setupdate.php", Response.Listener { response ->
            //response from the server
            try {
                val responseObj = JSONObject(response)
                val feedArray = responseObj.getJSONArray("user")
                val feedObj = feedArray.get(0) as JSONObject

                if (feedObj.getString("success") != null) {
                    val res = feedObj.getString("success")

                    if (Integer.parseInt(res) == 1) {

                        val db = DatabaseHandler(applicationContext)
                        db.resetTables()
                        db.addUser(feedObj.getString("id"), code!!, feedObj.getString("name"), "home", feedObj.getString("profileImage"))


                        startActivity(Intent(applicationContext, Main::class.java))
                        finish()
                    } else if (Integer.parseInt(res) == 3) {
                        usernameText!!.error = getString(R.string.usernameAlreadyTaken)
                        progressDialog.dismiss()
                        signUpButton!!.isEnabled = true
                    } else {
                        Toast.makeText(applicationContext, R.string.errorproblem, Toast.LENGTH_SHORT).show()
                        progressDialog.dismiss()
                        signUpButton!!.isEnabled = true
                    }


                }

            } catch (e: JSONException) {
                Toast.makeText(applicationContext, R.string.errorproblem, Toast.LENGTH_SHORT).show()
                progressDialog.dismiss()
                signUpButton!!.isEnabled = true
                // Toast.makeText(getApplicationContext(), "Error2: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        },
                Response.ErrorListener {
                    //  Log.e(TAG, "Error1: " + error.getMessage());
                    Toast.makeText(applicationContext, R.string.errorconection, Toast.LENGTH_SHORT).show()
                    progressDialog.dismiss()
                    signUpButton!!.isEnabled = true
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
        val mobile = mobileText!!.text.toString()
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


        if (mobile.isEmpty() || mobile.length != 10) {
            mobileText!!.error = getString(R.string.phone_pattern)
            valid = false
        } else {
            mobileText!!.error = null
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