package com.avon.spott

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.avon.spott.Utils.logd

/* 사진이미지가 들어있는 풍선 모양 마커 비트맵을 만드는 함수 */
fun getMarkerBitmapFromView(view: View, bitmap: Bitmap, size:Int, selected:Boolean, context:Context, kind:Int): Bitmap {
    val markerImageView = view.findViewById(R.id.img_photo_photo_m) as ImageView
    val countTextView = view.findViewById(R.id.text_count_photo_m) as TextView
    val markerCardView: CardView = view.findViewById(R.id.card_photo_m) as CardView
    val markerTriangleView = view.findViewById(R.id.view_triangle_photo_m) as View

    //클러스터면(사진 여러장이면) 카운트 텍스트 보이게, 클러스터아이템이면 사라지게
    if(size<2){
        countTextView.visibility = View.INVISIBLE
    }else{
        countTextView.visibility = View.VISIBLE
    }
    countTextView.text = size.toString()


    if(selected){ //선택된 클러스터 색깔 바꿈.
        markerCardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.markerSelected))
        markerTriangleView.setBackgroundResource(R.drawable.ic_signal_wifi_4_bar_select_24dp)
    }else{ //전에 선택되었던 클러스터 색깔 다시 하얀색으로 바꿈.
        if(kind==201){ // 포포 추천 장소 클러스터면 파란색으로 바꿈.
            markerCardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
            markerTriangleView.setBackgroundResource(R.drawable.ic_signal_wifi_4_bar_recommend_24dp)
        }else{ //일반 장소 클러스터면 하얀색으로 바꿈.
            markerCardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.text_white))
            markerTriangleView.setBackgroundResource(R.drawable.ic_signal_wifi_4_bar_white_24dp)
        }

    }

    markerImageView.setImageBitmap(bitmap)

    view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
    view.layout(0,0,view.getMeasuredWidth(),view.getMeasuredHeight() )

    val returnedBitmap = Bitmap.createBitmap(
        view.getMeasuredWidth(), view.getMeasuredHeight(),
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(returnedBitmap)
    canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN)
    val drawable =view.getBackground()
    if (drawable != null)
        drawable.draw(canvas)
    view.draw(canvas)

    logd("markerBitmap","here" + returnedBitmap)
    return returnedBitmap
}