package me.a01eg.photosharing

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.story_item.view.*
import java.util.*

/**
 * Model to display photo
 *
 * Created on 22/11/2017.
 * Copyright by oleg
 */

// Model
class Story {
    var user: String? = null
    var image: String? = null
    var timestamp: Date? = null
}

// View
class StoryHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val image = itemView.image
    val context = itemView.context
}

// Controller
class StoryAdapter(options: FirestoreRecyclerOptions<Story>) : FirestoreRecyclerAdapter<Story, StoryHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): StoryHolder {
        val layoutInflater = LayoutInflater.from(parent?.context)
        return StoryHolder(layoutInflater.inflate(R.layout.story_item, parent, false))
    }

    override fun onBindViewHolder(holder: StoryHolder?, position: Int, model: Story?) {
        val path = model?.image!!
        val ref = FirebaseStorage.getInstance().getReference(path)

        Glide.with(holder?.context)
                .load(ref)
                .into(holder?.image)
    }

    override fun onDataChanged() {
        super.onDataChanged()

        Log.d("tag", "changed")

        notifyDataSetChanged()
    }

    override fun onError(e: FirebaseFirestoreException?) {
        super.onError(e)
        Log.d("tag", e.toString())
    }
}

