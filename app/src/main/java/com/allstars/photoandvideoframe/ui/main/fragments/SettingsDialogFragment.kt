package com.allstars.photoandvideoframe.ui.main.fragments

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.widget.EditText
import com.allstars.photoandvideoframe.R

class SettingsDialogFragment : DialogFragment() {

    private lateinit var listener: OnChaneIntervalListener

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        if (context is OnChaneIntervalListener) {
            listener = context
        } else {
            throw ClassCastException("class doesn't implement OnChaneIntervalListener")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val et = LayoutInflater.from(context).inflate(R.layout.dialog_settings, null) as EditText

        et.setText(arguments!!.getLong(KEY_SLIDESHOW_INTERVAL).toString())
        et.setSelection(et.text.toString().length)

        return AlertDialog.Builder(context!!)
            .setTitle(R.string.lbl_slideshow_interval)
            .setView(et)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                try {
                    val interval = Integer.parseInt(et.editableText.toString()).toLong()
                    listener.onIntervalChanged(interval)
                } catch (e: Exception) {
                    et.error = getString(R.string.error_not_a_number)
                }
            }

            .setNegativeButton(android.R.string.cancel) { _, _ ->
                dismissAllowingStateLoss()
            }
            .create()
    }

    interface OnChaneIntervalListener {
        fun onIntervalChanged(newInterval: Long)
    }

    companion object {
        val TAG = SettingsDialogFragment::class.java.simpleName

        private const val KEY_SLIDESHOW_INTERVAL = "KEY_SLIDESHOW_INTERVAL"

        fun newInstance(slideShowInterval: Long): SettingsDialogFragment {
            val fragment = SettingsDialogFragment()
            val bundle = Bundle()
            bundle.putLong(KEY_SLIDESHOW_INTERVAL, slideShowInterval)
            fragment.arguments = bundle
            return fragment
        }
    }
}