package com.allstars.photoandvideoframe.ui.main.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.allstars.photoandvideoframe.R
import com.allstars.photoandvideoframe.ui.main.MainActivity
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_image.*

class ImageFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_image, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val path = arguments?.getString(KEY_IMAGE_PATH)

        Glide.with(this).load(path).into(imageView)

    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName

        private const val KEY_IMAGE_PATH = "KEY_IMAGE_PATH"

        fun newInstance(imagePath: String): ImageFragment {
            val fragment = ImageFragment()
            val bundle = Bundle()
            bundle.putString(KEY_IMAGE_PATH, imagePath)
            fragment.arguments = bundle
            return fragment
        }
    }
}