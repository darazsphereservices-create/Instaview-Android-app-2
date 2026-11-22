package com.example.instaview

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

private val Pink = Color(0xFFE1306C)
private val Purple = Color(0xFF833AB4)
private val Ink = Color(0xFF17131C)
private val Muted = Color(0xFF77717E)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { InstaViewApp(this) }
    }
}

@Composable
fun InstaViewApp(context: Context) {
    var profileUrl by remember { mutableStateOf("") }
    var mediaUrl by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Paste a public Instagram profile link to get started.") }

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Pink,
            secondary = Purple,
            background = Color(0xFFFAF8FB),
            surface = Color.White
        )
    ) {
        Scaffold(containerColor = Color(0xFFFAF8FB)) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Brush.linearGradient(listOf(Purple, Pink, Color(0xFFFFA44B))), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("InstaView", fontSize = 23.sp, fontWeight = FontWeight.ExtraBold, color = Ink)
                        Text("Public profile viewer", fontSize = 12.sp, color = Muted)
                    }
                    Spacer(Modifier.weight(1f))
                    Surface(color = Color(0xFFFCE8F0), shape = RoundedCornerShape(50)) {
                        Text("BETA", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = Pink, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(22.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Explore a profile", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = Ink)
                        Text("Paste the link to a public Instagram profile.", color = Muted, fontSize = 13.sp)
                        OutlinedTextField(
                            value = profileUrl,
                            onValueChange = { profileUrl = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("https://www.instagram.com/username/") },
                            leadingIcon = { Icon(Icons.Default.Person, null) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp)
                        )
                        Button(
                            onClick = {
                                val clean = profileUrl.trim().let {
                                    if (it.startsWith("http://") || it.startsWith("https://")) it else "https://$it"
                                }
                                val uri = Uri.parse(clean)
                                val validHost = uri.host?.lowercase() in listOf("instagram.com", "www.instagram.com")
                                if (validHost && uri.pathSegments.isNotEmpty()) {
                                    try {
                                    (context as? MainActivity)?.openProfile(uri)
                                    status = "Profile opened inside InstaView. Instagram controls what can be viewed."
                                    } catch (_: Exception) {
                                        status = "Could not open that link. Check the URL and try again."
                                    }
                                } else {
                                    status = "Enter a valid public Instagram profile URL."
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Pink)
                        ) {
                            Icon(Icons.Default.Link, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Open profile", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = Ink),
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("HD video downloader", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "Download a direct video file URL that you own or have permission to save. Instagram post links are not direct video files.",
                            color = Color(0xFFD2CBD8), fontSize = 12.sp, lineHeight = 18.sp
                        )
                        OutlinedTextField(
                            value = mediaUrl,
                            onValueChange = { mediaUrl = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("https://example.com/video.mp4") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Pink,
                                unfocusedBorderColor = Color(0xFF5C5363),
                                focusedPlaceholderColor = Color(0xFFB9B0C0),
                                unfocusedPlaceholderColor = Color(0xFFB9B0C0),
                                cursorColor = Pink
                            )
                        )
                        Button(
                            onClick = { startDownload(context, mediaUrl.trim()) { status = it } },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Pink)
                        ) {
                            Icon(Icons.Default.Download, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Download video", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Surface(color = Color(0xFFF1ECF5), shape = RoundedCornerShape(16.dp)) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                        Text("i", color = Purple, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "Current build: profile links open through Instagram's public web experience. Automatic profile scraping, Reels extraction and watermark removal are not implemented.",
                            color = Color(0xFF51465B), fontSize = 12.sp, lineHeight = 17.sp
                        )
                    }
                }

                Text(status, color = Muted, fontSize = 12.sp)
                Text("InstaView • Version 0.1.0", color = Color(0xFFAAA3AE), fontSize = 11.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
private fun MainActivity.openProfile(uri: Uri) {
    setContent {
        MaterialTheme(colorScheme = lightColorScheme(primary = Pink)) {
            Scaffold(topBar = {
                TopAppBar(title = { Text("Instagram profile") }, navigationIcon = {
                    TextButton(onClick = { setContent { InstaViewApp(this@openProfile) } }) { Text("Back") }
                })
            }) { padding ->
                AndroidView(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    factory = { viewContext -> WebView(viewContext).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.javaScriptCanOpenWindowsAutomatically = false
                        settings.setSupportMultipleWindows(false)
                        webViewClient = WebViewClient()
                        loadUrl(uri.toString())
                    } }
                )
            }
        }
    }
}

private fun startDownload(context: Context, url: String, report: (String) -> Unit) {
    val uri = runCatching { Uri.parse(url) }.getOrNull()
    if (uri == null || uri.scheme != "https" || uri.host.isNullOrBlank() ||
        !(url.lowercase().substringBefore("?").endsWith(".mp4") ||
          url.lowercase().substringBefore("?").endsWith(".m4v") ||
          url.lowercase().substringBefore("?").endsWith(".webm"))) {
        report("Enter a valid HTTPS direct video URL ending in .mp4, .m4v or .webm.")
        return
    }
    try {
        val request = DownloadManager.Request(uri)
            .setTitle("InstaView video")
            .setDescription("Downloading authorized video")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "InstaView_${System.currentTimeMillis()}.mp4")
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(false)
        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        manager.enqueue(request)
        report("Download started. Check your Downloads folder.")
        Toast.makeText(context, "Download started", Toast.LENGTH_SHORT).show()
    } catch (_: Exception) {
        report("Download could not start. Check the URL and your connection.")
    }
}
