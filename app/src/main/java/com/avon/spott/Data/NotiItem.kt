package com.avon.spott.Data

data class NotiItem(val kind:Int, val id:Int, val post_image:String,  val post_id:Int,
                    val reason:String, val comment_user_image:String?, val comment_post_id:Int,
                    val comment_user_id:Int,
                    val comment_user_nick:String, val created_date:String, val reason_detail:String) {
}