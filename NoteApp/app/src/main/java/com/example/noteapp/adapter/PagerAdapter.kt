package com.example.noteapp.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.noteapp.ui.notes.NotesFragment
import com.example.noteapp.ui.tasks.TasksFragment

class PagerAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> NotesFragment()
            1 -> TasksFragment()
            else -> NotesFragment()
        }
    }
}