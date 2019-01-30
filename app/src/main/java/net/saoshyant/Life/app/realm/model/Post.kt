package net.saoshyant.Life.app.realm.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Post : RealmObject() {

    // Standard getters & setters generated by your IDE…
    @PrimaryKey
    var id: Long = 0

    var text: String? = null
    var imageUrl: String? = null
    var video: String? = null
    var videoThumbName: String? = null
}

