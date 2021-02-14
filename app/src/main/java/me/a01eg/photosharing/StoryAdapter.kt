package me.a01eg.photosharing

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.choota.dev.ctimeago.TimeAgo
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.story_item.view.*
import me.a01eg.photosharing.model.Story

/**
 * Adapter to render photo items
 *
 * Created on 22/11/2017.
 * Copyright by 01eg.me
 */

//: View - render data from model and listen for user action
class StoryHolder(itemView: View) : RecyclerView.ViewHolder(itemView), ValueEventListener, View.OnClickListener {

    private var likesRef: DatabaseReference? = null // collection
    private var likeSnapshot: DataSnapshot? = null // latest state of like

    private fun likeReference(): DatabaseReference?
            = likesRef?.child(FirebaseAuth.getInstance().uid!!)

    fun bind(model: Story?) {
        val storyId = model?.uid
        val path = model?.image!!
        val ref = FirebaseStorage.getInstance().getReference(path)

        // show time stamp
        itemView.timestamp?.text = TimeAgo().getTimeAgo(model.timestamp)
        itemView.like.setOnClickListener(this)

        // load image
        Glide.with(itemView.context).load(ref).into(itemView.image)

        // listen to likes changes in real-time
        if (likesRef == null || storyId.equals(likesRef?.ref?.key)) {
            likesRef?.removeEventListener(this)

            likesRef = FirebaseDatabase.getInstance().getReference("likes/$storyId")
            likesRef?.addValueEventListener(this)
        }

        // request latest like state for current user
        likeSnapshot?.ref?.removeEventListener(this)
        likeReference()?.addValueEventListener(this)
    }

    // OnClickListener

    override fun onClick(view: View?) {
        val isLiked = likeSnapshot?.getValue(Boolean::class.java)

        if (isLiked == true) {
            likeReference()?.removeValue() // remove value
        } else {
            likeReference()?.setValue(true) // set as 👍
        }
    }

    // ValueEventListener

    override fun onDataChange(snapshot: DataSnapshot) {
        // cache latest version of like status
        if (FirebaseAuth.getInstance().uid.equals(snapshot.key)) {
            likeSnapshot = snapshot
            itemView.like.isSelected = likeSnapshot?.getValue(Boolean::class.java) ?: false
        }
        //
        if (snapshot.exists()) {
            this.itemView.like.text = snapshot.childrenCount.toString()
            // there no likes at all, just hide the number
        } else {
            this.itemView.like.text = null
            itemView.like.isSelected = false
        }
    }

    override fun onCancelled(snapshot: DatabaseError) {
        this.itemView.like.text = null
    }

}

//: Controller - connect view with model
class StoryAdapter(options: FirestoreRecyclerOptions<Story>) : FirestoreRecyclerAdapter<Story, StoryHolder>(options) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return StoryHolder(layoutInflater.inflate(R.layout.story_item, parent, false))
    }

    override fun onBindViewHolder(holder: StoryHolder, position: Int, model: Story) {
        holder.bind(model)
    }
}

