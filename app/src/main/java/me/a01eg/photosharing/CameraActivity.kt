package me.a01eg.photosharing

import android.app.Activity
import android.os.Bundle
import com.wonderkiln.camerakit.CameraView

// http://docs.camerakit.website/#/?id=capturing-images
class CameraActivity : Activity() {

    private lateinit var cameraView: CameraView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        cameraView = findViewById(R.id.camera)
    }

    override fun onResume() {
        super.onResume()
        cameraView.start()
    }

    override fun onPause() {
        cameraView.stop()
        super.onPause()
    }

}
