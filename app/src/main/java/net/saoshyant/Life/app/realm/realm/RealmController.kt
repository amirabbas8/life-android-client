package net.saoshyant.Life.app.realm.realm


import android.app.Activity
import android.support.v4.app.Fragment
import io.realm.Realm
import io.realm.RealmResults
import net.saoshyant.Life.app.realm.model.Post


class RealmController {
    val realm: Realm = Realm.getDefaultInstance()

    //find all objects in the Post.class
    val posts: RealmResults<Post>
        get() = realm.where<Post>(Post::class.java).findAll()

    //Refresh the realm istance
    fun refresh() {

        realm.refresh()
    }

    //clear all objects from Post.class
    fun clearAll() {

        realm.beginTransaction()
        realm.delete(Post::class.java)
        realm.commitTransaction()
    }

    //query a single item with the given id
    fun getPost(id: String): Post? {

        return realm.where<Post>(Post::class.java).equalTo("id", id).findFirst()
    }

    //check if Post.class is empty
    fun hasPosts(): Boolean {

        return !realm.where<Post>(Post::class.java!!).findAll().isEmpty()
    }

    //query example
    fun queryedPosts(): RealmResults<Post> {

        return realm.where<Post>(Post::class.java!!)
                .contains("author", "Author 0")
                .or()
                .contains("title", "Realm")
                .findAll()

    }

    companion object {

        var instance: RealmController? = null
            private set

        fun with(fragment: Fragment): RealmController {

            if (instance == null) {
                instance = RealmController()
            }
            return instance as RealmController
        }

        fun with(activity: Activity): RealmController {

            if (instance == null) {
                instance = RealmController()
            }
            return instance as RealmController
        }

        fun with(): RealmController {

            if (instance == null) {
                instance = RealmController()
            }
            return instance as RealmController
        }
    }
}
