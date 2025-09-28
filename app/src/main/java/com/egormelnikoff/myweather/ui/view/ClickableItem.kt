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

class ClickableItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {
    private var titleTextView: TextView
    private var valueTextView: TextView
    var selectedValue: String = ""
        set(value) {
            field = value
            setOptions(value)
        }

    init {
        LayoutInflater.from(context).inflate(R.layout.item_settings_clickable, this, true)
        titleTextView = findViewById(R.id.title)
        valueTextView = findViewById(R.id.value)
    }

    init {
        attrs?.let {
            context.withStyledAttributes(it, R.styleable.ClickableItem, 0) {

                val titleId = getResourceId(R.styleable.ClickableItem_android_title, 0)
                if (titleId != 0) {
                    titleTextView.text = resources.getText(titleId)
                    val iconId = getResourceId(R.styleable.ClickableItem_android_icon, 0)
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
    }

    @SuppressLint("ResourceType")
    private fun setOptions(selectedValue: String) {
        valueTextView.text = selectedValue
    }
}