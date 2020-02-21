package com.avon.spott.Data

data class MypageResult(val user:UserData, val posts:ArrayList<MapCluster>, val myself:Boolean,
                        val is_confirmation :Int) {
}