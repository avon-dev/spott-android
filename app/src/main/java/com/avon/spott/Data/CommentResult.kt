package com.avon.spott.Data

data class CommentResult(val pageable :Boolean, val items:ArrayList<Comment>, val created_time :String,
                         val notice_data:UserComment, val result:Boolean) {
}