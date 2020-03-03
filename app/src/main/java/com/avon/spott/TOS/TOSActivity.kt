package com.avon.spott.TOS

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.avon.spott.R
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_tos.*
import kotlinx.android.synthetic.main.toolbar.view.*

class TOSActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tos)

        //툴바 타이틀 넣기
        include_toolbar_tos_a.text_title_toolbar.text = getString(R.string.text_terms_of_service)

        include_toolbar_tos_a.img_back_toolbar.setOnClickListener {
            onBackPressed()
        }

        tab_tos_a.addTab(tab_tos_a.newTab().setText(getString(R.string.text_terms_of_service)))
        tab_tos_a.addTab(tab_tos_a.newTab().setText(getString(R.string.privacy_policy)))
        tab_tos_a.addTab(tab_tos_a.newTab().setText(getString(R.string.location_policy)))
        tab_tos_a.tabGravity = TabLayout.GRAVITY_FILL

        val adapter = TOSAdapter(supportFragmentManager, tab_tos_a.tabCount)
        viewpager_tos_a.adapter = adapter

        viewpager_tos_a.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tab_tos_a))

        tab_tos_a.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewpager_tos_a.currentItem = tab.position
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {

            }
            override fun onTabReselected(tab: TabLayout.Tab) {

            }
        })

        if(intent.getStringExtra("private")!=null){
            tab_tos_a.getTabAt(2)!!.select()
            viewpager_tos_a.currentItem = 2
        }

    }

    inner class TOSAdapter(fm: FragmentManager, internal var totalTabs: Int)
        : FragmentPagerAdapter(fm, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT){

        override fun getItem(position: Int): Fragment {
            when(position){
                0->{
                    return TOSFragment()
                }
                1->{
                    return LocationFragment()
                }
                else->{
                    return PrivateFragment()
                }
            }
        }

        override fun getCount(): Int {
            return totalTabs
        }

    }



}