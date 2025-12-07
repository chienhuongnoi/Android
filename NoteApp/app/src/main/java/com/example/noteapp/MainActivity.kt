package com.example.noteapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.example.noteapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = PagerAdapter(this)
        binding.viewPager.adapter = adapter
        binding.viewPager.isUserInputEnabled = true // vuốt 2 màn

        // bottom nav
        binding.bottomNav.setOnItemSelectedListener {
            when(it.itemId){
                R.id.nav_notes -> binding.viewPager.currentItem = 0
                R.id.nav_tasks -> binding.viewPager.currentItem = 1
            }
            true
        }

        // sync ViewPager với bottom nav
        binding.viewPager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                binding.bottomNav.menu.getItem(position).isChecked = true
            }
        })
    }
}