package net.saoshyant.Life.activity

import android.content.Context
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import com.erikagtierrez.multiple_media_picker.Adapters.MediaAdapter
import com.erikagtierrez.multiple_media_picker.Fragments.OneFragment
import com.erikagtierrez.multiple_media_picker.Fragments.TwoFragment
import com.erikagtierrez.multiple_media_picker.R
import java.util.*

class OpenGallery : AppCompatActivity() {
    private var recyclerView: RecyclerView? = null
    private var mAdapter: MediaAdapter? = null
    private val mediaList = ArrayList<String>()
    private val mediaPicked = ArrayList<String>()
    private var selected: MutableList<Boolean> = ArrayList()
    private var imagesSelected = ArrayList<String>()
    private var parent: String? = null
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_open_gallery)
        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener { finish() }
        title = Gallery.title
        if (imagesSelected.size > 0) {
            title = imagesSelected.size.toString()
        }
        recyclerView = findViewById(R.id.recycler_view)
        parent = intent.extras!!.getString("FROM")
        mediaList.clear()
        selected.clear()
        if (parent == "Images") {
            mediaList.addAll(OneFragment.imagesList)
            selected.addAll(OneFragment.selected)
        } else {
            mediaList.addAll(TwoFragment.videosList)
            selected.addAll(TwoFragment.selected)
        }
        populateRecyclerView()
    }


    private fun populateRecyclerView() {
        for (i in 0 until selected.size - 1) {
            selected[i] = imagesSelected.contains(mediaList[i])
        }
        mAdapter = MediaAdapter(mediaList, selected, applicationContext)
        val mLayoutManager = GridLayoutManager(applicationContext, 3)
        recyclerView!!.layoutManager = mLayoutManager
        recyclerView!!.itemAnimator!!.changeDuration = 0
        recyclerView!!.adapter = mAdapter
        recyclerView!!.addOnItemTouchListener(RecyclerTouchListener(this, recyclerView!!, object : ClickListener {
            override fun onClick(view: View, position: Int) {
                if (!selected[position]) {
                    imagesSelected.add(mediaList[position])
                    mediaPicked.add(mediaList[position])
                } else {
                    if (imagesSelected.indexOf(mediaList[position]) != -1) {
                        imagesSelected.removeAt(imagesSelected.indexOf(mediaList[position]))
                        mediaPicked.removeAt(mediaPicked.indexOf(mediaList[position]))
                    }
                }
                Gallery.selectionTitle = imagesSelected.size
                selected[position] = !selected[position]
                mAdapter!!.notifyItemChanged(position)
                if (imagesSelected.size != 0) {
                    title = imagesSelected.size.toString()
                } else {
                    title = Gallery.title
                }
            }

            override fun onLongClick(view: View?, position: Int) {

            }
        }))
    }

    interface ClickListener {
        fun onClick(view: View, position: Int)
        fun onLongClick(view: View?, position: Int)
    }

    class RecyclerTouchListener(context: Context, recyclerView: RecyclerView, private val clickListener: OpenGallery.ClickListener?) : RecyclerView.OnItemTouchListener {
        private val gestureDetector: GestureDetector

        init {
            gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
                override fun onSingleTapUp(e: MotionEvent): Boolean {
                    return true
                }

                override fun onLongPress(e: MotionEvent) {
                    val child = recyclerView.findChildViewUnder(e.x, e.y)
                    if (child != null && clickListener != null) {
                        clickListener.onLongClick(child, recyclerView.getChildPosition(child))
                    }
                }
            })
        }

        override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
            val child = rv.findChildViewUnder(e.x, e.y)
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildPosition(child))
            }
            return false
        }

        override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}

        override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
    }


}

