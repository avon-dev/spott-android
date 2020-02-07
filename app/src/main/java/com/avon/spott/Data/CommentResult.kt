package com.avon.spott.Data

data class CommentResult(var pageable :Boolean, var items:ArrayList<Comment>, var created_time :String) {
}