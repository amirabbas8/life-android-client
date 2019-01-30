package net.saoshyant.Life.activity

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.NetworkImageView
import com.android.volley.toolbox.StringRequest
import croppers.CropImage
import net.saoshyant.Life.R
import net.saoshyant.Life.app.DatabaseHandler
import net.saoshyant.Life.app.MyApplication
import net.saoshyant.Life.app.Upload
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.util.*

class EditProfile : AppCompatActivity() {

    private var coordinatorLayout: CoordinatorLayout? = null
    private var profilePic: NetworkImageView? = null
    private var inputName: EditText? = null
    private var inputBio: EditText? = null
    private var ok: ImageButton? = null
    private var db1: DatabaseHandler? = null
    private var mImageCaptureUri: Uri? = null

    private var user: HashMap<String, String>? = null


    private var progressBar: ProgressBar? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        coordinatorLayout = findViewById(R.id.coordinatorLayout)
        db1 = DatabaseHandler(applicationContext)
        user = db1!!.userDetails
        inputName = findViewById(R.id.input_name)
        inputName!!.setText(user!!["realname"])
        inputBio = findViewById(R.id.input_bio)
        getProfile()
        progressBar = findViewById(R.id.progressBar)


        profilePic = findViewById(R.id.profile)
        try {
            if (user!!["profileImage"] == "") {
                profilePic!!.setImageUrl("http://saoshyant.net/profileimages/ic_profile.png", MyApplication.instance!!.imageLoader)
            } else {
                profilePic!!.setImageUrl("http://saoshyant.net/profileimages/" + user!!["profileImage"], MyApplication.instance!!.imageLoader)
            }
        } catch (e: Exception) {
        }



