package net.saoshyant.Life.fragments

import android.os.Bundle
import android.os.CountDownTimer
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
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


class SearchFragment : Fragment() {

    private val searchList = ArrayList<HomeFeedItem>()
    private var mAdapter: HomeAdapter? = null
    private var edtSearch: EditText? = null
    private var refreshLayout: RefreshLayout? = null
    private var coordinatorLayout: CoordinatorLayout? = null
    private var cdt1: CountDownTimer? = null
    private var lastId = ""
    private var view = false
    private var recyclerView: RecyclerView? = null
    private var strText = ""

    fun goToTop() {
        recyclerView!!.smoothScrollToPosition(0)
    }

    fun updateList(name: String, profilePic: String) {
        for (a in searchList) {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreate(savedInstanceState)
        val rootView = inflater.inflate(R.layout.fragment_search, container, false)
        recyclerView = rootView.findViewById(R.id.recycler_view) as RecyclerView
        coordinatorLayout = rootView.findViewById(R.id.coordinatorLayout) as CoordinatorLayout
        refreshLayout = rootView.findViewById(R.id.refreshLayout) as RefreshLayout
        edtSearch = rootView.findViewById(R.id.edt_search) as EditText
        val networkImageView0 = rootView.findViewById(R.id.newImageView) as NetworkImageView
        networkImageView0.setOnClickListener {
            if (networkImageView0.visibility == View.VISIBLE) {
                networkImageView0.visibility = View.GONE
            }
        }
        mAdapter = HomeAdapter(this.activity!!, searchList, coordinatorLayout, networkImageView0)

        recyclerView!!.setHasFixedSize(true)
        val mLayoutManager = LinearLayoutManager(rootView.context)
        recyclerView!!.layoutManager = mLayoutManager

        recyclerView!!.itemAnimator = DefaultItemAnimator()
        recyclerView!!.adapter = mAdapter
        recyclerView!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {

            }
        })
        //        recyclerView.addOnItemTouchListener(new MovieTouchListener(rootView.getContext(), recyclerView, new MovieTouchListener.ClickListener() {
        //            @Override
        //            public void onClick(View view, int position) { }
        //
        //            @Override
        //            public void onLongClick(View view, int position) {
        //
        //            }
        //        }));
        cdt1 = object : CountDownTimer(1000, 1) {

            override fun onTick(millisUntilFinished: Long) {}

            override fun onFinish() {

                searchList.clear()
                mAdapter!!.notifyDataSetChanged()
                refreshLayout!!.setRefreshing(true)


            }

        }


        edtSearch!!.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {

            }

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {

                if (strText != edtSearch!!.text.toString()) {
                    cdt1!!.cancel()
                    cdt1!!.start()
                }
                strText = edtSearch!!.text.toString()
            }
        })
        refreshLayout!!.onRefreshListener = object : RefreshLayout.OnRefreshListener {
            override fun onRefresh() {
                searchList.clear()
                mAdapter!!.notifyDataSetChanged()
                getSearchData("search")


            }

            override fun onLoadMore() {
                if (9 < searchList.size) {
                    getSearchData("searchLoadMore")

                }

            }
        }
        if (!view) {

            refreshLayout!!.setRefreshing(true)
            view = true
        }
        return rootView

    }

    private fun getSearchData(tag: String) {
        val db1 = DatabaseHandler(activity!!.applicationContext)
        val user: HashMap<String, String>
        user = db1.userDetails

        val params = HashMap<String, String>()
        params["tag"] = tag
        params["id"] = user["idNo"]!!
        params["searchText"] = edtSearch!!.text.toString().trim { it <= ' ' }
        params["code"] = user["code"]!!
        params["pid"] = lastId
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
                        item.location = feedObj.getString("location")
                        item.isMyLike = feedObj.getBoolean("myLike")
                        item.prgLiking = false
                        lastId = feedObj.getString("id")
                        objCounter++
                        searchList.add(item)
                        if (searchList.size == feedArray.length()) {
                            val item1 = HomeFeedItem()
                            item1.id = "adad"
                            searchList.add(item1)
                        }
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


}// Required empty public constructor
