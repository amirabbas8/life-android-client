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


class PeopleFragment : Fragment() {
    private var recyclerView: RecyclerView? = null
    private val peopleList = ArrayList<PeopleFeedItem>()
    private var adapter: PeopleAdapter? = null
    private var edtSearch: EditText? = null
    private var refreshLayout: RefreshLayout? = null
    private var coordinatorLayout: CoordinatorLayout? = null
    private var cdt1: CountDownTimer? = null
    private var lastId = ""
    private var view = false
    private var strText = ""

    fun goToTop() {
        recyclerView!!.smoothScrollToPosition(0)
    }

    fun updateList(name: String, profilePic: String) {
        for (a in peopleList) {
            val db1 = DatabaseHandler(activity!!.applicationContext)
            val user: HashMap<String, String>
            user = db1.userDetails
            if (a.userId == user["idNo"]) {
                a.name = name
                a.profilePic = profilePic
                adapter!!.notifyDataSetChanged()
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

        val rootView = inflater.inflate(R.layout.fragment_people, container, false)
        recyclerView = rootView.findViewById(R.id.recycler_view)
        coordinatorLayout = rootView.findViewById(R.id.coordinatorLayout)

        refreshLayout = rootView.findViewById(R.id.refreshLayout)
        edtSearch = rootView.findViewById(R.id.edt_search)
        adapter = PeopleAdapter(this.activity!!, peopleList, coordinatorLayout!!)

        recyclerView!!.setHasFixedSize(true)
        val mLayoutManager = LinearLayoutManager(rootView.context)
        recyclerView!!.layoutManager = mLayoutManager
        recyclerView!!.itemAnimator = DefaultItemAnimator()
        recyclerView!!.adapter = adapter
        cdt1 = object : CountDownTimer(1000, 1) {

            override fun onTick(millisUntilFinished: Long) {}

            override fun onFinish() {

                peopleList.clear()
                adapter!!.notifyDataSetChanged()
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
                peopleList.clear()
                adapter!!.notifyDataSetChanged()
                getPeopleData("people")


            }

            override fun onLoadMore() {
                if (9 < peopleList.size) {
                    getPeopleData("peopleLoadMore")

                }

            }
        }
        if (!view) {

            refreshLayout!!.setRefreshing(true)
            view = true
        }
        return rootView

    }

    private fun getPeopleData(tag: String) {
        val db1 = DatabaseHandler(activity!!.applicationContext)
        val user: HashMap<String, String>
        user = db1.userDetails

        val params = HashMap<String, String>()
        params["tag"] = tag
        params["id"] = user["idNo"]!!
        params["code"] = user["code"]!!
        params["pid"] = lastId
        params["search"] = edtSearch!!.text.toString().trim { it <= ' ' }
        val strReq = object : StringRequest(Request.Method.POST, "http://saoshyant.net/Life/getPeopleData.php", Response.Listener { response ->
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
                    adapter!!.notifyDataSetChanged()
                    if (tag == "people") {
                        refreshLayout!!.setRefreshing(false)
                    } else if (tag == "peopleLoadMore") {
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
