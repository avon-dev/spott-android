package com.avon.spott

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings.Global.getString
import androidx.core.content.ContextCompat.startActivity

fun inquire(context: Context){
    try{
        val emailIntent = Intent(Intent.ACTION_SENDTO)
        emailIntent.setData(Uri.parse("mailto:"))
        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("avon.commu@gmail.com"))
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.inquiry_subject))

        startActivity(context, Intent.createChooser(emailIntent, context.getString(R.string.inquiry)), null)

    }catch (e:java.lang.Exception){
        e.printStackTrace()
    }

}