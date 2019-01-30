package net.saoshyant.Life.activity

import android.content.res.Configuration
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.NetworkImageView
import com.android.volley.toolbox.StringRequest
import net.saoshyant.Life.R
import net.saoshyant.Life.app.DatabaseHandler
import net.saoshyant.Life.app.FeedItems.HomeFeedItem
import net.saoshyant.Life.app.FeedListAdapters.HomeAdapter
import net.saoshyant.Life.app.MyApplication
import net.saoshyant.Life.app.refreshLayout.RefreshLayout
import org.json.JSONException
import org.json.JSONObject
import java.util.*


class PostActivity : AppCompatActivity() {

    private var coordinatorLayout: CoordinatorLayout? = null
    private var postId: String? = null
    private val homeList = ArrayList<HomeFeedItem>()
    private var mAdapter: HomeAdapter? = null
    private var refreshLayout: RefreshLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_recycler)

        coordinatorLayout = findViewById(R.id.coordinatorLayout) as CoordinatorLayout
        setTitle(R.string.post)
        val bundle = intent.extras
        postId = bundle!!.getString("postId")
        refreshLayout = findViewById(R.id.refreshLayout) as RefreshLayout
        val recyclerView = findViewById(R.id.recycler_view) as RecyclerView
        val networkImageView0 = findViewById(R.id.newImageView) as NetworkImageView
        networkImageView0.setOnClickListener {
            if (networkImageView0.visibility == View.VISIBLE) {
                networkImageView0.visibility = View.GONE
            }
        }
        mAdapter = HomeAdapter(this, homeList, coordinatorLayout, networkImageView0)

        recyclerView.setHasFixedSize(true)
        val mLayoutManager = LinearLayoutManager(applicationContext)
        recyclerView.layoutManager = mLayoutManager

        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = mAdapter

        refreshLayout!!.onRefreshListener = object : RefreshLayout.OnRefreshListener {
            override fun onRefresh() {
                getPost()
                homeList.clear()
                mAdapter!!.notifyDataSetChanged()


            }

            override fun onLoadMore() {

            }
        }
        refreshLayout!!.setRefreshing(true)
        refreshLayout!!.isNeedLoadMore = false
    }

    private fun getPost() {

        val db1 = DatabaseHandler(applicationContext)
        val user: HashMap<String, String>
        user = db1.userDetails
        val params = HashMap<String, String>()
        params["idNo"] = user["idNo"]!!
        params["tag"] = "getPostData"
        params["postId"] = postId!!
        val strReq = object : StringRequest(Request.Method.POST, "http://saoshyant.net/Life/getPostData.php", Response.Listener { response ->
            //response from the server
            //Log.d("Response :", response);
            try {
                val responseObj = JSONObject(response)
                if (!responseObj.isNull("post")) {
                    val feedArray = responseObj.getJSONArray("post")
                    for (i in 0 until feedArray.length()) {
                        val feedObj = feedArray.get(i) as JSONObject
                        val item = HomeFeedItem()
                        item.id = feedObj.getString("id")
                        item.userId = feedObj.getString("userId")
                        item.profilePic = feedObj.getString("profilePic")
                        item.text = feedObj.getString("status")
                        val image = "http://saoshyant.net/postsimages/" + feedObj.getString("image")
                        item.videoThumb = feedObj.getString("videoThumbName")
                        item.video = feedObj.getString("video")
                        item.setimage(image)
                        item.nLike = feedObj.getString("nLike")
                        item.name = feedObj.getString("name")
                        item.timeStamp = feedObj.getString("timeStamp")
                        item.isMyLike = feedObj.getBoolean("myLike")
                        item.prgLiking = false
                        homeList.add(item)
                    }
                    mAdapter!!.notifyDataSetChanged()
                    refreshLayout!!.setRefreshing(false)


                }


            } catch (e: JSONException) {
                refreshLayout!!.setRefreshing(false)
                // Toast.makeText(getActivity().getApplicationContext(), "Error2: " + e.getMessage(), Toast.LENGTH_LONG).show();
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

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    override fun onBackPressed() {
        if (findViewById<NetworkImageView>(R.id.newImageView) != null) {
            val networkImageView = findViewById<NetworkImageView>(R.id.newImageView)
            if (networkImageView.visibility == View.VISIBLE) {

                networkImageView.visibility = View.GONE
            } else {
                super.onBackPressed()
            }
        } else {
            super.onBackPressed()
        }

    }
}
