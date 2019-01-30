package net.saoshyant.Life.activity

import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import net.saoshyant.Life.R
import net.saoshyant.Life.app.DatabaseHandler
import net.saoshyant.Life.app.FeedItems.NotificationFeedItem
import net.saoshyant.Life.app.FeedListAdapters.NotificationAdapter
import net.saoshyant.Life.app.MyApplication
import net.saoshyant.Life.app.refreshLayout.RefreshLayout
import org.json.JSONException
import org.json.JSONObject
import java.util.*


class Notification : AppCompatActivity() {

    private val notificationList = ArrayList<NotificationFeedItem>()
    private var mAdapter: NotificationAdapter? = null
    private var refreshLayout: RefreshLayout? = null
    private var coordinatorLayout: CoordinatorLayout? = null
    private var view = false


    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_recycler)
        title = "notification"
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        coordinatorLayout = findViewById(R.id.coordinatorLayout)
        refreshLayout = findViewById(R.id.refreshLayout)
        mAdapter = NotificationAdapter(this, notificationList, coordinatorLayout!!)

        recyclerView.setHasFixedSize(true)
        val mLayoutManager = LinearLayoutManager(application)
        recyclerView.layoutManager = mLayoutManager
        recyclerView.addItemDecoration(android.support.v7.widget.DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = mAdapter


        refreshLayout!!.onRefreshListener = object : RefreshLayout.OnRefreshListener {
            override fun onRefresh() {
                notificationList.clear()
                mAdapter!!.notifyDataSetChanged()
                getNotificationData()


            }

            override fun onLoadMore() {

            }
        }
        refreshLayout!!.isNeedLoadMore = false
        if (!view) {

            refreshLayout!!.setRefreshing(true)
            view = true
        }

    }

    private fun getNotificationData() {
        val db1 = DatabaseHandler(applicationContext)
        val user: HashMap<String, String>
        user = db1.userDetails

        val params = HashMap<String, String>()
        params["tag"] = "notification"
        params["id"] = user["idNo"]!!
        params["code"] = user["code"]!!
        val strReq = object : StringRequest(Request.Method.POST, "http://saoshyant.net/Life/getNotificationData.php", Response.Listener { response ->
            //response from the server
            //Log.d("Response :", response);
            try {
                val responseObj = JSONObject(response)
                if (!responseObj.isNull("user")) {
                    val feedArray = responseObj.getJSONArray("user")
                    for (i in 0 until feedArray.length()) {
                        val feedObj = feedArray.get(i) as JSONObject
                        if (feedObj.getString("userId") == user["idNo"])
                            continue
                        val item = NotificationFeedItem()
                        item.id = feedObj.getString("id")
                        item.userId = feedObj.getString("userId")
                        item.name = feedObj.getString("name")
                        item.profilePic = feedObj.getString("profilePic")
                        item.myFollowStatus = feedObj.getBoolean("MyFollowStatus")
                        item.postId = feedObj.getString("postId")
                        item.prgFollowing = false
                        notificationList.add(item)
                        if (notificationList.size == 4) {
                            val item1 = NotificationFeedItem()
                            item1.id = "adad"
                            notificationList.add(item1)
                        }
                    }

                    mAdapter!!.notifyDataSetChanged()
                    refreshLayout!!.setRefreshing(false)


                }


            } catch (e: JSONException) {
                refreshLayout!!.setRefreshing(false)
                // Toast.makeText(getApplicationContext(), "Error2: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        },
                Response.ErrorListener {
                    refreshLayout!!.setRefreshing(false)
                    Snackbar.make(coordinatorLayout!!, R.string.errorconection, Snackbar.LENGTH_LONG).show()
                    //  Log.e(TAG, "Error1: " + error.getMessage());
                }
        ) {

            override fun getParams(): Map<String, String> {
                // Log.e(TAG, "Posting params: " + params.toString());
                return params
            }
        }
        MyApplication.instance!!.addToRequestQueue(strReq)

    }


}// Required empty public constructor
