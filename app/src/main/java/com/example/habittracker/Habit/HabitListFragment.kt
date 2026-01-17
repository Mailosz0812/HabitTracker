package com.example.habittracker.Habit

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
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
    }
    private val viewModel: HabitListViewModel by viewModels{
        val app = requireActivity().application as HabitApplication
        HabitViewModelFactory(app.entryRepo,app.habitRepo)
    }
    private lateinit var adapter : HabitAdapter
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView = view.findViewById<RecyclerView>(R.id.habit_recycler_view)
        adapter = HabitAdapter(
            onItemClicked = { habit ->
                Toast.makeText(context, "Kliknięto: ${habit.habit.name}", Toast.LENGTH_SHORT).show()
            },
        )
        recyclerView.adapter = adapter
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(recyclerView)
        val addButton = view.findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fab_add_habit)
        addButton.setOnClickListener {
            showAddHabitDialog()
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.habits.collect { listOfHabits ->
                    adapter.submitList(listOfHabits)
                }
            }
        }
    }


    private fun showAddHabitDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_habit, null)

        val nameInput = dialogView.findViewById<EditText>(R.id.edit_habit_name)
        val descInput = dialogView.findViewById<EditText>(R.id.edit_habit_desc)

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle("Add Habit")
            .setPositiveButton("Add") { _, _ ->
                val name = nameInput.text.toString()
                val desc = descInput.text.toString()

                if (name.isNotEmpty()) {
                    val newHabit = Habit(
                        name = name,
                        description = desc,
                        frequency = 1,
                        freqGroup = FreqGroup.DAILY
                    )
                    viewModel.addHabit(newHabit)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private inner class HabitAdapter(
        val onItemClicked: (HabitItemUi) -> Unit,
    ) : ListAdapter<HabitItemUi, HabitHolder>(HabitDiffCallback) {
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): HabitHolder {
            val view = layoutInflater.inflate(R.layout.list_item_habit,parent,false)
            return HabitHolder(view)
        }
        override fun onBindViewHolder(holder: HabitHolder, position: Int) {
            val habit = getItem(position)
            holder.bind(habit)
        }
    }

    private inner class HabitHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        private val nameTextView: TextView = itemView.findViewById(R.id.habit_name)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.habit_desc)
        private val doneCheckBox: CheckBox = itemView.findViewById(R.id.habit_check)

        fun bind(habit: HabitItemUi){
            nameTextView.text = habit.habit.name
            descriptionTextView.text = habit.habit.description

            doneCheckBox.setOnCheckedChangeListener(null)
            doneCheckBox.isChecked = habit.isCompleted

            itemView.setOnClickListener {
                adapter.onItemClicked(habit)
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