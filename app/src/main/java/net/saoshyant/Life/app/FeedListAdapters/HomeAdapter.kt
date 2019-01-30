package net.saoshyant.Life.app.FeedListAdapters

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.util.Linkify
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.ImageLoader
import com.android.volley.toolbox.NetworkImageView
import com.android.volley.toolbox.StringRequest
import com.sackcentury.shinebuttonlib.ShineButton
import ir.adad.client.Banner
import net.saoshyant.Life.R
import net.saoshyant.Life.activity.Likes
import net.saoshyant.Life.activity.Profile
import net.saoshyant.Life.activity.VideoPlayer
import net.saoshyant.Life.app.DatabaseHandler
import net.saoshyant.Life.app.FeedItems.HomeFeedItem
import net.saoshyant.Life.app.MyApplication
import org.json.JSONException
import org.json.JSONObject
import java.lang.Long
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern


class HomeAdapter(private val activity: Activity, private val homeList: MutableList<HomeFeedItem>, private val coordinatorLayout: CoordinatorLayout?, private val imageView: NetworkImageView) : RecyclerView.Adapter<HomeAdapter.MyViewHolder>() {

    inner class MyViewHolder constructor(view: View) : RecyclerView.ViewHolder(view) {
        val txtName: TextView = view.findViewById(R.id.txt_name)
        val txtDate: TextView = view.findViewById(R.id.txt_date)
        val nLike: TextView = view.findViewById(R.id.nLike)
        val text: TextView = view.findViewById(R.id.txt_text)
        val location: TextView
        val niwImage: NetworkImageView
        val videoView: ImageView
        val niwProfile: NetworkImageView = view.findViewById(R.id.profile_image)
        val imgShare: ImageButton
        val imgBtnDelete: ImageButton
        val imgBtnReport: ImageButton
        val imgBtnLike: ShineButton = view.findViewById(R.id.like)
        val prgLiking: ProgressBar
        val prgDeleting: ProgressBar
        val feedItem: RelativeLayout
        val layout: RelativeLayout
        val rlLocation: RelativeLayout
        val media: RecyclerView

        init {
            imgBtnLike.init(activity)
            imgBtnDelete = view.findViewById(R.id.delete)
            imgBtnReport = view.findViewById(R.id.report)
            imgShare = view.findViewById(R.id.img_share)
            niwImage = view.findViewById(R.id.image)
            prgDeleting = view.findViewById(R.id.prg_deleting)
            prgLiking = view.findViewById(R.id.prg_liking)
            feedItem = view.findViewById(R.id.feed_item)
            layout = view.findViewById(R.id.coordinator_layout)
            location = view.findViewById(R.id.location)
            rlLocation = view.findViewById(R.id.rl5)
            videoView = view.findViewById(R.id.video)
            media = view.findViewById(R.id.media)

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.feed_item_home, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        if (homeList[position].id == "adad") {
            holder.feedItem.visibility = View.GONE
            val banner = Banner(activity)
            holder.layout.addView(banner)
        } else {
            holder.feedItem.visibility = View.VISIBLE
            val homeFeedItem = homeList[position]
            val db1 = DatabaseHandler(activity)
            val user: HashMap<String, String>
            user = db1.userDetails
            user["idNo"]
            if (homeFeedItem.location == "") {
                holder.rlLocation.visibility = View.GONE
            } else {
                holder.rlLocation.visibility = View.VISIBLE
                holder.location.text = homeFeedItem.location
            }

            holder.txtName.text = homeFeedItem.name
            holder.txtName.setOnClickListener {
                val intent = Intent(activity, Profile::class.java)
                intent.putExtra("id", homeFeedItem.userId.toString())
                intent.putExtra("name", homeFeedItem.name.toString())
                intent.putExtra("profileImage", homeFeedItem.profilePic.toString())
                activity.startActivity(intent)
            }
            if (homeFeedItem.timeStamp == "now") {
                holder.txtDate.setText(R.string.now)
            } else {
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
                sdf.timeZone = TimeZone.getTimeZone("GMT")
                var date = ""
                try {
                    val cal = Calendar.getInstance()
                    cal.time = sdf.parse(homeFeedItem.timeStamp)
                    val time = cal.timeInMillis
                    val curr = System.currentTimeMillis()
                    val diff = (curr - time) / 1000
                    date = when {
                        diff < 60 -> Long.toString(diff) + " " + activity.getString(R.string.sec)
                        diff < 3600 -> Long.toString(diff / 60) + " " + activity.getString(R.string.min)
                        diff < 86400 -> Long.toString(diff / 3600) + " " + activity.getString(R.string.hour)
                        diff < 86400 * 30 -> Long.toString(diff / 86400) + " " + activity.getString(R.string.day)
                        else -> Long.toString(diff / (86400 * 7)) + " " + activity.getString(R.string.week)
                    }
                } catch (ignored: ParseException) {
                }

                holder.txtDate.text = date
            }

            holder.text.text = homeFeedItem.text
            holder.text.setOnLongClickListener {
                (activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).primaryClip = ClipData.newPlainText("Life App", holder.text.text.toString() + "\nLife")
                if (coordinatorLayout != null)
                    Snackbar.make(coordinatorLayout, R.string.copied, Snackbar.LENGTH_LONG).show()
                true
            }
            holder.imgShare.setOnClickListener {
                val sharingIntent = Intent(Intent.ACTION_SEND)
                sharingIntent.type = "text/plain"
                sharingIntent.putExtra(Intent.EXTRA_SUBJECT, R.string.app_name)
                sharingIntent.putExtra(Intent.EXTRA_TEXT, homeFeedItem.text!! + "\nLife Application")
                activity.startActivity(sharingIntent)
            }
            val tagMatcher = Pattern.compile("[#]+\\w+\\b")
            val newActivityURL = "lifehash://saoshyant.net/"

            Linkify.addLinks(holder.text, Linkify.WEB_URLS)
            Linkify.addLinks(holder.text, tagMatcher, newActivityURL)

            val tagMatcher2 = Pattern.compile("[@]+[A-Za-z0-9-_]+\\b")
            val newActivityURL2 = "lifepro://saoshyant.net/"
            Linkify.addLinks(holder.text, tagMatcher2, newActivityURL2)


            //start media
            val mAdapter = mediaAdapter(activity, homeFeedItem.media)
            holder.media.setHasFixedSize(true)
            val mLayoutManager = LinearLayoutManager(activity)
            holder.media.layoutManager = mLayoutManager
            holder.media.itemAnimator = DefaultItemAnimator()
            holder.media.adapter = mAdapter


            //end media

            holder.nLike.text = homeFeedItem.nLike
            holder.nLike.setOnClickListener { activity.startActivity(Intent(activity, Likes::class.java).putExtra("id", homeFeedItem.id)) }

            if ("http://saoshyant.net/postsimages/" == homeFeedItem.getimage()) {
                holder.niwImage.visibility = View.GONE
            } else {
                holder.niwImage.visibility = View.VISIBLE
                try {
                    holder.niwImage.setImageUrl(homeFeedItem.getimage(), imageLoader)
                } catch (e: Exception) {
                }
            }
            holder.niwImage.setOnClickListener {
                if (imageView != null) {
                    if (imageView.visibility == View.VISIBLE) {
                        imageView.visibility = View.GONE
                    } else {
                        imageView.visibility = View.VISIBLE
                        imageView.setImageUrl(homeFeedItem.getimage(), imageLoader)
                    }
                }
            }
            if (homeFeedItem.videoThumb == "") {
                holder.videoView.visibility = View.GONE
            } else {
                holder.videoView.visibility = View.VISIBLE
                MyApplication.instance!!.imageLoader.get("http://saoshyant.net/videoThumb/" + homeFeedItem.videoThumb!!, object : ImageLoader.ImageListener {
                    override fun onErrorResponse(volleyError: VolleyError) {

                    }

                    override fun onResponse(imageContainer: ImageLoader.ImageContainer, b: Boolean) {
                        holder.videoView.background = BitmapDrawable(activity.resources, imageContainer.bitmap)
                        holder.videoView.setImageResource(R.drawable.icon_video_play)
                    }
                })
                holder.videoView.setOnClickListener {
                    val intent = Intent(activity, VideoPlayer::class.java)
                    intent.putExtra("videoName", homeFeedItem.video.toString())
                    activity.startActivity(intent)
                }
            }

//            if ("" == homeFeedItem.profilePic) {
//                try {
//                    holder.niwProfile.setImageUrl("http://saoshyant.net/profileimages/" + homeFeedItem.profilePic!!, imageLoader)
//                    holder.niwProfile.setImageResource(R.drawable.ic_account_circle_black)
//
//                } catch (e: Exception) {
//                }
//            } else {
//                try {
//                    holder.niwProfile.setImageUrl("http://saoshyant.net/profileimages/" + homeFeedItem.profilePic!!, imageLoader)
//                } catch (e: Exception) {
//                }
//            }

            holder.niwProfile.setOnClickListener {
                val intent = Intent(activity, Profile::class.java)
                intent.putExtra("id", homeFeedItem.userId.toString())
                intent.putExtra("name", homeFeedItem.name.toString())
                intent.putExtra("profileImage", homeFeedItem.profilePic.toString())
                activity.startActivity(intent)
            }
            if (homeFeedItem.isPrgDeleting) {

                holder.prgDeleting.visibility = View.VISIBLE
                holder.imgBtnDelete.visibility = View.INVISIBLE

            } else {

                holder.prgDeleting.visibility = View.INVISIBLE
                holder.imgBtnDelete.visibility = View.VISIBLE

            }
            if (homeFeedItem.prgLiking) {

                holder.prgLiking.visibility = View.VISIBLE
                holder.imgBtnLike.visibility = View.INVISIBLE

            } else {

                holder.prgLiking.visibility = View.INVISIBLE
                holder.imgBtnLike.visibility = View.VISIBLE

            }

            holder.imgBtnLike.isChecked = homeFeedItem.isMyLike
            holder.imgBtnLike.setOnClickListener {
                homeFeedItem.prgLiking = true
                notifyDataSetChanged()
                like(homeFeedItem.id, homeFeedItem.userId, homeFeedItem)
            }
            holder.imgBtnDelete.setOnClickListener {
                homeFeedItem.isPrgDeleting = true
                notifyDataSetChanged()
                delete(homeFeedItem.id, homeFeedItem)
            }
            holder.imgBtnReport.setOnClickListener { report(homeFeedItem.id) }
        }


    }

    override fun getItemCount(): Int {
        return homeList.size
    }


    private fun like(postId: String?, userId: String?, homeFeedItem: HomeFeedItem) {


        val db1 = DatabaseHandler(activity.applicationContext)
        val user: HashMap<String, String>
        user = db1.userDetails
        val params = HashMap<String, String>()
        params["tag"] = "like_dislike"
        params["id"] = user["idNo"]!!
        params["code"] = user["code"]!!
        params["postid"] = postId!!
        params["userid"] = userId!!
        val strReq = object : StringRequest(Method.POST, "http://saoshyant.net/Life/like_dislike.php", Response.Listener { response ->
            //response from the server
            // Log.d("a:", response);
            try {
                val responseObj = JSONObject(response)
                val feedArray = responseObj.getJSONArray("post")
                val feedObj = feedArray.get(0) as JSONObject
                if (feedObj.getString("success") != null) {
                    val res = feedObj.getString("success")
                    if (Integer.parseInt(res) == 1) {
                        homeFeedItem.isMyLike = true
                        homeFeedItem.nLike = (Integer.parseInt(homeFeedItem.nLike) + 1).toString()


                    } else if (Integer.parseInt(res) == 2) {
                        homeFeedItem.isMyLike = false
                        homeFeedItem.nLike = (Integer.parseInt(homeFeedItem.nLike) - 1).toString()
                    } else {

                        if (coordinatorLayout != null)
                            Snackbar.make(coordinatorLayout, R.string.errorproblem, Snackbar.LENGTH_LONG).show()

                    }
                    homeFeedItem.prgLiking = false
                    notifyDataSetChanged()
                }
            } catch (e: JSONException) {
                homeFeedItem.prgLiking = false
                notifyDataSetChanged()
                // Toast.makeText(activity.getApplicationContext(), "Error2: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        },
                Response.ErrorListener {
                    //  Log.e(TAG, "Error1: " + error.getMessage());
                    homeFeedItem.prgLiking = false
                    notifyDataSetChanged()
                    if (coordinatorLayout != null)
                        Snackbar.make(coordinatorLayout, R.string.errorconection, Snackbar.LENGTH_LONG).show()
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


    private fun delete(postid: String?, homeFeedItem: HomeFeedItem) {


        val db1 = DatabaseHandler(activity.applicationContext)
        val user: HashMap<String, String>
        user = db1.userDetails

        val params = HashMap<String, String>()
        params["tag"] = "delete"
        params["id"] = user["idNo"]!!
        params["code"] = user["code"]!!
        params["postid"] = postid!!
        val strReq = object : StringRequest(Method.POST, "http://saoshyant.net/Life/deletepost.php", Response.Listener { response ->
            //response from the server
            Log.d("", response)
            try {
                val responseObj = JSONObject(response)
                val feedArray = responseObj.getJSONArray("post")
                val feedObj = feedArray.get(0) as JSONObject
                if (feedObj.getString("success") != null) {
                    val res = feedObj.getString("success")

                    if (Integer.parseInt(res) == 1) {
                        homeFeedItem.isPrgDeleting = false
                        homeList.remove(homeFeedItem)
                        notifyDataSetChanged()

                    } else if (Integer.parseInt(res) == 2) {
                        homeFeedItem.isPrgDeleting = false
                        if (coordinatorLayout != null)
                            Snackbar.make(coordinatorLayout, R.string.errorproblem, Snackbar.LENGTH_LONG).show()

                        notifyDataSetChanged()
                    } else {
                        homeFeedItem.isPrgDeleting = false
                        notifyDataSetChanged()
                        if (coordinatorLayout != null)
                            Snackbar.make(coordinatorLayout, R.string.errorproblem, Snackbar.LENGTH_LONG).show()

                    }
                }
            } catch (e: JSONException) {
                homeFeedItem.isPrgDeleting = false
                notifyDataSetChanged()
                // Toast.makeText(activity.getApplicationContext(), "Error2: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        },
                Response.ErrorListener {
                    //  Log.e(TAG, "Error1: " + error.getMessage());

                    homeFeedItem.isPrgDeleting = false
                    notifyDataSetChanged()
                    if (coordinatorLayout != null)
                        Snackbar.make(coordinatorLayout, R.string.errorconection, Snackbar.LENGTH_LONG).show()
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


    private fun report(postid: String?) {


        val db1 = DatabaseHandler(activity.applicationContext)
        val user: HashMap<String, String>
        user = db1.userDetails
        val params = HashMap<String, String>()
        params["tag"] = "report"
        params["id"] = user["idNo"]!!
        params["code"] = user["code"]!!
        params["postid"] = postid!!
        params["kind"] = "Life_post"
        params["comment"] = "comment"
        val strReq = object : StringRequest(Method.POST, "http://saoshyant.net/Life/report.php", Response.Listener { response ->
            //response from the server
            Log.d("", response)
            try {
                val responseObj = JSONObject(response)
                val feedArray = responseObj.getJSONArray("user")
                val feedObj = feedArray.get(0) as JSONObject
                if (feedObj.getString("success") != null) {
                    val res = feedObj.getString("success")

                    if (Integer.parseInt(res) == 1) {
                        if (coordinatorLayout != null)
                            Snackbar.make(coordinatorLayout, R.string.reported, Snackbar.LENGTH_LONG).show()
                    } else {
                        if (coordinatorLayout != null)
                            Snackbar.make(coordinatorLayout, R.string.errorproblem, Snackbar.LENGTH_LONG).show()

                    }
                }
            } catch (e: JSONException) {

                notifyDataSetChanged()
                // Toast.makeText(activity.getApplicationContext(), "Error2: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        },
                Response.ErrorListener {
                    //  Log.e(TAG, "Error1: " + error.getMessage());
                    if (coordinatorLayout != null)
                        Snackbar.make(coordinatorLayout, R.string.errorconection, Snackbar.LENGTH_LONG).show()
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

    companion object {
        private val imageLoader = MyApplication.instance!!.imageLoader
    }

}
