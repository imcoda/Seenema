package ink.c0d4.seenema.presentation.movie_details

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import ink.c0d4.seenema.R
import java.text.DateFormatSymbols
import java.util.Calendar
import java.util.Locale

class WatchedMonthDialogFragment : BottomSheetDialogFragment() {
    private var selectedYear = 1
    private var selectedMonth = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.SheetDialog)
        val today = Calendar.getInstance()
        val initial = arguments?.getString(MONTH)
        selectedYear = (savedInstanceState?.getInt(YEAR)
            ?: initial?.take(4)?.toIntOrNull() ?: today.get(Calendar.YEAR)).coerceIn(1, 9999)
        selectedMonth = (savedInstanceState?.getInt(MONTH)
            ?: initial?.takeLast(2)?.toIntOrNull() ?: (today.get(Calendar.MONTH) + 1)).coerceIn(1, 12)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?) =
        inflater.inflate(R.layout.dialog_watched_month, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val year = view.findViewById<NumberPicker>(R.id.watched_year_picker)
        val month = view.findViewById<NumberPicker>(R.id.watched_month_picker)
        year.minValue = 1
        year.maxValue = 9999
        year.wrapSelectorWheel = false
        year.value = selectedYear
        year.setOnValueChangedListener { _, _, value -> selectedYear = value }
        month.minValue = 1
        month.maxValue = 12
        month.displayedValues = DateFormatSymbols(resources.configuration.locales[0] ?: Locale.getDefault())
            .shortMonths.take(12).toTypedArray()
        month.wrapSelectorWheel = true
        month.value = selectedMonth
        month.setOnValueChangedListener { _, _, value -> selectedMonth = value }
        for (picker in listOf(year, month)) {
            picker.descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                picker.textColor = Color.WHITE
                picker.textSize = 22 * resources.displayMetrics.scaledDensity
                picker.selectionDividerHeight = (2 * resources.displayMetrics.density).toInt()
            }
        }
        view.findViewById<View>(R.id.watched_month_cancel).setOnClickListener { dismiss() }
        view.findViewById<View>(R.id.watched_month_save).setOnClickListener {
            parentFragmentManager.setFragmentResult(REQUEST, Bundle().apply {
                putInt(MOVIE_ID, requireArguments().getInt(MOVIE_ID))
                putInt(YEAR, year.value)
                putInt(MONTH, month.value)
            })
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        (dialog as? BottomSheetDialog)?.let { sheet ->
            sheet.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                ?.setBackgroundColor(Color.TRANSPARENT)
            sheet.behavior.state = BottomSheetBehavior.STATE_EXPANDED
            sheet.behavior.skipCollapsed = true
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(YEAR, view?.findViewById<NumberPicker>(R.id.watched_year_picker)?.value ?: selectedYear)
        outState.putInt(MONTH, view?.findViewById<NumberPicker>(R.id.watched_month_picker)?.value ?: selectedMonth)
        super.onSaveInstanceState(outState)
    }

    companion object {
        const val REQUEST = "watched_month"
        const val MOVIE_ID = "movie_id"
        const val YEAR = "year"
        const val MONTH = "month"
        fun newInstance(id: Int, month: String?) = WatchedMonthDialogFragment().apply {
            arguments = Bundle().apply { putInt(MOVIE_ID, id); putString(MONTH, month) }
        }
    }
}
