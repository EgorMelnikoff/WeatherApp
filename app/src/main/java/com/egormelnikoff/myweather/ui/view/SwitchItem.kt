package com.egormelnikoff.myweather.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import com.egormelnikoff.myweather.R
import com.google.android.material.materialswitch.MaterialSwitch

class SwitchItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {
    private var titleTextView: TextView
    private var valueSwitch: MaterialSwitch
    var selectedValue: Boolean? = false
        set(value) {
            field = value
            setOptions(value)
        }
    var onValueChangeListener: ((Boolean) -> Unit)? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.item_settings_switch, this, true)
        titleTextView = findViewById(R.id.title)
        valueSwitch = findViewById(R.id.value_switch)

        attrs?.let {
            context.withStyledAttributes(it, R.styleable.SwitchItem, 0) {
                val titleId = getResourceId(R.styleable.SwitchItem_android_title, 0)
                if (titleId != 0) {
                    titleTextView.text = resources.getText(titleId)
                    val iconId = getResourceId(R.styleable.SwitchItem_android_icon, 0)
                    if (iconId != 0) {
                        val iconDrawable = ContextCompat.getDrawable(context, iconId)

                        titleTextView.setCompoundDrawablesWithIntrinsicBounds(
                            iconDrawable, null, null, null
                        )
                    }
                }
                setOptions(selectedValue)
            }
        }

        valueSwitch.setOnCheckedChangeListener { _, _ ->
            onValueChangeListener?.invoke(valueSwitch.isChecked)
        }
    }

    @SuppressLint("ResourceType")
    private fun setOptions(selectedValue: Boolean?) {
        valueSwitch.isChecked = selectedValue ?: false
    }
}