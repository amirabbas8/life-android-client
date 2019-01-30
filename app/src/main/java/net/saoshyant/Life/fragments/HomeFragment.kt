package net.saoshyant.Life.fragments

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.NetworkImageView
import com.android.volley.toolbox.StringRequest
import net.saoshyant.Life.R
import net.saoshyant.Life.activity.OldSend
import net.saoshyant.Life.app.DatabaseHandler
import net.saoshyant.Life.app.FeedItems.HomeFeedItem
import net.saoshyant.Life.app.FeedListAdapters.HomeAdapter
import net.saoshyant.Life.app.MyApplication
import net.saoshyant.Life.app.refreshLayout.RefreshLayout
import org.json.JSONException
import org.json.JSONObject
import java.util.*


class HomeFragment : Fragment() {

    private val homeList = ArrayList<HomeFeedItem>()
    private var mAdapter: HomeAdapter? = null
    private var refreshLayout: RefreshLayout? = null
    private var coordinatorLayout: CoordinatorLayout? = null
    private var lastId = ""
    private var view: Boolean = false
    private var recyclerView: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreate(savedInstanceState)
        val rootView = inflater.inflate(R.layout.fragment_home, container, false)
        recyclerView = rootView.findViewById(R.id.recycler_view)
        coordinatorLayout = rootView.findViewById(R.id.coordinatorLayout)
        refreshLayout = rootView.findViewById(R.id.refreshLayout)
        val networkImageView0 = rootView.findViewById<NetworkImageView>(R.id.newImageView)
        networkImageView0.setOnClickListener {
            if (networkImageView0.visibility == View.VISIBLE) {
                networkImageView0.visibility = View.GONE
            }
        }
        mAdapter = HomeAdapter(this.activity!!, homeList, coordinatorLayout, networkImageView0)
        recyclerView!!.setHasFixedSize(true)
        val mLayoutManager = LinearLayoutManager(rootView.context)
        recyclerView!!.layoutManager = mLayoutManager
        recyclerView!!.itemAnimator = DefaultItemAnimator()
        recyclerView!!.adapter = mAdapter

        refreshLayout!!.onRefreshListener = object : RefreshLayout.OnRefreshListener {
            override fun onRefresh() {
                homeList.clear()
                mAdapter!!.notifyDataSetChanged()
                getHomeData("home")


            }

            override fun onLoadMore() {
                if (9 < homeList.size) {
                    getHomeData("homeLoadMore")

                }

            }
        }
        val share = rootView.findViewById<FloatingActionButton>(R.id.share)

        share.setOnClickListener {
            startActivityForResult(Intent(activity, OldSend::class.java), 989)
            //                startActivity(new Intent(getActivity(), AddPost.class));
        }

        if (!view) {

            //YoYo.with(Techniques.StandUp).duration(700).repeat(0).playOn(share);
            refreshLayout!!.setRefreshing(true)
            view = true
        }


        return rootView

    }

    fun goToTop() {
        recyclerView!!.smoothScrollToPosition(0)
    }

    fun updateList(name: String, profilePic: String) {
        for (a in homeList) {

            val db1 = DatabaseHandler(activity!!.applicationContext)
            val user: HashMap<String, String>
            user = db1.userDetails
            if (a.userId == user["idNo"]) {
                a.name = name
                a.profilePic = profilePic
                mAdapter!!.notifyDataSetChanged()
            }
        }
    }


    private fun getHomeData(tag: String) {
        val db1 = DatabaseHandler(activity!!.applicationContext)
        val user: HashMap<String, String>
        user = db1.userDetails

        val params = HashMap<String, String>()
        params["tag"] = tag
        params["id"] = user["idNo"]!!
        params["code"] = user["code"]!!
        params["pid"] = lastId
        val strReq = object : StringRequest(Request.Method.POST, "http://saoshyant.net/Life/gethomeData.php", Response.Listener { response ->
            //response from the server
            Log.e("Response :", response)
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
                        item.location = feedObj.getString("location")
                        item.timeStamp = feedObj.getString("timeStamp")
                        item.name = feedObj.getString("name")
                        item.isMyLike = feedObj.getBoolean("myLike")
                        item.prgLiking = false
                        lastId = feedObj.getString("id")
                        objCounter++
                        homeList.add(item)

                    }
                    mAdapter!!.notifyDataSetChanged()
                    if (tag == "home") {
                        refreshLayout!!.setRefreshing(false)
                    } else if (tag == "homeLoadMore") {
                        refreshLayout!!.setLoadMoreing(false)
                    }


                    if (objCounter >= 10) {
                        refreshLayout!!.isNeedLoadMore = true
                    } else {
                        refreshLayout!!.isNeedLoadMore = false

                    }

                }


            } catch (e: JSONException) {
                refreshLayout!!.setRefreshing(false)
                refreshLayout!!.setLoadMoreing(false)
                Toast.makeText(activity!!.applicationContext, "Error2: " + e.message, Toast.LENGTH_LONG).show()
            }
        },
                Response.ErrorListener { error ->
                    refreshLayout!!.setRefreshing(false)
                    refreshLayout!!.setLoadMoreing(false)
                    Snackbar.make(coordinatorLayout!!, R.string.errorconection, Snackbar.LENGTH_LONG).show()
                    Log.e("tagwew", "Error1: " + error.message)
                }
        ) {

            override fun getParams(): Map<String, String> {
                // Log.e(TAG, "Posting params: " + params.toString());
                return params
            }
        }
        MyApplication.instance!!.addToRequestQueue(strReq)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != -1)
            return
        when (requestCode) {


            989 -> {
                val db1 = DatabaseHandler(this.context!!)
                val user: HashMap<String, String>
                user = db1.userDetails
                val item = HomeFeedItem()
                item.id = data!!.getStringExtra("postId")
                item.userId = user["idNo"]
                item.profilePic = user["profileImage"]
                item.text = data.getStringExtra("input_text")
                item.location = data.getStringExtra("location")
                item.videoThumb = data.getStringExtra("videoThumbName")
                item.video = data.getStringExtra("video")
                item.setimage("http://saoshyant.net/postsimages/" + data.getStringExtra("imageName"))
                item.nLike = "0"
                item.name = user["realname"]
                item.timeStamp = "now"
                item.isMyLike = false
                item.prgLiking = false
                homeList.add(0, item)
                homeList.add(item)
                mAdapter!!.notifyDataSetChanged()
            }
        }


    }

}// Required empty public constructor
