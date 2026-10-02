package com.rushi.downly

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Bg = Color(0xFF101010)
private val Panel = Color(0xFF1B1B1B)
private val Muted = Color(0xFF9B9B9B)
private val White = Color(0xFFF5F5F5)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { DownlyScreen(::startDownload) }
    }

    private fun startDownload(rawUrl: String, format: String) {
        val url = rawUrl.trim()
        val parsed = runCatching { Uri.parse(url) }.getOrNull()
        val host = parsed?.host?.lowercase()
        val allowed = host == "youtube.com" || host == "www.youtube.com" ||
            host == "m.youtube.com" || host == "youtu.be" ||
            host == "music.youtube.com" || host == "www.youtube-nocookie.com"
        if (parsed?.scheme != "https" || !allowed) {
            Toast.makeText(this, "Enter a valid YouTube HTTPS link.", Toast.LENGTH_LONG).show()
            return
        }

        // URL is passed as a separate argument, not interpolated into a shell command.
        val args = if (format == "MP3") {
            arrayOf("-x", "--audio-format", "mp3", "-o",
                "/storage/emulated/0/Download/Downly/%(title)s.%(ext)s", url)
        } else {
            arrayOf("-f", "bv*+ba/b", "--merge-output-format", "mp4", "-o",
                "/storage/emulated/0/Download/Downly/%(title)s.%(ext)s", url)
        }

        try {
            val intent = Intent("com.termux.RUN_COMMAND").apply {
                component = ComponentName("com.termux", "com.termux.app.RunCommandService")
                putExtra("com.termux.RUN_COMMAND_PATH", "/data/data/com.termux/files/usr/bin/yt-dlp")
                putExtra("com.termux.RUN_COMMAND_ARGUMENTS", args)
                putExtra("com.termux.RUN_COMMAND_WORKDIR", "/data/data/com.termux/files/home")
                putExtra("com.termux.RUN_COMMAND_BACKGROUND", true)
                putExtra("com.termux.RUN_COMMAND_LABEL", "Downly download")
                putExtra("com.termux.RUN_COMMAND_DESCRIPTION", "Download started by Downly")
            }
            startService(intent)
            Toast.makeText(this, "Sent to Termux. Check Termux notifications.", Toast.LENGTH_LONG).show()
        } catch (e: SecurityException) {
            Toast.makeText(this, "Permission missing. Allow Downly to run commands in Termux.", Toast.LENGTH_LONG).show()
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "Termux is not installed or is incompatible.", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Could not start download: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}

@Composable
private fun DownlyScreen(onDownload: (String, String) -> Unit) {
    var url by remember { mutableStateOf("") }
    var format by remember { mutableStateOf("MP3") }

    MaterialTheme(colorScheme = darkColorScheme(
        background = Bg, surface = Panel, primary = White,
        onBackground = White, onSurface = White, onPrimary = Bg
    )) {
        Surface(modifier = Modifier.fillMaxSize(), color = Bg) {
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 30.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text("DOWNLY", color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 3.sp)
                Spacer(Modifier.height(24.dp))
                Text("Download simply.", color = White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("Paste a YouTube link. Choose your format.", color = Muted, fontSize = 14.sp)
                Spacer(Modifier.height(30.dp))
                OutlinedTextField(
                    value = url, onValueChange = { url = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Paste YouTube link", color = Muted) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = White, unfocusedTextColor = White,
                        focusedBorderColor = White, unfocusedBorderColor = Color(0xFF444444),
                        cursorColor = White
                    )
                )
                Spacer(Modifier.height(24.dp))
                Text("FORMAT", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FormatCard("MP3", "Audio", format == "MP3", Modifier.weight(1f)) { format = "MP3" }
                    FormatCard("MP4", "Video", format == "MP4", Modifier.weight(1f)) { format = "MP4" }
                }
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = { onDownload(url, format) },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = White, contentColor = Bg)
                ) {
                    Text("Download", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Spacer(Modifier.height(16.dp))
                Text("Downloads are handled by Termux and saved to Download/Downly.", color = Muted, fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
                Text("Only download content you have permission to save.", color = Muted, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun FormatCard(title: String, subtitle: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier.height(100.dp)
            .background(if (selected) Color(0xFF292929) else Panel, RoundedCornerShape(14.dp))
            .border(1.dp, if (selected) White else Color(0xFF383838), RoundedCornerShape(14.dp))
            .clickable { onClick() }.padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(title, color = White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(subtitle, color = Muted, fontSize = 12.sp)
    }
}
