package net.saoshyant.Life.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import net.saoshyant.Life.R

class AddPost : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_post)
        //        GalleryConfig config = new GalleryConfig.Build()
        //                .limitPickPhoto(3)
        //                .singlePhoto(false)
        //                .hintOfPick("this is pick hint")
        //                .filterMimeTypes(new String[]{})
        //                .build();
        //        GalleryActivity.openActivity(AddPost.this, 2, config);
        val intent = Intent(this, net.saoshyant.Life.activity.Gallery::class.java)
        //Set the title
        intent.putExtra("title", "Select media")
        startActivityForResult(intent, 1)
        //process result

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1) {
            // Make sure the request was successful
            if (resultCode == Activity.RESULT_OK && data != null) {
                val selectionResult = data.getStringArrayListExtra("result")
                for (s in selectionResult) {
                    Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
                }
            }
        }
        //        //list of photos of seleced
        //        List<String> photos = (List<String>) data.getSerializableExtra(GalleryActivity.PHOTOS);
        //        for (String s : photos) {
        //            Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
        //        }
    }
}
