package com.example.habittracker.Habit

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.habittracker.HabitApplication
import com.example.habittracker.R
import kotlinx.coroutines.launch

class HabitListFragment : Fragment(R.layout.fragment_habit_list) {

    private val swipeHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
        private val deleteBackground = ColorDrawable(Color.parseColor("#F44336")) // Czerwony
        private val checkBackground = ColorDrawable(Color.parseColor("#4CAF50"))  // Zielony

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return false
        }
        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.bindingAdapterPosition
            if (position == RecyclerView.NO_POSITION){
                return
            }
            val swipedHabit = adapter.currentList[position]
            when (direction) {
                ItemTouchHelper.LEFT -> {
                    viewModel.deleteHabit(swipedHabit.habit)
                }
                ItemTouchHelper.RIGHT -> {
                    viewModel.onCheckedChanged(swipedHabit.habit,!swipedHabit.isCompleted)
                }
            }
        }
        override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {
            val itemView = viewHolder.itemView
            val context = recyclerView.context

            val paint = Paint()
            val cornerRadius = 30f

            val deleteIcon: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_delete_24)
            val checkIcon: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_check_24)


            val iconMargin = (itemView.height - (deleteIcon?.intrinsicHeight ?: 0)) / 2

            if (dX > 0) {
                paint.color = Color.parseColor("#4CAF50")
                val background = RectF(
                    itemView.left.toFloat(),
                    itemView.top.toFloat(),
                    itemView.left + dX,
                    itemView.bottom.toFloat()
                )
                c.drawRoundRect(background, cornerRadius, cornerRadius, paint)
                checkIcon?.let {
                    val iconTop = itemView.top + (itemView.height - it.intrinsicHeight) / 2
                    val iconBottom = iconTop + it.intrinsicHeight
                    val iconLeft = itemView.left + iconMargin
                    val iconRight = itemView.left + iconMargin + it.intrinsicWidth

                    it.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                    it.draw(c)
                }

            } else if (dX < 0) {
                paint.color = Color.parseColor("#F44336")
                val background = RectF(
                    itemView.right.toFloat() + dX,
                    itemView.top.toFloat(),
                    itemView.right.toFloat(),
                    itemView.bottom.toFloat()
                )
                c.drawRoundRect(background, cornerRadius, cornerRadius, paint)

                deleteIcon?.let {
                    val iconTop = itemView.top + (itemView.height - it.intrinsicHeight) / 2
                    val iconBottom = iconTop + it.intrinsicHeight
                    val iconRight = itemView.right - iconMargin
                    val iconLeft = itemView.right - iconMargin - it.intrinsicWidth

                    it.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                    it.draw(c)
                }
            }

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }
        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            super.clearView(recyclerView, viewHolder)
            viewHolder.itemView.translationX = 0f
            viewHolder.itemView.alpha = 1f
        }
    }
    private val viewModel: HabitListViewModel by viewModels{
        val app = requireActivity().application as HabitApplication
        HabitViewModelFactory(app.entryRepo,app.habitRepo)
    }
    private lateinit var adapter : HabitAdapter

    private val habitColors = listOf(
        0xFFFFCDD2.toInt(),
        0xFFC8E6C9.toInt(),
        0xFFBBDEFB.toInt(),
        0xFFFFF9C4.toInt(),
        0xFFE1BEE7.toInt()
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.habit_recycler_view)


        adapter = HabitAdapter(
            onItemClicked = { habitUi ->
                showHabitDialog(habitUi.habit)
            },
        )
        recyclerView.adapter = adapter
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(recyclerView)
        val addButton = view.findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fab_add_habit)

        addButton.setOnClickListener {
            showHabitDialog(null)
        }


        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.habits.collect { listOfHabits ->
                    adapter.submitList(listOfHabits)
                }
            }
        }
    }

    private fun showHabitDialog(habitToEdit: Habit?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_habit, null)

        val titleTextView = dialogView.findViewById<TextView>(R.id.text_title_dialog)
        val nameInput = dialogView.findViewById<EditText>(R.id.edit_habit_name)
        val descInput = dialogView.findViewById<EditText>(R.id.edit_habit_desc)
        val freqAmountInput = dialogView.findViewById<EditText>(R.id.edit_habit_freq_amount)
        val freqSpinner = dialogView.findViewById<Spinner>(R.id.spinner_freq_group)

        val freqGroupsNames = listOf("Miesięcznie", "Dziennie", "Tygodniowo")
        val freqGroupValues = listOf(FreqGroup.MONTHLY, FreqGroup.DAILY, FreqGroup.WEEKLY)

        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, freqGroupsNames)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        freqSpinner.adapter = spinnerAdapter

        var selectedColor = habitColors[0]
        val colorViews = listOf(
            dialogView.findViewById<View>(R.id.color_1),
            dialogView.findViewById<View>(R.id.color_2),
            dialogView.findViewById<View>(R.id.color_3),
            dialogView.findViewById<View>(R.id.color_4),
            dialogView.findViewById<View>(R.id.color_5)
        )

        fun updateColorSelection(selectedView: View) {
            colorViews.forEach { it.alpha = 0.3f; it.scaleX = 1.0f; it.scaleY = 1.0f }
            selectedView.alpha = 1.0f
            selectedView.scaleX = 1.2f
            selectedView.scaleY = 1.2f
        }

        colorViews.forEachIndexed { index, view ->
            if (index < habitColors.size) {
                view.background.setTint(habitColors[index])
                view.setOnClickListener {
                    selectedColor = habitColors[index]
                    updateColorSelection(it)
                }
            }
        }

        if (habitToEdit != null) {

            nameInput.setText(habitToEdit.name)
            descInput.setText(habitToEdit.description)
            freqAmountInput.setText(habitToEdit.frequency.toString())

            val groupIndex = freqGroupValues.indexOf(habitToEdit.freqGroup)
            if (groupIndex >= 0) {
                freqSpinner.setSelection(groupIndex)
            }

            selectedColor = habitToEdit.color
            val colorIndex = habitColors.indexOf(habitToEdit.color)
            if (colorIndex >= 0 && colorIndex < colorViews.size) {
                updateColorSelection(colorViews[colorIndex])
            }
        } else {
            updateColorSelection(colorViews[0])
            freqSpinner.setSelection(1)
        }


        val buttonText = if (habitToEdit != null) "Zapisz" else "Dodaj"

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton(buttonText) { _, _ ->
                val name = nameInput.text.toString()
                val desc = descInput.text.toString()
                val freqAmount = freqAmountInput.text.toString().toIntOrNull() ?: 1
                val selectedFreqGroup = freqGroupValues[freqSpinner.selectedItemPosition]

                if (name.isNotEmpty()) {
                    if (habitToEdit == null) {
                        val newHabit = Habit(
                            name = name,
                            description = desc,
                            frequency = freqAmount,
                            freqGroup = selectedFreqGroup,
                            color = selectedColor
                        )
                        viewModel.addHabit(newHabit)
                    } else {
                        val updatedHabit = habitToEdit.copy(
                            name = name,
                            description = desc,
                            frequency = freqAmount,
                            freqGroup = selectedFreqGroup,
                            color = selectedColor
                        )
                        viewModel.updateHabit(updatedHabit)
                    }
                } else {
                    Toast.makeText(requireContext(), "Nazwa jest wymagana", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Anuluj", null)
            .show()
    }

    private inner class HabitAdapter(
        val onItemClicked: (HabitItemUi) -> Unit,
    ) : ListAdapter<HabitItemUi, HabitHolder>(HabitDiffCallback) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitHolder {
            val view = layoutInflater.inflate(R.layout.list_item_habit, parent, false)
            return HabitHolder(view)
        }

        override fun onBindViewHolder(holder: HabitHolder, position: Int) {
            val habit = getItem(position)
            holder.bind(habit)
        }

    }

    private inner class HabitHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        private val cardView: CardView = itemView.findViewById(R.id.habit_card)
        private val nameTextView: TextView = itemView.findViewById(R.id.habit_name)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.habit_desc)
        private val freqTextView: TextView = itemView.findViewById(R.id.habit_freq)
        private val doneCheckBox: CheckBox = itemView.findViewById(R.id.habit_check)

        fun bind(habitUi: HabitItemUi){
            val habit = habitUi.habit
            nameTextView.text = habit.name
            descriptionTextView.text = habit.description

            val freqText = when(habit.freqGroup) {
                FreqGroup.DAILY -> "Dziennie"
                FreqGroup.WEEKLY -> "Tygodniowo"
                FreqGroup.MONTHLY -> "Miesięcznie"
            }
            freqTextView.text = "${habit.frequency} / $freqText"

            cardView.setCardBackgroundColor(habit.color)

            doneCheckBox.setOnCheckedChangeListener(null)
            doneCheckBox.isChecked = habitUi.isCompleted

            itemView.setOnClickListener {
                adapter.onItemClicked(habitUi)
            }
        }
    }

    private companion object {
        private val HabitDiffCallback = object : DiffUtil.ItemCallback<HabitItemUi>() {
            override fun areItemsTheSame(oldItem: HabitItemUi, newItem: HabitItemUi) : Boolean {
                return oldItem.habit.habitId == newItem.habit.habitId
            }

            override fun areContentsTheSame(oldItem: HabitItemUi, newItem: HabitItemUi): Boolean {
                return oldItem == newItem
            }
        }
    }
}