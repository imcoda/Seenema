package ink.c0d4.seenema.presentation.movie_details

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import ink.c0d4.seenema.R
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class WatchedMonthDialogFragment : BottomSheetDialogFragment() {
    private var yearInput: TextInputEditText? = null
    private var selectedMonth = 1
    private var draftYear = ""
    private var monthButtons = emptyList<MaterialButton>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.SheetDialog)
        val today = Calendar.getInstance()
        val initial = arguments?.getString(MONTH)
        draftYear = savedInstanceState?.getString(YEAR) ?: initial?.take(4) ?: today.get(Calendar.YEAR).toString()
        selectedMonth = savedInstanceState?.getInt(MONTH) ?: initial?.takeLast(2)?.toIntOrNull()
            ?: (today.get(Calendar.MONTH) + 1)
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?) =
        inflater.inflate(R.layout.dialog_watched_month, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val year = view.findViewById<TextInputEditText>(R.id.watched_year_picker)
        yearInput = year
        year.setText(draftYear)
        val yearLabel = view.findViewById<TextView>(R.id.watched_year_value)
        yearLabel.setOnClickListener {
            yearLabel.visibility = View.GONE
            year.visibility = View.VISIBLE
            year.requestFocus()
            year.selectAll()
            year.post {
                (requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager)
                    .showSoftInput(year, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
            }
        }
        year.setOnEditorActionListener { _, action, _ ->
            if (action == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                (requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager)
                    .hideSoftInputFromWindow(year.windowToken, 0)
                year.clearFocus(); year.visibility = View.GONE; yearLabel.visibility = View.VISIBLE
                true
            } else false
        }
        val locale = resources.configuration.locales[0] ?: Locale.getDefault()
        val names = DateFormatSymbols(locale).shortMonths
        val grid = view.findViewById<GridLayout>(R.id.watched_month_grid)
        val density = resources.displayMetrics.density
        monthButtons = (1..12).map { month ->
            MaterialButton(requireContext()).apply {
                id = View.generateViewId()
                tag = "month_$month"
                text = names[month - 1]
                isAllCaps = false
                textSize = 14f
                typeface = ResourcesCompat.getFont(requireContext(), R.font.montserrat_medium)
                setTextColor(Color.WHITE)
                cornerRadius = (12 * density).toInt()
                insetTop = 0; insetBottom = 0
                minWidth = 0; minimumWidth = 0
                contentDescription = names[month - 1]
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 0; height = (48 * density).toInt()
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    setMargins((4 * density).toInt(), (4 * density).toInt(), (4 * density).toInt(), (4 * density).toInt())
                }
                setOnClickListener { selectedMonth = month; updateSelection(view) }
                grid.addView(this)
            }
        }
        year.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                draftYear = s.toString(); updateSelection(view)
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        view.findViewById<View>(R.id.watched_previous_year).setOnClickListener { changeYear(-1) }
        view.findViewById<View>(R.id.watched_next_year).setOnClickListener { changeYear(1) }
        view.findViewById<View>(R.id.watched_month_cancel).setOnClickListener { dismiss() }
        view.findViewById<View>(R.id.watched_month_save).setOnClickListener {
            val value = year.text.toString().toIntOrNull()?.takeIf { it in 1..9999 } ?: return@setOnClickListener
            parentFragmentManager.setFragmentResult(REQUEST, Bundle().apply {
                putInt(MOVIE_ID, requireArguments().getInt(MOVIE_ID))
                putInt(YEAR, value)
                putInt(MONTH, selectedMonth)
            })
            dismiss()
        }
        updateSelection(view)
    }
    private fun changeYear(delta: Int) {
        val year = (draftYear.toIntOrNull() ?: Calendar.getInstance().get(Calendar.YEAR))
        yearInput?.setText((year + delta).coerceIn(1, 9999).toString())
    }
    private fun updateSelection(view: View) {
        monthButtons.forEachIndexed { index, button ->
            val selected = index + 1 == selectedMonth
            button.isSelected = selected
            button.backgroundTintList = ColorStateList.valueOf(Color.parseColor(if (selected) "#5E17EB" else "#221A30"))
            button.strokeWidth = if (selected) 0 else resources.displayMetrics.density.toInt().coerceAtLeast(1)
            button.strokeColor = ColorStateList.valueOf(Color.parseColor("#443552"))
        }
        val year = draftYear.toIntOrNull()?.takeIf { it in 1..9999 }
        view.findViewById<TextView>(R.id.watched_year_value).text = year?.toString() ?: getString(R.string.watched_year)
        view.findViewById<View>(R.id.watched_month_save).isEnabled = year != null
        val label = view.findViewById<TextView>(R.id.watched_month_selection)
        if (year != null) {
            val locale = resources.configuration.locales[0] ?: Locale.getDefault()
            val date = SimpleDateFormat("yyyy-MM", Locale.ROOT).parse(String.format(Locale.ROOT, "%04d-%02d", year, selectedMonth))!!
            label.text = SimpleDateFormat(android.text.format.DateFormat.getBestDateTimePattern(locale, "yMMMM"), locale).format(date)
        } else label.text = getString(R.string.watched_month)
    }
    override fun onStart() {
        super.onStart()
        (dialog as? BottomSheetDialog)?.let { sheet ->
            sheet.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)?.setBackgroundColor(Color.TRANSPARENT)
            sheet.behavior.state = BottomSheetBehavior.STATE_EXPANDED
            sheet.behavior.skipCollapsed = true
        }
    }
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(YEAR, draftYear)
        outState.putInt(MONTH, selectedMonth)
        super.onSaveInstanceState(outState)
    }
    override fun onDestroyView() {
        yearInput = null; monthButtons = emptyList()
        super.onDestroyView()
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
