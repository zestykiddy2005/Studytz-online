package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val phone: String,
    val name: String,
    val uni: String,
    val bio: String = "",
    val avatarUrl: String = "",
    val postsCount: Int = 0,
    val reelsCount: Int = 0,
    val productsCount: Int = 0,
    val country: String = "Tanzania",
    val campus: String = "Main Campus",
    val faculty: String = "Informatics",
    val department: String = "Computer Science",
    val programme: String = "BSc in Computer Science",
    val yearOfStudy: String = "Mwaka wa 2",
    val verified: Boolean = false,
    val studyHours: Float = 0.0f
)

@Entity(tableName = "universities")
data class UniversityEntity(
    @PrimaryKey val code: String,
    val name: String,
    val type: String, // Public / Private
    val location: String
)

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val authorPhone: String,
    val authorName: String,
    val authorUni: String,
    val authorAvatarUrl: String = "",
    val text: String,
    val imageUrl: String = "", // Comma-separated urls for carousels
    val timestamp: Long = System.currentTimeMillis(),
    val likesCount: Int = 0,
    val isLiked: Boolean = false,
    val isPoll: Boolean = false,
    val pollQuestion: String = "",
    val pollOptions: String = "", // comma-separated
    val pollVotes: String = "", // comma-separated votes
    val viewCount: Int = 0,
    val isSaved: Boolean = false
)

@Entity(tableName = "reels")
data class ReelEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val authorPhone: String,
    val authorName: String,
    val authorUni: String,
    val authorAvatarUrl: String = "",
    val videoUrl: String = "",
    val description: String,
    val musicTitle: String = "",
    val likesCount: Int = 0,
    val isLiked: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val viewsCount: Int = 0,
    val isSaved: Boolean = false
)

@Entity(tableName = "market_products")
data class MarketEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val price: Double,
    val condition: String,
    val location: String,
    val phone: String,
    val description: String = "",
    val imageUrl: String = "",
    val sellerName: String,
    val sellerUni: String,
    val sellerPhone: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "stories")
data class StoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val authorPhone: String,
    val authorName: String,
    val authorAvatarUrl: String = "",
    val imageUrl: String,
    val caption: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val senderRole: String, // "user", "model", "system"
    val text: String,
    val audioUrl: String = "", // local audio file path
    val imageUrl: String = "", // base64
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "comments")
data class CommentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val postId: Long,
    val authorName: String,
    val authorAvatarUrl: String = "",
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "comment_replies")
data class CommentReplyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val commentId: Long,
    val authorName: String,
    val authorAvatarUrl: String = "",
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "study_groups")
data class StudyGroupEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val department: String,
    val course: String,
    val uni: String,
    val description: String = "",
    val memberCount: Int = 1
)

@Entity(tableName = "study_partners")
data class StudyPartnerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val uni: String,
    val department: String,
    val year: String,
    val bio: String = "",
    val contactPhone: String = ""
)

@Entity(tableName = "study_notes")
data class StudyNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val course: String,
    val author: String,
    val fileUrl: String = "",
    val downloads: Int = 0,
    val rating: Float = 5.0f,
    val uni: String = "UDSM"
)

@Entity(tableName = "peer_messages")
data class PeerMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val senderPhone: String,
    val receiverPhone: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "progress_goals")
data class ProgressGoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userPhone: String,
    val title: String,
    val targetHours: Float,
    val achievedHours: Float = 0f,
    val completed: Boolean = false
)

@Entity(tableName = "course_modules")
data class CourseModuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userPhone: String,
    val courseCode: String,
    val courseName: String,
    val moduleTitle: String,
    val completed: Boolean = false,
    val dueDate: String = "",
    val isSemesterGoal: Boolean = false,
    val priority: String = "Medium"
)

@Entity(tableName = "loan_notifications")
data class LoanNotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val message: String,
    val date: String,
    val isRead: Boolean = false
)

@Dao
interface AppDao {
    // Users
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE phone = :phone LIMIT 1")
    suspend fun getUserByPhone(phone: String): UserEntity?

    @Query("SELECT * FROM users WHERE uni = :uni")
    suspend fun getUsersByUni(uni: String): List<UserEntity>

    @Query("SELECT * FROM users")
    fun getAllUsersFlow(): Flow<List<UserEntity>>

    // Universities
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUniversity(uni: UniversityEntity)

    @Query("SELECT * FROM universities")
    fun getAllUniversities(): Flow<List<UniversityEntity>>

    @Query("DELETE FROM universities WHERE code = :code")
    suspend fun deleteUniversity(code: String)

