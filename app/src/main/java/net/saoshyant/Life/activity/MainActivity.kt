package net.saoshyant.Life.activity

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.NetworkImageView
import com.android.volley.toolbox.StringRequest
import net.saoshyant.Life.R
import net.saoshyant.Life.app.DatabaseHandler
import net.saoshyant.Life.app.MyApplication
import net.saoshyant.Life.fragments.HomeFragment
import net.saoshyant.Life.fragments.PeopleFragment
import net.saoshyant.Life.fragments.SearchFragment
import org.json.JSONException
import org.json.JSONObject
import java.util.*


class MainActivity : AppCompatActivity() {


    private var dialog: Dialog? = null
    private var tabLayout: TabLayout? = null
    private var coordinatorLayout: CoordinatorLayout? = null
    private var peopleFragment: PeopleFragment? = null
    private var searchFragment: SearchFragment? = null
    private var homeFragment: HomeFragment? = null
    private var imgEdit: ImageButton? = null
    private var imgNotification: ImageButton? = null
    private var imgProfile: ImageButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        coordinatorLayout = findViewById(R.id.coordinatorLayout)
//        Pushe.initialize(this, false)
        supportActionBar!!.setIcon(R.mipmap.ic_launcher)
        val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val v = inflater.inflate(R.layout.toolbar, null)
        imgEdit = v.findViewById(R.id.img_edit_profile)
        imgNotification = v.findViewById(R.id.img_notification)
        imgProfile = v.findViewById(R.id.img_profile)
        supportActionBar!!.customView = v
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        supportActionBar!!.setDisplayShowCustomEnabled(true)
        setupToolbar()

