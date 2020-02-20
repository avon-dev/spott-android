package com.avon.spott.Data

data class NotiResult(var pageable : Boolean, var items:ArrayList<NotiItem>, var created_time :String) {
}