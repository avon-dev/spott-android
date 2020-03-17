package com.avon.spott.Main

import com.avon.spott.Main.MainActivity.Companion.mToolbar
import kotlinx.android.synthetic.main.toolbar.view.*


fun controlToolbar(back:Int, profile:Int, name:Int, title:Int, titlelogo:Int, more:Int, menu:Int, noti:Int, search:Int){

    mToolbar.img_back_toolbar.visibility = back
    MainActivity.mToolbar.img_profile_toolbar.visibility=profile
    MainActivity.mToolbar.text_name_toolbar.visibility=name
    MainActivity.mToolbar.text_title_toolbar.visibility = title
    MainActivity.mToolbar.img_title_toolbar.visibility = titlelogo
    MainActivity.mToolbar.img_more_toolbar.visibility = more
    MainActivity.mToolbar.img_menu_toolbar.visibility = menu
    MainActivity.mToolbar.frame_noti_toolbar.visibility = noti
    MainActivity.mToolbar.edit_search_toolbar.visibility = search
}
