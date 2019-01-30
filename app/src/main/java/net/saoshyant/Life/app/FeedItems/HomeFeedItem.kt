package net.saoshyant.Life.app.FeedItems

class HomeFeedItem {

    var id: String? = null
    var name: String? = null
    var userId: String? = null
    var profilePic: String? = null
    private var image: String? = null
    var video: String? = null
    var timeStamp: String? = null
    var text: String? = null
    var nLike: String? = null
    var location: String? = null
    var videoThumb: String? = null
    var prgLiking: Boolean = false
    var isMyLike: Boolean = false
    var isPrgDeleting: Boolean = false
    var media: Array<String>? = null


    fun getimage(): String? {
        return image
    }


    fun setimage(image: String) {
        this.image = image
    }
}
