package net.saoshyant.Life.app.realm.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Post : RealmObject() {

    @PrimaryKey
    var id: Long = 0

    var text: String = ""
    var imageUrl: String = ""
    var video: String = ""
    var videoThumbName: String = ""
}

