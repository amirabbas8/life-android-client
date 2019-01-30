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
import net.saoshyant.Life.app.FeedItems.PeopleFeedItem
import net.saoshyant.Life.app.FeedListAdapters.PeopleAdapter
import net.saoshyant.Life.app.MyApplication
import net.saoshyant.Life.app.refreshLayout.RefreshLayout
import org.json.JSONException
import org.json.JSONObject
import java.util.*


class Follower : AppCompatActivity() {
    private val peopleList = ArrayList<PeopleFeedItem>()
    private var mAdapter: PeopleAdapter? = null
    private var refreshLayout: RefreshLayout? = null
    private var coordinatorLayout: CoordinatorLayout? = null
    private var lastId = ""
    private var id: String? = ""


    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_recycler)
        setTitle(R.string.followers)
        val bundle = intent.extras
        id = bundle!!.getString("id")
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        coordinatorLayout = findViewById(R.id.coordinatorLayout)

        refreshLayout = findViewById(R.id.refreshLayout)
        mAdapter = PeopleAdapter(this, peopleList, coordinatorLayout!!)

        recyclerView.setHasFixedSize(true)
        val mLayoutManager = LinearLayoutManager(applicationContext)
        recyclerView.layoutManager = mLayoutManager

        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = mAdapter

        refreshLayout!!.onRefreshListener = object : RefreshLayout.OnRefreshListener {
            override fun onRefresh() {
                peopleList.clear()
                mAdapter!!.notifyDataSetChanged()
                getPeopleData("follower")


            }

            override fun onLoadMore() {
                if (9 < peopleList.size) {
                    getPeopleData("followerLoadMore")

                }

            }
        }

        refreshLayout!!.setRefreshing(true)


    }

    private fun getPeopleData(tag: String) {
        val db1 = DatabaseHandler(applicationContext)
        val user: HashMap<String, String>
        user = db1.userDetails

        val params = HashMap<String, String>()
        params["tag"] = tag
        params["idNum"] = user["idNo"]!!
        params["id"] = id!!
        params["pid"] = lastId
        val strReq = object : StringRequest(Request.Method.POST, "http://saoshyant.net/Life/getFollowerData.php", Response.Listener { response ->
            //response from the server
            //Log.d("Response :", response);
            try {
                val responseObj = JSONObject(response)
                var objCounter = 0
                if (!responseObj.isNull("user")) {
                    val feedArray = responseObj.getJSONArray("user")
                    for (i in 0 until feedArray.length()) {
                        val feedObj = feedArray.get(i) as JSONObject
                        val item = PeopleFeedItem()
                        item.userId = feedObj.getString("userId")
                        item.name = feedObj.getString("name")
                        item.profilePic = feedObj.getString("profilePic")
                        item.myFollowStatus = feedObj.getBoolean("MyFollowStatus")
                        item.prgFollowing = false
                        lastId = feedObj.getString("userId")
                        objCounter++
                        peopleList.add(item)
                    }
                    mAdapter!!.notifyDataSetChanged()
                    if (tag == "follower") {
                        refreshLayout!!.setRefreshing(false)
                    } else if (tag == "followerLoadMore") {
                        refreshLayout!!.setLoadMoreing(false)
                    }


                    refreshLayout!!.isNeedLoadMore = objCounter >= 10

                }


            } catch (e: JSONException) {
                refreshLayout!!.setRefreshing(false)
                refreshLayout!!.setLoadMoreing(false)
                // Toast.makeText(getActivity().getApplicationContext(), "Error2: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        },
                Response.ErrorListener {
                    refreshLayout!!.setRefreshing(false)
                    refreshLayout!!.setLoadMoreing(false)
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
