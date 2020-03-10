package com.avon.spott.Utils

import android.annotation.SuppressLint
import android.text.format.DateFormat
import com.avon.spott.R
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
        private val HOUR_1 = 3600 // 분 전
        private val HOUR_24 = 86400 // 시간 전

        // 2020-03-06T03:47Z

        @SuppressLint("SimpleDateFormat")
        fun convertLocalDate(created:String):String {

            lateinit var result:String

            // 게시글 서버 시간에서 현지 시간으로 세팅하기
//            val timeZone = TimeZone.getDefault()
            val timeZone = TimeZone.getTimeZone("UTC")
            var simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'")
            simpleDateFormat.timeZone = timeZone
//            simpleDateFormat.timeZone = TimeZone.getTimeZone("UTC")
            val createdDate = simpleDateFormat.parse(created)
            val now = Calendar.getInstance(timeZone).time // 현재 시간 구하기

            val elapsedTime: Long = (now.time - createdDate.time) / 1000 // 경과한 시간

            if (elapsedTime < MINUTE) { // 방금
                result = App.mContext.getString(R.string.just)
            } else if (elapsedTime < HOUR_1) { // 분 전
                result = String.format(App.mContext.getString(R.string.minute_ago), elapsedTime/60)
            } else if (elapsedTime < HOUR_24) { // 시간 전
                result = String.format(App.mContext.getString(R.string.hour_ago), elapsedTime/3600)
            } else { // 년 월 일
                /* start */
                val formatString = DateFormat.getBestDateTimePattern(Locale.getDefault(), "ddMMMMyyyy")
                simpleDateFormat = SimpleDateFormat(formatString)
                val bestDateString = simpleDateFormat.format(createdDate)
                logd(TAG, "bestDateString : $bestDateString")
                logd(TAG, "bestDateString-fr: ${SimpleDateFormat(DateFormat.getBestDateTimePattern(Locale.FRANCE, "ddMMMMyyyy")).format(createdDate)}")
                logd(TAG, "bestDateString-jp: ${SimpleDateFormat(DateFormat.getBestDateTimePattern(Locale.JAPANESE, "ddMMMMyyyy")).format(createdDate)}")
                logd(TAG, "bestDateString-en: ${SimpleDateFormat(DateFormat.getBestDateTimePattern(Locale.UK, "ddMMMMyyyy")).format(createdDate)}")


                /* end */

                simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
                simpleDateFormat.timeZone = timeZone
                val createdDateToString = simpleDateFormat.format(createdDate)

                var list = createdDateToString.split("-")
                result = String.format(App.mContext.getString(R.string.yyyy_MM_dd), list[0], list[1], list[2])
            }

            /* Start */
//            val form = "yyyy-MM-dd'T'HH:mm'Z'"

//            val inputFormat = SimpleDateFormat(form)
//            inputFormat.timeZone = TimeZone.getTimeZone("Etc/UTC")
//            val tempDate = inputFormat.parse(created) // Z, Etc/UTC

//            val inputFormat2 = SimpleDateFormat(form)
//            inputFormat2.timeZone = TimeZone.getDefault()
//            val tempDate2 = inputFormat2.parse(created) // Z, default

//            val form2 = "yyyy-MM-dd'T'HH:mmX"
//            val inputFormat3 = SimpleDateFormat(form2)
//            inputFormat3.timeZone = TimeZone.getDefault()
//            val tempDate3 = inputFormat3.parse(created) // X, default

//            val a = 10
            /* End */

            return result
        }
    }
}