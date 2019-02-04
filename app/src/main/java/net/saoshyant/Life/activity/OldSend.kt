package net.saoshyant.Life.activity

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Geocoder
import android.location.LocationManager
import android.media.ThumbnailUtils
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
import android.widget.*
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.ImageLoader
import com.android.volley.toolbox.StringRequest
import croppers.CropImage
import life.knowledge4.videotrimmer.utils.FileUtils
import net.saoshyant.Life.R
import net.saoshyant.Life.app.DatabaseHandler
import net.saoshyant.Life.app.MyApplication
import net.saoshyant.Life.app.Upload
import net.saoshyant.Life.app.realm.model.Post
import net.saoshyant.Life.app.realm.realm.RealmController
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.util.*


class OldSend : AppCompatActivity() {
    private var input_text: EditText? = null
    private var shareProgressBar: ProgressBar? = null
    private var imageName = ""
    private var strLocation = ""
    private var videoName = ""
    private var videoThumbName = ""
    private var imageView: ImageView? = null
    private var videoView: ImageView? = null
    private var send: ImageView? = null
    private var imageCaptureUri: Uri? = null
    private var imageDialog: AlertDialog? = null
    private var videoDialog: AlertDialog? = null
    private var location: TextView? = null
    private var coordinatorLayout: CoordinatorLayout? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.send)

        val haveDrafts = findViewById<TextView>(R.id.have_drafts)
        send = findViewById(R.id.send)
        shareProgressBar = findViewById(R.id.prg_share)
        input_text = findViewById(R.id.text)
        imageView = findViewById(R.id.image)
        videoView = findViewById(R.id.video)
        location = findViewById(R.id.location)
        coordinatorLayout = findViewById(R.id.coordinatorLayout)
        val intent = intent
        val action = intent.action
        val type = intent.type

        if (Intent.ACTION_SEND == action && type != null) {
            if ("text/plain" == type) {
                if (intent.getStringExtra(Intent.EXTRA_TEXT) != null)
                    input_text!!.setText(intent.getStringExtra(Intent.EXTRA_TEXT))
            }
        }
        setTitle(R.string.share)
        val two = object : Thread() {
            override fun run() {
                getLocation()
            }
        }

        two.start()
        setupShare()
        setupShare()
