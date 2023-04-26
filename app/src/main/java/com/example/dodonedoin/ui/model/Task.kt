package com.example.dodonedoin.ui.model
import android.os.Parcelable
import com.example.dodonedoin.helper.FirebaseHelper
import kotlinx.parcelize.Parcelize

@Parcelize
data class Task(
    var id: String = "",
    var hour:String = "",
    var minutes:String = "",
    var title:String = "",
    var description: String = "",
    var status: Int = 0
) : Parcelable {
    init {
        this.id = FirebaseHelper.getDatabase().push().key ?: ""
    }
}