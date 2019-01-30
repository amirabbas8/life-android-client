package net.saoshyant.Life.app.FeedListAdapters

import android.app.Activity
import android.content.Intent
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.NetworkImageView
import com.android.volley.toolbox.StringRequest
import ir.adad.client.Banner
import net.saoshyant.Life.R
import net.saoshyant.Life.activity.PostActivity
import net.saoshyant.Life.activity.Profile
import net.saoshyant.Life.app.DatabaseHandler
import net.saoshyant.Life.app.FeedItems.NotificationFeedItem
import net.saoshyant.Life.app.MyApplication

import org.json.JSONException
import org.json.JSONObject
import java.lang.Exception
import java.util.*


class NotificationAdapter(private val activity: Activity, private val notificationList: List<NotificationFeedItem>, private val coordinatorLayout: CoordinatorLayout) : RecyclerView.Adapter<NotificationAdapter.MyViewHolder>() {

    inner class MyViewHolder constructor(view: View) : RecyclerView.ViewHolder(view) {
        val txtName: TextView = view.findViewById(R.id.txt_name) as TextView
        val niwProfile: NetworkImageView = view.findViewById(R.id.niw_profile) as NetworkImageView
        val imgBtnFollow: ImageButton = view.findViewById(R.id.img_btn_follow) as ImageButton
        val prgFollowing: ProgressBar = view.findViewById(R.id.prg_following) as ProgressBar
        val feedItem: RelativeLayout = view.findViewById(R.id.feed_item) as RelativeLayout
        val layout: RelativeLayout = view.findViewById(R.id.coordinator_layout) as RelativeLayout

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.feed_item_people, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        if (notificationList[position].id == "adad") {
            holder.feedItem.visibility = View.GONE
            val banner = Banner(activity)
            holder.layout.addView(banner)
        } else {
            holder.feedItem.visibility = View.VISIBLE
            val notificationFeedItem = notificationList[position]
            try {
                holder.niwProfile.setImageUrl("http://saoshyant.net/profileimages/" + notificationFeedItem.profilePic!!, imageLoader)
            } catch (e: Exception) {
            }
            if ("" == notificationFeedItem.profilePic) {
                holder.niwProfile.setImageResource(R.drawable.ic_account_circle_black)
            }

            holder.niwProfile.setOnClickListener {
                val intent = Intent(activity, Profile::class.java)
                intent.putExtra("id", notificationFeedItem.userId.toString())
                intent.putExtra("name", notificationFeedItem.name.toString())
                intent.putExtra("profileImage", notificationFeedItem.profilePic.toString())
                activity.startActivity(intent)
            }


            val db1 = DatabaseHandler(activity.applicationContext)
            val user: HashMap<String, String>
            user = db1.userDetails
            if (user["idNo"] == notificationFeedItem.userId) {
                holder.imgBtnFollow.visibility = View.INVISIBLE
            } else {
                holder.imgBtnFollow.visibility = View.VISIBLE
                if (notificationFeedItem.myFollowStatus) {
                    holder.imgBtnFollow.setImageResource(R.drawable.ic_remove_circle_black)
                } else if (!notificationFeedItem.myFollowStatus) {
                    holder.imgBtnFollow.setImageResource(R.drawable.ic_add_circle_black)
                }
                holder.imgBtnFollow.setOnClickListener {
                    notificationFeedItem.prgFollowing = true
                    notifyDataSetChanged()
                    processFollow(notificationFeedItem.userId, notificationFeedItem)
                }
            }
            if (notificationFeedItem.prgFollowing) {

                holder.prgFollowing.visibility = View.VISIBLE
                holder.imgBtnFollow.visibility = View.INVISIBLE

            } else if (!notificationFeedItem.prgFollowing) {

                holder.prgFollowing.visibility = View.INVISIBLE
                holder.imgBtnFollow.visibility = View.VISIBLE

            }
            if (notificationFeedItem.postId == "") {
                holder.txtName.setOnClickListener {
                    val intent = Intent(activity, Profile::class.java)
                    intent.putExtra("id", notificationFeedItem.userId.toString())
                    intent.putExtra("name", notificationFeedItem.name.toString())
                    intent.putExtra("profileImage", notificationFeedItem.profilePic.toString())
                    activity.startActivity(intent)
                }
                holder.txtName.text = notificationFeedItem.name!! + " started following you"
            } else {
                holder.txtName.setOnClickListener {
                    val intent = Intent(activity, PostActivity::class.java)
                    intent.putExtra("postId", notificationFeedItem.postId.toString())
                    activity.startActivity(intent)
                }
                holder.txtName.text = notificationFeedItem.name!! + " liked your post"
                holder.imgBtnFollow.visibility = View.GONE
                holder.prgFollowing.visibility = View.GONE
            }
        }
    }

    override fun getItemCount(): Int {
        return notificationList.size
    }

    private fun processFollow(userid: String?, notificationFeedItem: NotificationFeedItem) {


        val db1 = DatabaseHandler(activity.applicationContext)
        val user: HashMap<String, String>
        user = db1.userDetails
        val params = HashMap<String, String>()
        params["tag"] = "add_delete_friend"
        params["id"] = user["idNo"]!!
        params["code"] = user["code"]!!
        params["userId"] = userid!!
        val strReq = object : StringRequest(Request.Method.POST, "http://saoshyant.net/Life/add_delete_friend.php", Response.Listener { response ->
            //response from the server
            //Log.d("a:", response);
            try {
                val responseObj = JSONObject(response)
                val feedArray = responseObj.getJSONArray("user")
                val feedObj = feedArray.get(0) as JSONObject
                if (feedObj.getString("success") != null) {
                    val res = feedObj.getString("success")
                    if (Integer.parseInt(res) == 51) {
                        notificationFeedItem.prgFollowing = false
                        notifyDataSetChanged()
                        Snackbar.make(coordinatorLayout, R.string.errorfunblock, Snackbar.LENGTH_LONG).show()

                    } else if (Integer.parseInt(res) == 52) {
                        notificationFeedItem.prgFollowing = false
                        notifyDataSetChanged()
                        Snackbar.make(coordinatorLayout, R.string.errorblock, Snackbar.LENGTH_LONG).show()

                    } else if (Integer.parseInt(res) == 1) {
                        notificationFeedItem.myFollowStatus = false
                        notificationFeedItem.prgFollowing = false
                        notifyDataSetChanged()

                    } else if (Integer.parseInt(res) == 2) {
                        notificationFeedItem.myFollowStatus = true
                        notificationFeedItem.prgFollowing = false
                        notifyDataSetChanged()
                    } else {
                        notificationFeedItem.prgFollowing = false
                        notifyDataSetChanged()
                        Snackbar.make(coordinatorLayout, R.string.errorproblem, Snackbar.LENGTH_LONG).show()
                    }
                }
            } catch (e: JSONException) {
                // Toast.makeText(activity.getApplicationContext(), "Error2: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        },
                Response.ErrorListener {
                    //  Log.e(TAG, "Error1: " + error.getMessage());
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
