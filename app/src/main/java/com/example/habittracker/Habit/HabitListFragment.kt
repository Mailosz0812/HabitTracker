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
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.habittracker.HabitApplication
import com.example.habittracker.R
import kotlinx.coroutines.launch

class HabitListFragment : Fragment(R.layout.fragment_habit_list) {

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
            onCheckChanged = { habit, isChecked ->
                viewModel.onCheckedChanged(habit.habit,isChecked)
            },
            onDeleteClicked = { habit ->
                viewModel.deleteHabit(habit)
            }
        )
        recyclerView.adapter = adapter
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())

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
        val onCheckChanged: (HabitItemUi, Boolean) -> Unit,
        val onDeleteClicked: (Habit) -> Unit
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
        private val deleteButton: android.widget.ImageButton = itemView.findViewById(R.id.btn_delete_habit)

        fun bind(habit: HabitItemUi){
            nameTextView.text = habit.habit.name
            descriptionTextView.text = habit.habit.description

            doneCheckBox.setOnCheckedChangeListener(null)
            doneCheckBox.isChecked = habit.isCompleted

            itemView.setOnClickListener {
                adapter.onItemClicked(habit)
            }

            doneCheckBox.setOnCheckedChangeListener { _, isChecked ->
                adapter.onCheckChanged(habit,isChecked)
            }
            deleteButton.setOnClickListener {
                adapter.onDeleteClicked(habit.habit)
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