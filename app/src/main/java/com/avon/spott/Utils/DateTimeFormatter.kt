package com.avon.spott.Utils

import android.annotation.SuppressLint
import android.text.format.DateFormat
import android.text.format.DateUtils
import java.text.SimpleDateFormat
import java.util.*

class DateTimeFormatter {


    companion object{
        private val TAG = "DateTimeFormatter"

        fun formatCreated(dateTime:String):String{

            val array = dateTime.split("T").toTypedArray()

            val beforeDate = array[0]

            val arrayDate = beforeDate.split("-").toTypedArray()

            val year = arrayDate[0]

            var month = arrayDate[1]
            if(month.startsWith("0")){ //월이 0으로 시작하면
                month= month.substring(1) //0을 제외한 두번째 숫자 부터 시작한다.
            }

            var date = arrayDate[2]
            if(date.startsWith("0")){ //일이 0으로 시작하면
                date = date.substring(1) //0을 제외한 두번째 숫자 부터 시작한다.
            }

            val newDate = year+"년 "+month+"월 "+date+"일"

            return newDate
        }

        // 60 1분
        // 3600 1시간
        //

        private val MINUTE = 60 // 방금
        private val HOUR_1 = 60 * MINUTE // 분 전
        private val HOUR_24 = 24 * HOUR_1 // 시간 전

        // 2020-03-06T03:47Z
        @SuppressLint("SimpleDateFormat")
        fun convertLocalDate(created:String):String {

            lateinit var result:String

            // 게시글 서버 시간에서 현지 시간으로 세팅하기
            val timeZone = TimeZone.getTimeZone("UTC")
            var simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'")
            simpleDateFormat.timeZone = timeZone
            val createdDate = simpleDateFormat.parse(created)
            val now = Calendar.getInstance(timeZone).time // 현재 시간 구하기

            val elapsedTime: Long = (now.time - createdDate.time) / 1000 // 경과한 시간

            if (elapsedTime < HOUR_24) { // 방금
                result = DateUtils.getRelativeTimeSpanString(createdDate.time, now.time, 0).toString()

            } else { // 년 월 일
                val formatString = DateFormat.getBestDateTimePattern(Locale.getDefault(), "ddMMMMyyyy")
                simpleDateFormat = SimpleDateFormat(formatString)
                simpleDateFormat.timeZone = timeZone
                result = simpleDateFormat.format(createdDate)
            }

            return result
        }
    }
}