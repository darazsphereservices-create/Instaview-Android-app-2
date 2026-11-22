package com.example.instaview

import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File

private val Pink = Color(0xFFE1306C)
private val Purple = Color(0xFF833AB4)
private val Ink = Color(0xFF17131C)
private val Muted = Color(0xFF77717E)

data class ReelItem(val id: String, val title: String, val url: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { InstaViewApp() }
    }
}

@Composable
fun InstaViewApp() {
    val scope = rememberCoroutineScope()
    var profile by remember { mutableStateOf("") }
    var reels by remember { mutableStateOf<List<ReelItem>>(emptyList()) }
    var busy by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("Enter a public Instagram username or profile link.") }

    MaterialTheme(colorScheme = lightColorScheme(primary = Pink, secondary = Purple)) {
        Scaffold(containerColor = Color(0xFFFAF8FB)) { padding ->
            Column(
                Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(44.dp).background(Brush.linearGradient(listOf(Purple, Pink)), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.PlayArrow, null, tint = Color.White)
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("InstaView", fontSize = 23.sp, fontWeight = FontWeight.ExtraBold, color = Ink)
                        Text("Public Reels viewer", fontSize = 12.sp, color = Muted)
                    }
                }
                OutlinedTextField(
                    value = profile, onValueChange = { profile = it },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    placeholder = { Text("Instagram username or profile URL") },
                    shape = RoundedCornerShape(14.dp)
                )
                Button(
                    enabled = !busy,
                    onClick = {
                        val target = normalizeProfile(profile)
                        if (target == null) status = "Enter a valid instagram.com username or profile URL."
                        else {
                            busy = true; reels = emptyList(); status = "Checking public Reels…"
                            scope.launch {
                                try {
                                    reels = withContext(Dispatchers.IO) { loadPublicReels(target) }
                                    status = if (reels.isEmpty()) "No public Reels returned. Instagram may require login or block extraction."
                                    else "Found ${reels.size} public Reel links. Tap Play or Download."
                                } catch (e: Exception) { status = "Could not load: ${friendlyError(e)}" }
                                finally { busy = false }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Pink)
                ) {
                    if (busy) CircularProgressIndicator(Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                    else Text("Load public Reels", fontWeight = FontWeight.Bold)
                }
                Text(status, color = Muted, fontSize = 12.sp)
                if (reels.isEmpty()) {
                    Surface(color = Color(0xFFF1ECF5), shape = RoundedCornerShape(16.dp)) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                            Text("Open-source Reels engine", color = Ink, fontWeight = FontWeight.Bold)
                            Text("Uses yt-dlp Android and FFmpeg to try listing public Reels, resolving playable streams and downloading authorized videos. InstaView does not add a watermark.", color = Color(0xFF51465B), fontSize = 12.sp, lineHeight = 18.sp)
                            Text("Only save videos you own or have permission to download. Instagram may block public extraction or require login; this app does not bypass those restrictions.", color = Color(0xFF51465B), fontSize = 12.sp, lineHeight = 18.sp)
                        }
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
                        items(reels, key = { it.id }) { ReelCard(it) { status = it } }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReelCard(reel: ReelItem, onStatus: (String) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var videoUrl by remember(reel.id) { mutableStateOf<String?>(null) }
    var loading by remember(reel.id) { mutableStateOf(false) }
    var playing by remember(reel.id) { mutableStateOf(false) }
    var downloading by remember(reel.id) { mutableStateOf(false) }
    Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Text(reel.title, color = Ink, fontWeight = FontWeight.Bold)
            if (videoUrl != null && playing) {
                AndroidView(
                    factory = { ctx -> VideoView(ctx).apply {
                        setVideoURI(Uri.parse(videoUrl))
                        setOnPreparedListener { it.isLooping = true; start() }
                    } },
                    modifier = Modifier.fillMaxWidth().height(420.dp)
                )
            } else Text("Public Reel link ready. Resolve it to preview or download.", color = Muted, fontSize = 12.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    enabled = !loading && !downloading,
                    onClick = {
                        loading = true
                        scope.launch {
                            try {
                                videoUrl = withContext(Dispatchers.IO) {
                                    YoutubeDL.getInstance().getInfo(reel.url).url
                                        ?: throw IllegalStateException("No playable video stream returned.")
                                }
                                playing = true; onStatus("Reel stream resolved.")
                            } catch (e: Exception) { onStatus("Preview unavailable: ${friendlyError(e)}") }
                            finally { loading = false }
                        }
                    }, modifier = Modifier.weight(1f)
                ) {
                    if (loading) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                    else { Icon(Icons.Default.PlayArrow, null); Spacer(Modifier.width(4.dp)); Text("Play") }
                }
                Button(
                    enabled = !downloading && !loading,
                    onClick = {
                        downloading = true
                        scope.launch {
                            try {
                                val file = withContext(Dispatchers.IO) { downloadAuthorized(context, reel.url) }
                                onStatus("Saved: ${file.name}")
                            } catch (e: Exception) { onStatus("Download failed: ${friendlyError(e)}") }
                            finally { downloading = false }
                        }
                    }, modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Pink)
                ) {
                    if (downloading) CircularProgressIndicator(Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    else { Icon(Icons.Default.Download, null); Spacer(Modifier.width(4.dp)); Text("Download") }
                }
            }
            Text("Only download with the owner's permission.", color = Muted, fontSize = 10.sp)
        }
    }
}

private fun normalizeProfile(input: String): String? {
    val value = input.trim().removeSuffix("/")
    if (value.isBlank()) return null
    val uri = runCatching { Uri.parse(if (value.startsWith("http")) value else "https://www.instagram.com/$value") }.getOrNull() ?: return null
    if (uri.scheme != "https" || uri.host?.lowercase() !in listOf("instagram.com", "www.instagram.com")) return null
    val user = uri.pathSegments.firstOrNull() ?: return null
    if (user in listOf("accounts", "explore", "reels", "p", "reel", "stories", "direct")) return null
    if (!user.matches(Regex("[A-Za-z0-9._]{1,30}"))) return null
    return "https://www.instagram.com/$user/"
}

private fun loadPublicReels(url: String): List<ReelItem> {
    val request = YoutubeDLRequest(url)
    request.addOption("--flat-playlist")
    request.addOption("--dump-single-json")
    request.addOption("--playlist-end", "30")
    request.addOption("--no-warnings")
    request.addOption("--ignore-errors")
    val root = JSONObject(YoutubeDL.getInstance().execute(request).out)
    val entries = root.optJSONArray("entries") ?: return emptyList()
    val result = mutableListOf<ReelItem>()
    for (i in 0 until entries.length()) {
        val item = entries.optJSONObject(i) ?: continue
        val reelUrl = item.optString("url").takeIf { it.startsWith("http") }
            ?: item.optString("webpage_url").takeIf { it.startsWith("http") }
            ?: item.optString("id").takeIf { it.isNotBlank() }?.let { "https://www.instagram.com/reel/$it/" }
            ?: continue
        result += ReelItem(item.optString("id", reelUrl), item.optString("title").ifBlank { "Reel ${i + 1}" }, reelUrl)
    }
    return result
}

private fun downloadAuthorized(context: android.content.Context, url: String): File {
    val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir, "InstaView").apply { mkdirs() }
    val request = YoutubeDLRequest(url)
    request.addOption("--no-playlist")
    request.addOption("--no-mtime")
    request.addOption("-f", "best[ext=mp4]/best")
    request.addOption("-o", File(dir, "%(title).100B-%(id)s.%(ext)s").absolutePath)
    YoutubeDL.getInstance().execute(request)
    return dir.listFiles()?.filter { it.isFile }?.maxByOrNull { it.lastModified() }
        ?: throw IllegalStateException("No downloaded file was created.")
}

private fun friendlyError(error: Throwable): String =
    error.message.orEmpty().lineSequence().firstOrNull().orEmpty().take(160).ifBlank { "Instagram did not provide accessible media." }
