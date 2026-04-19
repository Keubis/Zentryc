package com.keubis.zentryc.ui.base

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.keubis.zentryc.R

abstract class BaseActivity : AppCompatActivity() {

    fun loadFragment(fragment: Fragment, addToBackStack: Boolean = false) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentContainer, fragment)
        if (addToBackStack) transaction.addToBackStack(null)
        transaction.commit()
    }
}