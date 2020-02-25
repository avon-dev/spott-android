package com.avon.spott.Data

data class ReportComment(val comment_contents:String, val comment_id :Int,
                         val post_id:Int, val detail:String, val reason:Int) {
}