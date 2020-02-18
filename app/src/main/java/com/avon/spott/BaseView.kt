package com.avon.spott

interface BaseView<T> {
    var presenter: T
}

/* Java로 Decompile하면 아래와 같음

Public interface BaseView {
   Object getPresenter();

   void setPresenter(Object var1);
}

 */