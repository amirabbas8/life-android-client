package net.saoshyant.Life.app.realm.adapters

import android.app.Activity
import android.content.Context
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.android.volley.toolbox.NetworkImageView
import io.realm.Realm
import net.saoshyant.Life.R
import net.saoshyant.Life.app.realm.model.Post
import net.saoshyant.Life.app.realm.realm.RealmController

class PostsAdapter(private val context: Context, private val activity: Activity) : RealmRecyclerViewAdapter<Post, PostsAdapter.CardViewHolder>() {
    private var realm: Realm? = null

    // create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        // inflate a new card view
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_posts, parent, false)
        return CardViewHolder(view)
    }

    // replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: CardViewHolder, position: Int) {

        realm = RealmController.instance!!.realm

        // get the article
//        val post = getItem(position)
        // cast the generic view holder to our specific one
        val holder = viewHolder as CardViewHolder

        // set the title and the snippet
//        holder.textDescription.text = post.text
//        holder.imageBackground.setImageUrl("http://saoshyant.net/postsimages/" + post.imageUrl!!, MyApplication.instance!!.imageLoader)
//        if (post.videoThumbName == "") {
//            holder.videoView.visibility = View.GONE
//        } else {
//            holder.videoView.visibility = View.VISIBLE
//            MyApplication.instance!!.imageLoader.get("http://saoshyant.net/videoThumb/" + post.videoThumbName!!, object : ImageLoader.ImageListener {
//                override fun onErrorResponse(volleyError: VolleyError) {
//
//                }
//
//                override fun onResponse(imageContainer: ImageLoader.ImageContainer, b: Boolean) {
//                    holder.videoView.background = BitmapDrawable(activity.resources, imageContainer.bitmap)
//                    holder.videoView.setImageResource(R.drawable.icon_video_play)
//                }
//            })
//            holder.videoView.setOnClickListener {
//                val intent = Intent(activity, VideoPlayer::class.java)
//                intent.putExtra("videoName", post.video.toString())
//                activity.startActivity(intent)
//            }
//        }
        //remove single match from realm
        holder.card.setOnLongClickListener {
            val results = realm!!.where<Post>(Post::class.java).findAll()

            // All changes to data must happen in a transaction
            realm!!.beginTransaction()

            // remove single match
            results.deleteFromRealm(viewHolder.getAdapterPosition())
            realm!!.commitTransaction()

            notifyDataSetChanged()

            Toast.makeText(context, "  removed", Toast.LENGTH_SHORT).show()
            false
        }

        //update single match from realm
        holder.card.setOnClickListener {
//            activity.setResult(Activity.RESULT_OK, Intent().putExtra("text", post.text).putExtra("image", post.imageUrl).putExtra("video", post.video).putExtra("videoThumb", post.videoThumbName))
            val results = realm!!.where<Post>(Post::class.java).findAll()

            // All changes to data must happen in a transaction
            realm!!.beginTransaction()

            // remove single match
            results.deleteFromRealm(viewHolder.getAdapterPosition())
            realm!!.commitTransaction()

            notifyDataSetChanged()
            activity.finish()
            //editThumbnail.setText(post.getImageUrl());
        }
    }

    // return the size of your data set (invoked by the layout manager)
    override fun getItemCount(): Int {
        return 0
//        return if (realmAdapter != null) {
//            realmAdapter!!.itemCount
//        } else 0
    }

    class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        internal val card: CardView
        internal val textDescription: TextView
        internal val imageBackground: NetworkImageView
        internal val videoView: ImageView

        init {

            card = itemView.findViewById(R.id.card_posts)
            textDescription = itemView.findViewById(R.id.txt_text)
            imageBackground = itemView.findViewById(R.id.image)
            videoView = itemView.findViewById(R.id.video)
        }
    }
}
