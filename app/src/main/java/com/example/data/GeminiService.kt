package com.example.data

import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

// --- Common Data Classes ---

data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val tools: List<Map<String, Any>>? = null,
    val systemInstruction: Content? = null
)

data class Content(
    val role: String? = null,
    val parts: List<Part>
)

data class Part(
    val text: String? = null,
    val inlineData: InlineData? = null
)

data class InlineData(
    val mimeType: String,
    val data: String // Base64
)

data class GenerationConfig(
    val thinkingConfig: ThinkingConfig? = null,
    val responseMimeType: String? = null,
    val responseSchema: Map<String, Any>? = null,
    val responseModalities: List<String>? = null,
    val speechConfig: SpeechConfig? = null,
    val imageConfig: ImageConfig? = null
)

data class ThinkingConfig(
    val thinkingLevel: String
)

data class SpeechConfig(
    val voiceConfig: VoiceConfig? = null
)

data class VoiceConfig(
    val prebuiltVoiceConfig: PrebuiltVoiceConfig? = null
)

data class PrebuiltVoiceConfig(
    val voiceName: String
)

data class ImageConfig(
    val aspectRatio: String,
    val imageSize: String
)

// --- Response Classes ---

data class GenerateContentResponse(
    val candidates: List<Candidate>? = null
)

data class Candidate(
    val content: Content? = null
)

// --- Gemini API Retrofit Service ---

interface GeminiApi {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object GeminiClient {
    private fun getApiKey(): String {
        return BuildConfig.GEMINI_API_KEY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val service = retrofit.create(GeminiApi::class.java)

    // Chat function
    suspend fun generateChat(
        prompt: String,
        model: String,
        history: List<ChatMessageEntity>,
        systemInstruction: String? = null,
        useSearch: Boolean = false,
        useMaps: Boolean = false,
        isHighThinking: Boolean = false
    ): String = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()

        // Build contents list
        val contentsList = mutableListOf<Content>()
        
        // Add historical context
        history.forEach { msg ->
            contentsList.add(
                Content(
                    role = if (msg.senderRole == "user") "user" else "model",
                    parts = listOf(Part(text = msg.text))
                )
            )
        }
        
        // Add current prompt
        contentsList.add(
            Content(
                role = "user",
                parts = listOf(Part(text = prompt))
            )
        )

        // Tools (Grounding)
        val toolsList = mutableListOf<Map<String, Any>>()
        if (useSearch) {
            toolsList.add(mapOf("googleSearch" to emptyMap<String, Any>()))
        }
        if (useMaps) {
            toolsList.add(mapOf("googleMaps" to emptyMap<String, Any>()))
        }

        // Config
        var config: GenerationConfig? = null
        if (isHighThinking) {
            config = GenerationConfig(
                thinkingConfig = ThinkingConfig(thinkingLevel = "HIGH")
            )
        }

        val systemContent = systemInstruction?.let {
            Content(parts = listOf(Part(text = it)))
        }

        val request = GenerateContentRequest(
            contents = contentsList,
            generationConfig = config,
            tools = if (toolsList.isNotEmpty()) toolsList else null,
            systemInstruction = systemContent
        )

        var lastException: Exception? = null
        val modelsToTry = if (model == "gemini-2.5-flash") {
            listOf("gemini-2.5-flash", "gemini-2.0-flash-lite", "gemini-2.5-pro")
        } else {
            listOf(model, "gemini-2.5-flash", "gemini-2.0-flash-lite")
        }

        var successText: String? = null
        for (currModel in modelsToTry) {
            try {
                val response = service.generateContent(currModel, apiKey, request)
                val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (text != null) {
                    successText = text
                    break
                }
            } catch (e: Exception) {
                lastException = e
            }
        }

        if (successText != null) {
            successText
        } else {
            if (lastException is retrofit2.HttpException) {
                val errorBody = lastException.response()?.errorBody()?.string() ?: "Suala lisilojulikana"
                "Makosa ya Mtandao (HTTP ${lastException.code()}): $errorBody"
            } else {
                "Makosa: ${lastException?.localizedMessage ?: lastException?.message ?: "Suala lisilojulikana"}"
            }
        }
    }

