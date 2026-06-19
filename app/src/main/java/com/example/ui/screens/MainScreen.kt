package com.example.ui.screens

import android.app.DatePickerDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.data.database.AgendaItem
import com.example.data.database.ChatSession
import com.example.ui.theme.*
import com.example.ui.viewmodel.OdevViewModel
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: OdevViewModel = viewModel()
) {
    val context = LocalContext.current
    val currentMode by viewModel.currentMode.collectAsState()
    
    val screenTitle = when(currentMode) {
        0 -> "Soru Çöz (Yapay Zeka)"
        1 -> "Yapay Zeka Sohbet"
        2 -> "Çalışma Ajandası"
        3 -> "İlerleme Raporu"
        else -> "Ödev Pro"
    }

    Scaffold(
        containerColor = ObsidianBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Brush.linearGradient(listOf(WhiteAccent, AccentGold))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = ObsidianBackground,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = screenTitle,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = PremiumTextPrimary,
                                fontFamily = FontFamily.SansSerif,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = ObsidianBackground.copy(alpha = 0.95f)
                ),
                actions = {
                    IconButton(
                        onClick = {
                            ToastHelper.showToast(context, "Ödev Pro - Premium Sürüm Aktif")
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Premium",
                            tint = AccentGold
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = ObsidianSurface,
                tonalElevation = 8.dp,
                modifier = Modifier.border(1.dp, ObsidianBorder, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                NavigationBarItem(
                    selected = currentMode == 0,
                    onClick = { viewModel.changeMode(0) },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "Soru Çöz") },
                    label = { Text("Soru Çöz", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ObsidianBackground,
                        selectedTextColor = WhiteAccent,
                        indicatorColor = WhiteAccent,
                        unselectedIconColor = PremiumTextSecondary,
                        unselectedTextColor = PremiumTextSecondary
                    ),
                    modifier = Modifier.testTag("nav_btn_solve")
                )
                NavigationBarItem(
                    selected = currentMode == 1,
                    onClick = { viewModel.changeMode(1) },
                    icon = { Icon(Icons.Default.Chat, contentDescription = "Sohbet") },
                    label = { Text("AI Sohbet", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ObsidianBackground,
                        selectedTextColor = WhiteAccent,
                        indicatorColor = WhiteAccent,
                        unselectedIconColor = PremiumTextSecondary,
                        unselectedTextColor = PremiumTextSecondary
                    ),
                    modifier = Modifier.testTag("nav_btn_chat")
                )
                NavigationBarItem(
                    selected = currentMode == 2,
                    onClick = { viewModel.changeMode(2) },
                    icon = { Icon(Icons.Default.DateRange, contentDescription = "Ajanda") },
                    label = { Text("Ajanda", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ObsidianBackground,
                        selectedTextColor = WhiteAccent,
                        indicatorColor = WhiteAccent,
                        unselectedIconColor = PremiumTextSecondary,
                        unselectedTextColor = PremiumTextSecondary
                    ),
                    modifier = Modifier.testTag("nav_btn_agenda")
                )
                NavigationBarItem(
                    selected = currentMode == 3,
                    onClick = { viewModel.changeMode(3) },
                    icon = { Icon(Icons.Default.Assessment, contentDescription = "Rapor") },
                    label = { Text("Raporlar", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ObsidianBackground,
                        selectedTextColor = WhiteAccent,
                        indicatorColor = WhiteAccent,
                        unselectedIconColor = PremiumTextSecondary,
                        unselectedTextColor = PremiumTextSecondary
                    ),
                    modifier = Modifier.testTag("nav_btn_report")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentMode) {
                0 -> SolverTab(viewModel)
                1 -> ChatTab(viewModel)
                2 -> AgendaTab(viewModel)
                3 -> ReportsTab(viewModel)
            }
        }
    }
}

// ==================== TAB 0: HOMEWORK SOLVER ====================

@Composable
fun SolverTab(viewModel: OdevViewModel) {
    val context = LocalContext.current
    val bitmap by viewModel.selectedBitmap.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    val solveResult by viewModel.solveResult.collectAsState()
    val uploadMessage by viewModel.uploadStatusMessage.collectAsState()
    val serverUrl by viewModel.customServerUrl.collectAsState()

    var showServerConfig by remember { mutableStateOf(false) }
    var userNoteText by remember { mutableStateOf("") }
    var tempServerUrl by remember { mutableStateOf(serverUrl) }

    // Contracts for capturing or choosing image
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { resultBitmap: Bitmap? ->
        if (resultBitmap != null) {
            viewModel.selectBitmap(resultBitmap)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val selected = BitmapFactory.decodeStream(inputStream)
                viewModel.selectBitmap(selected)
            } catch (e: Exception) {
                ToastHelper.showToast(context, "Görsel yüklenemedi: ${e.localizedMessage}")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Key Warning (Required security guidelines mandate)
        Card(
            colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
            border = BorderStroke(1.dp, ObsidianBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Güvenlik",
                    tint = AccentGold,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Güvenlik Uyarısı: API anahtarı geliştirme prototipi amacıyla dahil edilmiştir. APK dosyasını herkese açık paylaşmayınız.",
                    color = PremiumTextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }

        // Image View / Selector Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(2.dp, Brush.linearGradient(listOf(ObsidianBorder, WhiteAccent.copy(alpha = 0.2f))), RoundedCornerShape(16.dp))
                .background(ObsidianSurface),
            contentAlignment = Alignment.Center
        ) {
            if (bitmap != null) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        bitmap = bitmap!!.asImageBitmap(),
                        contentDescription = "Seçilen Soru",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                    
                    // Clear selected button
                    IconButton(
                        onClick = { viewModel.selectBitmap(null) },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(ObsidianBackground.copy(alpha = 0.7f), CircleShape)
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = "Görseli Kaldır", tint = Color.Red)
                    }
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = null,
                        modifier = Modifier.size(54.dp),
                        tint = PremiumTextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Bir görsel seçin veya anlık fotoğrafını çekin",
                        color = PremiumTextSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Image Operations Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { cameraLauncher.launch() },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("btn_capture"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ObsidianSurface, contentColor = WhiteAccent),
                border = BorderStroke(1.dp, ObsidianBorder)
            ) {
                Icon(Icons.Default.PhotoCamera, "Kamera")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Kamera", fontSize = 13.sp)
            }

            Button(
                onClick = { galleryLauncher.launch("image/*") },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("btn_gallery"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ObsidianSurface, contentColor = WhiteAccent),
                border = BorderStroke(1.dp, ObsidianBorder)
            ) {
                Icon(Icons.Default.PhotoLibrary, "Galeri")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Galeri", fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Input prompt for homework analysis notes
        OutlinedTextField(
            value = userNoteText,
            onValueChange = { userNoteText = it },
            label = { Text("Yapay zekaya not veya ek soru ekleyin (isteğe bağlı)...", fontSize = 12.sp) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("question_text_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = PremiumTextPrimary,
                unfocusedTextColor = PremiumTextPrimary,
                focusedBorderColor = WhiteAccent,
                unfocusedBorderColor = ObsidianBorder,
                focusedLabelColor = WhiteAccent,
                unfocusedLabelColor = PremiumTextSecondary
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Large Primary Solve Action
        Button(
            onClick = {
                viewModel.solveHomework(userNoteText)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("btn_solve_action"),
            colors = ButtonDefaults.buttonColors(containerColor = WhiteAccent, contentColor = ObsidianBackground),
            shape = RoundedCornerShape(14.dp),
            enabled = !isAnalyzing && (bitmap != null || userNoteText.isNotBlank())
        ) {
            if (isAnalyzing) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = ObsidianBackground)
            } else {
                Icon(Icons.Default.AutoAwesome, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ödevi Yapay Zeka ile Çöz", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Optional Upload PHP Settings Header
        Text(
            text = "Sunucuya Görsel Yükle (upload.php)",
            fontSize = 12.sp,
            color = PremiumTextSecondary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .clickable { showServerConfig = !showServerConfig }
                .padding(vertical = 4.dp)
                .testTag("btn_toggle_server_config")
        )

        if (showServerConfig) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = tempServerUrl,
                onValueChange = { tempServerUrl = it },
                label = { Text("upload.php URL adresi") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = WhiteAccent,
                    unfocusedBorderColor = ObsidianBorder
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.setCustomServerUrl(tempServerUrl)
                        ToastHelper.showToast(context, "Sunucu adresi kaydedildi")
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = ObsidianSurface)
                ) {
                    Text("Kaydet")
                }
                Button(
                    onClick = {
                        viewModel.setCustomServerUrl(tempServerUrl)
                        viewModel.uploadToPhpServer()
                    },
                    modifier = Modifier.weight(1.5f),
                    enabled = bitmap != null,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGold, contentColor = ObsidianBackground)
                ) {
                    Icon(Icons.Default.CloudUpload, null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Yükle ve Çöz")
                }
            }
        }

        // Status Loading Message
        if (!uploadMessage.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = uploadMessage!!,
                color = AccentGold,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 14.dp)
            )
        }

        // --- RESULTS DISPLAY ---
        if (solveResult != null) {
            Spacer(modifier = Modifier.height(24.dp))
            Divider(color = ObsidianBorder, thickness = 1.dp)
            Spacer(modifier = Modifier.height(14.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                // Category Tag
                Surface(
                    color = AccentGold.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Text(
                        text = solveResult!!.subject_category,
                        color = AccentGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = "Ayrıştırılan Soru Metni:",
                    color = PremiumTextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = solveResult!!.question,
                    color = PremiumTextPrimary,
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Massive Correct Choice Display
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                    border = BorderStroke(2.dp, AccentGold)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "YAPAY ZEKA CEVABI",
                            color = AccentGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = solveResult!!.correct_option,
                            color = WhiteAccent,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Bu doğru seçenektir.",
                            color = PremiumTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                // Step-by-Step Explanation
                Text(
                    text = "Detaylı Çözüm Açıklaması:",
                    color = PremiumTextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                    border = BorderStroke(1.dp, ObsidianBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Text(
                        text = solveResult!!.explanation,
                        color = PremiumTextPrimary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                // Options Breakdown Analysis (Neden bu cevap doğru? Diğerleri neden yanlış?)
                Text(
                    text = "Seçenek Analizleri (Doğru ve Yanlış Nedenleri):",
                    color = PremiumTextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                for ((option, reason) in solveResult!!.options_breakdown) {
                    val isCorrectChoice = option.trim().uppercase() == solveResult!!.correct_option.trim().uppercase()
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCorrectChoice) ObsidianSurfaceElevated else ObsidianSurface
                        ),
                        border = BorderStroke(
                            1.dp, 
                            if (isCorrectChoice) AccentGold.copy(alpha = 0.5f) else ObsidianBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(if (isCorrectChoice) AccentGold else ObsidianBorder),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = option,
                                    color = if (isCorrectChoice) ObsidianBackground else PremiumTextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = reason,
                                color = if (isCorrectChoice) PremiumTextPrimary else PremiumTextSecondary,
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==================== TAB 1: ADVANCED CHATBOT ====================

@Composable
fun ChatTab(viewModel: OdevViewModel) {
    val context = LocalContext.current
    val sessions by viewModel.chatSessions.collectAsState()
    val activeSessionId by viewModel.currentSessionId.collectAsState()
    val messages by viewModel.chatMessages.collectAsState()
    val isGenerating by viewModel.isChatGenerating.collectAsState()

    var chatTextInput by remember { mutableStateOf("") }
    var showNewSessionDialog by remember { mutableStateOf(false) }
    var newSessionTitleText by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        // Chat Session Bar (Horizon Scroll list)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(ObsidianSurface)
                .border(BorderStroke(1.dp, ObsidianBorder))
                .padding(vertical = 8.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { showNewSessionDialog = true },
                modifier = Modifier
                    .background(WhiteAccent, RoundedCornerShape(8.dp))
                    .size(36.dp)
                    .testTag("btn_new_chat_session")
            ) {
                Icon(Icons.Default.Add, "Yeni Sohbet", tint = ObsidianBackground)
            }
            
            Spacer(modifier = Modifier.width(10.dp))

            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (sessions.isEmpty()) {
                    Text(
                        text = "Henüz sohbet yok. Oluşturun ➡️",
                        color = PremiumTextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }

                sessions.forEach { sess ->
                    val isSelected = activeSessionId == sess.id
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) WhiteAccent else ObsidianBorder)
                            .border(BorderStroke(1.dp, ObsidianBorder), RoundedCornerShape(8.dp))
                            .clickable { viewModel.selectChatSession(sess.id) }
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = sess.title,
                                color = if (isSelected) ObsidianBackground else PremiumTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Kapat",
                                tint = if (isSelected) ObsidianBackground.copy(alpha = 0.6f) else PremiumTextSecondary,
                                modifier = Modifier
                                    .size(14.dp)
                                    .clickable { viewModel.deleteChatSession(sess.id) }
                            )
                        }
                    }
                }
            }
        }

        // Messaging Board
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(ObsidianBackground)
        ) {
            if (activeSessionId == null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = PremiumTextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Gelişmiş Ödev Sorun Asistanı",
                        color = PremiumTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "AI yapay zekamız geçmişinizi hatırlar ve detaylı açıklamalar sunar. Bir sohbet oturumu seçerek veya yeni açarak başlayın.",
                        color = PremiumTextSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showNewSessionDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = WhiteAccent, contentColor = ObsidianBackground),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Yeni Sohbet Oturumu Aç")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(messages) { msg ->
                        val isUser = msg.role == "user"
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
                        ) {
                            Text(
                                text = if (isUser) "Sen" else "Ödev Pro Asistanı",
                                color = PremiumTextSecondary,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                            Surface(
                                color = if (isUser) WhiteAccent else ObsidianSurface,
                                shape = RoundedCornerShape(
                                    topStart = 12.dp,
                                    topEnd = 12.dp,
                                    bottomStart = if (isUser) 12.dp else 2.dp,
                                    bottomEnd = if (isUser) 2.dp else 12.dp
                                ),
                                border = if (isUser) null else BorderStroke(1.dp, ObsidianBorder)
                            ) {
                                Text(
                                    text = msg.text,
                                    color = if (isUser) ObsidianBackground else PremiumTextPrimary,
                                    fontSize = 13.sp,
                                    lineHeight = 17.sp,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }

                    if (isGenerating) {
                        item {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = AccentGold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Asistan yazıyor...", color = PremiumTextSecondary, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // Bottom input controls
        if (activeSessionId != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ObsidianSurface)
                    .border(BorderStroke(1.dp, ObsidianBorder))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = chatTextInput,
                    onValueChange = { chatTextInput = it },
                    placeholder = { Text("Sorunuzu buraya yazın...", fontSize = 13.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_text_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = PremiumTextPrimary,
                        unfocusedTextColor = PremiumTextPrimary,
                        focusedBorderColor = WhiteAccent,
                        unfocusedBorderColor = ObsidianBorder
                    ),
                    maxLines = 3
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (chatTextInput.isNotBlank()) {
                            viewModel.sendChatMessage(chatTextInput)
                            chatTextInput = ""
                        }
                    },
                    modifier = Modifier
                        .background(WhiteAccent, CircleShape)
                        .size(46.dp)
                        .testTag("btn_send_chat")
                ) {
                    Icon(Icons.Default.Send, "Gönder", tint = ObsidianBackground)
                }
            }
        }
    }

    // New Session Dialog
    if (showNewSessionDialog) {
        AlertDialog(
            onDismissRequest = { showNewSessionDialog = false },
            containerColor = ObsidianSurface,
            title = { Text("Yeni Sohbet Konusu", color = PremiumTextPrimary) },
            text = {
                Column {
                    Text("Sohbet oturumu için bir başlık girin:", color = PremiumTextSecondary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newSessionTitleText,
                        onValueChange = { newSessionTitleText = it },
                        placeholder = { Text("Örn: Matematik Denklem Ödevim") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = WhiteAccent,
                            unfocusedBorderColor = ObsidianBorder
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val title = newSessionTitleText.ifBlank { "Yeni Sohbet" }
                        viewModel.createNewChatSession(title)
                        newSessionTitleText = ""
                        showNewSessionDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WhiteAccent, contentColor = ObsidianBackground)
                ) {
                    Text("Oluştur")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewSessionDialog = false }) {
                    Text("İptal", color = PremiumTextSecondary)
                }
            }
        )
    }
}

// ==================== TAB 2: PERSOANALIZED AGENDA ====================

@Composable
fun AgendaTab(viewModel: OdevViewModel) {
    val context = LocalContext.current
    val items by viewModel.agendaItems.collectAsState()
    
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var taskTitle by remember { mutableStateOf("") }
    var taskDesc by remember { mutableStateOf("") }
    var taskCategory by remember { mutableStateOf("Matematik") }
    var selectedDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }

    val categoriesList = listOf("Matematik", "Türkçe", "Fen Bilgisi", "Sosyal Bilgiler", "İngilizce", "Diğer")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Öğrenci Çalışma Planı", color = PremiumTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("Ders planlarınızı organize edin ve takip edin.", color = PremiumTextSecondary, fontSize = 12.sp)
            }
            Button(
                onClick = { showAddTaskDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = WhiteAccent, contentColor = ObsidianBackground),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Plan Ekle")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.EventNote, null, modifier = Modifier.size(48.dp), tint = PremiumTextSecondary)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Çalışma planı bulunmuyor.", color = PremiumTextSecondary, fontSize = 13.sp)
                    Text("Ders çalışmaya teşvik için plan ekleyin!", color = PremiumTextSecondary, fontSize = 11.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(items) { item ->
                    val dateFormatted = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(item.dateMillis))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                        border = BorderStroke(1.dp, if (item.isCompleted) ObsidianBorder else AccentGold.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = item.isCompleted,
                                onCheckedChange = { viewModel.toggleAgendaCompletion(item, it) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = AccentGold,
                                    uncheckedColor = PremiumTextSecondary,
                                    checkmarkColor = ObsidianBackground
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // small category tag
                                    Surface(
                                        color = ObsidianBorder,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = item.category,
                                            color = AccentGold,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = item.title,
                                        color = if (item.isCompleted) PremiumTextSecondary else PremiumTextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        style = if (item.isCompleted) MaterialTheme.typography.bodyMedium.copy(
                                            textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                        ) else MaterialTheme.typography.bodyMedium
                                    )
                                }
                                if (item.description.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = item.description,
                                        color = PremiumTextSecondary,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Son Gün: $dateFormatted",
                                    color = AccentGold,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            IconButton(onClick = { viewModel.deleteAgendaItem(item.id) }) {
                                Icon(Icons.Outlined.Delete, "Sil", tint = CustomColors.mutedRed)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddTaskDialog) {
        val calendar = Calendar.getInstance()
        var dateText by remember { mutableStateOf("Tarih Seç") }

        AlertDialog(
            onDismissRequest = { showAddTaskDialog = false },
            containerColor = ObsidianSurface,
            title = { Text("Çalışma Planı Ekle", color = PremiumTextPrimary) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = taskTitle,
                        onValueChange = { taskTitle = it },
                        label = { Text("Plan Başlığı (Örn: Fen Bilgisi Soru Çözümü)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = WhiteAccent, unfocusedBorderColor = ObsidianBorder)
                    )
                    OutlinedTextField(
                        value = taskDesc,
                        onValueChange = { taskDesc = it },
                        label = { Text("Detaylı Açıklama / Notlar") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = WhiteAccent, unfocusedBorderColor = ObsidianBorder)
                    )
                    // Category Selection
                    Text("Ders Kategorisi:", color = PremiumTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categoriesList.forEach { cat ->
                            val isSel = cat == taskCategory
                            Surface(
                                modifier = Modifier.clickable { taskCategory = cat },
                                color = if (isSel) WhiteAccent else ObsidianBorder,
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, ObsidianBorder)
                            ) {
                                Text(
                                    text = cat,
                                    color = if (isSel) ObsidianBackground else PremiumTextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    // Simple Native DatePicker dialog
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    calendar.set(Calendar.YEAR, year)
                                    calendar.set(Calendar.MONTH, month)
                                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                    selectedDateMillis = calendar.timeInMillis
                                    dateText = "$dayOfMonth/${month + 1}/$year"
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ObsidianBorder)
                    ) {
                        Icon(Icons.Default.DateRange, null, tint = AccentGold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(dateText, color = PremiumTextPrimary)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (taskTitle.isNotBlank()) {
                            viewModel.addAgendaItem(taskTitle, taskDesc, selectedDateMillis, taskCategory)
                            taskTitle = ""
                            taskDesc = ""
                            showAddTaskDialog = false
                        } else {
                            ToastHelper.showToast(context, "Başlık boş olamaz")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WhiteAccent, contentColor = ObsidianBackground)
                ) {
                    Text("Plana Ekle")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTaskDialog = false }) {
                    Text("Geri", color = PremiumTextSecondary)
                }
            }
        )
    }
}

// ==================== TAB 3: PROGRESS & REPORTS ====================

@Composable
fun ReportsTab(viewModel: OdevViewModel) {
    val logs by viewModel.progressLogs.collectAsState()
    val isGenRep by viewModel.isGeneratingRecommendation.collectAsState()
    val recommendation by viewModel.aiRecommendation.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Yapay Zeka Eğitim Analizi & İlerleme Raporu",
            color = PremiumTextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "Eğitim asistanınız tarafından toplanan çözümlerinizin detaylı analizi.",
            color = PremiumTextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Large Stats Container
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
            border = BorderStroke(1.dp, ObsidianBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "TOPLAM ANALİZ EDİLEN ÖDEV",
                    color = PremiumTextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "${logs.size} Adet",
                    color = AccentGold,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Çözüm Başarısı", color = PremiumTextSecondary, fontSize = 11.sp)
                        Text("%100", color = WhiteAccent, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Ders Sayısı", color = PremiumTextSecondary, fontSize = 11.sp)
                        val catCount = logs.groupBy { it.category }.size
                        Text("$catCount Ders", color = WhiteAccent, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Category distribution breakdown details
        Text(
            text = "Derslere Göre Soru Dağılımı",
            color = PremiumTextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (logs.isEmpty()) {
            Text(
                text = "Henüz grafik verisi bulunmuyor. Ödev çözümü yapınca burada ders dağılımları yer alacaktır.",
                color = PremiumTextSecondary,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        } else {
            val categorizedGroup = logs.groupBy { it.category }
            categorizedGroup.forEach { (cat, list) ->
                val ratio = list.size.toFloat() / logs.size.toFloat()
                Column(modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = cat, color = PremiumTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = "${list.size} Soru", color = AccentGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(ObsidianBorder)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(ratio)
                                .clip(CircleShape)
                                .background(Brush.horizontalGradient(listOf(AccentGold, WhiteAccent)))
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // AI Professional Education Coach Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
            border = BorderStroke(1.dp, AccentGold.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.School, null, tint = AccentGold, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Yapay Zeka Eğitim Danışmanı",
                        color = PremiumTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Ders dağılımınızı ve çözdüğünüz ödevleri analiz ederek kişisel çalışma tavsiyeleri hazırlayan yapay zeka eğitim mühendisi raporu alın.",
                    color = PremiumTextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = { viewModel.generateAiStudyReport() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = WhiteAccent, contentColor = ObsidianBackground),
                    enabled = !isGenRep && logs.isNotEmpty()
                ) {
                    if (isGenRep) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = ObsidianBackground)
                    } else {
                        Icon(Icons.Default.QueryStats, null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Kişisel Tavsiye Raporu Oluştur")
                    }
                }

                if (recommendation.isNotBlank()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = recommendation,
                        color = PremiumTextPrimary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        modifier = Modifier
                            .background(ObsidianBackground)
                            .border(1.dp, ObsidianBorder, RoundedCornerShape(8.dp))
                            .padding(11.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // History list logs
        if (logs.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Çözüm Geçmişi",
                    color = PremiumTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { viewModel.clearAllProgressHistory() }) {
                    Text("Tümünü Temizle", color = CustomColors.mutedRed, fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            logs.forEach { log ->
                val logDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(log.timestamp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                    border = BorderStroke(1.dp, ObsidianBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = ObsidianBackground,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = log.category,
                                    color = AccentGold,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            IconButton(onClick = { viewModel.deleteProgressLogById(log.id) }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Delete, "Sil", tint = PremiumTextSecondary, modifier = Modifier.size(16.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Soru/Özet: ${log.questionText}",
                            color = PremiumTextPrimary,
                            fontSize = 12.sp,
                            maxLines = 2,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Öğretmen Çözümü: ${log.explanation}",
                            color = PremiumTextSecondary,
                            fontSize = 11.sp,
                            maxLines = 3,
                            lineHeight = 15.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Doğru Cevap: ${log.correctAnswer ?: "-"}",
                                color = AccentGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = logDate,
                                color = PremiumTextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==================== SYSTEM HELPERS ====================

object ToastHelper {
    fun showToast(context: android.content.Context, message: String) {
        android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_LONG).show()
    }
}

object CustomColors {
    val mutedRed = Color(0xFFFF5252)
}
