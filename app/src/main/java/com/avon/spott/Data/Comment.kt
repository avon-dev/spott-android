package com.avon.spott.Data

data class Comment(val id :Int, val user: NickPhoto, val myself:Boolean, var contents: String, val created:String){

}