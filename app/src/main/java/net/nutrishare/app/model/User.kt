package net.nutrishare.app.model

import java.io.Serializable

data class User(
    val userId:String,
    val userName:String,
    val profileImageUrl:String
):Serializable{
     constructor():this("","","")
}
