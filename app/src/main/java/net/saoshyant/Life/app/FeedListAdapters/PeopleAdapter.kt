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
import android.widget.TextView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.NetworkImageView
import com.android.volley.toolbox.StringRequest
import net.saoshyant.Life.R
import net.saoshyant.Life.activity.Profile
import net.saoshyant.Life.app.DatabaseHandler
import net.saoshyant.Life.app.FeedItems.PeopleFeedItem
import net.saoshyant.Life.app.MyApplication
import org.json.JSONException
import org.json.JSONObject
import java.util.*


class PeopleAdapter(private val activity: Activity, private val peopleList: List<PeopleFeedItem>, private val coordinatorLayout: CoordinatorLayout) : RecyclerView.Adapter<PeopleAdapter.MyViewHolder>() {

     inner class MyViewHolder  constructor(view: View) : RecyclerView.ViewHolder(view) {
         val txtName: TextView
         val niwProfile: NetworkImageView
         val imgBtnFollow: ImageButton
         val prgFollowing: ProgressBar

        init {
            txtName = view.findViewById(R.id.txt_name)
            niwProfile = view.findViewById(R.id.niw_profile)
            imgBtnFollow = view.findViewById(R.id.img_btn_follow)
            prgFollowing = view.findViewById(R.id.prg_following)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.feed_item_people, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val peopleFeedItem = peopleList[position]
        holder.txtName.text = peopleFeedItem.name
      try {
          holder.niwProfile.setImageUrl("http://saoshyant.net/profileimages/" + peopleFeedItem.profilePic!!, imageLoader)
      }catch (e:Exception){}
        if ("" == peopleFeedItem.profilePic) {
            holder.niwProfile.setImageResource(R.drawable.ic_account_circle_black)
        }
        holder.txtName.setOnClickListener {
            val intent = Intent(activity, Profile::class.java)
            intent.putExtra("id", peopleFeedItem.userId.toString())
            intent.putExtra("name", peopleFeedItem.name.toString())
            intent.putExtra("profileImage", peopleFeedItem.profilePic.toString())
            activity.startActivity(intent)
        }
        holder.niwProfile.setOnClickListener {
            val intent = Intent(activity, Profile::class.java)
            intent.putExtra("id", peopleFeedItem.userId.toString())
            intent.putExtra("name", peopleFeedItem.name.toString())
            intent.putExtra("profileImage", peopleFeedItem.profilePic.toString())
            activity.startActivity(intent)
        }


        val db1 = DatabaseHandler(activity.applicationContext)
        val user: HashMap<String, String>
        user = db1.userDetails
        if (user["idNo"] == peopleFeedItem.userId) {
            holder.imgBtnFollow.visibility = View.GONE
        } else {
            holder.imgBtnFollow.visibility = View.VISIBLE
            if (peopleFeedItem.myFollowStatus) {
                holder.imgBtnFollow.setImageResource(R.drawable.ic_remove_circle_black)
            } else if (!peopleFeedItem.myFollowStatus) {
                holder.imgBtnFollow.setImageResource(R.drawable.ic_add_circle_black)
            }
            holder.imgBtnFollow.setOnClickListener {
                peopleFeedItem.prgFollowing = true
                notifyDataSetChanged()
                processFollow(peopleFeedItem.userId, peopleFeedItem)
            }
        }
        if (peopleFeedItem.prgFollowing) {
            if (user["idNo"] == peopleFeedItem.userId) {
                holder.imgBtnFollow.visibility = View.GONE
                holder.prgFollowing.visibility = View.GONE
            } else {
                holder.prgFollowing.visibility = View.VISIBLE
                holder.imgBtnFollow.visibility = View.INVISIBLE
            }
        } else if (!peopleFeedItem.prgFollowing) {

            if (user["idNo"] == peopleFeedItem.userId) {
                holder.imgBtnFollow.visibility = View.GONE
                holder.prgFollowing.visibility = View.GONE
            } else {
                holder.prgFollowing.visibility = View.INVISIBLE
                holder.imgBtnFollow.visibility = View.VISIBLE
            }
        }
    }

    override fun getItemCount(): Int {
        return peopleList.size
    }

    private fun processFollow(userid: String?, peopleFeedItem: PeopleFeedItem) {


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
                        peopleFeedItem.prgFollowing = false
                        notifyDataSetChanged()
                        Snackbar.make(coordinatorLayout, R.string.errorfunblock, Snackbar.LENGTH_LONG).show()

                    } else if (Integer.parseInt(res) == 52) {
                        peopleFeedItem.prgFollowing = false
                        notifyDataSetChanged()
                        Snackbar.make(coordinatorLayout, R.string.errorblock, Snackbar.LENGTH_LONG).show()

                    } else if (Integer.parseInt(res) == 1) {
                        peopleFeedItem.myFollowStatus = false
                        peopleFeedItem.prgFollowing = false
                        notifyDataSetChanged()

                    } else if (Integer.parseInt(res) == 2) {
                        peopleFeedItem.myFollowStatus = true
                        peopleFeedItem.prgFollowing = false
                        notifyDataSetChanged()
                    } else {
                        peopleFeedItem.prgFollowing = false
                        notifyDataSetChanged()
                        Snackbar.make(coordinatorLayout, R.string.errorproblem, Snackbar.LENGTH_LONG).show()
                    }
                }
            } catch (e: JSONException) {
                peopleFeedItem.prgFollowing = false
                notifyDataSetChanged()
                // Toast.makeText(activity.getApplicationContext(), "Error2: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        },
                Response.ErrorListener {
                    peopleFeedItem.prgFollowing = false
                    notifyDataSetChanged()
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
