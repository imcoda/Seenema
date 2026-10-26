package com.shalenmathew.movieflix.presentation.movie_details

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.NumberPicker
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.shalenmathew.movieflix.R
import java.util.Calendar

class WatchedMonthDialogFragment : DialogFragment() {
    private lateinit var yearPicker: NumberPicker
    private lateinit var monthPicker: NumberPicker

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = MaterialAlertDialogBuilder(requireContext(), R.style.TrackingAlertDialog)
        val view = LayoutInflater.from(builder.context).inflate(R.layout.dialog_watched_month, null)
        yearPicker = view.findViewById(R.id.watched_year_picker)
        monthPicker = view.findViewById(R.id.watched_month_picker)
        val today = Calendar.getInstance()
        val initial = arguments?.getString(MONTH)
        yearPicker.minValue = 1
        yearPicker.maxValue = 9999
        yearPicker.wrapSelectorWheel = false
        yearPicker.value = savedInstanceState?.getInt(YEAR)
            ?: initial?.take(4)?.toIntOrNull() ?: today.get(Calendar.YEAR)
        monthPicker.minValue = 1
        monthPicker.maxValue = 12
        monthPicker.value = savedInstanceState?.getInt(MONTH)
            ?: initial?.takeLast(2)?.toIntOrNull() ?: (today.get(Calendar.MONTH) + 1)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val white = ContextCompat.getColor(requireContext(), R.color.white)
            yearPicker.textColor = white
            monthPicker.textColor = white
        }
        return builder.setTitle(R.string.watched_month)
            .setView(view)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.save_changes) { _, _ ->
                // Commit a typed value even if the keyboard still has focus.
                view.clearFocus()
                parentFragmentManager.setFragmentResult(REQUEST, Bundle().apply {
                    putInt(MOVIE_ID, requireArguments().getInt(MOVIE_ID))
                    putInt(YEAR, yearPicker.value)
                    putInt(MONTH, monthPicker.value)
                })
            }.create()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        yearPicker.clearFocus()
        monthPicker.clearFocus()
        outState.putInt(YEAR, yearPicker.value)
        outState.putInt(MONTH, monthPicker.value)
        super.onSaveInstanceState(outState)
    }

    companion object {
        const val REQUEST = "watched_month"
        const val MOVIE_ID = "movie_id"
        const val YEAR = "year"
        const val MONTH = "month"

        fun newInstance(id: Int, month: String?) = WatchedMonthDialogFragment().apply {
            arguments = Bundle().apply {
                putInt(MOVIE_ID, id)
                putString(MONTH, month)
            }
        }
    }
}
