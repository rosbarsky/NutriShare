package net.nutrishare.app.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "posts")
data class Post(
    @PrimaryKey @ColumnInfo(name = "post_id") val postId: String,
    @ColumnInfo(name = "description") var description: String,
    @ColumnInfo(name = "product_link") var productLink: String,
    @ColumnInfo(name = "post_image") var postImage: String?,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "user_name") val userName: String,
    @ColumnInfo(name = "user_image") val userImage: String,
    val timestamp: Long = System.currentTimeMillis(),
): Serializable{
   constructor():this("","","","","","","",0)
}
