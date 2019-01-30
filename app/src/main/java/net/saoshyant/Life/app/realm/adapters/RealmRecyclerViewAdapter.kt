package net.saoshyant.Life.app.realm.adapters

import android.support.v7.widget.RecyclerView

import io.realm.RealmObject

abstract class RealmRecyclerViewAdapter<T : RealmObject, V : RecyclerView.ViewHolder> : RecyclerView.Adapter<V>() {


}
