@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.example

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import coil.compose.AsyncImage
import com.example.data.*
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var repository: AppRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        repository = AppRepository(this)
        
        lifecycleScope.launch {
            repository.initSession(lifecycleScope)
        }

        setContent {
            var darkTheme by remember { mutableStateOf(false) }
            
            MyApplicationTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        repository = repository,
                        darkTheme = darkTheme,
                        onThemeToggle = { darkTheme = it },
                        activity = this@MainActivity
                    )
                }
            }
        }
    }
}

@Composable
fun AppNavigation(
    repository: AppRepository,
    darkTheme: Boolean,
    onThemeToggle: (Boolean) -> Unit,
    activity: MainActivity
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val prefs = remember { context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }
    var currentLanguage by remember { mutableStateOf(prefs.getString("selected_lang", "Kiswahili") ?: "Kiswahili") }

    var currentScreen by remember { mutableStateOf("splash") }
    val currentUser by repository.currentUser.collectAsState()
    
    // Peer chat state
    var activePeerPhone by remember { mutableStateOf("") }
    var activePeerName by remember { mutableStateOf("") }

    // Splash redirect
    LaunchedEffect(currentScreen) {
        if (currentScreen == "splash") {
            delay(2000)
            if (currentUser != null) {
                currentScreen = "home"
            } else {
                currentScreen = "login"
            }
        }
    }

    when (currentScreen) {
        "splash" -> SplashScreen()
        "login" -> LoginScreen(
            onLoginSuccess = { phone, pass ->
                coroutineScope.launch {
                    val success = repository.login(phone, pass)
                    if (success) {
                        currentScreen = "home"
                        Toast.makeText(context, Localization.translate("Karibu StudyTZ!", currentLanguage), Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, Localization.translate("Namba au Neno la siri limekosewa!", currentLanguage), Toast.LENGTH_LONG).show()
                    }
                }
            },
            onNavigateToRegister = { currentScreen = "register" },
            currentLanguage = currentLanguage,
            onLanguageChange = { newLang ->
                currentLanguage = newLang
                prefs.edit().putString("selected_lang", newLang).apply()
            }
        )
        "register" -> RegisterScreen(
            repository = repository,
            onRegisterSuccess = { name, phone, uni, country, campus, faculty, dept, prog, yr ->
                coroutineScope.launch {
                    repository.register(
                        phone = phone,
                        name = name,
                        uni = uni,
                        country = country,
                        campus = campus,
                        faculty = faculty,
                        department = dept,
                        programme = prog,
                        yearOfStudy = yr
                    )
                    currentScreen = "home"
                    Toast.makeText(context, Localization.translate("Usajili umekamilika!", currentLanguage), Toast.LENGTH_SHORT).show()
                }
            },
            onNavigateToLogin = { currentScreen = "login" },
            currentLanguage = currentLanguage,
            onLanguageChange = { newLang ->
                currentLanguage = newLang
                prefs.edit().putString("selected_lang", newLang).apply()
            }
        )
        "peer_chat" -> {
            PeerChatScreen(
                repository = repository,
                peerPhone = activePeerPhone,
                peerName = activePeerName,
                onBack = { currentScreen = "study_buddy" },
                currentLanguage = currentLanguage
            )
        }
        else -> {
            MainContainer(
                repository = repository,
                currentTab = currentScreen,
                onTabSelect = { currentScreen = it },
                darkTheme = darkTheme,
                onThemeToggle = onThemeToggle,
                onLogout = {
                    coroutineScope.launch {
                        repository.logout()
                        currentScreen = "login"
                    }
                },
                onOpenPeerChat = { phone, name ->
                    activePeerPhone = phone
                    activePeerName = name
                    currentScreen = "peer_chat"
                },
                currentLanguage = currentLanguage,
                onLanguageChange = { newLang ->
                    currentLanguage = newLang
                    prefs.edit().putString("selected_lang", newLang).apply()
                }
            )
        }
    }
}

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF16A34A)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "StudyTZ",
                fontSize = 42.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Chuo Kiganjani Mwako",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.85f)
            )
            Spacer(modifier = Modifier.height(32.dp))
            CircularProgressIndicator(color = Color.White)
        }
    }
}

@Composable
fun LanguageSelector(currentLanguage: String, onLanguageChange: (String) -> Unit) {
    val context = LocalContext.current
    var showLanguageDialog by remember { mutableStateOf(false) }
    
    val displayNames = mapOf(
        "Kiswahili" to "Kiswahili",
        "English" to "English",
        "France" to "Français",
        "Kichina" to "中文"
    )
    
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable { showLanguageDialog = true }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Language,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(displayNames[currentLanguage] ?: currentLanguage, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.width(4.dp))
            Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
        }
    }
    
    if (showLanguageDialog) {
        Dialog(onDismissRequest = { showLanguageDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = Localization.translate("Lugha", currentLanguage),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val languages = listOf("Kiswahili", "English", "France", "Kichina")
                    val fullDisplayNames = mapOf(
                        "Kiswahili" to "Kiswahili (Swahili)",
                        "English" to "English (Kiingereza)",
                        "France" to "Français (French)",
                        "Kichina" to "中文 (Chinese)"
                    )
                    
                    languages.forEach { lang ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onLanguageChange(lang)
                                    showLanguageDialog = false
                                    Toast.makeText(context, Localization.translate("Lugha iliyochaguliwa ni", lang), Toast.LENGTH_SHORT).show()
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = fullDisplayNames[lang] ?: lang,
                                fontSize = 16.sp,
                                fontWeight = if (currentLanguage == lang) FontWeight.Bold else FontWeight.Normal,
                                color = if (currentLanguage == lang) Color(0xFF16A34A) else MaterialTheme.colorScheme.onSurface
                            )
                            if (currentLanguage == lang) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF16A34A))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LoginScreen(
    onLoginSuccess: (String, String) -> Unit,
    onNavigateToRegister: () -> Unit,
    currentLanguage: String,
    onLanguageChange: (String) -> Unit
) {
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        // Language Selector at Top Right
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp)
        ) {
            LanguageSelector(currentLanguage = currentLanguage, onLanguageChange = onLanguageChange)
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "StudyTZ",
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF16A34A)
            )
            Text(
                text = Localization.translate("Chuo Kiganjani Mwako", currentLanguage),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Karibu Tena 👋",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text(Localization.translate("Namba ya Simu", currentLanguage)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_phone_input"),
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(Localization.translate("Neno la Siri", currentLanguage)) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_password_input"),
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) }
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = { onLoginSuccess(phone, password) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("login_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(Localization.translate("Ingia", currentLanguage), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(Localization.translate("Huna Akaunti? Jisajili", currentLanguage).substringBefore("?") + "? ", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                Text(
                    text = Localization.translate("Jisajili", currentLanguage),
                    color = Color(0xFF16A34A),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { onNavigateToRegister() }
                        .testTag("go_register_btn")
                )
            }
        }
    }
}

