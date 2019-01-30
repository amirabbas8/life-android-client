package net.saoshyant.Life.app.realm.adapters

import android.content.Context
import io.realm.RealmResults
import net.saoshyant.Life.app.realm.model.Post

internal class RealmPostsAdapter(context: Context, realmResults: RealmResults<Post>, automaticUpdate: Boolean) : RealmModelAdapter<Post,Nothing>(context, realmResults, automaticUpdate)