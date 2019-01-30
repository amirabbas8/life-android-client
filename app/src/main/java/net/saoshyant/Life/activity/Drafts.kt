package net.saoshyant.Life.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import io.realm.RealmResults
import net.saoshyant.Life.R
import net.saoshyant.Life.app.realm.adapters.PostsAdapter
import net.saoshyant.Life.app.realm.model.Post
import net.saoshyant.Life.app.realm.realm.RealmController

class Drafts : AppCompatActivity() {

    private var adapter: PostsAdapter? = null
    private var recycler: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.drafts)
        title = "Drafts"
        recycler = findViewById(R.id.recycler)

        setupRecycler()

        //        if (!Prefs.with(this).getPreLoad()) {
        //           setRealmData();
        //        }

        // refresh the realm instance
        RealmController.with(this).refresh()
        // get all persisted objects
        // create the helper adapter and notify data set changes
        // changes will be reflected automatically
        setRealmAdapter(RealmController.with(this).posts)

    }

    private fun setRealmAdapter(posts: RealmResults<Post>) {

//        val realmAdapter = RealmPostsAdapter(this.applicationContext, posts, true)
        // Set the data and tell the RecyclerView to draw
//        adapter!!.realmAdapter = realmAdapter
//        adapter!!.notifyDataSetChanged()
    }


    private fun setupRecycler() {
        adapter = PostsAdapter(this, this)
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true
        recycler!!.layoutManager = layoutManager
        recycler!!.adapter = adapter
        recycler!!.setHasFixedSize(true)
        recycler!!.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

    }

}