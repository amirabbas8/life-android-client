package net.saoshyant.Life.app.realm.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup

import io.realm.RealmObject
import io.realm.RealmResults


internal open class RealmModelAdapter<T : RealmObject,V:RecyclerView.ViewHolder> protected constructor(context: Context, realmResults: RealmResults<T>, automaticUpdate: Boolean) : RealmRecyclerViewAdapter<T,V>() {
    override fun onBindViewHolder(holder: V, position: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): V {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    override fun getItemCount(): Int {
        return 0
    }
}