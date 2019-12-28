package com.avon.spott.Main

import com.avon.spott.Main.MainActivity.Companion.mToolbar
import kotlinx.android.synthetic.main.toolbar.view.*


fun controlToobar(back:Int, profile:Int, name:Int, title:Int, more:Int, menu:Int, noti:Int){

    mToolbar.img_back_toolbar.visibility = back
    MainActivity.mToolbar.img_profile_toolbar.visibility=profile
    MainActivity.mToolbar.text_name_toolbar.visibility=name
    MainActivity.mToolbar.text_title_toolbar.visibility = title
    MainActivity.mToolbar.img_more_toolbar.visibility = more
    MainActivity.mToolbar.img_menu_toolbar.visibility = menu
    MainActivity.mToolbar.img_noti_toolbar.visibility = noti
}