//        if (RealmController.with(this).hasPosts()) {
//            haveDrafts.visibility = View.VISIBLE
//            haveDrafts.setOnClickListener { startActivityForResult(Intent(this@OldSend, Drafts::class.java), 3) }
//        }


    }

    fun getLocation() {


        if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 3)
            } else {
                val three = object : Thread() {
                    override fun run() {
                        getLocation()
                    }
                }

                three.start()
            }
        } else {
            try {

                val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
                val net_loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                val geocoder = Geocoder(applicationContext, Locale.getDefault())
                val addresses = geocoder.getFromLocation(net_loc.latitude, net_loc.longitude, 1)
                val obj = addresses[0]
                strLocation = (obj.getAddressLine(0) + " (" + obj.countryName + " " + obj.adminArea + ")").replace("null", "")
                location!!.post { location!!.text = strLocation }
            } catch (ignored: Exception) {
                strLocation = ""
                location!!.post { location!!.text = strLocation }
            }

        }

    }

    fun setupShare() {

        send!!.setOnClickListener {
            send!!.visibility = View.INVISIBLE
            shareProgressBar!!.visibility = View.VISIBLE
            if (input_text!!.text.toString().length < 1 && imageName == "" && videoName == "") {
                Snackbar.make(coordinatorLayout!!, R.string.insertfeedback, Snackbar.LENGTH_LONG).show()
                send!!.visibility = View.VISIBLE
                shareProgressBar!!.visibility = View.INVISIBLE

            } else {
                Share()
            }
        }


        val items = arrayOf(getText(R.string.takecamera).toString(), getText(R.string.takegallery).toString())
        val adapter = ArrayAdapter(this, android.R.layout.select_dialog_item, items)
        val builder = AlertDialog.Builder(this)

        builder.setTitle(R.string.selectpic)
        builder.setAdapter(adapter) { dialog, item ->
            //pick from camera
            if (item == 0) {
                imageDialog!!.dismiss()
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

                imageCaptureUri = Uri.fromFile(File(Environment.getExternalStorageDirectory(),
                        "tmp_Life_" + System.currentTimeMillis().toString() + ".jpg"))

                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageCaptureUri)

                try {
                    intent.putExtra("return-data", true)

                    startActivityForResult(intent, 1)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }

            } else if (item == 1) { //pick from file
                imageDialog!!.dismiss()
                val intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(Intent.createChooser(intent, "completeaction"), 2)
            }
        }

        imageDialog = builder.create()
        imageView!!.setOnClickListener {
            if (imageName == "") {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 2)
                } else {
                    imageDialog!!.show()
                }


            } else {
                imageName = ""
                imageView!!.setImageResource(R.drawable.ic_add_a_photo)
            }
        }
        val videoBuilder = AlertDialog.Builder(this)

        videoBuilder.setTitle(R.string.label_select_video)
        videoBuilder.setAdapter(adapter) { dialog, item ->
            //pick from camera
            if (item == 0) {
                videoDialog!!.dismiss()
                startActivityForResult(Intent(MediaStore.ACTION_VIDEO_CAPTURE), 4)
            } else if (item == 1) {
                videoDialog!!.dismiss()
                pickFromGallery()
            }
        }

        videoDialog = videoBuilder.create()
        videoView!!.setOnClickListener {
            if (videoName == "") {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 4)
                } else {
                    videoDialog!!.show()
                }


            } else {
                videoName = ""
                videoThumbName = ""
                videoView!!.setImageResource(R.drawable.ic_video)
            }
        }
    }


    private fun pickFromGallery() {
        val intent = Intent()
        intent.setTypeAndNormalize("video/*")
        intent.action = Intent.ACTION_GET_CONTENT
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(Intent.createChooser(intent, getString(R.string.label_select_video)), 4)

    }

    fun Share() {

        val db1 = DatabaseHandler(applicationContext)

        val user: HashMap<String, String>
        user = db1.userDetails
        val params = HashMap<String, String>()
        params["tag"] = "share"
        params["id"] = user["idNo"]!!
        params["code"] = user["code"]!!
        params["profilePic"] = user["profileImage"]!!
        params["name"] = user["realname"]!!
        params["status"] = input_text!!.text.toString()
        params["location"] = location!!.text.toString()
        params["imageName"] = imageName
        params["videoName"] = videoName
        params["videoThumbName"] = videoThumbName
        val strReq = object : StringRequest(Request.Method.POST, "http://saoshyant.net/Life/share.php", Response.Listener { response ->
            //response from the server
            //   Log.d("a:", response);
            try {
                val responseObj = JSONObject(response)
                val feedArray = responseObj.getJSONArray("user")
                val feedObj = feedArray.get(0) as JSONObject

                if (feedObj.getString("success") != null) {
                    val res = feedObj.getString("success")

                    if (Integer.parseInt(res) == 1) {
                        val intent = Intent()
                        intent.putExtra("postId", feedObj.getString("postId"))
                        intent.putExtra("input_text", input_text!!.text.toString())
                        intent.putExtra("imageName", imageName)
                        intent.putExtra("video", videoName)
                        intent.putExtra("videoThumbName", videoThumbName)
                        intent.putExtra("location", location!!.text.toString())
                        setResult(Activity.RESULT_OK, intent)
                        Snackbar.make(coordinatorLayout!!, R.string.success, Snackbar.LENGTH_LONG).show()
                        finish()
                    } else {

                        send!!.visibility = View.VISIBLE
                        shareProgressBar!!.visibility = View.INVISIBLE
                        Snackbar.make(coordinatorLayout!!, R.string.errorproblem, Snackbar.LENGTH_LONG).show()
                    }


                }

            } catch (e: JSONException) {
                send!!.visibility = View.VISIBLE
                shareProgressBar!!.visibility = View.INVISIBLE
                //   Toast.makeText(getApplicationContext(), "Error2: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        },
                Response.ErrorListener {
                    //  Log.e(TAG, "Error1: " + error.getMessage());

                    send!!.visibility = View.VISIBLE
                    shareProgressBar!!.visibility = View.INVISIBLE
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {


        when (requestCode) {
            2 -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    imageDialog!!.show()
                } else {

                    Snackbar.make(coordinatorLayout!!, R.string.permission_denied, Snackbar.LENGTH_LONG).show()
                }
            }
            3 -> {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    val three = object : Thread() {
                        override fun run() {
                            getLocation()
                        }
                    }

                    three.start()
                } else {

                    Snackbar.make(coordinatorLayout!!, R.string.permission_denied, Snackbar.LENGTH_LONG).show()
                }

            }
            4 -> {

                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    videoDialog!!.show()
                } else {

                    Snackbar.make(coordinatorLayout!!, R.string.permission_denied, Snackbar.LENGTH_LONG).show()
                }

            }
        }// other 'case' lines to check for other
        // permissions this app might request
    }


    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != -1)
            return
        when (requestCode) {
            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                send!!.visibility = View.INVISIBLE
                shareProgressBar!!.visibility = View.VISIBLE
                val result = CropImage.getActivityResult(data)
                if (resultCode == Activity.RESULT_OK) {
                    val resultUri = result.uri
                    imageView!!.setImageURI(resultUri)
                    val f = File(Environment.getExternalStorageDirectory(), "life_" + System.currentTimeMillis().toString() + ".jpg")
                    val imageData = ByteArray(1024)
                    var in0: InputStream? = null
                    var out0: OutputStream? = null
                    try {
                        in0 = contentResolver.openInputStream(resultUri)
                        out0 = FileOutputStream(f)

                        var bytesRead: Int
                        while ((in0!!.read(imageData)) > 0) {
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
                    val one = object : Thread() {
                        override fun run() {
                            if (Upload().uploadFile(this@OldSend, coordinatorLayout, f, "http://saoshyant.net/uploadimage.php") == 200) {
                                imageName = f.name

                            }

                            if (f.exists()) f.delete()
                            shareProgressBar!!.post {
                                send!!.visibility = View.VISIBLE
                                shareProgressBar!!.visibility = View.INVISIBLE
                            }

                        }
                    }

                    one.start()

                }
            }
            1 -> CropImage.activity(imageCaptureUri).setOutputCompressQuality(50).start(this)

            2 -> {
                imageCaptureUri = data!!.data
                CropImage.activity(imageCaptureUri).setOutputCompressQuality(50).start(this)
            }

            3 -> {
                input_text!!.setText(data!!.getStringExtra("text"))
                imageName = data.getStringExtra("image")
                videoName = data.getStringExtra("video")
                videoThumbName = data.getStringExtra("videoThumb")
                if (imageName != "") {
                    MyApplication.instance!!.imageLoader.get("http://saoshyant.net/postsimages/$imageName", object : ImageLoader.ImageListener {
                        override fun onErrorResponse(volleyError: VolleyError) {

                        }

                        override fun onResponse(imageContainer: ImageLoader.ImageContainer, b: Boolean) {
                            imageView!!.setImageBitmap(imageContainer.bitmap)

                        }
                    })
                }
                if (videoName != "") {
                    MyApplication.instance!!.imageLoader.get("http://saoshyant.net/videoThumb/$videoThumbName", object : ImageLoader.ImageListener {
                        override fun onErrorResponse(volleyError: VolleyError) {

                        }

                        override fun onResponse(imageContainer: ImageLoader.ImageContainer, b: Boolean) {
                            videoView!!.setImageBitmap(imageContainer.bitmap)

                        }
                    })
                }
            }
            4 -> {
                val selectedUri = data!!.data
                if (selectedUri != null) {
                    val intent = Intent(this, TrimmerActivity::class.java)
                    intent.putExtra("EXTRA_VIDEO_PATH", FileUtils.getPath(this, selectedUri))
                    startActivityForResult(intent, 5)
                } else {
                    Toast.makeText(this@OldSend, R.string.toast_cannot_retrieve_selected_video, Toast.LENGTH_SHORT).show()
                }
            }
            5 -> {
                send!!.visibility = View.INVISIBLE
                shareProgressBar!!.visibility = View.VISIBLE
                val uri = data!!.data
                val f = File(uri!!.path)
                val bitmap = ThumbnailUtils.createVideoThumbnail(uri.path, MediaStore.Images.Thumbnails.MICRO_KIND)
                videoView!!.setImageBitmap(bitmap)
                val four = object : Thread() {
                    override fun run() {
                        if (Upload().uploadFile(this@OldSend, coordinatorLayout, f, "http://saoshyant.net/uploadvideo.php") == 200) {

                            try {
                                val file = File(Environment.getExternalStorageDirectory(), "life_" + System.currentTimeMillis().toString() + ".png")
                                val fOut = FileOutputStream(file)
                                bitmap.compress(Bitmap.CompressFormat.PNG, 85, fOut)
                                fOut.flush()
                                fOut.close()
                                if (Upload().uploadFile(this@OldSend, coordinatorLayout, file, "http://saoshyant.net/uploadvideothumb.php") == 200) {
                                    videoName = f.name
                                    videoThumbName = file.name
                                }
                                if (f.exists()) f.delete()
                                if (file.exists()) file.delete()
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }


                        }


                        shareProgressBar!!.post {
                            send!!.visibility = View.VISIBLE
                            shareProgressBar!!.visibility = View.INVISIBLE
                        }

                    }
                }

                four.start()
            }
        }


    }

    override fun onBackPressed() {

        val dialog = Dialog(this@OldSend)
        dialog.setContentView(R.layout.dialog)
        val textview = dialog.findViewById<TextView>(R.id.text)
        textview.setText(R.string.save_draft)
        dialog.setTitle("Drafts")
        val yes = dialog.findViewById<Button>(R.id.yes)
        val no = dialog.findViewById<Button>(R.id.no)

        yes.setOnClickListener {
            val post = Post()
            post.id = RealmController.instance!!.posts.size + System.currentTimeMillis()
            post.text = input_text!!.text.toString()
            post.imageUrl = imageName
            post.video = videoName
            post.videoThumbName = videoThumbName
            val realm = RealmController.with(this@OldSend).realm
            // Persist your data easily
            realm.beginTransaction()
            realm.copyToRealm(post)
            realm.commitTransaction()
            dialog.dismiss()
            finish()
        }

        no.setOnClickListener {
            dialog.dismiss()
            finish()
        }
        if (imageName != "" || videoName != "" || input_text!!.text.toString() != "") {

            dialog.show()
        } else {
            dialog.dismiss()
            super.onBackPressed()
        }


        //  super.onBackPressed();


    }


}