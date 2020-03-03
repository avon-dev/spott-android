package com.avon.spott.NewPassword

import com.avon.spott.BaseView

interface NewPasswordContract {

    interface View : BaseView<Presenter> {
        fun navigateUp()
        fun isPassword(isPassword:Boolean)
        fun isCheck(isCheck:Boolean)
        fun showWarning()
        fun fixResult(result:Boolean)
        fun showMessage(code:Int)
    }

    interface Presenter {
        fun navigateUp()
        fun isPassword(password:String)
        fun isCheck(password:String, checkpw:String)
        fun fixPassword(baseUrl:String, email:String, password:String)
    }
}