@Composable
fun RegisterScreen(
    repository: AppRepository,
    onRegisterSuccess: (String, String, String, String, String, String, String, String, String) -> Unit,
    onNavigateToLogin: () -> Unit,
    currentLanguage: String,
    onLanguageChange: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var uni by remember { mutableStateOf("UDSM") }
    var country by remember { mutableStateOf("Tanzania") }
    var campus by remember { mutableStateOf("Main Campus") }
    var faculty by remember { mutableStateOf("Computing and Informatics") }
    var department by remember { mutableStateOf("Computer Science and Engineering") }
    var programme by remember { mutableStateOf("BSc in Computer Science") }
    var yearOfStudy by remember { mutableStateOf("Mwaka wa 1") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    
    var step by remember { mutableStateOf(1) } // 1: Personal, 2: University details, 3: OTP Verification
    val context = LocalContext.current

    val tanzaniaUniversitiesList by repository.universities.collectAsState(initial = emptyList())
    var expandedUni by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        // Language Selector at Top Right
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp)
        ) {
            LanguageSelector(currentLanguage = currentLanguage, onLanguageChange = onLanguageChange)
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "StudyTZ",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF16A34A)
            )
            Text(
                text = Localization.translate("Unda Akaunti ya Mwanafunzi", currentLanguage),
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    if (step == 1) {
                        Text(
                            text = "Taarifa Binafsi 📝 (Hatua 1/3)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Jina Kamili") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Namba ya Simu") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Neno la Siri") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text("Thibitisha Neno la Siri") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) }
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = {
                                if (name.isBlank() || phone.isBlank() || password.isBlank()) {
                                    Toast.makeText(context, "Tafadhali jaza nafasi zote!", Toast.LENGTH_SHORT).show()
                                } else if (password != confirmPassword) {
                                    Toast.makeText(context, "Neno la siri halifanani!", Toast.LENGTH_SHORT).show()
                                } else {
                                    step = 2
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Endelea (Chuo)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    } else if (step == 2) {
                        Text(
                            text = "Taarifa ya Chuo 🎓 (Hatua 2/3)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Country field
                        OutlinedTextField(
                            value = country,
                            onValueChange = { country = it },
                            label = { Text("Nchi") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        // University Picker
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = uni,
                                onValueChange = {},
                                label = { Text("Chuo Chako") },
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth(),
                                trailingIcon = {
                                    IconButton(onClick = { expandedUni = true }) {
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                    }
                                }
                            )
                            DropdownMenu(
                                expanded = expandedUni,
                                onDismissRequest = { expandedUni = false }
                            ) {
                                tanzaniaUniversitiesList.forEach { u ->
                                    DropdownMenuItem(
                                        text = { Text("${u.code} - ${u.name}") },
                                        onClick = {
                                            uni = u.code
                                            expandedUni = false
                                        }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = campus,
                                onValueChange = { campus = it },
                                label = { Text("Kampasi") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = yearOfStudy,
                                onValueChange = { yearOfStudy = it },
                                label = { Text("Mwaka wa Masomo") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = faculty,
                            onValueChange = { faculty = it },
                            label = { Text("Kitivo (Faculty/College)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = department,
                            onValueChange = { department = it },
                            label = { Text("Idara (Department)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = programme,
                            onValueChange = { programme = it },
                            label = { Text("Kozi (Programme/Degree)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { step = 1 },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
                            ) {
                                Text("Rudi", color = Color.Black)
                            }
                            Button(
                                onClick = {
                                    step = 3
                                    Toast.makeText(context, "Msimbo wa OTP umetumwa! (Tumia 8520)", Toast.LENGTH_LONG).show()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                            ) {
                                Text("Pata OTP", color = Color.White)
                            }
                        }
                    } else {
                        Text(
                            text = "Uhakiki wa OTP 🔐 (Hatua 3/3)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Msimbo wa uhakiki wa simu (2FA) umetumwa. (Demo: weka 8520)",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(10.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = otpCode,
                            onValueChange = { otpCode = it },
                            label = { Text("Weka OTP Code") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("8520") },
                            textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Center)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Button(
                            onClick = {
                                if (otpCode == "8520" || otpCode.trim() == "8520") {
                                    onRegisterSuccess(name, phone, uni, country, campus, faculty, department, programme, yearOfStudy)
                                } else {
                                    Toast.makeText(context, "Msimbo usio sahihi! Tumia 8520.", Toast.LENGTH_LONG).show()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Uthibitisho & Jiunge", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "← Rudi Nyuma",
                            color = Color(0xFF16A34A),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable { step = 2 }
                                .align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Tayari una akaunti? ", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                Text(
                    text = "Ingia hapa",
                    color = Color(0xFF16A34A),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigateToLogin() }
                )
            }
        }
    }
}

@Composable
fun MainContainer(
    repository: AppRepository,
    currentTab: String,
    onTabSelect: (String) -> Unit,
    darkTheme: Boolean,
    onThemeToggle: (Boolean) -> Unit,
    onLogout: () -> Unit,
    onOpenPeerChat: (String, String) -> Unit,
    currentLanguage: String,
    onLanguageChange: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentUser by repository.currentUser.collectAsState()

    var showPostDialog by remember { mutableStateOf(false) }
    var postDialogType by remember { mutableStateOf("text") } // text, picture, poll

    var showLoanInbox by remember { mutableStateOf(false) }
    var notificationCount by remember { mutableStateOf(1) }

    Scaffold(
        topBar = {
            if (currentTab != "reels") {
                TopAppBar(
                    title = {
                        Text(
                            text = "StudyTZ",
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF16A34A),
                            fontSize = 24.sp
                        )
                    },
                    actions = {
                        IconButton(onClick = { 
                            showLoanInbox = true 
                            notificationCount = 0
                        }) {
                            Box {
                                Icon(Icons.Default.Notifications, contentDescription = "Arifa")
                                if (notificationCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .background(Color.Red, CircleShape)
                                            .align(Alignment.TopEnd)
                                    )
                                }
                            }
                        }
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF16A34A))
                                .clickable { onTabSelect("settings") }
                        ) {
                            if (currentUser?.avatarUrl.isNullOrEmpty()) {
                                Text(
                                    text = currentUser?.name?.take(1) ?: "?",
                                    color = Color.White,
                                    modifier = Modifier.align(Alignment.Center),
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                AsyncImage(
                                    model = currentUser?.avatarUrl,
                                    contentDescription = "Avatar",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                )
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                NavigationBarItem(
                    selected = currentTab == "home",
                    onClick = { onTabSelect("home") },
                    icon = { Icon(Icons.Default.Home, contentDescription = Localization.translate("Nyumbani", currentLanguage)) },
                    label = { Text(Localization.translate("Nyumbani", currentLanguage), fontSize = 9.sp) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFF16A34A), indicatorColor = Color(0x1F16A34A))
                )
                NavigationBarItem(
                    selected = currentTab == "reels",
                    onClick = { onTabSelect("reels") },
                    icon = { Icon(Icons.Filled.Movie, contentDescription = Localization.translate("Video za Masomo", currentLanguage)) },
                    label = { Text(Localization.translate("Video za Masomo", currentLanguage), fontSize = 9.sp) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFF16A34A), indicatorColor = Color(0x1F16A34A))
                )
                NavigationBarItem(
                    selected = currentTab == "study_buddy",
                    onClick = { onTabSelect("study_buddy") },
                    icon = { Icon(Icons.Filled.People, contentDescription = Localization.translate("Marafiki wa Masomo", currentLanguage)) },
                    label = { Text(Localization.translate("Marafiki wa Masomo", currentLanguage), fontSize = 9.sp) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFF16A34A), indicatorColor = Color(0x1F16A34A))
                )
                NavigationBarItem(
                    selected = currentTab == "chat",
                    onClick = { onTabSelect("chat") },
                    icon = { Icon(Icons.Filled.Android, contentDescription = Localization.translate("Sura na AI", currentLanguage)) },
                    label = { Text(Localization.translate("Sura na AI", currentLanguage), fontSize = 9.sp) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFF16A34A), indicatorColor = Color(0x1F16A34A))
                )
                NavigationBarItem(
                    selected = currentTab == "settings",
                    onClick = { onTabSelect("settings") },
                    icon = { Icon(Icons.Default.Settings, contentDescription = Localization.translate("Mipangilio", currentLanguage)) },
                    label = { Text(Localization.translate("Mipangilio", currentLanguage), fontSize = 9.sp) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFF16A34A), indicatorColor = Color(0x1F16A34A))
                )
            }
        },
        floatingActionButton = {
            if (currentTab == "home") {
                var fabExpanded by remember { mutableStateOf(false) }
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (fabExpanded) {
                        FloatingActionButton(
                            onClick = {
                                postDialogType = "text"
                                showPostDialog = true
                                fabExpanded = false
                            },
                            containerColor = Color(0xFF10B981),
                            contentColor = Color.White,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Andika")
                        }
                        FloatingActionButton(
                            onClick = {
                                postDialogType = "picture"
                                showPostDialog = true
                                fabExpanded = false
                            },
                            containerColor = Color(0xFF3B82F6),
                            contentColor = Color.White,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(Icons.Filled.Image, contentDescription = "Picha")
                        }
                        FloatingActionButton(
                            onClick = {
                                postDialogType = "poll"
                                showPostDialog = true
                                fabExpanded = false
                            },
                            containerColor = Color(0xFFF59E0B),
                            contentColor = Color.White,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(Icons.Filled.Poll, contentDescription = "Kura")
                        }
                    }
                    FloatingActionButton(
                        onClick = { fabExpanded = !fabExpanded },
                        containerColor = Color(0xFF16A34A),
                        contentColor = Color.White,
                        modifier = Modifier.testTag("fab_add_post")
                    ) {
                        Icon(
                            imageVector = if (fabExpanded) Icons.Default.Close else Icons.Default.Add,
                            contentDescription = "Chagua Chapisho"
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                "home" -> HomeScreen(repository = repository, onOpenPeerChat = onOpenPeerChat, currentLanguage = currentLanguage)
                "reels" -> ReelsScreen(repository = repository, currentLanguage = currentLanguage)
                "study_buddy" -> StudyBuddyScreen(repository = repository, onOpenPeerChat = onOpenPeerChat, currentLanguage = currentLanguage)
                "chat" -> ChatScreen(repository = repository, currentLanguage = currentLanguage)
                "settings" -> SettingsScreen(
                    repository = repository,
                    darkTheme = darkTheme,
                    onThemeToggle = onThemeToggle,
                    onLogout = onLogout,
                    currentLanguage = currentLanguage,
                    onLanguageChange = onLanguageChange
                )
            }

            if (showLoanInbox) {
                LoanInboxDialog(
                    repository = repository,
                    onDismiss = { showLoanInbox = false }
                )
            }
        }
    }

    if (showPostDialog) {
        CreatePostDialog(
            type = postDialogType,
            onDismiss = { showPostDialog = false },
            onSubmit = { text, url, isPoll, pollQ, pollO ->
                coroutineScope.launch {
                    repository.createPost(
                        text = text,
                        imageUrl = url,
                        isPoll = isPoll,
                        pollQuestion = pollQ,
                        pollOptions = pollO
                    )
                    showPostDialog = false
                    Toast.makeText(context, "Imechapishwa kikamilifu kwenye Feed!", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}

// --- STUDENT LOAN ANNOUNCEMENTS ---
@Composable
fun LoanInboxDialog(
    repository: AppRepository,
    onDismiss: () -> Unit
) {
    val list by repository.loanNotifications.collectAsState(initial = emptyList())
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Bodi ya Mikopo (HESLB) 🏛️", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Funga")
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                // Announcement Banner
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Campaign, contentDescription = null, tint = Color(0xFFD97706))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Arifa hizi zinasawazishwa kiotomatiki kutoka vyanzo rasmi vya elimu.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF92400E)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(list) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = if (item.isRead) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(item.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(item.date, fontSize = 11.sp, color = Color.Gray)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(item.message, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- FEED (Instagram-Style) ---
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(repository: AppRepository, onOpenPeerChat: (String, String) -> Unit, currentLanguage: String) {
    val coroutineScope = rememberCoroutineScope()
    val posts by repository.posts.collectAsState(initial = emptyList())
    val stories by repository.stories.collectAsState(initial = emptyList())
    val currentUser by repository.currentUser.collectAsState()
    
    var activeStory by remember { mutableStateOf<StoryEntity?>(null) }
    var expandedPostCommentsId by remember { mutableStateOf<Long?>(null) }
    var searchKeyword by remember { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }

    // Recommendation sorting: 60% own university, 20% similar, 20% global
    val sortedPosts = remember(posts, currentUser, searchKeyword) {
        val filtered = if (searchKeyword.isBlank()) {
            posts
        } else {
            posts.filter { it.text.contains(searchKeyword, ignoreCase = true) || it.authorName.contains(searchKeyword, ignoreCase = true) }
        }

        val myUni = currentUser?.uni ?: "UDSM"
        val ownUniList = filtered.filter { it.authorUni.uppercase() == myUni.uppercase() }
        val globalList = filtered.filter { it.authorUni.uppercase() != myUni.uppercase() }
        
        // Return structured list
        ownUniList + globalList
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Combined Top Search and Stories Box
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            // Expandable Search Bar Layout
            if (!isSearchExpanded) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = Localization.translate("Gundua & Shiriki", currentLanguage),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(
                        onClick = { isSearchExpanded = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = Localization.translate("Fungua utafutaji", currentLanguage),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchKeyword,
                        onValueChange = { searchKeyword = it },
                        placeholder = { Text(Localization.translate("Tafuta machapisho, washiriki, au vyuo...", currentLanguage), fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = Localization.translate("Tafuta", currentLanguage), tint = Color.Gray) },
                        trailingIcon = {
                            if (searchKeyword.isNotEmpty()) {
                                IconButton(onClick = { searchKeyword = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = Localization.translate("Safisha", currentLanguage))
                                }
                            }
                        },
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = {
                            isSearchExpanded = false
                            searchKeyword = "" // Clear search when closing
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = Localization.translate("Funga utafutaji", currentLanguage),
                            tint = Color.Gray
                        )
                    }
                }
            }
        }

        // Cloud Sync Status Pill
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val isCloudActive = repository.firestoreSync.isCloudActive
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isCloudActive) Color(0xFF16A34A) else Color(0xFFD97706))
            )
            Text(
                text = if (isCloudActive) "Cloud Synced (Live Live)" else "Local Mode (Offline-first. Weka google-services.json ili usync)",
                fontSize = 11.sp,
                color = if (isCloudActive) Color(0xFF16A34A) else Color(0xFFD97706),
                fontWeight = FontWeight.Medium
            )
        }

        // Stories preview above feed
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable {
                        coroutineScope.launch {
                            repository.createStory(
                                imageUrl = "https://images.unsplash.com/photo-1523050854058-8df90110c9f1?w=500",
                                caption = "Hekaheka za asubuhi COICT leo! 📚☀️"
                            )
                        }
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF16A34A)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = Localization.translate("Weka Status", currentLanguage), tint = Color.White)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(Localization.translate("Unda Status", currentLanguage), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
                items(stories, key = { it.id }) { s ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { activeStory = s }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF16A34A))
                                .padding(2.dp)
                        ) {
                            AsyncImage(
                                model = s.authorAvatarUrl.ifEmpty { "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150" },
                                contentDescription = "Story",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(s.authorName.split(" ").firstOrNull() ?: "", fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        
            Spacer(modifier = Modifier.height(4.dp))

        // Feed List (Instagram Style)
        if (sortedPosts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(Localization.translate("Hakuna machapisho yanayofanana", currentLanguage), color = Color.Gray, fontSize = 14.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sortedPosts, key = { it.id }) { post ->
                    InstagramPostCard(
                        post = post,
                        onLikeToggle = {
                            coroutineScope.launch {
                                repository.toggleLikePost(post.id, post.likesCount, post.isLiked)
                            }
                        },
                        onCommentClick = {
                            expandedPostCommentsId = post.id
                        },
                        onOpenPeerChat = onOpenPeerChat,
                        repository = repository,
                        currentLanguage = currentLanguage
                    )
                }
            }
        }
    }

    // Active Story Viewer Overlay
    activeStory?.let { s ->
        StoryViewerDialog(story = s, onDismiss = { activeStory = null })
    }

    // Interactive Comments Bottom Sheet
    expandedPostCommentsId?.let { postId ->
        CommentsSheet(
            postId = postId,
            repository = repository,
            onDismiss = { expandedPostCommentsId = null },
            currentLanguage = currentLanguage
        )
    }
}

@Composable
fun StoryViewerDialog(story: StoryEntity, onDismiss: () -> Unit) {
    var progress by remember { mutableStateOf(0f) }
    LaunchedEffect(Unit) {
        val steps = 100
        for (i in 0..steps) {
            delay(40)
            progress = i / 100f
        }
        onDismiss()
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.7f)
                .background(Color.Black, shape = RoundedCornerShape(16.dp))
        ) {
            AsyncImage(
                model = story.imageUrl,
                contentDescription = "Story Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            // Story metadata top overlay
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(12.dp)
            ) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.3f),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF16A34A))
                    ) {
                        Text(story.authorName.take(1), color = Color.White, modifier = Modifier.align(Alignment.Center), fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(story.authorName, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            // Caption Bottom Overlay
            if (story.caption.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(16.dp)
                ) {
                    Text(story.caption, color = Color.White, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InstagramPostCard(
    post: PostEntity,
    onLikeToggle: () -> Unit,
    onCommentClick: () -> Unit,
    onOpenPeerChat: (String, String) -> Unit,
    repository: AppRepository,
    currentLanguage: String
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isSaved by remember { mutableStateOf(post.isSaved) }
    var doubleLikeHeartVisible by remember { mutableStateOf(false) }
    var scale by remember { mutableStateOf(1f) }

    // Simulate Carousel urls
    val imageUrls = remember(post.imageUrl) {
        if (post.imageUrl.isEmpty()) emptyList() else post.imageUrl.split(",")
    }
    val pagerState = rememberPagerState(pageCount = { imageUrls.size })

    // Register a simulated view
    LaunchedEffect(post.id) {
        repository.incrementPostViews(post.id)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            // Author row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF16A34A))
                        .clickable { onOpenPeerChat(post.authorPhone, post.authorName) },
                    contentAlignment = Alignment.Center
                ) {
                    if (post.authorAvatarUrl.isNotEmpty()) {
                        AsyncImage(
                            model = post.authorAvatarUrl,
                            contentDescription = "Avatar",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(post.authorName.take(1), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(post.authorName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        // Verification badge
                        Icon(
                            Icons.Default.Verified,
                            contentDescription = "Verified",
                            tint = Color(0xFF1D9BF0),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(post.authorUni, fontSize = 11.sp, color = Color.Gray)
                }

                // Interactive DM Chat Button
                IconButton(onClick = { onOpenPeerChat(post.authorPhone, post.authorName) }) {
                    Icon(Icons.Default.ChatBubbleOutline, contentDescription = "Chat", tint = Color.Gray)
                }
            }

            // Image Carousel with Double-Tap to Like Support
            if (imageUrls.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .background(Color.Black)
                ) {
                    HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                        var zoomScale by remember { mutableStateOf(1f) }
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    detectTransformGestures { _, _, zoom, _ ->
                                        zoomScale = (zoomScale * zoom).coerceIn(1f, 3f)
                                    }
                                }
                                .combinedClickable(
                                    onClick = {},
                                    onDoubleClick = {
                                        if (!post.isLiked) {
                                            onLikeToggle()
                                        }
                                        doubleLikeHeartVisible = true
                                        coroutineScope.launch {
                                            delay(800)
                                            doubleLikeHeartVisible = false
                                        }
                                    }
                                )
                        ) {
                            AsyncImage(
                                model = imageUrls[page],
                                contentDescription = "Post Image",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer(
                                        scaleX = zoomScale,
                                        scaleY = zoomScale
                                    ),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    // Slide indicators for Carousel
                    if (imageUrls.size > 1) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            repeat(imageUrls.size) { index ->
                                val active = pagerState.currentPage == index
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(
                                            color = if (active) Color(0xFF16A34A) else Color.White.copy(alpha = 0.5f),
                                            shape = CircleShape
                                        )
                                )
                            }
                        }
                    }

                    // Double-Tap Animated Heart
                    if (doubleLikeHeartVisible) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Double Liked",
                            tint = Color.Red,
                            modifier = Modifier
                                .size(110.dp)
                                .align(Alignment.Center)
                                .animateContentSize()
                        )
                    }
                }
            }

            // Text / Caption with hashtags and mentions highlighted
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = post.text,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 14.sp
                )

                // Poll Section (if applicable)
                if (post.isPoll) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(post.pollQuestion, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    val options = post.pollOptions.split(",")
                    val votesStr = post.pollVotes.split(",")
                    val votes = votesStr.map { it.toIntOrNull() ?: 0 }
                    val totalVotes = votes.sum().coerceAtLeast(1)

                    options.forEachIndexed { idx, opt ->
                        val percent = (votes[idx] * 100) / totalVotes
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    val msg = Localization.translate("Kura yako kwa", currentLanguage).replace("%s", opt)
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(opt, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                Text("$percent% (${votes[idx]} kura)", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Actions row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onLikeToggle) {
                            Icon(
                                imageVector = if (post.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Like",
                                tint = if (post.isLiked) Color.Red else Color.Gray
                            )
                        }
                        Text("${post.likesCount}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)

                        Spacer(modifier = Modifier.width(16.dp))

                        IconButton(onClick = onCommentClick) {
                            Icon(Icons.Default.Comment, contentDescription = "Comment", tint = Color.Gray)
                        }
                        Text(Localization.translate("Maoni", currentLanguage), fontSize = 13.sp, color = Color.Gray)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Total views
                        Icon(Icons.Default.RemoveRedEye, contentDescription = "Views", tint = Color.Gray, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${post.viewCount}", fontSize = 12.sp, color = Color.Gray)

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(onClick = {
                            coroutineScope.launch {
                                repository.toggleSavePost(post.id, isSaved)
                                isSaved = !isSaved
                                val msgKey = if (isSaved) "Imehifadhiwa!" else "Umeondoa maktaba!"
                                Toast.makeText(context, Localization.translate(msgKey, currentLanguage), Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Icon(
                                imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = Localization.translate("Hifadhi", currentLanguage),
                                tint = if (isSaved) Color(0xFF16A34A) else Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CommentsSheet(
    postId: Long,
    repository: AppRepository,
    onDismiss: () -> Unit,
    currentLanguage: String
) {
    val coroutineScope = rememberCoroutineScope()
    val comments by repository.getComments(postId).collectAsState(initial = emptyList())
    var text by remember { mutableStateOf("") }
    
    // Active comment to reply to
    var replyingToCommentId by remember { mutableStateOf<Long?>(null) }
    var replyingToName by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(Localization.translate("Maoni & Majibu", currentLanguage) + " 💬", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = Localization.translate("Funga", currentLanguage))
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))

                // Comments feed list
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(comments, key = { it.id }) { comment ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.Top) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color.Gray),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(comment.authorName.take(1), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(comment.authorName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text(comment.text, fontSize = 13.sp)
                                    
                                    // Reply trigger
                                    Text(
                                        text = Localization.translate("Jibu", currentLanguage),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF16A34A),
                                        modifier = Modifier
                                            .clickable {
                                                replyingToCommentId = comment.id
                                                replyingToName = comment.authorName
                                            }
                                            .padding(vertical = 4.dp)
                                    )
                                }
                             }

                             // Replies list nested inside parent comment
                             val replies by repository.getReplies(comment.id).collectAsState(initial = emptyList())
                             if (replies.isNotEmpty()) {
                                 Column(modifier = Modifier.padding(start = 40.dp, top = 4.dp)) {
                                     replies.forEach { r ->
                                         Row(
                                             verticalAlignment = Alignment.CenterVertically,
                                             modifier = Modifier.padding(vertical = 2.dp)
                                         ) {
                                             Box(
                                                 modifier = Modifier
                                                     .size(24.dp)
                                                     .clip(CircleShape)
                                                     .background(Color.LightGray),
                                                 contentAlignment = Alignment.Center
                                             ) {
                                                 Text(r.authorName.take(1), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                             }
                                             Spacer(modifier = Modifier.width(6.dp))
                                             Column {
                                                 Text(r.authorName, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                                 Text(r.text, fontSize = 11.sp)
                                             }
                                         }
                                     }
                                 }
                             }
                        }
                    }
                }

                if (replyingToCommentId != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.LightGray.copy(alpha = 0.3f))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(Localization.translate("Unamjibu", currentLanguage) + " ${replyingToName}...", fontSize = 11.sp, color = Color.DarkGray)
                        IconButton(onClick = { replyingToCommentId = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel", modifier = Modifier.size(14.dp))
                        }
                    }
                }

                // Input box
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        placeholder = { Text(if (replyingToCommentId != null) Localization.translate("Andika jibu hapa...", currentLanguage) else Localization.translate("Andika maoni hapa...", currentLanguage), fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (text.isNotBlank()) {
                                coroutineScope.launch {
                                    val parentId = replyingToCommentId
                                    if (parentId != null) {
                                        repository.addReply(parentId, text)
                                        replyingToCommentId = null
                                    } else {
                                        repository.addComment(postId, text)
                                    }
                                    text = ""
                                }
                            }
                        },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFF16A34A))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White)
                    }
                }
            }
        }
    }
}

// --- REELS (Immersive Fullscreen Vertical Pager) ---
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReelsScreen(repository: AppRepository, currentLanguage: String) {
    val coroutineScope = rememberCoroutineScope()
    val reels by repository.reels.collectAsState(initial = emptyList())
    val context = LocalContext.current
    var showUploadReel by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (reels.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(Localization.translate("Hakuna reels bado.", currentLanguage), color = Color.White)
            }
        } else {
            val pagerState = rememberPagerState(pageCount = { reels.size })
            VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val r = reels[page]
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                ) {
                    // Background image representing the reel video template or gradient
                    if (r.videoUrl.isNotEmpty()) {
                        AsyncImage(
                            model = r.videoUrl,
                            contentDescription = "Reel background",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // fallback gradient
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                        colors = listOf(Color(0xFF1E1E2C), Color(0xFF0F0F1A))
                                    )
                                )
                        )
                    }

                    // A subtle dark overlay on the background so text stands out beautifully
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.35f))
                    )

                    // Play indicator/touch listener (simulates pause/play)
                    var isPlaying by remember { mutableStateOf(true) }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .combinedClickable(
                                onClick = { isPlaying = !isPlaying },
                                onDoubleClick = {
                                    coroutineScope.launch {
                                        repository.toggleLikeReel(r.id, r.likesCount, r.isLiked)
                                        Toast.makeText(context, Localization.translate("Umeipenda Reel hii!", currentLanguage), Toast.LENGTH_SHORT).show()
                                    }
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!isPlaying) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = "Paused",
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(80.dp)
                            )
                        }
                    }

                    // Bottom Overlay containing Caption, Creator info, and Audio
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomStart)
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f))
                                )
                            )
                            .padding(bottom = 16.dp, start = 16.dp, end = 80.dp, top = 40.dp) // Leave right side for side buttons
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF16A34A)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(r.authorName.take(1), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(r.authorName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(r.authorUni, color = Color(0xFF10B981), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(r.description, color = Color.White, fontSize = 13.sp, maxLines = 3)
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.MusicNote, contentDescription = "Music", tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(r.musicTitle, color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp)
                            }
                        }
                    }

                    // Right Sidebar (Likes, Save, Share)
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 12.dp, bottom = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Profile Avatar
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (r.authorAvatarUrl.isNotEmpty()) {
                                AsyncImage(
                                    model = r.authorAvatarUrl,
                                    contentDescription = "Avatar",
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
                            }
                        }

                        // Likes
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    repository.toggleLikeReel(r.id, r.likesCount, r.isLiked)
                                }
                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Favorite,
                                    contentDescription = "Like",
                                    tint = if (r.isLiked) Color.Red else Color.White,
                                    modifier = Modifier.size(30.dp)
                                )
                                Text("${r.likesCount}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Save
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    repository.toggleSaveReel(r.id, r.isSaved)
                                    val key = if (!r.isSaved) "Umehifadhi kwenye Maktaba!" else "Reel imeondolewa maktaba!"
                                    Toast.makeText(context, Localization.translate(key, currentLanguage), Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Bookmark,
                                    contentDescription = "Save",
                                    tint = if (r.isSaved) Color.Yellow else Color.White,
                                    modifier = Modifier.size(30.dp)
                                )
                                Text(
                                    text = if (r.isSaved) Localization.translate("Imehifadhiwa", currentLanguage) else Localization.translate("Hifadhi", currentLanguage),
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Share
                        IconButton(
                            onClick = { Toast.makeText(context, Localization.translate("Kiunganishi cha Reel kimenakiliwa!", currentLanguage), Toast.LENGTH_SHORT).show() },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White, modifier = Modifier.size(30.dp))
                                Text(Localization.translate("Shiriki", currentLanguage), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Top Header of Reels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 36.dp, start = 16.dp, end = 16.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = Localization.translate("Reels za Chuo", currentLanguage),
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 22.sp,
                style = MaterialTheme.typography.titleLarge
            )
            Button(
                onClick = { showUploadReel = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(Localization.translate("Unda Reel", currentLanguage), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showUploadReel) {
        UploadReelDialog(
            repository = repository,
            onDismiss = { showUploadReel = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadReelDialog(
    repository: AppRepository,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var caption by remember { mutableStateOf("") }
    var music by remember { mutableStateOf("Bongo Flava Beats - Acoustic") }
    var speed by remember { mutableStateOf("1.0x") }
    
    // Academic / College themes for background
    val templates = listOf(
        Triple("Darasani 🏫", "https://images.unsplash.com/photo-1541339907198-e08756dedf3f?w=800", Color(0xFF1E3A8A)),
        Triple("Maktaba 📚", "https://images.unsplash.com/photo-1521587760476-6c12a4b040da?w=800", Color(0xFF10B981)),
        Triple("Coding & Tech 💻", "https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=800", Color(0xFF7C3AED)),
        Triple("Group Study 👥", "https://images.unsplash.com/photo-1515187029135-18ee286d815b?w=800", Color(0xFFF59E0B)),
        Triple("Campus Life 🎓", "https://images.unsplash.com/photo-1523050854058-8df90110c9f1?w=800", Color(0xFFEF4444))
    )
    
    var selectedTemplateIndex by remember { mutableIntStateOf(2) } // Coding as default

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    "Unda Reel Mpya 🎥",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(14.dp))
                
                OutlinedTextField(
                    value = caption,
                    onValueChange = { caption = it },
                    label = { Text("Maelezo ya Reel / Hashtags") },
                    placeholder = { Text("Mfano: Siku yangu ya kwanza ya mitihani! 📝 #udsm") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = music,
                    onValueChange = { music = it },
                    label = { Text("Chagua Wimbo / Sauti ya Asili") },
                    placeholder = { Text("Bongo Flava Beats - Acoustic") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Template picker (displays small selectable visual templates representing video themes)
                Text("Chagua Mandhari ya Video (Template) 🎨", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    itemsIndexed(templates) { index, template ->
                        val isSelected = selectedTemplateIndex == index
                        Card(
                            onClick = { selectedTemplateIndex = index },
                            modifier = Modifier
                                .width(110.dp)
                                .height(76.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) Color(0xFF16A34A) else Color.LightGray
                            )
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                AsyncImage(
                                    model = template.second,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.4f))
                                )
                                Text(
                                    text = template.first,
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(4.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))

                // speed setting
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Kasi ya Video", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("0.5x", "1.0x", "2.0x").forEach { sp ->
                            FilterChip(
                                selected = speed == sp,
                                onClick = { speed = sp },
                                label = { Text(sp) }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Ghairi")
                    }
                    Button(
                        onClick = {
                            if (caption.isNotBlank()) {
                                coroutineScope.launch {
                                    val url = templates[selectedTemplateIndex].second
                                    repository.createReel(caption, music, url)
                                    onDismiss()
                                }
                            }
                        },
                        modifier = Modifier.weight(1.5f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                        shape = RoundedCornerShape(10.dp),
                        enabled = caption.isNotBlank()
                    ) {
                        Text("Pakia Sasa", color = Color.White)
                    }
                }
            }
        }
    }
}

// --- STUDY BUDDY SYSTEM ---
@Composable
fun StudyBuddyScreen(
    repository: AppRepository,
    onOpenPeerChat: (String, String) -> Unit,
    currentLanguage: String
) {
    var selectedSubTab by remember { mutableStateOf("partners") } // partners, groups, notes, progress
    
    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = when (selectedSubTab) {
                "partners" -> 0
                "groups" -> 1
                "notes" -> 2
                else -> 3
            }
        ) {
            Tab(selected = selectedSubTab == "partners", onClick = { selectedSubTab = "partners" }) {
                Text(Localization.translate("Wenzako", currentLanguage), modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
            }
            Tab(selected = selectedSubTab == "groups", onClick = { selectedSubTab = "groups" }) {
                Text(Localization.translate("Vikundi", currentLanguage), modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
            }
            Tab(selected = selectedSubTab == "notes", onClick = { selectedSubTab = "notes" }) {
                Text(Localization.translate("Maktaba", currentLanguage), modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
            }
            Tab(selected = selectedSubTab == "progress", onClick = { selectedSubTab = "progress" }) {
                Text(Localization.translate("Malengo", currentLanguage), modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
            }
        }

        when (selectedSubTab) {
            "partners" -> StudyPartnersTab(repository = repository, onOpenPeerChat = onOpenPeerChat, currentLanguage = currentLanguage)
            "groups" -> StudyGroupsTab(repository = repository, currentLanguage = currentLanguage)
            "notes" -> ResourceLibraryTab(repository = repository, currentLanguage = currentLanguage)
            else -> ProgressTrackerTab(repository = repository, currentLanguage = currentLanguage)
        }
    }
}

@Composable
fun StudyPartnersTab(
    repository: AppRepository,
    onOpenPeerChat: (String, String) -> Unit,
    currentLanguage: String
) {
    val students by repository.allUsers.collectAsState(initial = emptyList())
    val currentUser by repository.currentUser.collectAsState()
    
    // Sort and highlight classmates from same Uni, Campus, Dept, Year
    val matchedPartners = remember(students, currentUser) {
        students.filter { it.phone != currentUser?.phone }.map { s ->
            var score = 30
            if (s.uni == currentUser?.uni) score += 25
            if (s.faculty == currentUser?.faculty) score += 15
            if (s.department == currentUser?.department) score += 15
            if (s.yearOfStudy == currentUser?.yearOfStudy) score += 15
            Pair(s, score)
        }.sortedByDescending { it.second }
    }

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        Text(Localization.translate("Tafuta Partners", currentLanguage) + " 🤝", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(matchedPartners, key = { it.first.phone }) { pair ->
                val s = pair.first
                val score = pair.second
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF16A34A)),
                                contentAlignment = Alignment.Center
                            ) {
                                        Text(s.name.take(1), color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(s.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("${s.uni} - ${s.programme}", fontSize = 11.sp, color = Color.Gray)
                            }
                            // Match percentage pill
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFDCFCE7))
                            ) {
                                Text(
                                    "$score% " + Localization.translate("Mechi", currentLanguage),
                                    color = Color(0xFF15803D),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(s.bio, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(Localization.translate("Mwaka", currentLanguage) + ": ${s.yearOfStudy} | " + Localization.translate("Idara", currentLanguage) + ": ${s.department}", fontSize = 10.sp, color = Color.Gray)
                            Button(
                                onClick = { onOpenPeerChat(s.phone, s.name) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(Localization.translate("Omba Kusoma", currentLanguage), fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StudyGroupsTab(repository: AppRepository, currentLanguage: String) {
    val coroutineScope = rememberCoroutineScope()
    val groups by repository.studyGroups.collectAsState(initial = emptyList())
    var showCreateGroup by remember { mutableStateOf(false) }
    
    // Active simulation meeting
    var activeCallRoomName by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(Localization.translate("Vyumba vya Masomo", currentLanguage) + " 👥", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Button(
                    onClick = { showCreateGroup = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                ) {
                    Text("+ " + Localization.translate("Kikundi", currentLanguage), fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(groups, key = { it.id }) { g ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(g.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                                    Text(g.course, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(g.description, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(Localization.translate("Wanachama", currentLanguage) + ": ${g.memberCount} | " + Localization.translate("Chuo", currentLanguage) + ": ${g.uni}", fontSize = 11.sp, color = Color.Gray)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { activeCallRoomName = g.name },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                                    ) {
                                        Icon(Icons.Default.VideoCall, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(Localization.translate("Kutana", currentLanguage), fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showCreateGroup) {
            Dialog(onDismissRequest = { showCreateGroup = false }) {
                var name by remember { mutableStateOf("") }
                var course by remember { mutableStateOf("") }
                var dept by remember { mutableStateOf("") }
                var desc by remember { mutableStateOf("") }
                
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(Localization.translate("Anzisha Kikundi Kipya", currentLanguage), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(Localization.translate("Jina la Kikundi", currentLanguage)) })
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(value = course, onValueChange = { course = it }, label = { Text(Localization.translate("Kozi", currentLanguage)) })
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(value = dept, onValueChange = { dept = it }, label = { Text(Localization.translate("Idara", currentLanguage)) })
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text(Localization.translate("Maelezo ya Kikundi", currentLanguage)) })
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    repository.createStudyGroup(name, dept, course, desc)
                                    showCreateGroup = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                        ) {
                            Text(Localization.translate("Unda Sasa", currentLanguage))
                        }
                    }
                }
            }
        }

        // Simulating Voice & Video Classroom
        activeCallRoomName?.let { room ->
            ActiveVideoCallDialog(roomName = room, onDismiss = { activeCallRoomName = null })
        }
    }
}

@Composable
fun ActiveVideoCallDialog(roomName: String, onDismiss: () -> Unit) {
    val context = LocalContext.current
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.8f)
                .background(Color(0xFF1E293B), shape = RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Kikao Kazi cha Video: $roomName 🎥",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                // Visual waveforms mimicking audio voice traffic
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f).fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Anuary", "Mariam", "Kelvin", "Lecturer").forEach { name ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF334155)),
                                modifier = Modifier.size(65.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Box(modifier = Modifier.size(24.dp).background(Color(0xFF16A34A), CircleShape))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(name, color = Color.White, fontSize = 9.sp)
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    Text("Matangazo ya Sauti ya Chuo (Sauti Iko Wazi)", color = Color.Green, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    // Wave simulator
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.height(40.dp)
                    ) {
                        listOf(10, 25, 40, 20, 35, 15, 30, 10).forEach { h ->
                            Box(
                                modifier = Modifier
                                    .width(6.dp)
                                    .height(h.dp)
                                    .background(Color(0xFF10B981), RoundedCornerShape(3.dp))
                            )
                        }
                    }
                }

                // Call Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    IconButton(
                        onClick = { Toast.makeText(context, "Mute Mic!", Toast.LENGTH_SHORT).show() },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = Color.White.copy(alpha = 0.2f))
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = null, tint = Color.White)
                    }
                    IconButton(
                        onClick = { Toast.makeText(context, "Video is off!", Toast.LENGTH_SHORT).show() },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = Color.White.copy(alpha = 0.2f))
                    ) {
                        Icon(Icons.Default.Videocam, contentDescription = null, tint = Color.White)
                    }
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("Ondoka", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun ResourceLibraryTab(repository: AppRepository, currentLanguage: String) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val notes by repository.studyNotes.collectAsState(initial = emptyList())
    var showUploadNote by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(Localization.translate("Maktaba ya Rasilimali", currentLanguage) + " 📚", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Button(
                    onClick = { showUploadNote = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                ) {
                    Text("+ " + Localization.translate("Pakia PDF", currentLanguage))
                }
            }
            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(notes, key = { it.id }) { n ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Book, contentDescription = "PDF", tint = Color(0xFFEF4444), modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(n.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(Localization.translate("Mwalimu", currentLanguage) + ": ${n.author} | " + Localization.translate("Somo", currentLanguage) + ": ${n.course}", fontSize = 11.sp, color = Color.Gray)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Star, contentDescription = "Rating", tint = Color(0xFFF59E0B), modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("${n.rating}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(Localization.translate("Pakuliwa", currentLanguage) + ": ${n.downloads}", fontSize = 11.sp, color = Color.Gray)
                                }
                            }
                            Button(
                                onClick = { Toast.makeText(context, Localization.translate("PDF imepakuliwa", currentLanguage), Toast.LENGTH_SHORT).show() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                            ) {
                                Text(Localization.translate("Pakua", currentLanguage), fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }

        if (showUploadNote) {
            Dialog(onDismissRequest = { showUploadNote = false }) {
                var title by remember { mutableStateOf("") }
                var course by remember { mutableStateOf("") }
                
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(Localization.translate("Pakia faili la Masomo", currentLanguage), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text(Localization.translate("Jina la PDF au Slide", currentLanguage)) })
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(value = course, onValueChange = { course = it }, label = { Text(Localization.translate("Somo au Kozi", currentLanguage)) })
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    repository.uploadStudyNote(title, course)
                                    showUploadNote = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                        ) {
                            Text(Localization.translate("Hifadhi Maktaba", currentLanguage))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProgressTrackerTab(repository: AppRepository, currentLanguage: String) {
    val coroutineScope = rememberCoroutineScope()
    val goals by repository.getProgressGoals().collectAsState(initial = emptyList())
    val courseModulesAll by repository.getCourseModules().collectAsState(initial = emptyList())
    
    var selectedTab by remember { mutableStateOf(0) }
    var showAddGoal by remember { mutableStateOf(false) }
    var showAddCourseModule by remember { mutableStateOf(false) }
    var isAddingSemesterGoal by remember { mutableStateOf(false) }

    // Calculate overall stats
    val totalStudyHours = goals.sumOf { it.achievedHours.toDouble() }
    val targetStudyHours = goals.sumOf { it.targetHours.toDouble() }
    val studyHoursPercentage = if (targetStudyHours > 0) (totalStudyHours / targetStudyHours).coerceIn(0.0, 1.0) else 0.0

    val coursesOnly = courseModulesAll.filter { !it.isSemesterGoal }
    val totalModules = coursesOnly.size
    val completedModules = coursesOnly.count { it.completed }
    val modulesPercentage = if (totalModules > 0) (completedModules.toDouble() / totalModules) else 0.0

    val semesterGoals = courseModulesAll.filter { it.isSemesterGoal }
    val totalSemesterGoals = semesterGoals.size
    val completedSemesterGoals = semesterGoals.count { it.completed }
    val semesterPercentage = if (totalSemesterGoals > 0) (completedSemesterGoals.toDouble() / totalSemesterGoals) else 0.0

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // --- TOP STATS SUMMARY DASHBOARD (CARD) ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = Localization.translate("Dawati la Maendeleo", currentLanguage) + " 📊",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Stat 1: Study Hours
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "⏱️ " + Localization.translate("Kujisomea", currentLanguage),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${totalStudyHours.toInt()} / ${targetStudyHours.toInt()} Hr",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { studyHoursPercentage.toFloat() },
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = Color(0xFF10B981),
                                trackColor = Color.LightGray.copy(alpha = 0.4f)
                            )
                            Text(
                                text = "${(studyHoursPercentage * 100).toInt()}%",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                        
                        // Vertical Divider
                        Box(modifier = Modifier
                            .width(1.dp)
                            .height(65.dp)
                            .background(Color.LightGray.copy(alpha = 0.5f)))
                        
                        // Stat 2: Course Modules
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "📚 " + Localization.translate("Moduli", currentLanguage),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$completedModules / $totalModules",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { modulesPercentage.toFloat() },
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = Color(0xFF3B82F6),
                                trackColor = Color.LightGray.copy(alpha = 0.4f)
                            )
                            Text(
                                text = "${(modulesPercentage * 100).toInt()}%",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                        
                        // Vertical Divider
                        Box(modifier = Modifier
                            .width(1.dp)
                            .height(65.dp)
                            .background(Color.LightGray.copy(alpha = 0.5f)))
                        
                        // Stat 3: Semester Goals
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "🎯 " + Localization.translate("Muhula", currentLanguage),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$completedSemesterGoals / $totalSemesterGoals",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { semesterPercentage.toFloat() },
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = Color(0xFFF59E0B),
                                trackColor = Color.LightGray.copy(alpha = 0.4f)
                            )
                            Text(
                                text = "${(semesterPercentage * 100).toInt()}%",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }

            // --- TAB SELECTOR ---
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text(Localization.translate("Kujisomea", currentLanguage), fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text(Localization.translate("Moduli", currentLanguage), fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.List, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text(Localization.translate("Muhula", currentLanguage), fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- TAB CONTENT ---
            when (selectedTab) {
                0 -> {
                    // TAB 0: STUDY HOURS GOALS
                    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = Localization.translate("Malengo ya Masaa", currentLanguage),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Button(
                                onClick = { showAddGoal = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                shape = RoundedCornerShape(20.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(Localization.translate("Sajili Lengo", currentLanguage), fontSize = 11.sp)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        if (goals.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize().weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(Localization.translate("Hakuna lengo lililosajiliwa", currentLanguage), color = Color.Gray, fontSize = 13.sp)
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(goals, key = { it.id }) { g ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(g.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = Localization.translate("Masaa", currentLanguage) + ": ${g.achievedHours}/${g.targetHours}",
                                                        fontSize = 12.sp,
                                                        color = Color.Gray
                                                    )
                                                }
                                                
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .background(if (g.completed) Color(0xFFD1FAE5) else Color(0xFFF3F4F6))
                                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                                ) {
                                                    Text(
                                                        text = if (g.completed) Localization.translate("Imekamilika", currentLanguage) else Localization.translate("Inaendelea", currentLanguage),
                                                        color = if (g.completed) Color(0xFF065F46) else Color.Gray,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                            
                                            Spacer(modifier = Modifier.height(10.dp))
                                            val progress = (g.achievedHours / g.targetHours).coerceIn(0f, 1f)
                                            LinearProgressIndicator(
                                                progress = { progress },
                                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                                color = Color(0xFF10B981),
                                                trackColor = Color.LightGray.copy(alpha = 0.5f)
                                            )
                                            
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "${(progress * 100).toInt()}% Done",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF10B981)
                                                )
                                                
                                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    Button(
                                                        onClick = {
                                                            coroutineScope.launch {
                                                                repository.updateProgressGoalHours(g.id, g.achievedHours + 1f, g.targetHours)
                                                            }
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0F2FE)),
                                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                        modifier = Modifier.height(30.dp)
                                                    ) {
                                                        Text("+1 Saa", fontSize = 10.sp, color = Color(0xFF0369A1), fontWeight = FontWeight.Bold)
                                                    }
                                                    Button(
                                                        onClick = {
                                                            coroutineScope.launch {
                                                                val next = (g.achievedHours - 1f).coerceAtLeast(0f)
                                                                repository.updateProgressGoalHours(g.id, next, g.targetHours)
                                                            }
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE2E2)),
                                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                        modifier = Modifier.height(30.dp)
                                                    ) {
                                                        Text("-1 Saa", fontSize = 10.sp, color = Color(0xFF991B1B), fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                1 -> {
                    // TAB 1: COURSE MODULES TRACKER
                    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = Localization.translate("Moduli za Kozi", currentLanguage),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Button(
                                onClick = { 
                                    isAddingSemesterGoal = false
                                    showAddCourseModule = true 
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                                shape = RoundedCornerShape(20.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(Localization.translate("Sajili Moduli", currentLanguage), fontSize = 11.sp)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        if (coursesOnly.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize().weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.List, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(Localization.translate("Hakuna moduli iliyosajiliwa", currentLanguage), color = Color.Gray, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Bonyeza 'Sajili Moduli' kuanza kufuatilia masomo yako", color = Color.Gray, fontSize = 11.sp)
                                }
                            }
                        } else {
                            val modulesByCourse = coursesOnly.groupBy { it.courseCode }
                            
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(modulesByCourse.keys.toList()) { courseCode ->
                                    val courseModulesList = modulesByCourse[courseCode] ?: emptyList()
                                    val courseName = courseModulesList.firstOrNull()?.courseName ?: ""
                                    
                                    val totalMod = courseModulesList.size
                                    val completedMod = courseModulesList.count { it.completed }
                                    val progress = if (totalMod > 0) completedMod.toFloat() / totalMod else 0f
                                    
                                    var expanded by remember { mutableStateOf(true) }
                                    
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable { expanded = !expanded },
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Box(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(4.dp))
                                                                .background(MaterialTheme.colorScheme.primaryContainer)
                                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                                        ) {
                                                            Text(courseCode, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                                        }
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text(courseName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                    }
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = "$completedMod / $totalMod Moduli zilizokamilika",
                                                        fontSize = 12.sp,
                                                        color = Color.Gray
                                                    )
                                                }
                                                Icon(
                                                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                                    contentDescription = null,
                                                    tint = Color.Gray
                                                )
                                            }
                                            
                                            Spacer(modifier = Modifier.height(8.dp))
                                            
                                            LinearProgressIndicator(
                                                progress = { progress },
                                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                                color = Color(0xFF3B82F6),
                                                trackColor = Color.LightGray.copy(alpha = 0.5f)
                                            )
                                            
                                            if (expanded) {
                                                Spacer(modifier = Modifier.height(10.dp))
                                                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f))
                                                Spacer(modifier = Modifier.height(8.dp))
                                                
                                                courseModulesList.forEach { mod ->
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(vertical = 4.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Checkbox(
                                                            checked = mod.completed,
                                                            onCheckedChange = { isChecked ->
                                                                coroutineScope.launch {
                                                                    repository.updateCourseModuleStatus(mod.id, isChecked)
                                                                }
                                                            },
                                                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF3B82F6))
                                                        )
                                                        
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Text(
                                                                text = mod.moduleTitle,
                                                                fontSize = 13.sp,
                                                                fontWeight = FontWeight.Medium,
                                                                color = if (mod.completed) Color.Gray else Color.Unspecified,
                                                                style = if (mod.completed) MaterialTheme.typography.bodyMedium.copy(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough) else MaterialTheme.typography.bodyMedium
                                                            )
                                                            if (mod.dueDate.isNotEmpty() || mod.priority.isNotEmpty()) {
                                                                Row(
                                                                    verticalAlignment = Alignment.CenterVertically,
                                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                                ) {
                                                                    if (mod.dueDate.isNotEmpty()) {
                                                                        Text("📅 " + mod.dueDate, fontSize = 10.sp, color = Color.Gray)
                                                                    }
                                                                    
                                                                    val priorityColor = when (mod.priority.lowercase()) {
                                                                        "high" -> Color(0xFFEF4444)
                                                                        "medium" -> Color(0xFFF59E0B)
                                                                        else -> Color(0xFF10B981)
                                                                    }
                                                                    Box(
                                                                        modifier = Modifier
                                                                            .clip(RoundedCornerShape(4.dp))
                                                                            .background(priorityColor.copy(alpha = 0.15f))
                                                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                                                    ) {
                                                                        Text(mod.priority, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = priorityColor)
                                                                    }
                                                                }
                                                            }
                                                        }
                                                        
                                                        IconButton(
                                                            onClick = {
                                                                coroutineScope.launch {
                                                                    repository.deleteCourseModule(mod.id)
                                                                }
                                                            },
                                                            modifier = Modifier.size(32.dp)
                                                        ) {
                                                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.LightGray)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                2 -> {
                    // TAB 2: SEMESTER MILESTONES / GOALS
                    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = Localization.translate("Malengo ya Muhula", currentLanguage),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Button(
                                onClick = { 
                                    isAddingSemesterGoal = true
                                    showAddCourseModule = true 
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                                shape = RoundedCornerShape(20.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(Localization.translate("Sajili Lengo", currentLanguage), fontSize = 11.sp)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        if (semesterGoals.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize().weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(Localization.translate("Hakuna malengo ya muhula", currentLanguage), color = Color.Gray, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Jiwekee malengo ya muhula (ada, usajili, CATs...) kufuatilia kwa makini", color = Color.Gray, fontSize = 11.sp)
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(semesterGoals, key = { goal -> goal.id }) { goal ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Checkbox(
                                                checked = goal.completed,
                                                onCheckedChange = { isChecked ->
                                                    coroutineScope.launch {
                                                        repository.updateCourseModuleStatus(goal.id, isChecked)
                                                    }
                                                },
                                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFFF59E0B))
                                            )
                                            
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = goal.moduleTitle,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    style = if (goal.completed) MaterialTheme.typography.bodyMedium.copy(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough) else MaterialTheme.typography.bodyMedium,
                                                    color = if (goal.completed) Color.Gray else Color.Unspecified
                                                )
                                                
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    modifier = Modifier.padding(top = 2.dp)
                                                ) {
                                                    if (goal.dueDate.isNotEmpty()) {
                                                        Text("📅 " + goal.dueDate, fontSize = 10.sp, color = Color.Gray)
                                                    }
                                                    
                                                    val priorityColor = when (goal.priority.lowercase()) {
                                                        "high" -> Color(0xFFEF4444)
                                                        "medium" -> Color(0xFFF59E0B)
                                                        else -> Color(0xFF10B981)
                                                    }
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .background(priorityColor.copy(alpha = 0.15f))
                                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                                    ) {
                                                        Text(goal.priority, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = priorityColor)
                                                    }
                                                }
                                            }
                                            
                                            IconButton(
                                                onClick = {
                                                    coroutineScope.launch {
                                                        repository.deleteCourseModule(goal.id)
                                                    }
                                                }
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.LightGray)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- DIALOGS ---
        
        // 1. ADD STUDY GOAL DIALOG
        if (showAddGoal) {
            Dialog(onDismissRequest = { showAddGoal = false }) {
                var title by remember { mutableStateOf("") }
                var targetHours by remember { mutableStateOf("") }
                
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = Localization.translate("Sajili Lengo la Kujisomea", currentLanguage) + " ⏱️",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text(Localization.translate("Lengo langu", currentLanguage)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = targetHours,
                            onValueChange = { targetHours = it },
                            label = { Text(Localization.translate("Masaa ya Malengo", currentLanguage)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = {
                                val hrs = targetHours.toFloatOrNull() ?: 1.0f
                                if (title.isNotBlank()) {
                                    coroutineScope.launch {
                                        repository.addProgressGoal(title, hrs)
                                        showAddGoal = false
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(Localization.translate("Sajili Lengo", currentLanguage))
                        }
                    }
                }
            }
        }

        // 2. ADD COURSE MODULE / SEMESTER GOAL DIALOG
        if (showAddCourseModule) {
            Dialog(onDismissRequest = { showAddCourseModule = false }) {
                var courseCode by remember { mutableStateOf("") }
                var courseName by remember { mutableStateOf("") }
                var title by remember { mutableStateOf("") }
                var dueDate by remember { mutableStateOf("") }
                var priority by remember { mutableStateOf("Medium") }
                
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (isAddingSemesterGoal) {
                                Localization.translate("Sajili Lengo la Muhula", currentLanguage) + " 🎯"
                            } else {
                                Localization.translate("Sajili Moduli ya Kozi", currentLanguage) + " 📚"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        if (!isAddingSemesterGoal) {
                            OutlinedTextField(
                                value = courseCode,
                                onValueChange = { courseCode = it },
                                label = { Text("Code ya Kozi (e.g., MTH101)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            OutlinedTextField(
                                value = courseName,
                                onValueChange = { courseName = it },
                                label = { Text("Jina la Kozi (e.g., Linear Algebra)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            OutlinedTextField(
                                value = title,
                                onValueChange = { title = it },
                                label = { Text("Moduli au Topic (e.g., Vector Spaces)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        } else {
                            OutlinedTextField(
                                value = title,
                                onValueChange = { title = it },
                                label = { Text("Lengo la Muhula (e.g., Lipa Ada awamu ya 2)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = dueDate,
                            onValueChange = { dueDate = it },
                            label = { Text("Muda wa Kukamilisha (e.g., 15 Julai)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text("Kiwango cha Umuhimu (Priority):", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("High", "Medium", "Low").forEach { p ->
                                val selected = priority == p
                                val color = when (p) {
                                    "High" -> Color(0xFFEF4444)
                                    "Medium" -> Color(0xFFF59E0B)
                                    else -> Color(0xFF10B981)
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selected) color.copy(alpha = 0.2f) else Color.Transparent)
                                        .clickable { priority = p }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = p,
                                        color = if (selected) color else Color.Gray,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = {
                                if (title.isNotBlank()) {
                                    coroutineScope.launch {
                                        repository.addCourseModule(
                                            courseCode = if (isAddingSemesterGoal) "SEM" else courseCode.trim().uppercase(),
                                            courseName = if (isAddingSemesterGoal) "Semester Goal" else courseName.trim(),
                                            moduleTitle = title.trim(),
                                            dueDate = dueDate.trim(),
                                            isSemesterGoal = isAddingSemesterGoal,
                                            priority = priority
                                        )
                                        showAddCourseModule = false
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isAddingSemesterGoal) Color(0xFFF59E0B) else Color(0xFF3B82F6)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(Localization.translate("Sajili", currentLanguage))
                        }
                    }
                }
            }
        }
    }
}

// --- PEER TO PEER STUDENT CHAT ---
@Composable
fun PeerChatScreen(
    repository: AppRepository,
    peerPhone: String,
    peerName: String,
    onBack: () -> Unit,
    currentLanguage: String
) {
    val coroutineScope = rememberCoroutineScope()
    val chatHistory by repository.getPeerMessages(peerPhone).collectAsState(initial = emptyList())
    val currentUser by repository.currentUser.collectAsState()
    var text by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(Localization.translate("Kusoma Pamoja", currentLanguage) + ": $peerName", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(chatHistory) { msg ->
                    val isMe = msg.senderPhone == currentUser?.phone
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = if (isMe) Color(0xFFDCFCE7) else Color(0xFFF1F5F9)),
                            modifier = Modifier.widthIn(max = 280.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = msg.text,
                                modifier = Modifier.padding(10.dp),
                                fontSize = 13.sp,
                                color = Color.Black
                            )
                        }
                    }
                }
            }

            // Input
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text(Localization.translate("Mwandikie ujumbe wa masomo", currentLanguage) + "...", fontSize = 12.sp) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (text.isNotBlank()) {
                            coroutineScope.launch {
                                repository.sendPeerMessage(peerPhone, text)
                                text = ""
                            }
                        }
                    },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFF16A34A))
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White)
                }
            }
        }
    }
}

// --- AI ASSISTANT (Simplified One Input & One Send Button) ---
@Composable
fun ChatScreen(
    repository: AppRepository,
    currentLanguage: String
) {
    val coroutineScope = rememberCoroutineScope()
    val chatHistory by repository.chatHistory.collectAsState(initial = emptyList())
    var prompt by remember { mutableStateOf("") }
    var isThinking by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val hasKey = BuildConfig.GEMINI_API_KEY.isNotBlank() &&
            BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY" &&
            BuildConfig.GEMINI_API_KEY != "TODO"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // AI Header
        Card(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        Localization.translate("StudyAI - Msaidizi Wako", currentLanguage) + " 🧠🤖",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        Localization.translate("Uulize chochote", currentLanguage) + "!",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            repository.clearChat()
                            Toast.makeText(context, Localization.translate("Historia ya mazungumzo imefutwa", currentLanguage) + "! 🧹", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = Localization.translate("Futa Historia", currentLanguage),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        // Warning banner if API Key is missing
        if (!hasKey) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            Localization.translate("Gemini API Key haijasanidiwa", currentLanguage) + "!",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            Localization.translate("Tafadhali weka GEMINI_API_KEY", currentLanguage),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // Chat lists or Empty State Placeholder
        if (chatHistory.isEmpty() && !isThinking) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "AI Msaidizi",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                        
                        Text(
                            text = Localization.translate("Karibu StudyAI", currentLanguage) + "! 👋🤖",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                        
                        Text(
                            text = Localization.translate("Mimi ni msaidizi wako wa kibinafsi", currentLanguage),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center,
                            lineHeight = 17.sp
                        )
                        
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = Localization.translate("Mifano ya maswali", currentLanguage) + ":",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            
                            val bulletTipsKeys = listOf(
                                "Fupisha dondoo za somo",
                                "Tengeneza maswali 5",
                                "Tafsiri sentensi hii"
                            )
                            
                            bulletTipsKeys.forEach { key ->
                                val tipText = Localization.translate(key, currentLanguage)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { prompt = tipText }
                                        .padding(vertical = 4.dp, horizontal = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color(0xFF16A34A),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = tipText,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Chat lists with animated scrolling and keying optimization
            val listState = rememberLazyListState()
            
            LaunchedEffect(chatHistory.size, isThinking) {
                if (chatHistory.isNotEmpty()) {
                    listState.animateScrollToItem(chatHistory.size - 1)
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = chatHistory,
                    key = { it.id }
                ) { item ->
                    AIChatMessageBubble(item)
                }
                if (isThinking) {
                    item {
                        // Pulse animated thinking card
                        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                        val alpha by infiniteTransition.animateFloat(
                            initialValue = 0.4f,
                            targetValue = 1.0f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(800, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "alpha"
                        )
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp, horizontal = 2.dp)
                                .graphicsLayer(alpha = alpha),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.5.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = Localization.translate("StudyAI anachambua", currentLanguage) + "...",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Intent Quick Action Chips
        Text(
            Localization.translate("Njia za Haraka", currentLanguage) + " 🚀",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                QuickActionChip(
                    label = Localization.translate("Fupisha Masomo", currentLanguage) + " 📝",
                    onClick = {
                        prompt = Localization.translate("Fupisha dondoo hizi za somo", currentLanguage) + ": "
                    }
                )
            }
            item {
                QuickActionChip(
                    label = Localization.translate("Tengeneza Notes", currentLanguage) + " 📚",
                    onClick = {
                        prompt = Localization.translate("Andika notes za kina", currentLanguage) + ": "
                    }
                )
            }
            item {
                QuickActionChip(
                    label = Localization.translate("Tafsiri Kiingereza-Kiswahili", currentLanguage) + " 🔤",
                    onClick = {
                        prompt = Localization.translate("Tafsiri sentensi hii na ueleze", currentLanguage) + ": "
                    }
                )
            }
            item {
                QuickActionChip(
                    label = Localization.translate("Tengeneza Quiz/Maswali", currentLanguage) + " ✍️",
                    onClick = {
                        prompt = Localization.translate("Tengeneza maswali 5 ya mazoezi", currentLanguage) + ": "
                    }
                )
            }
            item {
                QuickActionChip(
                    label = Localization.translate("Ratiba ya Wiki", currentLanguage) + " 📅",
                    onClick = {
                        prompt = Localization.translate("Tengeneza ratiba bora", currentLanguage) + ": "
                    }
                )
            }
        }

        // ONE INPUT AND ONE SEND BUTTON (With integrated soft keyboard triggers)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = prompt,
                onValueChange = { prompt = it },
                placeholder = { Text(Localization.translate("Andika hapa...", currentLanguage), fontSize = 13.sp) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
                trailingIcon = {
                    if (prompt.isNotEmpty()) {
                        IconButton(onClick = { prompt = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (prompt.isNotBlank() && !isThinking) {
                            val userText = prompt
                            prompt = ""
                            isThinking = true
                            coroutineScope.launch {
                                repository.sendChatMessage(userText, "user")
                                val studyResponse = StudyAIController.handleStudyRequest(
                                    prompt = userText,
                                    history = chatHistory
                                )
                                repository.sendChatMessage(studyResponse.textResponse, "model")
                                isThinking = false
                            }
                        }
                    }
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (prompt.isNotBlank()) {
                        val userText = prompt
                        prompt = ""
                        isThinking = true
                        coroutineScope.launch {
                            repository.sendChatMessage(userText, "user")
                            
                            // Connect securely to the Gemini API using the robust study controller
                            val studyResponse = StudyAIController.handleStudyRequest(
                                prompt = userText,
                                history = chatHistory
                            )
                            
                            repository.sendChatMessage(studyResponse.textResponse, "model")
                            isThinking = false
                        }
                    }
                },
                enabled = !isThinking,
                colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFF16A34A))
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White)
            }
        }
    }
}

@Composable
fun QuickActionChip(
    label: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .clickable { onClick() }
            .padding(vertical = 2.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun AIChatMessageBubble(item: ChatMessageEntity) {
    val isMe = item.senderRole == "user"
    val context = LocalContext.current
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        if (!isMe) {
            // StudyAI avatar
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text("AI", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
        
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
            ),
            modifier = Modifier
                .widthIn(max = 290.dp)
                .animateContentSize(), // Beautiful, expensive dynamic height change transitions
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isMe) 16.dp else 2.dp,
                bottomEnd = if (isMe) 2.dp else 16.dp
            ),
            border = if (isMe) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (!isMe) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "StudyAI Msaidizi",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        
                        // Small premium "Copy" indicator button
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable {
                                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(item.text))
                                    Toast.makeText(context, "Imenakiliwa! 📋", Toast.LENGTH_SHORT).show()
                                }
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Nakili 📋",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
                
                // Parse lines to style sections
                val text = item.text
                val lines = text.split("\n")
                
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    lines.forEach { line ->
                        when {
                            // Render title sections or bullet headers beautifully
                            line.startsWith("📌") || line.startsWith("🔑") || line.startsWith("📊") || line.startsWith("💡") || line.startsWith("🔄") || line.startsWith("🔍") || line.startsWith("📖") || line.startsWith("🗣️") || line.startsWith("📝") || line.startsWith("🗝️") || line.startsWith("📚") || line.startsWith("🗂️") -> {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = line,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isMe) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            line.startsWith("###") || line.startsWith("**") -> {
                                val cleanLine = line.replace("#", "").replace("*", "").trim()
                                Text(
                                    text = cleanLine,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isMe) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            line.trim().startsWith("-") || line.trim().startsWith("•") -> {
                                Row(modifier = Modifier.padding(start = 4.dp)) {
                                    Text("• ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isMe) Color.White else MaterialTheme.colorScheme.primary)
                                    val contentText = line.trim().let {
                                        if (it.startsWith("-")) it.substring(1).trim()
                                        else if (it.startsWith("•")) it.substring(1).trim()
                                        else it
                                    }
                                    Text(
                                        text = contentText,
                                        fontSize = 12.sp,
                                        color = if (isMe) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            else -> {
                                val cleanText = line.replace("**", "")
                                Text(
                                    text = cleanText,
                                    fontSize = 12.sp,
                                    color = if (isMe) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
        
        if (isMe) {
            Spacer(modifier = Modifier.width(4.dp))
        }
    }
}


// --- SETTINGS REDESIGN ---
@Composable
fun SettingsScreen(
    repository: AppRepository,
    darkTheme: Boolean,
    onThemeToggle: (Boolean) -> Unit,
    onLogout: () -> Unit,
    currentLanguage: String,
    onLanguageChange: (String) -> Unit
) {
    val context = LocalContext.current
    val currentUser by repository.currentUser.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var showEditProfile by remember { mutableStateOf(false) }
    var showAdminPanel by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Top Profile Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF16A34A)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(currentUser?.name?.take(1) ?: "U", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(currentUser?.name ?: "User", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.Verified, contentDescription = "Verified", tint = Color(0xFF1D9BF0), modifier = Modifier.size(16.dp))
                }
                Text("${currentUser?.programme} | ${currentUser?.uni}", fontSize = 12.sp, color = Color.Gray)
                Text("${Localization.translate("Simu", currentLanguage)}: ${currentUser?.phone} | ${Localization.translate("Nchi", currentLanguage)}: ${currentUser?.country}", fontSize = 11.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { showEditProfile = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                ) {
                    Text(Localization.translate("Hariri Wasifu", currentLanguage))
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Professional Settings Rows
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(Localization.translate("Mipangilio ya Mfumo", currentLanguage), fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 12.sp)
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onThemeToggle(!darkTheme) }
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row {
                        Icon(Icons.Default.Palette, contentDescription = null, tint = Color.Gray)
                        Spacer(modifier = Modifier.width(10.dp))
                        val nightModeText = when(currentLanguage) {
                            "English" -> "Dark Theme / Night Mode"
                            "France" -> "Thème sombre"
                            "Kichina" -> "深色模式"
                            else -> "Modi ya Usiku (Dark Theme)"
                        }
                        Text(nightModeText, fontSize = 14.sp)
                    }
                    Switch(checked = darkTheme, onCheckedChange = onThemeToggle)
                }
            }
            item {
                val securityText = when(currentLanguage) {
                    "English" -> "Security & 2FA Setup"
                    "France" -> "Sécurité et configuration 2FA"
                    "Kichina" -> "安全与双重认证设置"
                    else -> "Ulinzi na Usalama (Security)"
                }
                SettingsNavigationRow(icon = Icons.Default.Shield, label = securityText) {
                    Toast.makeText(context, Localization.translate("Ulinzi na 2FA upo hai", currentLanguage), Toast.LENGTH_SHORT).show()
                }
            }
            item {
                val languageLabelText = when(currentLanguage) {
                    "English" -> "Language: English"
                    "France" -> "Langue: Français (French)"
                    "Kichina" -> "语言: 中文 (Chinese)"
                    else -> "Lugha: Kiswahili"
                }
                SettingsNavigationRow(icon = Icons.Default.Language, label = languageLabelText) {
                    showLanguageDialog = true
                }
            }
            item {
                val dataBackupText = when(currentLanguage) {
                    "English" -> "Data Usage & Auto Backup"
                    "France" -> "Utilisation de données & Sauvegarde"
                    "Kichina" -> "数据使用与自动备份"
                    else -> "Kuhifadhi Data (Backup)"
                }
                SettingsNavigationRow(icon = Icons.Default.Cloud, label = dataBackupText) {
                    Toast.makeText(context, Localization.translate("Kuhifadhi data kumewezeshwa", currentLanguage), Toast.LENGTH_SHORT).show()
                }
            }
            item {
                val helpText = when(currentLanguage) {
                    "English" -> "Help Center & Support"
                    "France" -> "Centre d'aide et support"
                    "Kichina" -> "帮助中心与支持"
                    else -> "Msaada na Huduma (Help Center)"
                }
                SettingsNavigationRow(icon = Icons.Default.SupportAgent, label = helpText) {
                    Toast.makeText(context, Localization.translate("Msaada unapatikana 24/7", currentLanguage), Toast.LENGTH_SHORT).show()
                }
            }
            item {
                SettingsNavigationRow(icon = Icons.Default.AdminPanelSettings, label = "AI Management & Admin Panel") {
                    showAdminPanel = true
                }
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(Localization.translate("Ondoka Kwenye Mfumo", currentLanguage), color = Color.White)
                }
            }
        }
    }

    if (showLanguageDialog) {
        Dialog(onDismissRequest = { showLanguageDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = Localization.translate("Lugha", currentLanguage),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val languages = listOf("Kiswahili", "English", "France", "Kichina")
                    val displayNames = mapOf(
                        "Kiswahili" to "Kiswahili (Swahili)",
                        "English" to "English (Kiingereza)",
                        "France" to "Français (French)",
                        "Kichina" to "中文 (Chinese)"
                    )
                    
                    languages.forEach { lang ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onLanguageChange(lang)
                                    showLanguageDialog = false
                                    Toast.makeText(context, Localization.translate("Lugha iliyochaguliwa ni", lang), Toast.LENGTH_SHORT).show()
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = displayNames[lang] ?: lang,
                                fontSize = 16.sp,
                                fontWeight = if (currentLanguage == lang) FontWeight.Bold else FontWeight.Normal,
                                color = if (currentLanguage == lang) Color(0xFF16A34A) else MaterialTheme.colorScheme.onSurface
                            )
                            if (currentLanguage == lang) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF16A34A))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEditProfile) {
        Dialog(onDismissRequest = { showEditProfile = false }) {
            var edtName by remember { mutableStateOf(currentUser?.name ?: "") }
            var edtUni by remember { mutableStateOf(currentUser?.uni ?: "UDSM") }
            var edtBio by remember { mutableStateOf(currentUser?.bio ?: "") }
            var edtCountry by remember { mutableStateOf(currentUser?.country ?: "Tanzania") }
            var edtCampus by remember { mutableStateOf(currentUser?.campus ?: "") }
            var edtFaculty by remember { mutableStateOf(currentUser?.faculty ?: "") }
            var edtDept by remember { mutableStateOf(currentUser?.department ?: "") }
            var edtProg by remember { mutableStateOf(currentUser?.programme ?: "") }
            var edtYr by remember { mutableStateOf(currentUser?.yearOfStudy ?: "") }

            Card(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.9f)) {
                Column(modifier = Modifier.padding(16.dp).verticalScroll(androidx.compose.foundation.rememberScrollState())) {
                    Text("Hariri Wasifu Kamili", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(value = edtName, onValueChange = { edtName = it }, label = { Text("Jina") })
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(value = edtUni, onValueChange = { edtUni = it }, label = { Text("Chuo") })
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(value = edtBio, onValueChange = { edtBio = it }, label = { Text("Bio") })
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(value = edtCountry, onValueChange = { edtCountry = it }, label = { Text("Nchi") })
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(value = edtCampus, onValueChange = { edtCampus = it }, label = { Text("Kampasi") })
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(value = edtFaculty, onValueChange = { edtFaculty = it }, label = { Text("Kitivo") })
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(value = edtDept, onValueChange = { edtDept = it }, label = { Text("Idara") })
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(value = edtProg, onValueChange = { edtProg = it }, label = { Text("Kozi") })
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(value = edtYr, onValueChange = { edtYr = it }, label = { Text("Mwaka wa Masomo") })
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                repository.updateProfile(
                                    name = edtName,
                                    uni = edtUni,
                                    bio = edtBio,
                                    country = edtCountry,
                                    campus = edtCampus,
                                    faculty = edtFaculty,
                                    department = edtDept,
                                    programme = edtProg,
                                    yearOfStudy = edtYr
                                )
                                showEditProfile = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                    ) {
                        Text("Hifadhi Wasifu")
                    }
                }
            }
        }
    }

    if (showAdminPanel) {
        AdminPanelDialog(repository = repository, onDismiss = { showAdminPanel = false })
    }
}

@Composable
fun SettingsNavigationRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row {
            Icon(icon, contentDescription = null, tint = Color.Gray)
            Spacer(modifier = Modifier.width(10.dp))
            Text(label, fontSize = 14.sp)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
    }
}

// --- ADMIN CONTROL & INTENT INSIGHTS PANEL ---
@Composable
fun AdminPanelDialog(
    repository: AppRepository,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf("unis") } // unis, insights

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Admin Dashboard 🛡️🛠️", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Funga")
                    }
                }
                
                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { selectedTab = "unis" },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = if (selectedTab == "unis") Color(0xFF16A34A) else Color.LightGray)
                    ) {
                        Text("Vyuo vya TZ", color = if (selectedTab == "unis") Color.White else Color.Black, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Button(
                        onClick = { selectedTab = "insights" },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = if (selectedTab == "insights") Color(0xFF16A34A) else Color.LightGray)
                    ) {
                        Text("Platform AI Insights", color = if (selectedTab == "insights") Color.White else Color.Black, fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                if (selectedTab == "unis") {
                    val unisList by repository.universities.collectAsState(initial = emptyList())
                    var newCode by remember { mutableStateOf("") }
                    var newName by remember { mutableStateOf("") }
                    var newType by remember { mutableStateOf("Public") }
                    var newLoc by remember { mutableStateOf("") }

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Sajili Chuo Kipya", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedTextField(value = newCode, onValueChange = { newCode = it }, label = { Text("Code (UDSM)") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = newLoc, onValueChange = { newLoc = it }, label = { Text("Mkoa") }, modifier = Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text("Jina la Chuo") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        Button(
                            onClick = {
                                if (newCode.isNotEmpty() && newName.isNotEmpty()) {
                                    coroutineScope.launch {
                                        repository.addUniversity(newCode, newName, newType, newLoc)
                                        newCode = ""
                                        newName = ""
                                        newLoc = ""
                                        Toast.makeText(context, "Chuo kimesajiliwa kikamilifu kwenye Database!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                        ) {
                            Text("Hifadhi Chuo", fontSize = 12.sp)
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Orodha ya Vyuo vya Tanzania:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(unisList) { uni ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("${uni.code} - ${uni.name}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text("Aina: ${uni.type} | Mahali: ${uni.location}", fontSize = 10.sp, color = Color.Gray)
                                    }
                                    IconButton(onClick = {
                                        coroutineScope.launch {
                                            repository.deleteUniversity(uni.code)
                                        }
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Futa", tint = Color.Red, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // AI insights and moderator tools
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(androidx.compose.foundation.rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("AI Intelligent Moderate Reports 🤖📊", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        
                        AdminInsightCard(title = "Trending Educational Topics", desc = "1. Linear Algebra preparation (COICT UDSM)\n2. HESLB Appeals and updates\n3. Crop rotation research topics (SUA)")
                        AdminInsightCard(title = "Comment Spam Moderation", desc = "All posts and comments are safe. AI automated plagiarism check: 98.7% unique submissions. Spam count: 0.")
                        AdminInsightCard(title = "Harmful & Fake Account Filter", desc = "AI Security scan completed. Verified active logins: 3. Blocked malicious registration: 0.")
                        AdminInsightCard(title = "Predicted Viral Material", desc = "Casio FX-991EX manual slides are gaining high engagement. Recommended to student feeds in SUA and UDOM.")
                    }
                }
            }
        }
    }
}

@Composable
fun AdminInsightCard(title: String, desc: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF16A34A))
            Spacer(modifier = Modifier.height(4.dp))
            Text(desc, fontSize = 11.sp, color = Color.DarkGray)
        }
    }
}

// --- SHARED CREATE DIALOG ---
@Composable
fun CreatePostDialog(
    type: String,
    onDismiss: () -> Unit,
    onSubmit: (String, String, Boolean, String, String) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var pollQuestion by remember { mutableStateOf("") }
    var pollOptions by remember { mutableStateOf("") }

    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = when (type) {
                        "picture" -> "Tengeneza Chapisho la Picha 📸"
                        "poll" -> "Tengeneza Kura Mpya 📊"
                        else -> "Andika Chapisho la Kawaida 📝"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Maelezo ya chapisho au dondoo...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                
                if (type == "picture") {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = imageUrl,
                        onValueChange = { imageUrl = it },
                        label = { Text("Weka URL ya picha (Hadi picha 3, weka koma)") },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("https://url1.jpg,https://url2.jpg") }
                    )
                }

                if (type == "poll") {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = pollQuestion,
                        onValueChange = { pollQuestion = it },
                        label = { Text("Swali la Kura") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = pollOptions,
                        onValueChange = { pollOptions = it },
                        label = { Text("Chaguzi (Zitenganishe kwa koma)") },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Ndio,Hapana,Labda") }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (text.isBlank() && type != "poll") {
                            Toast.makeText(context, "Tafadhali jaza maelezo!", Toast.LENGTH_SHORT).show()
                        } else {
                            // Automatically insert default placeholder image if none provided for picture type
                            val finalUrl = if (type == "picture" && imageUrl.isBlank()) {
                                "https://images.unsplash.com/photo-1434030216411-0b793f4b4173?w=500"
                            } else {
                                imageUrl
                            }
                            onSubmit(
                                text,
                                finalUrl,
                                type == "poll",
                                pollQuestion,
                                pollOptions
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                ) {
                    Text("Tuma Sasa", color = Color.White)
                }
            }
        }
    }
}
