package net.nutrishare.app.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "posts")
@Parcelize
data class Post(
    @PrimaryKey @ColumnInfo(name = "post_id") val postId: String,
    @ColumnInfo(name = "description") var description: String,
    @ColumnInfo(name = "product_link") var productLink: String,
    @ColumnInfo(name = "post_image") var postImage: String?,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "user_name") var userName: String,
    @ColumnInfo(name = "user_image") var userImage: String,
    @ColumnInfo(name = "is_favorite") var isFavorite: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
): Parcelable{
   constructor():this("","","","","","","",false,0)
}
