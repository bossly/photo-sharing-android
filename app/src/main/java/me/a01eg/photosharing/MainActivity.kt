package me.a01eg.photosharing

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * Displaying collection of stories (images) in feed.
 *
 * Created on 22/11/2017.
 * Copyright by 01eg.me
 */

const val REQUEST_AUTH_CODE = 1
const val REQUEST_IMAGE_CAPTURE = 2

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        list.layoutManager = LinearLayoutManager(applicationContext)
        list.itemAnimator = DefaultItemAnimator()

        fab.setOnClickListener {
            openCamera()
        }

        loadConfigs()
    }

    override fun onStart() {
        super.onStart()

        val auth = FirebaseAuth.getInstance()

        if (auth.currentUser == null) {
            // request to loading
            openLoginScreen()
        } else {
            displayData()
        }
    }

    private fun loadConfigs(): FirebaseRemoteConfig? {
        val config = FirebaseRemoteConfig.getInstance()
        config.setDefaults(R.xml.config_defaults)
        config.fetch().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                config.activateFetched()
            }
        }

        return config
    }

    private fun openCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        if (takePictureIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
    }

    private fun uploadImage(success: Boolean, data: Intent?) {
        if (success) {
            val userId = FirebaseAuth.getInstance().uid as String
            val timestamp = SimpleDateFormat("yyyy-MM/dd-HHmmssSSS", Locale.UK).format(Date())
            val bitmap = data?.extras?.get("data") as Bitmap
            val storage = FirebaseStorage.getInstance().reference
            val ref = storage.child("uploads/$timestamp.jpg")
            val baos = ByteArrayOutputStream()

            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, baos)
            val bytes = baos.toByteArray()

            val metadata = StorageMetadata.Builder()
                    .setContentType("image/jpg")
                    .build()

            ref.putBytes(bytes, metadata).addOnSuccessListener {
                val story = Story()
                story.user = userId
                story.timestamp = Date(System.currentTimeMillis())
                story.image = ref.path

                FirebaseFirestore.getInstance().collection("feed").add(story)
            }

            Snackbar.make(list, "New Story created", Snackbar.LENGTH_SHORT).show()
        } else {
            // cancelled
        }
    }

    private fun openLoginScreen() {

        //: uncomment providers if you wanna use it
        val providers = mutableListOf(
//            AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build(),
//            AuthUI.IdpConfig.Builder(AuthUI.TWITTER_PROVIDER).build(),
//            AuthUI.IdpConfig.Builder(AuthUI.PHONE_VERIFICATION_PROVIDER).build(),
                AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build(),
                AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build()
        )

        val intent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build()
        startActivityForResult(intent, REQUEST_AUTH_CODE)
    }

    private fun authCompleted(success: Boolean) {
        if (success) {
            displayData()
        } else {
            // show message
            Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
            // open login screen again
            openLoginScreen()
        }
    }

    private fun displayData() {
        // request for stories
        val query = FirebaseFirestore.getInstance()
                .collection("feed")
                .orderBy("timestamp", Query.Direction.DESCENDING) // newest goes first
                .limit(50)

        val presenter = FirestoreRecyclerOptions.Builder<Story>()
                .setQuery(query, Story::class.java)
                .build()

        val adapter = StoryAdapter(presenter)
        list.adapter = adapter
        adapter.startListening()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = resultCode == Activity.RESULT_OK

        when (requestCode) {
            REQUEST_AUTH_CODE -> authCompleted(result)
            REQUEST_IMAGE_CAPTURE -> uploadImage(result, data)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            R.id.action_logout -> logout()
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun logout(): Boolean {
        FirebaseAuth.getInstance().signOut()
        // open new screen
        openLoginScreen()
        return true
    }
}
