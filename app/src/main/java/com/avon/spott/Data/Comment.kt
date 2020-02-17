package com.avon.spott.Data

data class Comment(val id :Int, val user: UserData, val myself:Boolean, var contents: String, val created:String){

}