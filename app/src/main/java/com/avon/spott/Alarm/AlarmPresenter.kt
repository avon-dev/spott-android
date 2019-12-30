package com.avon.spott.Alarm

class AlarmPresenter (val alarmView: AlarmContract.View) : AlarmContract.Presenter {
    init{ alarmView.presenter = this}

    override fun openPhoto() { alarmView.showPhotoUi() }

    override fun openCommnet() { alarmView.showCommentUi() }
}