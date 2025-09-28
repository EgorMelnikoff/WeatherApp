package com.egormelnikoff.myweather.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import com.egormelnikoff.myweather.R
import com.egormelnikoff.myweather.model.Place
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class MyBottomSheetFragment(
    private var places: List<Pair<Long, Place>> = listOf(),
    private var defaultId: Long?,
    private var onLocationSelected: ((Long) -> Unit)? = null
) : BottomSheetDialogFragment() {

    private lateinit var radioGroupLocations: RadioGroup
    private lateinit var clearSelectionButton: Button
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_dialog, container, false)
        radioGroupLocations = view.findViewById(R.id.radioGroupLocations)
        clearSelectionButton = view.findViewById(R.id.clear_selection)
        places.forEachIndexed { index, place ->
            val radioButton = RadioButton(context).apply {
                id = index + 1
                tag = place.first
                text = place.second.name
                textSize = 16F
                isChecked = place.first == defaultId
                layoutParams = RadioGroup.LayoutParams(
                    RadioGroup.LayoutParams.MATCH_PARENT,
                    RadioGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 16)
                }
            }
            radioGroupLocations.addView(radioButton)
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        radioGroupLocations.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId != -1) {
                val selectedRadioButton = group.findViewById<RadioButton>(checkedId)
                val placeId = selectedRadioButton?.tag as? Long
                placeId?.let {
                    onLocationSelected?.invoke(it)
                }
            }
        }
        clearSelectionButton.setOnClickListener {
            radioGroupLocations.clearCheck()
            onLocationSelected?.invoke(0L)
        }
    }
}