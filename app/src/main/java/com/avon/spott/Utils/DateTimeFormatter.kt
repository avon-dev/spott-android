package com.avon.spott.Utils

class DateTimeFormatter {
    companion object{
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
    }
}