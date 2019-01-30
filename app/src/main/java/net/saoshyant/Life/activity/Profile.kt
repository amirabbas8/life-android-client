package net.saoshyant.Life.activity

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CollapsingToolbarLayout
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
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


class Profile : AppCompatActivity() {

    private var id: String? = null
    private var isUsername = ""
    private var name: TextView? = null
    private var clpName: TextView? = null
    private var clpBio: TextView? = null
    private var clpNPosts: TextView? = null
    private var clpNFollowers: TextView? = null
    private var clpNFollowing: TextView? = null
    private var profilePic: NetworkImageView? = null
    private var clpProfilePic: NetworkImageView? = null
    private var toolbar2: Toolbar? = null
    private var f_uf: ImageView? = null
    private var pro_following: ProgressBar? = null
    private val imageLoader = MyApplication.instance!!.imageLoader
    private val homeList = ArrayList<HomeFeedItem>()
    private var mAdapter: HomeAdapter? = null
    private var refreshLayout: RefreshLayout? = null
    private var coordinatorLayout: CoordinatorLayout? = null
    private var lastId = ""


    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile)

        toolbar2 = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar2)

        initCollapsingToolbar()
        coordinatorLayout = findViewById(R.id.coordinatorLayout)
        val bundle = intent.extras
        id = bundle!!.getString("id")
        val data = intent.data
        if (data != null) {
            //   String scheme = data.getScheme(); // "http"

            //  String host = data.getHost(); // "twitter.com"
            //  String inurl = data.toString();

            //  List<String> params = data.getPathSegments();
            //   String first = params.get(0); // "status"
            //strip off HashTag from the URI
            id = data.toString().split("/".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()[3].replace("@".toRegex(), "")
            isUsername = "yes"
            // Snackbar.make(coordinatorLayout, id, Snackbar.LENGTH_LONG).show();
        }
        name = findViewById(R.id.txt_name)
        profilePic = findViewById(R.id.profile)
        clpName = findViewById(R.id.clp_txt_name)
        clpBio = findViewById(R.id.clp_bio)
        clpNPosts = findViewById(R.id.n_posts)
        clpNFollowers = findViewById(R.id.n_followers)
        clpNFollowing = findViewById(R.id.n_following)
        clpNFollowing!!.setOnClickListener { startActivity(Intent(this@Profile, Following::class.java).putExtra("id", id)) }
        clpNFollowers!!.setOnClickListener { startActivity(Intent(this@Profile, Follower::class.java).putExtra("id", id)) }
        clpProfilePic = findViewById(R.id.clp_profile)
        f_uf = findViewById(R.id.btn_follow)
        pro_following = findViewById(R.id.progressBar3)
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)

        name!!.text = bundle.getString("name")
        clpName!!.text = bundle.getString("name")
        try {
            profilePic!!.setImageUrl("http://saoshyant.net/profileimages/" + bundle.getString("profileImage")!!, imageLoader)
            clpProfilePic!!.setImageUrl("http://saoshyant.net/profileimages/" + bundle.getString("profileImage")!!, imageLoader)
        }
        catch (e: Exception) {
        }
       if ("" == bundle.getString("profileImage")) {
            profilePic!!.setImageResource(R.drawable.ic_account_circle_white)
            clpProfilePic!!.setImageResource(R.drawable.ic_account_circle_white)
        }


        f_uf!!.setOnClickListener {
            f_uf!!.visibility = View.GONE
            pro_following!!.visibility = View.VISIBLE
            addDeleteFriend(id)
        }

        refreshLayout = findViewById(R.id.refreshLayout)
        val networkImageView0 = findViewById<NetworkImageView>(R.id.newImageView)
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
                getProfile()
                homeList.clear()
                mAdapter!!.notifyDataSetChanged()


            }

            override fun onLoadMore() {
                if (9 < homeList.size) {
                    getProfileData("profileLoadMore")

                }

            }
        }
        refreshLayout!!.setRefreshing(true)


    }

    private fun initCollapsingToolbar() {
        val collapsingToolbar = findViewById<CollapsingToolbarLayout>(R.id.collapsing_toolbar)
        collapsingToolbar.title = " "
        val appBarLayout = findViewById<AppBarLayout>(R.id.appbar)
        appBarLayout.setExpanded(true)

        // hiding & showing the title when toolbar expanded & collapsed
        appBarLayout.addOnOffsetChangedListener(object : AppBarLayout.OnOffsetChangedListener {
            var isShow = true
            var scrollRange = -1

            override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
                pro_following!!.visibility = View.GONE
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.totalScrollRange
                }
                if (scrollRange + verticalOffset == 0) {
                    toolbar2!!.visibility = View.VISIBLE
                    isShow = true
                } else if (isShow) {
                    toolbar2!!.visibility = View.GONE
                    collapsingToolbar.title = " "
                    isShow = false
                }
            }
        })
    }


    private fun getProfile() {
        val db1 = DatabaseHandler(applicationContext)
        val user: HashMap<String, String>
        user = db1.userDetails
        val params = HashMap<String, String>()
        params["tag"] = "getProfile"
        params["idNo"] = user["idNo"]!!
        params["code"] = user["code"]!!
        params["id"] = id!!
        params["isUsername"] = isUsername
        val strReq = object : StringRequest(Request.Method.POST, "http://saoshyant.net/Life/profile.php", Response.Listener { response ->
            //response from the server
            // Log.d("++++", response);
            try {
                val responseObj = JSONObject(response)
                val feedArray = responseObj.getJSONArray("user")
                val feedObj = feedArray.get(0) as JSONObject

                if (feedObj.getString("success") != null) {

                    if (Integer.parseInt(feedObj.getString("success")) == 1) {
                        id = feedObj.getString("id")
                        isUsername = ""
                        f_uf!!.setImageResource(R.drawable.ic_remove_circle_white)
                        pro_following!!.visibility = View.GONE
                        name!!.text = feedObj.getString("name")
                        clpName!!.text = feedObj.getString("name")
                        clpBio!!.text = feedObj.getString("bio")
                        clpNPosts!!.text = feedObj.getString("nPosts")
                        clpNFollowers!!.text = feedObj.getString("nFollowers")
                        clpNFollowing!!.text = feedObj.getString("nFollowing")
                        try {
                            profilePic!!.setImageUrl("http://saoshyant.net/profileimages/" + feedObj.getString("profileImage"), imageLoader)
                            clpProfilePic!!.setImageUrl("http://saoshyant.net/profileimages/" + feedObj.getString("profileImage"), imageLoader)
                        }
                        catch (e: Exception) {
                        }
                       if ("" == feedObj.getString("profileImage")) {
                            profilePic!!.setImageResource(R.drawable.ic_account_circle_white)
                            clpProfilePic!!.setImageResource(R.drawable.ic_account_circle_white)
                        }

                        getProfileData("profile")

                    } else if (Integer.parseInt(feedObj.getString("success")) == 2) {
                        id = feedObj.getString("id")
                        isUsername = ""
                        f_uf!!.setImageResource(R.drawable.ic_add_circle_white)
                        pro_following!!.visibility = View.GONE
                        name!!.text = feedObj.getString("name")
                        clpName!!.text = feedObj.getString("name")
                        clpBio!!.text = feedObj.getString("bio")
                        clpNPosts!!.text = feedObj.getString("nPosts")
                        clpNFollowers!!.text = feedObj.getString("nFollowers")
                        clpNFollowing!!.text = feedObj.getString("nFollowing")
                        try {
                            profilePic!!.setImageUrl("http://saoshyant.net/profileimages/" + feedObj.getString("profileImage"), imageLoader)
                            clpProfilePic!!.setImageUrl("http://saoshyant.net/profileimages/" + feedObj.getString("profileImage"), imageLoader)
                        }
                        catch (e: Exception) {
                        }
                        if ("" == feedObj.getString("profileImage")) {
                            profilePic!!.setImageResource(R.drawable.ic_account_circle_white)
                            clpProfilePic!!.setImageResource(R.drawable.ic_account_circle_white)
                        }
                        getProfileData("profile")
                    } else {
                        finish()
                    }
                    if (id != user["idNo"]) {
                        f_uf!!.visibility = View.VISIBLE
                        pro_following!!.visibility = View.VISIBLE
                    } else {
                        f_uf!!.visibility = View.GONE
                    }

                } else {
                    finish()
                }

            } catch (e: JSONException) {
                finish()
                //  Toast.makeText(getApplicationContext(), "Error2: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        },
                Response.ErrorListener {
                    //Todo // FIXME: 5/30/17
                    //Log.e("+++++", "Error1: " + error.getMessage());
                    //Toast.makeText(getApplicationContext(), "Error1: connection error", Toast.LENGTH_SHORT).show();
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

    internal fun addDeleteFriend(userId: String?) {

        val db1 = DatabaseHandler(applicationContext)
        val user: HashMap<String, String>
        user = db1.userDetails
        val params = HashMap<String, String>()
        params["tag"] = "add_delete_friend"
        params["id"] = user["idNo"]!!
        params["code"] = user["code"]!!
        params["userId"] = userId!!
        val strReq = object : StringRequest(Request.Method.POST, "http://saoshyant.net/Life/add_delete_friend.php", Response.Listener { response ->
            // Log.d("a:", response);
            try {
                val responseObj = JSONObject(response)
                val feedArray = responseObj.getJSONArray("user")
                val feedObj = feedArray.get(0) as JSONObject
                if (feedObj.getString("success") != null) {
                    val res = feedObj.getString("success")

                    if (Integer.parseInt(res) == 51) {
                        f_uf!!.visibility = View.VISIBLE
                        pro_following!!.visibility = View.GONE
                        Snackbar.make(coordinatorLayout!!, R.string.errorfunblock, Snackbar.LENGTH_LONG).show()

                    } else if (Integer.parseInt(res) == 52) {
                        Snackbar.make(coordinatorLayout!!, R.string.errorblock, Snackbar.LENGTH_LONG).show()
                    } else if (Integer.parseInt(res) == 1) {
                        //hh
                        f_uf!!.setImageResource(R.drawable.ic_add_circle_white)
                        f_uf!!.visibility = View.VISIBLE
                        pro_following!!.visibility = View.GONE

                    } else if (Integer.parseInt(res) == 2) {
                        f_uf!!.setImageResource(R.drawable.ic_remove_circle_white)
                        f_uf!!.visibility = View.VISIBLE
                        pro_following!!.visibility = View.GONE
                    } else {
                        f_uf!!.visibility = View.VISIBLE
                        pro_following!!.visibility = View.GONE
                        Snackbar.make(coordinatorLayout!!, R.string.errorproblem, Snackbar.LENGTH_LONG).show()
                    }
                }
            } catch (e: JSONException) {
                // Toast.makeText(activity.getApplicationContext(), "Error2: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        },
                Response.ErrorListener {
                    //  Log.e(TAG, "Error1: " + error.getMessage());
                    f_uf!!.visibility = View.VISIBLE
                    pro_following!!.visibility = View.GONE
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

    private fun getProfileData(tag: String) {

        val db1 = DatabaseHandler(applicationContext)
        val user: HashMap<String, String>
        user = db1.userDetails
        val params = HashMap<String, String>()
        params["idNo"] = user["idNo"]!!
        params["tag"] = tag
        params["id"] = id!!
        params["pid"] = lastId
        val strReq = object : StringRequest(Request.Method.POST, "http://saoshyant.net/Life/getProfileData.php", Response.Listener { response ->
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
                        item.location = feedObj.getString("location")
                        item.name = feedObj.getString("name")
                        item.timeStamp = feedObj.getString("timeStamp")
                        item.isMyLike = feedObj.getBoolean("myLike")
                        item.prgLiking = false
                        lastId = feedObj.getString("id")
                        objCounter++
                        homeList.add(item)
                    }
                    mAdapter!!.notifyDataSetChanged()
                    if (tag == "profile") {
                        refreshLayout!!.setRefreshing(false)
                    } else if (tag == "profileLoadMore") {
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