        profilePic!!.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ActivityCompat.requestPermissions(this@EditProfile, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
            } else {
                changeProfilePic()
            }
        }


        ok = findViewById(R.id.ok)

        ok!!.setOnClickListener { editProfile() }


    }

    private fun getProfile() {
        val db1 = DatabaseHandler(applicationContext)
        val user: HashMap<String, String>
        user = db1.userDetails
        val params = HashMap<String, String>()
        params["tag"] = "getProfile"
        params["idNo"] = user["idNo"].orEmpty()
        params["code"] = user["code"].orEmpty()
        params["id"] = user["idNo"].orEmpty()
        params["isUsername"] = ""
        val strReq = object : StringRequest(Request.Method.POST, "http://saoshyant.net/Life/profile.php", Response.Listener { response ->
            //response from the server
            // Log.d("++++", response);
            try {
                val responseObj = JSONObject(response)
                val feedArray = responseObj.getJSONArray("user")
                val feedObj = feedArray.get(0) as JSONObject

                if (feedObj.getString("success") != null) {

                    if (Integer.parseInt(feedObj.getString("success")) == 1) {
                        inputBio!!.setText(feedObj.getString("bio"))


                    } else if (Integer.parseInt(feedObj.getString("success")) == 2) {
                        inputBio!!.setText(feedObj.getString("bio"))
                    }
                    progressBar!!.visibility = View.GONE
                    ok!!.visibility = View.VISIBLE
                }

            } catch (e: JSONException) {
                finish()
                //  Toast.makeText(getApplicationContext(), "Error2: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        },
                Response.ErrorListener {
                    progressBar!!.visibility = View.GONE
                    ok!!.visibility = View.VISIBLE
                    Snackbar.make(coordinatorLayout!!, R.string.errorconection, Snackbar.LENGTH_LONG).show()
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

    private fun editProfile() {
        if ((inputName!!.text.toString().length > 4) and (inputName!!.text.toString().length < 21)) {

            ok!!.visibility = View.INVISIBLE
            progressBar!!.visibility = View.VISIBLE
            user = db1!!.userDetails
            val params = HashMap<String, String>()
            params["tag"] = "editname"
            params["id"] = user!!["idNo"].orEmpty()
            params["code"] = user!!["code"].orEmpty()
            params["realname"] = inputName!!.text.toString()
            params["bio"] = inputBio!!.text.toString()
            val strReq = object : StringRequest(Request.Method.POST, "http://saoshyant.net/Life/editname.php", Response.Listener { response ->
                //response from the server
                // Log.e(EditProfile.class.getSimpleName(), response);
                try {
                    val responseObj = JSONObject(response)
                    val feedArray = responseObj.getJSONArray("user")
                    val feedObj = feedArray.get(0) as JSONObject

                    if (feedObj.getInt("success") == 1) {

                        val db = DatabaseHandler(applicationContext)
                        db.resetTables()
                        db.addUser(feedObj.getString("id"), feedObj.getString("code"), feedObj.getString("realname"), "home", feedObj.getString("profileImage"))

                        val intent = Intent()
                        intent.putExtra("name", feedObj.getString("realname"))
                        intent.putExtra("profileImage", feedObj.getString("profileImage"))
                        setResult(Activity.RESULT_OK, intent)
                        ok!!.visibility = View.VISIBLE
                        progressBar!!.visibility = View.GONE
                        finish()
                    } else {
                        ok!!.visibility = View.VISIBLE
                        progressBar!!.visibility = View.INVISIBLE
                        Snackbar.make(coordinatorLayout!!, R.string.errorproblem, Snackbar.LENGTH_LONG).show()
                    }


                } catch (e: JSONException) {
                    ok!!.visibility = View.VISIBLE
                    progressBar!!.visibility = View.INVISIBLE
                    // Toast.makeText(getApplicationContext(), "Error2: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            },
                    Response.ErrorListener {
                        //  Log.e(TAG, "Error1: " + error.getMessage());
                        Snackbar.make(coordinatorLayout!!, R.string.errorconection, Snackbar.LENGTH_SHORT).show()
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
        } else {
            Snackbar.make(coordinatorLayout!!, R.string.errorrname, Snackbar.LENGTH_LONG).show()
        }
    }

    private fun changeProfilePic() {

        val items = arrayOf(getText(R.string.takecamera).toString(), getText(R.string.takegallery).toString())
        val adapter = ArrayAdapter(this, android.R.layout.select_dialog_item, items)
        val builder = AlertDialog.Builder(this)

        builder.setTitle(R.string.selectpic)
        builder.setAdapter(adapter) { _, item ->
            //pick from camera
            if (item == 0) {
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

                mImageCaptureUri = Uri.fromFile(File(Environment.getExternalStorageDirectory(),
                        "life_" + System.currentTimeMillis().toString() + ".jpg"))

                intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri)

                try {
                    intent.putExtra("return-data", true)

                    startActivityForResult(intent, 1)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }

            } else { //pick from file
                val intent = Intent()

                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT

                startActivityForResult(Intent.createChooser(intent, "completeaction"), 3)
            }
        }

        val dialog = builder.create()
        dialog.show()


    }

    private fun editProfilePic(f: File) {
        ok!!.visibility = View.INVISIBLE
        progressBar!!.visibility = View.VISIBLE
        val one = object : Thread() {
            override fun run() {
                if (Upload().uploadFile(this@EditProfile, coordinatorLayout, f, "http://saoshyant.net/upload.php") == 200) {
                    user = db1!!.userDetails
                    val params = HashMap<String, String>()
                    params["tag"] = "editprofileimage"
                    params["id"] = user!!["idNo"].orEmpty()
                    params["code"] = user!!["code"].orEmpty()
                    params["profileImage"] = f.name
                    val strReq = object : StringRequest(Request.Method.POST, "http://saoshyant.net/Life/editprofileimage.php", Response.Listener { response ->
                        //response from the server
                        // Log.d(PhoneLogin1.class.getSimpleName(), response);
                        try {
                            val responseObj = JSONObject(response)
                            val feedArray = responseObj.getJSONArray("user")
                            val feedObj = feedArray.get(0) as JSONObject


                            if (feedObj.getInt("success") == 1) {

                                db1!!.resetTables()
                                db1!!.addUser(feedObj.getString("id"), feedObj.getString("code"), feedObj.getString("realname"), "home", feedObj.getString("profileImage"))

                                val intent = Intent()
                                intent.putExtra("realname", feedObj.getString("realname"))
                                intent.putExtra("profileImage", feedObj.getString("profileImage"))
                                setResult(Activity.RESULT_OK, intent)
                                profilePic!!.setImageUrl("http://saoshyant.net/profileimages/" + feedObj.getString("profileImage"), MyApplication.instance!!.imageLoader)
                                coordinatorLayout!!.post {
                                    Snackbar.make(coordinatorLayout!!, R.string.success, Snackbar.LENGTH_LONG).show()

                                    ok!!.visibility = View.VISIBLE
                                    progressBar!!.visibility = View.GONE
                                }
                            } else {
                                coordinatorLayout!!.post {
                                    ok!!.visibility = View.VISIBLE
                                    progressBar!!.visibility = View.GONE
                                    Snackbar.make(coordinatorLayout!!, R.string.errorproblem, Snackbar.LENGTH_LONG).show()
                                }
                            }


                        } catch (e: JSONException) {
                            coordinatorLayout!!.post { Snackbar.make(coordinatorLayout!!, R.string.errorproblem, Snackbar.LENGTH_LONG).show() }

                            // Toast.makeText(getApplicationContext(), "Error2: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    },
                            Response.ErrorListener {
                                //  Log.e(TAG, "Error1: " + error.getMessage());
                                coordinatorLayout!!.post { Snackbar.make(coordinatorLayout!!, R.string.errorconection, Snackbar.LENGTH_LONG).show() }
                                Snackbar.make(coordinatorLayout!!, R.string.errorconection, Snackbar.LENGTH_LONG).show()
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
                if (f.exists()) f.delete()
                progressBar!!.post { progressBar!!.visibility = View.GONE }

            }
        }

        one.start()

    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (resultCode != -1)
            return
        when (requestCode) {
            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                progressBar!!.visibility = View.VISIBLE
                val result = CropImage.getActivityResult(data)
                if (resultCode == Activity.RESULT_OK) {
                    val resultUri = result.uri
                    val f = File(Environment.getExternalStorageDirectory(), "life_" + System.currentTimeMillis().toString() + ".jpg")
                    val imageData = ByteArray(1024)
                    var in0: InputStream? = null
                    var out0: OutputStream? = null
                    try {
                        in0 = contentResolver.openInputStream(resultUri)
                        out0 = FileOutputStream(f)

                        var bytesRead: Int
                        while (in0!!.read(imageData) > 0) {
                            bytesRead = in0.read(imageData)
                            out0.write(Arrays.copyOfRange(imageData, 0, Math.max(0, bytesRead)))
                        }

                    } catch (ignored: Exception) {
                    } finally {

                        try {
                            in0!!.close()
                            out0!!.close()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }

                    }
                    editProfilePic(f)
                }
            }
            1 -> CropImage.activity(mImageCaptureUri).setFixAspectRatio(true).setAspectRatio(1, 1).setOutputCompressQuality(50).start(this)

            3 -> {
                mImageCaptureUri = data?.data
                CropImage.activity(mImageCaptureUri).setFixAspectRatio(true).setAspectRatio(1, 1).setOutputCompressQuality(50).start(this)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 ->

                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    changeProfilePic()
                } else {

                    Snackbar.make(coordinatorLayout!!, "Permission denied", Snackbar.LENGTH_LONG).show()
                }
        }
    }

}
