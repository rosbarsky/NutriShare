package net.nutrishare.app.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(
    tableName = "comments",
    foreignKeys = [ForeignKey(
        entity = Post::class,
        parentColumns = ["post_id"],
        childColumns = ["post_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Comment(
    @PrimaryKey @ColumnInfo(name = "comment_id") val commentId: String,
    @ColumnInfo(name = "post_id", index = true) val postId: String,
    @ColumnInfo(name = "message") val message: String,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "user_name") val userName: String,
    @ColumnInfo(name = "user_image") val userImage: String,
    @ColumnInfo(name = "timestamp") val timestamp: Long,
): Serializable {
    constructor():this("","","","","","",0)
}