package com.example.instaview

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import org.json.JSONObject
import java.io.File
import java.util.concurrent.Executors

private val Pink = Color(0xFFE1306C)
private val Purple = Color(0xFF833AB4)
private val Ink = Color(0xFF17131C)
private val Worker = Executors.newSingleThreadExecutor()

data class Reel(val id: String, val title: String, val url: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { InstaViewApp() }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}

@Composable
private fun InstaViewApp() {
    val context = LocalContext.current
    var profile by remember { mutableStateOf("") }
    var reels by remember { mutableStateOf(emptyList<Reel>()) }
    var selected by remember { mutableStateOf<Reel?>(null) }
    var streamUrl by remember { mutableStateOf<String?>(null) }
    var status by remember { mutableStateOf("Enter a public Instagram username or profile URL.") }
    var busy by remember { mutableStateOf(false) }

    MaterialTheme(colorScheme = lightColorScheme(primary = Pink, secondary = Purple)) {
        Scaffold(containerColor = Color(0xFFFAF8FB)) { insets ->
            Column(Modifier.fillMaxSize().padding(insets).padding(horizontal = 16.dp, vertical = 12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("InstaView", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = Ink)
                    Spacer(Modifier.weight(1f))
                    Text("OPEN SOURCE", color = Purple, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
                Text("Public Reels viewer • authorized downloads", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = profile, onValueChange = { profile = it },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    label = { Text("Instagram username or profile URL") },
                    placeholder = { Text("username or instagram.com/username") }
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        enabled = !busy,
                        onClick = {
                            val username = normalizeProfile(profile)
                            if (username == null) {
                                status = "Enter a valid instagram.com public profile or username."
                            } else {
                                busy = true
                                status = "Finding public Reels…"
                                reels = emptyList()
                                selected = null
                                streamUrl = null
                                Worker.execute {
                                    try {
                                        val url = "https://www.instagram.com/$username/reels/"
                                        val request = YoutubeDLRequest(url).apply {
                                            addOption("--flat-playlist")
                                            addOption("--dump-single-json")
                                            addOption("--playlist-end", "20")
                                            addOption("--no-warnings")
                                        }
                                        val output = YoutubeDL.getInstance().execute(request).out
                                        val json = JSONObject(output)
                                        val entries = json.optJSONArray("entries")
                                        val found = mutableListOf<Reel>()
                                        if (entries != null) {
                                            for (i in 0 until entries.length()) {
                                                val item = entries.optJSONObject(i) ?: continue
                                                val id = item.optString("id").takeIf { it.isNotBlank() } ?: continue
                                                val page = item.optString("webpage_url").takeIf { it.startsWith("http") }
                                                    ?: item.optString("url").takeIf { it.startsWith("http") }
                                                    ?: "https://www.instagram.com/reel/$id/"
                                                found += Reel(id, item.optString("title").ifBlank { "Instagram Reel" }, page)
                                            }
                                        }
                                        (context as MainActivity).runOnUiThread {
                                            reels = found
                                            status = if (found.isEmpty()) "No public Reels were returned. Instagram may require login or may have blocked extraction." else "Found ${found.size} public Reel(s). Select Play or Download."
                                            busy = false
                                        }
                                    } catch (e: Exception) {
                                        (context as MainActivity).runOnUiThread {
                                            status = "Could not load Reels: ${e.message ?: "Instagram did not provide public data."}"
                                            busy = false
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) { Icon(Icons.Default.Link, null); Spacer(Modifier.width(6.dp)); Text("Load Reels") }
                    OutlinedButton(onClick = {
                        val username = normalizeProfile(profile)
                        if (username != null) openInstagram(context, Uri.parse("https://www.instagram.com/$username/"))
                        else status = "Enter a valid Instagram profile first."
                    }) { Text("Open profile") }
                }
                if (busy) LinearProgressIndicator(Modifier.fillMaxWidth().padding(top = 8.dp))
                Text(status, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray, modifier = Modifier.padding(vertical = 10.dp))
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(reels, key = { it.id }) { reel ->
                        Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                            Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(reel.title, fontWeight = FontWeight.Bold)
                                Text("Reel ID: ${reel.id}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            selected = reel
                                            streamUrl = null
                                            status = "Resolving playable video…"
                                            Worker.execute {
                                                try {
                                                    val info = YoutubeDL.getInstance().getInfo(reel.url)
                                                    val direct = info.url
                                                    (context as MainActivity).runOnUiThread {
                                                        streamUrl = direct
                                                        status = if (direct.isNullOrBlank()) "No playable stream was provided." else "Playing selected Reel."
                                                    }
                                                } catch (e: Exception) {
                                                    (context as MainActivity).runOnUiThread { status = "Playback unavailable: ${e.message ?: "Instagram blocked this Reel."}" }
                                                }
                                            }
                                        }
                                    ) { Icon(Icons.Default.PlayArrow, null); Text("Play") }
                                    OutlinedButton(
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            status = "Downloading authorized Reel…"
                                            Worker.execute {
                                                try {
                                                    val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "InstaView")
                                                    if (!dir.exists()) dir.mkdirs()
                                                    val request = YoutubeDLRequest(reel.url).apply {
                                                        addOption("-f", "bestvideo[ext=mp4]+bestaudio[ext=m4a]/best[ext=mp4]/best")
                                                        addOption("--merge-output-format", "mp4")
                                                        addOption("--no-part")
                                                        addOption("-o", File(dir, "%(title).80s-%(id)s.%(ext)s").absolutePath)
                                                    }
                                                    YoutubeDL.getInstance().execute(request, "reel-${reel.id}")
                                                    (context as MainActivity).runOnUiThread { status = "Download saved in Downloads/InstaView." }
                                                } catch (e: Exception) {
                                                    (context as MainActivity).runOnUiThread { status = "Download failed: ${e.message ?: "Instagram blocked this Reel."}" }
                                                }
                                            }
                                        }
                                    ) { Icon(Icons.Default.Download, null); Text("Download") }
                                }
                                if (selected?.id == reel.id && !streamUrl.isNullOrBlank()) {
                                    AndroidView(
                                        modifier = Modifier.fillMaxWidth().height(360.dp),
                                        factory = { ctx ->
                                            VideoView(ctx).apply {
                                                setVideoURI(Uri.parse(streamUrl))
                                                setOnPreparedListener { it.isLooping = true; start() }
                                            }
                                        },
                                        update = { view ->
                                            if (view.tag != streamUrl) {
                                                view.tag = streamUrl
                                                view.setVideoURI(Uri.parse(streamUrl))
                                                view.setOnPreparedListener { it.isLooping = true; it.start() }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                Text("Only download videos you own or have permission to save. Public access is not permission by itself. Instagram may block extraction or require login; InstaView does not bypass those restrictions.", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        }
    }
}

private fun normalizeProfile(input: String): String? {
    val value = input.trim().removePrefix("@")
    if (value.isBlank()) return null
    val uri = runCatching { Uri.parse(if (value.startsWith("http")) value else "https://www.instagram.com/$value/") }.getOrNull() ?: return null
    if (uri.scheme != "https" || uri.host?.lowercase() !in listOf("instagram.com", "www.instagram.com")) return null
    val username = uri.pathSegments.firstOrNull() ?: return null
    if (username in listOf("reel", "reels", "p", "stories", "accounts", "explore")) return null
    return username.takeIf { it.matches(Regex("[A-Za-z0-9._]{1,30}")) }
}

private fun openInstagram(context: Context, uri: Uri) {
    try {
        CustomTabsIntent.Builder().setShowTitle(true).build().launchUrl(context, uri)
    } catch (_: Exception) {
        context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, uri))
    }
}
