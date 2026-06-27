package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppRepository(private val context: Context) {
    private val appDao = DatabaseProvider.getDatabase(context).appDao()
    val firestoreSync = FirestoreSync(context, appDao)

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    // Flows from Dao
    val posts: Flow<List<PostEntity>> = appDao.getAllPosts()
    val reels: Flow<List<ReelEntity>> = appDao.getAllReels()
    val products: Flow<List<MarketEntity>> = appDao.getAllProducts()
    val stories: Flow<List<StoryEntity>> = appDao.getAllStories()
    val chatHistory: Flow<List<ChatMessageEntity>> = appDao.getChatHistory()
    val universities: Flow<List<UniversityEntity>> = appDao.getAllUniversities()
    val studyGroups: Flow<List<StudyGroupEntity>> = appDao.getAllStudyGroups()
    val studyPartners: Flow<List<StudyPartnerEntity>> = appDao.getAllStudyPartners()
    val studyNotes: Flow<List<StudyNoteEntity>> = appDao.getAllStudyNotes()
    val loanNotifications: Flow<List<LoanNotificationEntity>> = appDao.getAllLoanNotifications()
    val allUsers: Flow<List<UserEntity>> = appDao.getAllUsersFlow()

    fun getComments(postId: Long): Flow<List<CommentEntity>> = appDao.getCommentsForPost(postId)
    fun getReplies(commentId: Long): Flow<List<CommentReplyEntity>> = appDao.getRepliesForComment(commentId)
    fun getPeerMessages(receiverPhone: String): Flow<List<PeerMessageEntity>> {
        val currentPhone = _currentUser.value?.phone ?: ""
        return appDao.getPeerMessages(currentPhone, receiverPhone)
    }
    fun getProgressGoals(): Flow<List<ProgressGoalEntity>> {
        val currentPhone = _currentUser.value?.phone ?: ""
        return appDao.getProgressGoals(currentPhone)
    }

    suspend fun initSession(scope: kotlinx.coroutines.CoroutineScope) {
        val dbUser = appDao.getUserByPhone("0712345678")
        if (dbUser != null) {
            _currentUser.value = dbUser
        } else {
            seedInitialData()
        }
        firestoreSync.startRealtimeSync(scope)
    }

    suspend fun login(phone: String, pass: String): Boolean {
        return withContext(Dispatchers.IO) {
            val user = appDao.getUserByPhone(phone)
            if (user != null) {
                _currentUser.value = user
                true
            } else if (phone == "0712345678") {
                val newUser = UserEntity(
                    phone = "0712345678",
                    name = "Anuary Yahya",
                    uni = "UDSM",
                    bio = "Ninasoma Computer Engineering UDSM. Mapenzi yangu ni AI na Web development! 💻🔥",
                    avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150",
                    country = "Tanzania",
                    campus = "CoICT Kijitonyama",
                    faculty = "Faculty of Computing and Informatics",
                    department = "Department of Computer Science and Engineering",
                    programme = "BSc in Computer Engineering",
                    yearOfStudy = "Mwaka wa 3",
                    verified = true
                )
                appDao.insertUser(newUser)
                _currentUser.value = newUser
                true
            } else {
                false
            }
        }
    }

    suspend fun register(
        phone: String,
        name: String,
        uni: String,
        country: String = "Tanzania",
        campus: String = "Main Campus",
        faculty: String = "Informatics",
        department: String = "Computer Science",
        programme: String = "Degree",
        yearOfStudy: String = "Mwaka wa 1"
    ): UserEntity {
        return withContext(Dispatchers.IO) {
            val newUser = UserEntity(
                phone = phone,
                name = name,
                uni = uni,
                bio = "Mwanafunzi wa chuo! Karibu tubadilishane mawazo.",
                avatarUrl = "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=150",
                country = country,
                campus = campus,
                faculty = faculty,
                department = department,
                programme = programme,
                yearOfStudy = yearOfStudy,
                verified = false
            )
            appDao.insertUser(newUser)
            _currentUser.value = newUser
            newUser
        }
    }

    suspend fun logout() {
        _currentUser.value = null
    }

    suspend fun updateProfile(
        name: String,
        uni: String,
        bio: String,
        country: String,
        campus: String,
        faculty: String,
        department: String,
        programme: String,
        yearOfStudy: String
    ) {
        val current = _currentUser.value ?: return
        val updated = current.copy(
            name = name,
            uni = uni,
            bio = bio,
            country = country,
            campus = campus,
            faculty = faculty,
            department = department,
            programme = programme,
            yearOfStudy = yearOfStudy
        )
        appDao.insertUser(updated)
        _currentUser.value = updated
        firestoreSync.uploadProfile(updated)
    }

    suspend fun updateAvatar(url: String) {
        val current = _currentUser.value ?: return
        val updated = current.copy(avatarUrl = url)
        appDao.insertUser(updated)
        _currentUser.value = updated
        firestoreSync.uploadProfile(updated)
    }

    // Creating Posts
    suspend fun createPost(
        text: String,
        imageUrl: String = "",
        isPoll: Boolean = false,
        pollQuestion: String = "",
        pollOptions: String = ""
    ) {
        val current = _currentUser.value ?: return
        val post = PostEntity(
            authorPhone = current.phone,
            authorName = current.name,
            authorUni = current.uni,
            authorAvatarUrl = current.avatarUrl,
            text = text,
            imageUrl = imageUrl,
            isPoll = isPoll,
            pollQuestion = pollQuestion,
            pollOptions = pollOptions,
            pollVotes = if (isPoll) pollOptions.split(",").joinToString(",") { "0" } else ""
        )
        val generatedId = appDao.insertPost(post)
        val finalPost = post.copy(id = generatedId)
        firestoreSync.uploadPost(finalPost)
        
        // Update user post count
        val updatedUser = current.copy(postsCount = current.postsCount + 1)
        appDao.insertUser(updatedUser)
        _currentUser.value = updatedUser
    }

    suspend fun deletePost(postId: Long) {
        appDao.deletePost(postId)
    }

    suspend fun toggleLikePost(postId: Long, currentLikes: Int, currentLiked: Boolean) {
        appDao.updatePostLike(postId, if (currentLiked) currentLikes - 1 else currentLikes + 1, !currentLiked)
    }

    suspend fun toggleSavePost(postId: Long, isSaved: Boolean) {
        appDao.updatePostSave(postId, !isSaved)
    }

    suspend fun incrementPostViews(postId: Long) {
        appDao.incrementPostView(postId)
    }

    // Comments
    suspend fun addComment(postId: Long, text: String) {
        val current = _currentUser.value ?: return
        val comment = CommentEntity(
            postId = postId,
            authorName = current.name,
            authorAvatarUrl = current.avatarUrl,
            text = text
        )
        appDao.insertComment(comment)
    }

    suspend fun addReply(commentId: Long, text: String) {
        val current = _currentUser.value ?: return
        val reply = CommentReplyEntity(
            commentId = commentId,
            authorName = current.name,
            authorAvatarUrl = current.avatarUrl,
            text = text
        )
        appDao.insertReply(reply)
    }

    // Reels
    suspend fun createReel(description: String, musicTitle: String, videoUrl: String = "") {
        val current = _currentUser.value ?: return
        val reel = ReelEntity(
            authorPhone = current.phone,
            authorName = current.name,
            authorUni = current.uni,
            authorAvatarUrl = current.avatarUrl,
            videoUrl = videoUrl,
            description = description,
            musicTitle = musicTitle
        )
        val generatedId = appDao.insertReel(reel)
        val finalReel = reel.copy(id = generatedId)
        firestoreSync.uploadReel(finalReel)

        // Update user reel count
        val updatedUser = current.copy(reelsCount = current.reelsCount + 1)
        appDao.insertUser(updatedUser)
        _currentUser.value = updatedUser
    }

    suspend fun toggleLikeReel(reelId: Long, currentLikes: Int, currentLiked: Boolean) {
        appDao.updateReelLike(reelId, if (currentLiked) currentLikes - 1 else currentLikes + 1, !currentLiked)
    }

    suspend fun toggleSaveReel(reelId: Long, isSaved: Boolean) {
        appDao.updateReelSave(reelId, !isSaved)
    }

    suspend fun incrementReelViews(reelId: Long) {
        appDao.incrementReelView(reelId)
    }

    // Products
    suspend fun createProduct(title: String, price: Double, condition: String, location: String, phone: String, description: String, imageUrl: String = "") {
        val current = _currentUser.value ?: return
        val product = MarketEntity(
            title = title,
            price = price,
            condition = condition,
            location = location,
            phone = phone,
            description = description,
            imageUrl = imageUrl,
            sellerName = current.name,
            sellerUni = current.uni,
            sellerPhone = current.phone
        )
        val generatedId = appDao.insertProduct(product)
        val finalProduct = product.copy(id = generatedId)
        firestoreSync.uploadProduct(finalProduct)

        // Update user product count
        val updatedUser = current.copy(productsCount = current.productsCount + 1)
        appDao.insertUser(updatedUser)
        _currentUser.value = updatedUser
    }

    // Stories
    suspend fun createStory(imageUrl: String, caption: String) {
        val current = _currentUser.value ?: return
        val story = StoryEntity(
            authorPhone = current.phone,
            authorName = current.name,
            authorAvatarUrl = current.avatarUrl,
            imageUrl = imageUrl,
            caption = caption
        )
        val generatedId = appDao.insertStory(story)
        val finalStory = story.copy(id = generatedId)
        firestoreSync.uploadStory(finalStory)
    }

    // Chat
    suspend fun sendChatMessage(text: String, senderRole: String, audioUrl: String = "", imageUrl: String = ""): ChatMessageEntity {
        val message = ChatMessageEntity(
            senderRole = senderRole,
            text = text,
            audioUrl = audioUrl,
            imageUrl = imageUrl
        )
        val id = appDao.insertChatMessage(message)
        return message.copy(id = id)
    }

    suspend fun clearChat() {
        appDao.clearChatHistory()
    }

    // Peer chat messaging
    suspend fun sendPeerMessage(receiverPhone: String, text: String) {
        val currentPhone = _currentUser.value?.phone ?: return
        val msg = PeerMessageEntity(
            senderPhone = currentPhone,
            receiverPhone = receiverPhone,
            text = text
        )
        appDao.insertPeerMessage(msg)
    }

    // Study buddies & groups & notes
    suspend fun createStudyGroup(name: String, department: String, course: String, description: String) {
        val current = _currentUser.value ?: return
        val group = StudyGroupEntity(
            name = name,
            department = department,
            course = course,
            uni = current.uni,
            description = description
        )
        appDao.insertStudyGroup(group)
    }

    suspend fun uploadStudyNote(title: String, course: String) {
        val current = _currentUser.value ?: return
        val note = StudyNoteEntity(
            title = title,
            course = course,
            author = current.name,
            downloads = 0,
            rating = 5.0f,
            uni = current.uni
        )
        val generatedId = appDao.insertStudyNote(note)
        val finalNote = note.copy(id = generatedId)
        firestoreSync.uploadStudyNote(finalNote)
    }

    suspend fun addProgressGoal(title: String, targetHours: Float) {
        val currentPhone = _currentUser.value?.phone ?: return
        val goal = ProgressGoalEntity(
            userPhone = currentPhone,
            title = title,
            targetHours = targetHours
        )
        appDao.insertProgressGoal(goal)
    }

    suspend fun updateProgressGoalHours(id: Long, achievedHours: Float, targetHours: Float) {
        appDao.updateProgressGoal(id, achievedHours, achievedHours >= targetHours)
    }

    fun getCourseModules(): Flow<List<CourseModuleEntity>> {
        val currentPhone = _currentUser.value?.phone ?: ""
        return appDao.getCourseModules(currentPhone)
    }

    suspend fun addCourseModule(courseCode: String, courseName: String, moduleTitle: String, dueDate: String, isSemesterGoal: Boolean, priority: String) {
        val currentPhone = _currentUser.value?.phone ?: return
        val item = CourseModuleEntity(
            userPhone = currentPhone,
            courseCode = courseCode,
            courseName = courseName,
            moduleTitle = moduleTitle,
            dueDate = dueDate,
            isSemesterGoal = isSemesterGoal,
            priority = priority
        )
        val generatedId = appDao.insertCourseModule(item)
        firestoreSync.uploadCourseModule(item.copy(id = generatedId))
    }

    suspend fun updateCourseModuleStatus(id: Long, completed: Boolean) {
        appDao.updateCourseModuleStatus(id, completed)
    }

    suspend fun deleteCourseModule(id: Long) {
        appDao.deleteCourseModule(id)
        firestoreSync.deleteCourseModule(id)
    }

    // Admin commands for Universities
    suspend fun addUniversity(code: String, name: String, type: String, location: String) {
        appDao.insertUniversity(UniversityEntity(code, name, type, location))
    }

    suspend fun deleteUniversity(code: String) {
        appDao.deleteUniversity(code)
    }

    // Initial Data Seeding
    private suspend fun seedInitialData() = withContext(Dispatchers.IO) {
        // Seed Tanzanian Universities Database
        val defaultUnis = listOf(
            UniversityEntity("UDSM", "University of Dar es Salaam", "Public", "Dar es Salaam"),
            UniversityEntity("UDOM", "University of Dodoma", "Public", "Dodoma"),
            UniversityEntity("SUA", "Sokoine University of Agriculture", "Public", "Morogoro"),
            UniversityEntity("MUHAS", "Muhimbili University of Health and Allied Sciences", "Public", "Dar es Salaam"),
            UniversityEntity("SUZA", "State University of Zanzibar", "Public", "Zanzibar"),
            UniversityEntity("ARU", "Ardhi University", "Public", "Dar es Salaam"),
            UniversityEntity("MUST", "Mbeya University of Science and Technology", "Public", "Mbeya"),
            UniversityEntity("SAUT", "St. Augustine University of Tanzania", "Private", "Mwanza"),
            UniversityEntity("HKMU", "Hubert Kairuki Memorial University", "Private", "Dar es Salaam"),
            UniversityEntity("CUHAS", "Catholic University of Health and Allied Sciences", "Private", "Mwanza"),
            UniversityEntity("OUT", "Open University of Tanzania", "Public", "Dar es Salaam"),
            UniversityEntity("DIT", "Dar es Salaam Institute of Technology", "Public", "Dar es Salaam"),
            UniversityEntity("IFM", "Institute of Finance Management", "Public", "Dar es Salaam")
        )
        defaultUnis.forEach { appDao.insertUniversity(it) }

        // Seed default official Loan Notifications
        appDao.insertLoanNotification(LoanNotificationEntity(1, "HESLB: Malipo ya Mikopo Awamu ya 1", "Bodi ya Mikopo ya Wanafunzi wa Elimu ya Juu (HESLB) imeachia orodha ya awamu ya kwanza ya mikopo kwa Mwaka wa Masomo 2026/2027. Tafadhali angalia akaunti yako ya SIPA au wasiliana na Loan Officer wa chuo.", "25 Juni 2026", false))
        appDao.insertLoanNotification(LoanNotificationEntity(2, "HESLB: Maelekezo ya Rufaa za Mikopo", "Kwa wanafunzi ambao hawajaridhika na viwango vya mikopo walivyopangiwa, dirisha la rufaa (Appeals) litafunguliwa kuanzia tarehe 1 Julai hadi 15 Julai 2026.", "20 Juni 2026", true))
    }
}