    // TTS
    suspend fun generateSpeech(text: String, voice: String): String? = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = text)))),
            generationConfig = GenerationConfig(
                responseModalities = listOf("AUDIO"),
                speechConfig = SpeechConfig(
                    voiceConfig = VoiceConfig(
                        prebuiltVoiceConfig = PrebuiltVoiceConfig(voiceName = voice)
                    )
                )
            )
        )
        try {
            val response = service.generateContent("gemini-2.5-flash-preview-tts", apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.inlineData?.data
        } catch (e: Exception) {
            null
        }
    }

    // Generate Music using Lyria models
    suspend fun generateMusic(prompt: String, duration30s: Boolean): String? = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        val model = if (duration30s) "lyria-002" else "lyria-002"
        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(
                responseModalities = listOf("AUDIO")
            )
        )
        try {
            val response = service.generateContent(model, apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.inlineData?.data
        } catch (e: Exception) {
            null
        }
    }

    // Image generation
    suspend fun generateImage(prompt: String, size: String): String? = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(
                imageConfig = ImageConfig(aspectRatio = "1:1", imageSize = size),
                responseModalities = listOf("TEXT", "IMAGE")
            )
        )
        try {
            val response = service.generateContent("imagen-3.0-generate-002", apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull { it.inlineData != null }?.inlineData?.data
        } catch (e: Exception) {
            null
        }
    }

    // Video understanding using Gemini Pro
    suspend fun analyzeVideo(prompt: String, videoBase64: String): String = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        val request = GenerateContentRequest(
            contents = listOf(
                Content(
                    parts = listOf(
                        Part(text = prompt),
                        Part(inlineData = InlineData(mimeType = "video/mp4", data = videoBase64))
                    )
                )
            )
        )
        try {
            val response = service.generateContent("gemini-2.5-pro", apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "Sikuweza kuchambua video hii."
        } catch (e: Exception) {
            "Makosa ya Video: ${e.message}"
        }
    }

    // Audio Transcription
    suspend fun transcribeAudio(audioBase64: String): String = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        val request = GenerateContentRequest(
            contents = listOf(
                Content(
                    parts = listOf(
                        Part(text = "Tafsiri au andika maneno yote yaliyopo kwenye sauti hii kwa usahihi kwa Kiswahili au Kiingereza."),
                        Part(inlineData = InlineData(mimeType = "audio/wav", data = audioBase64))
                    )
                )
            )
        )
        try {
            val response = service.generateContent("gemini-2.5-flash", apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "Sikuweza kunakili sauti hii."
        } catch (e: Exception) {
            "Makosa ya Sauti: ${e.message}"
        }
    }
}

// --- Robust AI Intent Controller & Structured Response Parsers ---
enum class AcademicIntent {
    SUMMARIZATION,
    NOTE_GENERATION,
    TRANSLATION,
    QUIZ_GENERATION,
    GENERAL_ACADEMIC
}

object AIIntentController {
    fun detectAcademicIntent(prompt: String): AcademicIntent {
        val lowercasePrompt = prompt.lowercase()
        
        val noteKeywords = listOf("notes", "lecture notes", "andika notes", "generate notes", "dondoo za somo", "dondoo muhimu", "tengeneza notes", "kutengeneza notes", "andika dondoo", "note generation")
        if (noteKeywords.any { lowercasePrompt.contains(it) }) {
            return AcademicIntent.NOTE_GENERATION
        }

        val summaryKeywords = listOf("summarize", "summary", "muhtasari", "fupisha", "outline", "fanya muhtasari", "kuhusisha", "dondoo", "notes fupi")
        if (summaryKeywords.any { lowercasePrompt.contains(it) }) {
            return AcademicIntent.SUMMARIZATION
        }
        
        val translationKeywords = listOf("translate", "translation", "tafsiri", "vocabulary", "msamiati", "meaning of", "maana ya", "how do you say", "unasemaje", "swahili", "english", "kiswahili", "kiingereza")
        if (translationKeywords.any { lowercasePrompt.contains(it) }) {
            return AcademicIntent.TRANSLATION
        }
        
        val quizKeywords = listOf("quiz", "test", "maswali", "mitihani", "mcq", "mazoezi", "q&a", "question", "swali", "tengeneza maswali")
        if (quizKeywords.any { lowercasePrompt.contains(it) }) {
            return AcademicIntent.QUIZ_GENERATION
        }
        
        return AcademicIntent.GENERAL_ACADEMIC
    }

