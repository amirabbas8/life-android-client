package net.saoshyant.Life.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import com.erikagtierrez.multiple_media_picker.Fragments.OneFragment
import com.erikagtierrez.multiple_media_picker.Fragments.TwoFragment
import com.erikagtierrez.multiple_media_picker.OpenGallery
import com.erikagtierrez.multiple_media_picker.R
import java.util.*

class Gallery : AppCompatActivity() {
    private var tabLayout: TabLayout? = null
    private var viewPager: ViewPager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)


        val fab = findViewById(R.id.fab) as FloatingActionButton
        fab.setOnClickListener { returnResult() }

        title = intent.extras!!.getString("title")
        title = title
        selectionTitle = 0

        viewPager = findViewById(R.id.viewpager) as ViewPager
        setupViewPager(viewPager!!)
        tabLayout = findViewById(R.id.tabs) as TabLayout
        tabLayout!!.setupWithViewPager(viewPager)

        OpenGallery.selected.clear()
        OpenGallery.imagesSelected.clear()

    }

    override fun onPostResume() {
        super.onPostResume()
        if (selectionTitle > 0) {
            title = selectionTitle.toString()
        }
    }

    //This method set up the tab view for images and videos
    private fun setupViewPager(viewPager: ViewPager) {
        val adapter = ViewPagerAdapter(supportFragmentManager)
        adapter.addFragment(OneFragment(), "Images")
        adapter.addFragment(TwoFragment(), "Videos")
        viewPager.adapter = adapter
    }

    internal inner class ViewPagerAdapter(manager: FragmentManager) : FragmentPagerAdapter(manager) {
        private val mFragmentList = ArrayList<Fragment>()
        private val mFragmentTitleList = ArrayList<String>()

        override fun getItem(position: Int): Fragment {
            return mFragmentList[position]
        }

        override fun getCount(): Int {
            return mFragmentList.size
        }

        fun addFragment(fragment: Fragment, title: String) {
            mFragmentList.add(fragment)
            mFragmentTitleList.add(title)
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return mFragmentTitleList[position]
        }
    }

    private fun returnResult() {
        val returnIntent = Intent()
        returnIntent.putStringArrayListExtra("result", OpenGallery.imagesSelected)
        setResult(Activity.RESULT_OK, returnIntent)
        finish()
    }

    companion object {
        var selectionTitle: Int = 0
        var title: String? = null
    }
}
