package net.saoshyant.Life.activity

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


class HashTag : AppCompatActivity() {

    private var hashTag: String? = null
    private val list = ArrayList<HomeFeedItem>()
    private var mAdapter: HomeAdapter? = null
    private var refreshLayout: RefreshLayout? = null
    private var coordinatorLayout: CoordinatorLayout? = null
    private var lastId = ""

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_recycler)
        coordinatorLayout = findViewById<CoordinatorLayout>(R.id.coordinatorLayout)
        val data = intent.data
        if (data != null) {
            //   String scheme = data.getScheme(); // "http"

            //  String host = data.getHost(); // "twitter.com"
            //  String inurl = data.toString();

            //  List<String> params = data.getPathSegments();
            //   String first = params.get(0); // "status"
            //strip off HashTag from the URI
            hashTag = data.toString().split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[3]
            title = hashTag
            // Snackbar.make(coordinatorLayout, hashTag, Snackbar.LENGTH_LONG).show();
        }
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        refreshLayout = findViewById<RefreshLayout>(R.id.refreshLayout)
        val networkImageView0 = findViewById<NetworkImageView>(R.id.newImageView)
        networkImageView0.setOnClickListener {
            if (networkImageView0.visibility == View.VISIBLE) {
                networkImageView0.visibility = View.GONE
            }
        }

        mAdapter = HomeAdapter(this, list, coordinatorLayout, networkImageView0)

        recyclerView.setHasFixedSize(true)
        val mLayoutManager = LinearLayoutManager(applicationContext)
        recyclerView.layoutManager = mLayoutManager

        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = mAdapter

        refreshLayout!!.onRefreshListener = object : RefreshLayout.OnRefreshListener {
            override fun onRefresh() {
                list.clear()
                mAdapter!!.notifyDataSetChanged()
                getHashTagData("search")

            }

            override fun onLoadMore() {
                if (9 < list.size) {
                    getHashTagData("searchLoadMore")

                }

            }
        }
        refreshLayout!!.setRefreshing(true)


    }


    private fun getHashTagData(tag: String) {

        val db1 = DatabaseHandler(applicationContext)
        val user: HashMap<String, String>
        user = db1.userDetails
        val params = HashMap<String, String>()
        params["tag"] = tag
        params["id"] = user["idNo"].orEmpty()
        params["pid"] = lastId
        params["searchText"] = hashTag.orEmpty()
        val strReq = object : StringRequest(Request.Method.POST, "http://saoshyant.net/Life/getSearchData.php", Response.Listener { response ->
            //response from the server
            //Log.d("Response :", response);
            try {
                val responseObj = JSONObject(response)
                var objCounter = 0
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
                        lastId = feedObj.getString("id")
                        objCounter++
                        list.add(item)
                    }
                    mAdapter!!.notifyDataSetChanged()
                    if (tag == "search") {
                        refreshLayout!!.setRefreshing(false)
                    } else if (tag == "searchLoadMore") {
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