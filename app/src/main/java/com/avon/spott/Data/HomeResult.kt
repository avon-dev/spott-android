package com.avon.spott.Data

data class HomeResult(var pageable : Boolean, var items:ArrayList<HomeItem>, var created_time :String) {
}