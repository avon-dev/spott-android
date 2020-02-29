package com.avon.spott.Data

data class PhotoResult(val user:UserData, val posts_image:String, val back_image:String?,
                       val latitude:Double, val longitude:Double, val contents:String,
                       val created:String, val public:Boolean, val comment:Int, val count:Int,
                       val like_checked:Boolean, val scrap_checked:Boolean, val myself:Boolean,
                       val result:Int) {
}