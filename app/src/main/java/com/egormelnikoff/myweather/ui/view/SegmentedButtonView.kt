package com.egormelnikoff.myweather.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import com.egormelnikoff.myweather.R


class SegmentedButtonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private var entryValues: List<String> = emptyList()
    private var entries: List<String> = emptyList()
    private var titleTextView: TextView
    private var radioGroup: RadioGroup

    var selectedValue: String? = ""
        set(value) {
            field = value
            if (value == null) {
                radioGroup.clearCheck()
                return
            }

            val index = entryValues.indexOf(value)
            if (index != -1) {
                val radioButtonId = index + 1
                radioGroup.findViewById<RadioButton>(radioButtonId)?.isChecked = true
            }
        }
    var onValueChangeListener: ((String) -> Unit)? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.item_segmented_button, this, true)
        titleTextView = findViewById(R.id.title)
        radioGroup = findViewById(R.id.segmented_button_row)
    }

    init {
        attrs?.let {
            context.withStyledAttributes(it, R.styleable.SegmentedButtonsView, 0) {

                val titleId = getResourceId(R.styleable.SegmentedButtonsView_android_title, 0)
                if (titleId != 0) {
                    titleTextView.text = resources.getText(titleId)
                    val iconId = getResourceId(R.styleable.SegmentedButtonsView_android_icon, 0)
                    if (iconId != 0) {
                        val iconDrawable = ContextCompat.getDrawable(context, iconId)

                        titleTextView.setCompoundDrawablesWithIntrinsicBounds(
                            iconDrawable, null, null, null
                        )
                    }
                }

                val entriesResId =
                    getResourceId(R.styleable.SegmentedButtonsView_android_entries, 0)
                val entryValuesResId =
                    getResourceId(R.styleable.SegmentedButtonsView_android_entryValues, 0)

                if (entriesResId != 0 && entryValuesResId != 0) {
                    selectedValue = getString(R.styleable.SegmentedButtonsView_value)
                    entries = resources.getStringArray(entriesResId).toList()
                    entryValues = resources.getStringArray(entryValuesResId).toList()
                    setOptions(entries, entryValues, selectedValue)
                }
            }
        }
    }

    @SuppressLint("ResourceType")
    private fun setOptions(entries: List<String>, entryValues: List<String>, selectedValue: String?) {
        radioGroup.removeAllViews()
        entryValues.forEachIndexed { index, currentValue ->
            val radioButton = RadioButton(context)
            val params = LayoutParams(
                0,
                108,
                1f
            )

            radioButton.layoutParams = params
            radioButton.text = entries[index]
            radioButton.gravity = android.view.Gravity.CENTER
            radioButton.setPadding(12, 12, 12, 12)
            radioButton.background =
                ContextCompat.getDrawable(context, R.drawable.segmented_button_selector)
            radioButton.buttonDrawable = null
            radioButton.setTextColor(
                ContextCompat.getColorStateList(
                    context,
                    R.drawable.segmented_button_text_color
                )
            )

            radioButton.isChecked = selectedValue == currentValue


            radioButton.id = index + 1
            radioGroup.addView(radioButton)
        }

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId != -1) {
                val clickIndex = checkedId - 1
                if (clickIndex in entryValues.indices) {
                    val newValue = entryValues[clickIndex]
                    if (newValue != this.selectedValue) {
                        this.selectedValue = newValue
                        onValueChangeListener?.invoke(newValue)
                    }
                }
            }
        }
    }
}