package com.keubis.zentryc

import android.os.Bundle
import com.keubis.zentryc.ui.base.BaseActivity
import com.keubis.zentryc.ui.dashboard.DashboardFragment

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            loadFragment(DashboardFragment())
        }
    }
}