    fun getSystemInstructionForIntent(intent: AcademicIntent): String {
        return when (intent) {
            AcademicIntent.SUMMARIZATION -> {
                "Wewe ni StudyAI Summarizer. Mtumiaji anataka muhtasari (summary) wa dondoo au mada ya masomo. " +
                "Hakikisha unarudisha jibu lililopangwa kitaalamu kwa kutumia emoji na vichwa vya habari vifuatavyo:\n\n" +
                "📌 **MUHTASARI WA HARAKA (Overview)**: Eleza mada hii kwa ufupi kwa maneno machache rahisi lakini yenye kueleweka.\n\n" +
                "🔑 **HOJA MUHIMU (Key Concepts)**: Orodhesha mambo makuu yote ya kipekee kwa mfumo wa bullet points (alama za •).\n\n" +
                "📊 **JEDWALI LA DHANA (Key Terms)**: Kama kuna maneno magumu ya kitaalamu, unda jedwali la Markdown la kulinganisha maneno, maana zake, na mifano yake ya haraka.\n\n" +
                "💡 **MIFANO YA KITANZANIA (Local Context)**: Toa mifano inayohusiana na mazingira ya vyuo vya Tanzania (mfano: mikopo ya bodi ya HESLB, maisha ya hostel za chuo kama COICT, UDSM, UDOM, nk) ili mwanafunzi aelewe kwa urahisi zaidi."
            }
            AcademicIntent.NOTE_GENERATION -> {
                "Wewe ni StudyAI Lecture Note Architect. Mtumiaji anataka dondoo kamili na zilizopangwa vizuri (Detailed Lecture Notes) kuhusu mada fulani ya masomo.\n" +
                "Hakikisha unarudisha dondoo zilizoandaliwa kitaalamu sana kwa kutumia emoji na vichwa vya habari vifuatavyo:\n\n" +
                "📚 **DONDOO KUU ZA SOMO (Lecture Summary)**: Muhtasari mrefu na thabiti wa mada yenyewe.\n\n" +
                "🗂️ **VIPENGELE NA MADA NDOGO (Core Subtopics)**: Eleza mada ndogo ndogo (subtopics) muhimu kwa urefu na kwa mifano ya kueleweka.\n\n" +
                "📝 **MIFANO YA KIKALKULASI NA MIFANO YA KIHALISI (Practical/Worked Examples)**: Toa mifano ya hatua kwa hatua ya kutatua changamoto za mada hii, au mifano ya maisha halisi chuo.\n\n" +
                "💡 **USHAURI WA MAREJEO (Study Recommendations)**: Toa ushauri wa vitabu au mada za kusoma ili kuimarisha uelewa."
            }
            AcademicIntent.TRANSLATION -> {
                "Wewe ni StudyAI Translator. Mtumiaji anataka kutafsiri sentensi, msamiati, au maneno ya kiakademia (hasa Kiingereza kwenda Kiswahili au kinyume chake). " +
                "Hakikisha unarudisha jibu lililopangwa kitaalamu kwa kutumia emoji na vichwa vya habari vifuatavyo:\n\n" +
                "🔄 **TAFSIRI SAHIHI (Exact Translation)**: Toa tafsiri iliyo sahihi zaidi kwa herufi nzito (bold).\n\n" +
                "🔍 **MUKTADHA WA KITAALUMA (Academic Context)**: Eleza neno au msemo huu unatumikaje kwenye masomo ya chuo au tasnia husika.\n\n" +
                "📖 **MSAMIATI NA SARUFI (Grammar Breakdown)**: Unda jedwali la Markdown lenye maneno muhimu yaliyotumika, aina ya neno (kivumishi, nomino, nk), na tafsiri zake mbadala.\n\n" +
                "🗣️ **MIFANO YA MAZUNGUMZO (Example Sentences)**: Toa angalau mifano miwili ya sentensi za kiakademia/masomo ambapo neno au dhana hii imetumiwa kwa usahihi."
            }
            AcademicIntent.QUIZ_GENERATION -> {
                "Wewe ni StudyAI Quiz Architect. Mtumiaji anataka utengeneze maswali ya mazoezi (Quiz) kuhusu mada fulani ya chuo. " +
                "Tengeneza Quiz yenye muundo ufuatao wenye vichwa vya habari vifuatavyo:\n\n" +
                "📝 **MASWALI YA MAZOEZI (Quiz Questions)**:\n" +
                "- Tengeneza maswali 3 au 5 ya kuchagua (Multiple Choice Questions - MCQs) yenye chaguzi A, B, C, na D.\n" +
                "- Hakikisha maswali ni yenye changamoto na yanapima uelewa wa kweli wa mwanafunzi.\n\n" +
                "🗝️ **MAJIBU NA MAELEZO YA KINA (Answers & Explanations)**:\n" +
                "- Weka kichwa hiki cha habari mwishoni kabisa mwa ujumbe ili mwanafunzi asione majibu mapema.\n" +
                "- Toa maelezo kwa kueleza kwanini kila jibu ndio sahihi na kwanini chaguzi nyingine si sahihi kwa lugha ya Kiswahili rahisi na sanifu."
            }
            AcademicIntent.GENERAL_ACADEMIC -> {
                "Wewe ni StudyAI - Msaidizi bora wa kitaaluma kwa wanafunzi wa vyuo vikuu nchini Tanzania. Mtumiaji ana swali la jumla la kitaaluma au anahitaji msaada wa masomo. " +
                "Hakikisha unarudisha jibu safi, lililojaa muundo mzuri wa vichwa vya habari vyenye herufi nzito, bullet points (•), na kueleza dhana ngumu kwa kutumia mifano ya maisha halisi ya hapa Tanzania. Lugha ya mazungumzo iwe ya kirafiki, yenye kutia moyo, na yenye usahihi wa hali ya juu."
            }
        }
    }
}

