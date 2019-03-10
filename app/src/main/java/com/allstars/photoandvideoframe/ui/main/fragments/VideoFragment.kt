package com.allstars.photoandvideoframe.ui.main.fragments

import android.content.Context
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.Surface
import android.view.TextureView.SurfaceTextureListener
import android.view.View
import android.view.ViewGroup
import com.allstars.photoandvideoframe.R
import kotlinx.android.synthetic.main.fragment_video.*


class VideoFragment : Fragment() {

    private lateinit var path: String

    private var mediaPlayer: MediaPlayer? = null

    private var videoHeight: Float = 0f
    private var videoWidth: Float = 0f

    private lateinit var listener: OnVideoErrorListener

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        if (context is OnVideoErrorListener) {
            listener = context
        } else {
            throw ClassCastException("class doesn't implement OnVideoErrorListener")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_video, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        path = arguments!!.getString(KEY_VIDEO_PATH) as String

        calculateVideoSize()

        textureView.surfaceTextureListener = object : SurfaceTextureListener {
            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {

            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {

            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
                return true
            }

            override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture?, width: Int, height: Int) {
                updateTextureViewSize(width, height)

                val surface = Surface(surfaceTexture)

                try {
                    mediaPlayer = MediaPlayer()
                    mediaPlayer?.setDataSource(path)
                    mediaPlayer?.setSurface(surface)
                    mediaPlayer?.isLooping = true
                    mediaPlayer?.setVolume(0f,0f)
                    mediaPlayer?.prepareAsync()
                    mediaPlayer?.setOnPreparedListener { mediaPlayer ->
                        mediaPlayer.start()
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "Error", e)
                    listener.onVideoError()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        mediaPlayer?.pause()
    }

    override fun onPause() {
        super.onPause()

        mediaPlayer?.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun calculateVideoSize() {
        try {
            val metaRetriever = MediaMetadataRetriever()
            metaRetriever.setDataSource(path)
            val height = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
            val width = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)

            videoHeight = java.lang.Float.parseFloat(height)
            videoWidth = java.lang.Float.parseFloat(width)

        } catch (e: java.lang.Exception) {
            Log.d(TAG, "Error", e)
            listener.onVideoError()
        }
    }

    private fun updateTextureViewSize(viewWidth: Int, viewHeight: Int) {
        var scaleX: Float = viewWidth / videoWidth
        var scaleY: Float = viewHeight / videoHeight

        val scale = Math.max(scaleX, scaleY)

        scaleX = scale / scaleX
        scaleY = scale / scaleY

        val pivotPointX = viewWidth / 2
        val pivotPointY = viewHeight / 2

        val matrix = Matrix()
        matrix.setScale(scaleX, scaleY, pivotPointX.toFloat(), pivotPointY.toFloat())
        textureView.setTransform(matrix)
    }

    companion object {
        private val TAG = VideoFragment::class.java.simpleName

        private const val KEY_VIDEO_PATH = "KEY_VIDEO_PATH"

        fun newInstance(imagePath: String): VideoFragment {
            val fragment = VideoFragment()
            val bundle = Bundle()
            bundle.putString(KEY_VIDEO_PATH, imagePath)
            fragment.arguments = bundle
            return fragment
        }
    }

    interface OnVideoErrorListener {
        fun onVideoError()
    }
}