    // Posts
    @Query("SELECT * FROM posts ORDER BY timestamp DESC")
    fun getAllPosts(): Flow<List<PostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: PostEntity): Long

    @Query("UPDATE posts SET likesCount = :likes, isLiked = :liked WHERE id = :id")
    suspend fun updatePostLike(id: Long, likes: Int, liked: Boolean)

    @Query("UPDATE posts SET pollVotes = :votes WHERE id = :id")
    suspend fun updatePostPollVotes(id: Long, votes: String)

    @Query("UPDATE posts SET viewCount = viewCount + 1 WHERE id = :id")
    suspend fun incrementPostView(id: Long)

    @Query("UPDATE posts SET isSaved = :saved WHERE id = :id")
    suspend fun updatePostSave(id: Long, saved: Boolean)

    @Query("DELETE FROM posts WHERE id = :id")
    suspend fun deletePost(id: Long)

    @Query("SELECT * FROM posts WHERE id = :id LIMIT 1")
    suspend fun getPostByIdDirect(id: Long): PostEntity?

    // Reels
    @Query("SELECT * FROM reels ORDER BY timestamp DESC")
    fun getAllReels(): Flow<List<ReelEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReel(reel: ReelEntity): Long

    @Query("UPDATE reels SET likesCount = :likes, isLiked = :liked WHERE id = :id")
    suspend fun updateReelLike(id: Long, likes: Int, liked: Boolean)

    @Query("UPDATE reels SET viewsCount = viewsCount + 1 WHERE id = :id")
    suspend fun incrementReelView(id: Long)

    @Query("UPDATE reels SET isSaved = :saved WHERE id = :id")
    suspend fun updateReelSave(id: Long, saved: Boolean)

    @Query("SELECT * FROM reels WHERE id = :id LIMIT 1")
    suspend fun getReelByIdDirect(id: Long): ReelEntity?

    // Market
    @Query("SELECT * FROM market_products ORDER BY timestamp DESC")
    fun getAllProducts(): Flow<List<MarketEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: MarketEntity): Long

    // Stories
    @Query("SELECT * FROM stories ORDER BY timestamp DESC")
    fun getAllStories(): Flow<List<StoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(story: StoryEntity): Long

    // Chat Messages
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getChatHistory(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessageEntity): Long

    @Query("DELETE FROM chat_messages")
    suspend fun clearChatHistory()

    // Comments
    @Query("SELECT * FROM comments WHERE postId = :postId ORDER BY timestamp ASC")
    fun getCommentsForPost(postId: Long): Flow<List<CommentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: CommentEntity)

    // Comment Replies
    @Query("SELECT * FROM comment_replies WHERE commentId = :commentId ORDER BY timestamp ASC")
    fun getRepliesForComment(commentId: Long): Flow<List<CommentReplyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReply(reply: CommentReplyEntity)

    // Study Groups
    @Query("SELECT * FROM study_groups ORDER BY name ASC")
    fun getAllStudyGroups(): Flow<List<StudyGroupEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudyGroup(group: StudyGroupEntity)

    // Study Partners
    @Query("SELECT * FROM study_partners")
    fun getAllStudyPartners(): Flow<List<StudyPartnerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudyPartner(partner: StudyPartnerEntity)

    // Study Notes / Resource Library
    @Query("SELECT * FROM study_notes ORDER BY title ASC")
    fun getAllStudyNotes(): Flow<List<StudyNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudyNote(note: StudyNoteEntity): Long

    @Query("SELECT * FROM study_notes WHERE id = :id LIMIT 1")
    suspend fun getStudyNoteByIdDirect(id: Long): StudyNoteEntity?

    // Peer Messages
    @Query("SELECT * FROM peer_messages WHERE (senderPhone = :p1 AND receiverPhone = :p2) OR (senderPhone = :p2 AND receiverPhone = :p1) ORDER BY timestamp ASC")
    fun getPeerMessages(p1: String, p2: String): Flow<List<PeerMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPeerMessage(msg: PeerMessageEntity)

    // Progress Tracker
    @Query("SELECT * FROM progress_goals WHERE userPhone = :phone")
    fun getProgressGoals(phone: String): Flow<List<ProgressGoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgressGoal(goal: ProgressGoalEntity)

    @Query("UPDATE progress_goals SET achievedHours = :hours, completed = :completed WHERE id = :id")
    suspend fun updateProgressGoal(id: Long, hours: Float, completed: Boolean)

    // Course Modules & Semester Goals
    @Query("SELECT * FROM course_modules WHERE userPhone = :phone ORDER BY courseCode ASC, id ASC")
    fun getCourseModules(phone: String): Flow<List<CourseModuleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourseModule(module: CourseModuleEntity): Long

    @Query("UPDATE course_modules SET completed = :completed WHERE id = :id")
    suspend fun updateCourseModuleStatus(id: Long, completed: Boolean)

    @Query("DELETE FROM course_modules WHERE id = :id")
    suspend fun deleteCourseModule(id: Long)

    // Loan Notifications
    @Query("SELECT * FROM loan_notifications ORDER BY date DESC")
    fun getAllLoanNotifications(): Flow<List<LoanNotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoanNotification(notif: LoanNotificationEntity)

    @Query("UPDATE loan_notifications SET isRead = 1 WHERE id = :id")
    suspend fun markLoanNotificationAsRead(id: Long)
}

@Database(
    entities = [
        UserEntity::class,
        UniversityEntity::class,
        PostEntity::class,
        ReelEntity::class,
        MarketEntity::class,
        StoryEntity::class,
        ChatMessageEntity::class,
        CommentEntity::class,
        CommentReplyEntity::class,
        StudyGroupEntity::class,
        StudyPartnerEntity::class,
        StudyNoteEntity::class,
        PeerMessageEntity::class,
        ProgressGoalEntity::class,
        LoanNotificationEntity::class,
        CourseModuleEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
}

object DatabaseProvider {
    private var db: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return db ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "studytz_database"
            ).fallbackToDestructiveMigration().build()
            db = instance
            instance
        }
    }
}