data class StudyResponse(
    val intent: AcademicIntent,
    val textResponse: String,
    val isSuccess: Boolean,
    val errorMessage: String? = null
)

object StudyAIController {
    /**
     * Exposes a robust, secure entry point for handling natural language study requests.
     * It parses multi-intent logic, applies the correct system instructions, and connects to the
     * Gemini API securely using the GEMINI_API_KEY.
     */
    suspend fun handleStudyRequest(
        prompt: String,
        history: List<ChatMessageEntity>
    ): StudyResponse {
        if (prompt.isBlank()) {
            return StudyResponse(
                intent = AcademicIntent.GENERAL_ACADEMIC,
                textResponse = "Tafadhali andika swali au mada ya masomo ili nikusaidie.",
                isSuccess = false,
                errorMessage = "Blank prompt"
            )
        }

        // Secure API Key validation
        val apiKey = BuildConfig.GEMINI_API_KEY
        val hasKey = apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY" && apiKey != "TODO"
        
        if (!hasKey) {
            val missingKeyMessage = "Msaidizi wa AI (StudyAI) anahitaji Gemini API Key ili kufanya kazi!\n\n" +
                    "Tafadhali fuata hatua hizi rahisi ili kuongeza API Key yako kwenye Google AI Studio:\n\n" +
                    "1️⃣ Nenda kwenye **Secrets Panel** ya Google AI Studio upande wa kulia chini ya skrini yako.\n" +
                    "2️⃣ Ongeza Secret mpya yenye jina la **GEMINI_API_KEY**.\n" +
                    "3️⃣ Nakili API Key yako kutoka Google AI Studio na uiweke kama thamani (Value) ya hiyo variable.\n" +
                    "4️⃣ Mara tu baada ya kuongeza, fungua upya app hii na utafurahia huduma zote za AI! 🚀"
            return StudyResponse(
                intent = AcademicIntent.GENERAL_ACADEMIC,
                textResponse = missingKeyMessage,
                isSuccess = false,
                errorMessage = "Missing or invalid Gemini API Key"
            )
        }

        // Perform Multi-Intent Parsing Logic
        val intent = AIIntentController.detectAcademicIntent(prompt)
        val systemInstruction = AIIntentController.getSystemInstructionForIntent(intent)

        return try {
            val rawResponse = GeminiClient.generateChat(
                prompt = prompt,
                model = "gemini-2.5-flash",
                history = history,
                systemInstruction = systemInstruction
            )
            
            StudyResponse(
                intent = intent,
                textResponse = rawResponse,
                isSuccess = true
            )
        } catch (e: Exception) {
            StudyResponse(
                intent = intent,
                textResponse = "Samahani, kulitokea hitilafu wakati wa kuwasiliana na Gemini API: ${e.localizedMessage}",
                isSuccess = false,
                errorMessage = e.message
            )
        }
    }
}

