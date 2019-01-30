package net.saoshyant.Life.app.FeedListAdapters

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.android.volley.VolleyError
import com.android.volley.toolbox.ImageLoader
import com.android.volley.toolbox.NetworkImageView
import net.saoshyant.Life.R
import net.saoshyant.Life.activity.VideoPlayer
import net.saoshyant.Life.app.DatabaseHandler
import net.saoshyant.Life.app.MyApplication
import java.util.*


class mediaAdapter(private val activity: Activity, private val mediaList: Array<String>?) : RecyclerView.Adapter<mediaAdapter.MyViewHolder>() {

    inner class MyViewHolder constructor(view: View) : RecyclerView.ViewHolder(view) {
        val niwImage: NetworkImageView
        val videoView: ImageView


        init {

            niwImage = view.findViewById(R.id.image)
            videoView = view.findViewById(R.id.video)

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.feed_item_media, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val mediaName = mediaList!![position]
        val db1 = DatabaseHandler(activity)
        val user: HashMap<String, String>
        user = db1.userDetails
        user["idNo"]


        if ("http://saoshyant.net/postsimages/" == mediaName) {
            holder.niwImage.visibility = View.GONE
        } else {
            holder.niwImage.visibility = View.VISIBLE
            try {
                holder.niwImage.setImageUrl(mediaName, imageLoader)
            } catch (e: Exception) {
            }
        }

        if (mediaName == "") {
            holder.videoView.visibility = View.GONE
        } else {
            holder.videoView.visibility = View.VISIBLE
            MyApplication.instance!!.imageLoader.get("http://saoshyant.net/videoThumb/$mediaName", object : ImageLoader.ImageListener {
                override fun onErrorResponse(volleyError: VolleyError) {

                }

                override fun onResponse(imageContainer: ImageLoader.ImageContainer, b: Boolean) {
                    holder.videoView.background = BitmapDrawable(activity.resources, imageContainer.bitmap)
                    holder.videoView.setImageResource(R.drawable.icon_video_play)
                }
            })
            holder.videoView.setOnClickListener {
                val intent = Intent(activity, VideoPlayer::class.java)
                intent.putExtra("videoName", mediaName)
                activity.startActivity(intent)
            }
        }

    }


    override fun getItemCount(): Int {
        return mediaList?.size ?: 0
    }

    companion object {
        private val imageLoader = MyApplication.instance!!.imageLoader
    }


}