        val viewPager = findViewById<ViewPager>(R.id.viewpager)
        setupViewPager(viewPager)
        viewPager.currentItem = 1
        viewPager.offscreenPageLimit = 2
        tabLayout = findViewById(R.id.tabs)
        tabLayout!!.setupWithViewPager(viewPager)
        setupTabIcons()
        update()
    }

    private fun setupToolbar() {
        imgEdit!!.setOnClickListener { startActivityForResult(Intent(this@MainActivity, EditProfile::class.java), 111) }
        imgNotification!!.setOnClickListener { startActivity(Intent(this@MainActivity, Notification::class.java)) }
        imgProfile!!.setOnClickListener {
            val db1 = DatabaseHandler(applicationContext)

            val user = db1.userDetails
            val intent = Intent(this@MainActivity, Profile::class.java)
            intent.putExtra("id", user["idNo"].toString())
            intent.putExtra("name", user["realname"].toString())
            intent.putExtra("profileImage", user["profileImage"].toString())
            startActivity(intent)
        }
    }

    private fun setupTabIcons() {

        val tabPeople = LayoutInflater.from(this).inflate(R.layout.custom_tab, null) as TextView
        tabPeople.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_people_white, 0, 0)
        tabLayout!!.getTabAt(0)!!.customView = tabPeople
        val tabMyLife = LayoutInflater.from(this).inflate(R.layout.custom_tab, null) as TextView
        tabMyLife.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_home, 0, 0)
        tabLayout!!.getTabAt(1)!!.customView = tabMyLife
        val tabSearch = LayoutInflater.from(this).inflate(R.layout.custom_tab, null) as TextView
        tabSearch.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_search_white, 0, 0)
        tabLayout!!.getTabAt(2)!!.customView = tabSearch
        //        TextView tabAthena = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        //        tabAthena.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_mic_none_white, 0, 0);
        //        tabLayout.getTabAt(3).setCustomView(tabAthena);
        tabLayout!!.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {}

            override fun onTabUnselected(tab: TabLayout.Tab) {

            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                try {
                    when (tab.position) {
                        0 -> peopleFragment!!.goToTop()
                        1 -> homeFragment!!.goToTop()
                        2 -> searchFragment!!.goToTop()
                    }
                } catch (ignored: Exception) {
                }


            }
        })
    }


    private fun setupViewPager(viewPager: ViewPager) {
        val adapter = ViewPagerAdapter(supportFragmentManager)
        peopleFragment = PeopleFragment()
        adapter.addFrag(peopleFragment!!, "")
        homeFragment = HomeFragment()
        adapter.addFrag(homeFragment!!, "")
        searchFragment = SearchFragment()
        adapter.addFrag(searchFragment!!, "")
        //        adapter.addFrag(new AthenaFragment(), "");
        viewPager.adapter = adapter

    }

    private inner class ViewPagerAdapter internal constructor(manager: FragmentManager) : FragmentPagerAdapter(manager) {
        private val mFragmentList = ArrayList<Fragment>()
        private val mFragmentTitleList = ArrayList<String>()

        override fun getItem(position: Int): Fragment {
            return mFragmentList[position]
        }

        override fun getCount(): Int {
            return mFragmentList.size
        }

        internal fun addFrag(fragment: Fragment, title: String) {
            mFragmentList.add(fragment)
            mFragmentTitleList.add(title)
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return mFragmentTitleList[position]
        }

    }

    fun getUsername(editText: EditText) {
        val db1 = DatabaseHandler(applicationContext)

        val user = db1.userDetails
        val params = HashMap<String, String>()
        params["tag"] = "username"
        params["id"] = user["idNo"].orEmpty()
        params["code"] = user["code"].orEmpty()
        val strReq = object : StringRequest(Request.Method.POST, "http://saoshyant.net/Life/getUsername.php", Response.Listener { response ->
            //response from the server
            //Log.e("username: ", response);
            try {
                val responseObj = JSONObject(response)
                val feedArray = responseObj.getJSONArray("user")
                val feedObj = feedArray.get(0) as JSONObject

                if (feedObj.getInt("success") == 1) {
                    editText.setText(feedObj.getString("username"))

                } else {
                    Snackbar.make(coordinatorLayout!!, R.string.errorproblem, Snackbar.LENGTH_LONG).show()
                }


            } catch (e: JSONException) {
                //  Toast.makeText(getApplicationContext(), "Error2: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        },
                Response.ErrorListener {
                    //  Log.e(TAG, "Error1: " + error.getMessage());
                    Snackbar.make(coordinatorLayout!!, R.string.errorconection, Snackbar.LENGTH_SHORT).show()
                }
        ) {
            @Throws(com.android.volley.AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Authorization: Basic"] = ""
                return params
            }

            override fun getParams(): Map<String, String> {
                // Log.e(TAG, "Posting params: " + params.toString());
                return params
            }
        }
        // Adding request to request queue
        MyApplication.instance!!.addToRequestQueue(strReq)
    }

    private fun setUsername(username: String, button: Button) {
        val db1 = DatabaseHandler(applicationContext)
        val user: HashMap<String, String>
        user = db1.userDetails
        val params = HashMap<String, String>()
        params["tag"] = "username"
        params["id"] = user["idNo"].orEmpty()
        params["code"] = user["code"].orEmpty()
        params["username"] = username
        val strReq = object : StringRequest(Request.Method.POST, "http://saoshyant.net/Life/setUsername.php", Response.Listener { response ->
            //response from the server
            //   Log.e("a: ", response);
            try {
                val responseObj = JSONObject(response)
                val feedArray = responseObj.getJSONArray("user")
                val feedObj = feedArray.get(0) as JSONObject

                if (feedObj.getString("success") != null) {
                    val res = feedObj.getString("success")

                    if (Integer.parseInt(res) == 1) {
                        Snackbar.make(coordinatorLayout!!, R.string.success, Snackbar.LENGTH_LONG).show()
                        dialog!!.dismiss()
                    } else if (Integer.parseInt(res) == 2) {
                        Snackbar.make(coordinatorLayout!!, R.string.usernameAlreadyTaken, Snackbar.LENGTH_LONG).show()
                        button.visibility = View.VISIBLE
                    } else {
                        button.visibility = View.VISIBLE
                        Snackbar.make(coordinatorLayout!!, R.string.errorproblem, Snackbar.LENGTH_LONG).show()
                    }


                }

            } catch (e: JSONException) {
                button.visibility = View.VISIBLE
                //Toast.makeText(getApplicationContext(), "Error2: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        },
                Response.ErrorListener {
                    //  Log.e(TAG, "Error1: " + error.getMessage());
                    button.visibility = View.VISIBLE
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId


        when (id) {
            R.id.action_username -> {
                dialog = Dialog(this@MainActivity)
                dialog!!.setContentView(R.layout.dialog_username)
                val textview = dialog!!.findViewById<TextView>(R.id.text)
                textview.setText(R.string.errorUsername)
                val edtUsername = dialog!!.findViewById<EditText>(R.id.edt_username)
                getUsername(edtUsername)
                dialog!!.setTitle(R.string.logout)
                val yes = dialog!!.findViewById<Button>(R.id.yes)
                val no = dialog!!.findViewById<Button>(R.id.no)

                yes.setOnClickListener {
                    if ((edtUsername.text.toString().length > 4) and (edtUsername.text.toString().length < 20)) {
                        if (!edtUsername.text.toString().matches("[A-Za-z0-9_]+".toRegex())) {
                            edtUsername.error = getString(R.string.invalid_username)

                        } else {
                            edtUsername.error = null
                            yes.visibility = View.INVISIBLE
                            setUsername(edtUsername.text.toString(), yes)
                        }

                    } else {
                        edtUsername.error = getString(R.string.at_least)

                    }
                }

                no.setOnClickListener { dialog!!.dismiss() }
                dialog!!.show()
                return true
            }
            R.id.action_about -> {
                startActivity(Intent(this@MainActivity, About::class.java))

                return true
            }
            R.id.action_policies -> {
                startActivity(Intent(this@MainActivity, Policies::class.java))
                return true
            }
            R.id.action_logout -> {

                dialog = Dialog(this@MainActivity)
                dialog!!.setContentView(R.layout.dialog)
                val textview = dialog!!.findViewById<TextView>(R.id.text)
                textview.setText(R.string.logouterror)
                dialog!!.setTitle(R.string.logout)
                val yes = dialog!!.findViewById<Button>(R.id.yes)
                val no = dialog!!.findViewById<Button>(R.id.no)

                yes.setOnClickListener {
                    val db1 = DatabaseHandler(applicationContext)
                    db1.resetTables()
                    startActivity(Intent(applicationContext, Main::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                    finish()
                }
                no.setOnClickListener { dialog!!.dismiss() }
                dialog!!.show()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }

    }


    override fun onBackPressed() {
        if (findViewById<NetworkImageView>(R.id.newImageView) != null) {
            val networkImageView = findViewById<NetworkImageView>(R.id.newImageView)
            if (networkImageView.visibility == View.VISIBLE) {

                networkImageView.visibility = View.GONE
            } else {
                if (back_pressed + 2000 > System.currentTimeMillis()) {
                    super.onBackPressed()
                } else {
                    Snackbar.make(coordinatorLayout!!, R.string.onback, Snackbar.LENGTH_LONG).show()
                    back_pressed = System.currentTimeMillis()
                }
            }
        } else {
            if (back_pressed + 2000 > System.currentTimeMillis()) {
                super.onBackPressed()
            } else {
                Snackbar.make(coordinatorLayout!!, R.string.onback, Snackbar.LENGTH_LONG).show()
                back_pressed = System.currentTimeMillis()
            }
        }

    }


    protected fun getLocationOnScreen(mEditText: EditText): Rect {
        val mRect = Rect()
        val location = IntArray(2)

        mEditText.getLocationOnScreen(location)

        mRect.left = location[0]
        mRect.top = location[1]
        mRect.right = location[0] + mEditText.width
        mRect.bottom = location[1] + mEditText.height

        return mRect
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {

        val handleReturn = super.dispatchTouchEvent(ev)
        if (findViewById<NetworkImageView>(R.id.newImageView) != null) {
            val networkImageView = findViewById<NetworkImageView>(R.id.newImageView)
            networkImageView.visibility = View.GONE
        }
        val view = currentFocus

        val x = ev.x.toInt()
        val y = ev.y.toInt()

        if (view is EditText) {
            val innerView = currentFocus

            if (ev.action == MotionEvent.ACTION_UP && !getLocationOnScreen(innerView as EditText).contains(x, y)) {

                val input = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                input.hideSoftInputFromWindow(window.currentFocus!!
                        .windowToken, 0)
            }
        }

        return handleReturn
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != -1) return
        when (requestCode) {


            111 -> {

                homeFragment!!.updateList(data!!.getStringExtra("name"), data.getStringExtra("profileImage"))
                peopleFragment!!.updateList(data.getStringExtra("name"), data.getStringExtra("profileImage"))
                searchFragment!!.updateList(data.getStringExtra("name"), data.getStringExtra("profileImage"))
            }
        }


        super.onActivityResult(requestCode, resultCode, data)
    }

    fun update() {
        val db1 = DatabaseHandler(applicationContext)

        val user = db1.userDetails
        val params = HashMap<String, String>()
        params["tag"] = "update"
        params["id"] = user["idNo"].orEmpty()
        params["code"] = user["code"].orEmpty()
        val strReq = object : StringRequest(Request.Method.POST, "http://saoshyant.net/Life/update.php", Response.Listener { response ->
            //response from the server
            //Log.e("username: ", response);
            try {
                val responseObj = JSONObject(response)
                val feedArray = responseObj.getJSONArray("user")
                val feedObj = feedArray.get(0) as JSONObject

                if (feedObj.getInt("success") == 1) {
                    if (feedObj.getBoolean("pass")) {
                        val intent = Intent(this@MainActivity, update3::class.java)
                        intent.putExtra("id", user["idNo"])
                        intent.putExtra("code", user["code"])
                        intent.putExtra("username", feedObj.getString("username"))
                        intent.putExtra("phone", feedObj.getString("phone"))
                        startActivity(intent)
                        finish()
                    }

                }


            } catch (e: JSONException) {
                //  Toast.makeText(getApplicationContext(), "Error2: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        },
                Response.ErrorListener {
                    //  Log.e(TAG, "Error1: " + error.getMessage());
                }
        ) {
            @Throws(com.android.volley.AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Authorization: Basic"] = ""
                return params
            }

            override fun getParams(): Map<String, String> {
                // Log.e(TAG, "Posting params: " + params.toString());
                return params
            }
        }
        // Adding request to request queue
        MyApplication.instance!!.addToRequestQueue(strReq)
    }

    companion object {

        private var back_pressed: Long = 0
    }

}
