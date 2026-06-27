package com.example.data

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FirestoreSync(private val context: Context, private val appDao: AppDao) {
    private var db: FirebaseFirestore? = null
    var isCloudActive: Boolean = false
        private set

    private var postsListener: ListenerRegistration? = null
    private var storiesListener: ListenerRegistration? = null
    private var reelsListener: ListenerRegistration? = null
    private var productsListener: ListenerRegistration? = null
    private var profilesListener: ListenerRegistration? = null
    private var notesListener: ListenerRegistration? = null

    init {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
            db = FirebaseFirestore.getInstance()
            isCloudActive = true
            Log.d("FirestoreSync", "Firebase Firestore initialized successfully!")
        } catch (e: Exception) {
            isCloudActive = false
            Log.e("FirestoreSync", "Firebase not initialized. Ensure google-services.json is in /app. Error: ${e.message}")
        }
    }

    fun startRealtimeSync(scope: CoroutineScope) {
        if (!isCloudActive) return
        val firestore = db ?: return

        // 1. Sync Posts
        try {
            postsListener = firestore.collection("posts")
                .addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        Log.e("FirestoreSync", "Posts sync failed: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshots != null) {
                        scope.launch(Dispatchers.IO) {
                            for (doc in snapshots.documents) {
                                try {
                                    val id = doc.getLong("id") ?: continue
                                    val authorPhone = doc.getString("authorPhone") ?: ""
                                    val authorName = doc.getString("authorName") ?: ""
                                    val authorUni = doc.getString("authorUni") ?: ""
                                    val authorAvatarUrl = doc.getString("authorAvatarUrl") ?: ""
                                    val text = doc.getString("text") ?: ""
                                    val imageUrl = doc.getString("imageUrl") ?: ""
                                    val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                                    val likesCount = doc.getLong("likesCount")?.toInt() ?: 0
                                    val isPoll = doc.getBoolean("isPoll") ?: false
                                    val pollQuestion = doc.getString("pollQuestion") ?: ""
                                    val pollOptions = doc.getString("pollOptions") ?: ""
                                    val pollVotes = doc.getString("pollVotes") ?: ""
                                    val viewCount = doc.getLong("viewCount")?.toInt() ?: 0

                                    // Preserve local user interactions
                                    val existing = appDao.getPostByIdDirect(id)
                                    val isLiked = existing?.isLiked ?: false
                                    val isSaved = existing?.isSaved ?: false

                                    val post = PostEntity(
                                        id = id,
                                        authorPhone = authorPhone,
                                        authorName = authorName,
                                        authorUni = authorUni,
                                        authorAvatarUrl = authorAvatarUrl,
                                        text = text,
                                        imageUrl = imageUrl,
                                        timestamp = timestamp,
                                        likesCount = likesCount,
                                        isLiked = isLiked,
                                        isPoll = isPoll,
                                        pollQuestion = pollQuestion,
                                        pollOptions = pollOptions,
                                        pollVotes = pollVotes,
                                        viewCount = viewCount,
                                        isSaved = isSaved
                                    )
                                    appDao.insertPost(post)
                                } catch (e: Exception) {
                                    Log.e("FirestoreSync", "Error parsing post doc: ${e.message}")
                                }
                            }
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Error starting posts listener: ${e.message}")
        }

        // 2. Sync Stories
        try {
            storiesListener = firestore.collection("stories")
                .addSnapshotListener { snapshots, error ->
                    if (error != null) return@addSnapshotListener
                    if (snapshots != null) {
                        scope.launch(Dispatchers.IO) {
                            for (doc in snapshots.documents) {
                                try {
                                    val id = doc.getLong("id") ?: continue
                                    val authorPhone = doc.getString("authorPhone") ?: ""
                                    val authorName = doc.getString("authorName") ?: ""
                                    val authorAvatarUrl = doc.getString("authorAvatarUrl") ?: ""
                                    val imageUrl = doc.getString("imageUrl") ?: ""
                                    val caption = doc.getString("caption") ?: ""
                                    val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()

                                    val story = StoryEntity(
                                        id = id,
                                        authorPhone = authorPhone,
                                        authorName = authorName,
                                        authorAvatarUrl = authorAvatarUrl,
                                        imageUrl = imageUrl,
                                        caption = caption,
                                        timestamp = timestamp
                                    )
                                    appDao.insertStory(story)
                                } catch (e: Exception) {
                                    Log.e("FirestoreSync", "Error parsing story doc: ${e.message}")
                                }
                            }
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Error starting stories listener: ${e.message}")
        }

        // 3. Sync Reels
        try {
            reelsListener = firestore.collection("reels")
                .addSnapshotListener { snapshots, error ->
                    if (error != null) return@addSnapshotListener
                    if (snapshots != null) {
                        scope.launch(Dispatchers.IO) {
                            for (doc in snapshots.documents) {
                                try {
                                    val id = doc.getLong("id") ?: continue
                                    val authorPhone = doc.getString("authorPhone") ?: ""
                                    val authorName = doc.getString("authorName") ?: ""
                                    val authorUni = doc.getString("authorUni") ?: ""
                                    val authorAvatarUrl = doc.getString("authorAvatarUrl") ?: ""
                                    val videoUrl = doc.getString("videoUrl") ?: ""
                                    val description = doc.getString("description") ?: ""
                                    val musicTitle = doc.getString("musicTitle") ?: ""
                                    val likesCount = doc.getLong("likesCount")?.toInt() ?: 0
                                    val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                                    val viewsCount = doc.getLong("viewsCount")?.toInt() ?: 0

                                    val existing = appDao.getReelByIdDirect(id)
                                    val isLiked = existing?.isLiked ?: false
                                    val isSaved = existing?.isSaved ?: false

                                    val reel = ReelEntity(
                                        id = id,
                                        authorPhone = authorPhone,
                                        authorName = authorName,
                                        authorUni = authorUni,
                                        authorAvatarUrl = authorAvatarUrl,
                                        videoUrl = videoUrl,
                                        description = description,
                                        musicTitle = musicTitle,
                                        likesCount = likesCount,
                                        isLiked = isLiked,
                                        timestamp = timestamp,
                                        viewsCount = viewsCount,
                                        isSaved = isSaved
                                    )
                                    appDao.insertReel(reel)
                                } catch (e: Exception) {
                                    Log.e("FirestoreSync", "Error parsing reel doc: ${e.message}")
                                }
                            }
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Error starting reels listener: ${e.message}")
        }

        // 4. Sync Products
        try {
            productsListener = firestore.collection("products")
                .addSnapshotListener { snapshots, error ->
                    if (error != null) return@addSnapshotListener
                    if (snapshots != null) {
                        scope.launch(Dispatchers.IO) {
                            for (doc in snapshots.documents) {
                                try {
                                    val id = doc.getLong("id") ?: continue
                                    val title = doc.getString("title") ?: ""
                                    val price = doc.getDouble("price") ?: 0.0
                                    val condition = doc.getString("condition") ?: ""
                                    val location = doc.getString("location") ?: ""
                                    val phone = doc.getString("phone") ?: ""
                                    val description = doc.getString("description") ?: ""
                                    val imageUrl = doc.getString("imageUrl") ?: ""
                                    val sellerName = doc.getString("sellerName") ?: ""
                                    val sellerUni = doc.getString("sellerUni") ?: ""
                                    val sellerPhone = doc.getString("sellerPhone") ?: ""
                                    val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()

                                    val product = MarketEntity(
                                        id = id,
                                        title = title,
                                        price = price,
                                        condition = condition,
                                        location = location,
                                        phone = phone,
                                        description = description,
                                        imageUrl = imageUrl,
                                        sellerName = sellerName,
                                        sellerUni = sellerUni,
                                        sellerPhone = sellerPhone,
                                        timestamp = timestamp
                                    )
                                    appDao.insertProduct(product)
                                } catch (e: Exception) {
                                    Log.e("FirestoreSync", "Error parsing product doc: ${e.message}")
                                }
                            }
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Error starting products listener: ${e.message}")
        }

        // 5. Sync Profiles (UserEntity)
        try {
            profilesListener = firestore.collection("profiles")
                .addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        Log.e("FirestoreSync", "Profiles sync failed: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshots != null) {
                        scope.launch(Dispatchers.IO) {
                            for (doc in snapshots.documents) {
                                try {
                                    val phone = doc.id
                                    val name = doc.getString("name") ?: continue
                                    val uni = doc.getString("uni") ?: ""
                                    val bio = doc.getString("bio") ?: ""
                                    val avatarUrl = doc.getString("avatarUrl") ?: ""
                                    val postsCount = doc.getLong("postsCount")?.toInt() ?: 0
                                    val reelsCount = doc.getLong("reelsCount")?.toInt() ?: 0
                                    val productsCount = doc.getLong("productsCount")?.toInt() ?: 0
                                    val country = doc.getString("country") ?: "Tanzania"
                                    val campus = doc.getString("campus") ?: ""
                                    val faculty = doc.getString("faculty") ?: ""
                                    val department = doc.getString("department") ?: ""
                                    val programme = doc.getString("programme") ?: ""
                                    val yearOfStudy = doc.getString("yearOfStudy") ?: ""
                                    val verified = doc.getBoolean("verified") ?: false
                                    val studyHours = doc.getDouble("studyHours")?.toFloat() ?: 0.0f

                                    val user = UserEntity(
                                        phone = phone,
                                        name = name,
                                        uni = uni,
                                        bio = bio,
                                        avatarUrl = avatarUrl,
                                        postsCount = postsCount,
                                        reelsCount = reelsCount,
                                        productsCount = productsCount,
                                        country = country,
                                        campus = campus,
                                        faculty = faculty,
                                        department = department,
                                        programme = programme,
                                        yearOfStudy = yearOfStudy,
                                        verified = verified,
                                        studyHours = studyHours
                                    )
                                    appDao.insertUser(user)
                                } catch (e: Exception) {
                                    Log.e("FirestoreSync", "Error parsing profile doc: ${e.message}")
                                }
                            }
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Error starting profiles listener: ${e.message}")
        }

        // 6. Sync Academic Materials (StudyNoteEntity)
        try {
            notesListener = firestore.collection("academic_materials")
                .addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        Log.e("FirestoreSync", "Notes sync failed: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshots != null) {
                        scope.launch(Dispatchers.IO) {
                            for (doc in snapshots.documents) {
                                try {
                                    val id = doc.getLong("id") ?: continue
                                    val title = doc.getString("title") ?: ""
                                    val course = doc.getString("course") ?: ""
                                    val author = doc.getString("author") ?: ""
                                    val fileUrl = doc.getString("fileUrl") ?: ""
                                    val downloads = doc.getLong("downloads")?.toInt() ?: 0
                                    val rating = doc.getDouble("rating")?.toFloat() ?: 5.0f
                                    val uni = doc.getString("uni") ?: "UDSM"

                                    val note = StudyNoteEntity(
                                        id = id,
                                        title = title,
                                        course = course,
                                        author = author,
                                        fileUrl = fileUrl,
                                        downloads = downloads,
                                        rating = rating,
                                        uni = uni
                                    )
                                    appDao.insertStudyNote(note)
                                } catch (e: Exception) {
                                    Log.e("FirestoreSync", "Error parsing note doc: ${e.message}")
                                }
                            }
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Error starting notes listener: ${e.message}")
        }
    }

    fun uploadPost(post: PostEntity) {
        if (!isCloudActive) return
        val firestore = db ?: return
        firestore.collection("posts").document(post.id.toString()).set(post)
            .addOnFailureListener { Log.e("FirestoreSync", "Failed to upload post to Firestore: ${it.message}") }
    }

    fun uploadStory(story: StoryEntity) {
        if (!isCloudActive) return
        val firestore = db ?: return
        firestore.collection("stories").document(story.id.toString()).set(story)
            .addOnFailureListener { Log.e("FirestoreSync", "Failed to upload story to Firestore: ${it.message}") }
    }

    fun uploadReel(reel: ReelEntity) {
        if (!isCloudActive) return
        val firestore = db ?: return
        firestore.collection("reels").document(reel.id.toString()).set(reel)
            .addOnFailureListener { Log.e("FirestoreSync", "Failed to upload reel to Firestore: ${it.message}") }
    }

    fun uploadProduct(product: MarketEntity) {
        if (!isCloudActive) return
        val firestore = db ?: return
        firestore.collection("products").document(product.id.toString()).set(product)
            .addOnFailureListener { Log.e("FirestoreSync", "Failed to upload product to Firestore: ${it.message}") }
    }

    fun uploadProfile(user: UserEntity) {
        if (!isCloudActive) return
        val firestore = db ?: return
        firestore.collection("profiles").document(user.phone).set(user)
            .addOnFailureListener { Log.e("FirestoreSync", "Failed to upload profile to Firestore: ${it.message}") }
    }

    fun uploadStudyNote(note: StudyNoteEntity) {
        if (!isCloudActive) return
        val firestore = db ?: return
        firestore.collection("academic_materials").document(note.id.toString()).set(note)
            .addOnFailureListener { Log.e("FirestoreSync", "Failed to upload academic material to Firestore: ${it.message}") }
    }

    fun uploadCourseModule(module: CourseModuleEntity) {
        if (!isCloudActive) return
        val firestore = db ?: return
        firestore.collection("course_modules").document(module.id.toString()).set(module)
            .addOnFailureListener { Log.e("FirestoreSync", "Failed to upload course module to Firestore: ${it.message}") }
    }

    fun deleteCourseModule(id: Long) {
        if (!isCloudActive) return
        val firestore = db ?: return
        firestore.collection("course_modules").document(id.toString()).delete()
            .addOnFailureListener { Log.e("FirestoreSync", "Failed to delete course module from Firestore: ${it.message}") }
    }

    fun stopSync() {
        postsListener?.remove()
        storiesListener?.remove()
        reelsListener?.remove()
        productsListener?.remove()
        profilesListener?.remove()
        notesListener?.remove()
    }
}
