// MainActivity.kt
package com.example.datasave

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    FileManagerScreen()
                }
            }
        }
    }
}

@Composable
fun FileManagerScreen() {
    val context = LocalContext.current as Activity // 转换为Activity上下文
    var fileName by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var permissionGranted by remember { mutableStateOf(false) }

    // 权限申请逻辑
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionGranted = isGranted
    }

    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = fileName,
            onValueChange = { fileName = it },
            label = { Text("文件名") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            label = { Text("内容") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            singleLine = false,
            modifier = Modifier.height(200.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            // 保存到内部存储
            Button(
                onClick = {
                    if (saveToInternalStorage(context, fileName, content)) {
                        showToast(context, "内部存储保存成功")
                    } else {
                        showToast(context, "请填写文件名和内容")
                    }
                }
            ) {
                Text("保存到内部存储")
            }

            // 追加到内部存储
            Button(
                onClick = {
                    if (appendToInternalStorage(context, fileName, content)) {
                        showToast(context, "追加成功")
                    } else {
                        showToast(context, "请填写文件名和内容")
                    }
                }
            ) {
                Text("追加到内部存储")
            }

            // 保存到SDCard（处理权限）
            Button(
                onClick = {
                    if (ContextCompat.checkSelfPermission(
                            context,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        requestPermissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    } else {
                        if (saveToExternalStorage(context, fileName, content)) {
                            showToast(context, "SDCard保存成功")
                        } else {
                            showToast(context, "请填写文件名和内容")
                        }
                    }
                }
            ) {
                Text("保存到SDCard")
            }
        }
    }
}

// 保存到内部存储（返回是否成功）
private fun saveToInternalStorage(context: Activity, fileName: String, content: String): Boolean {
    if (fileName.isEmpty() || content.isEmpty()) return false
    return try {
        context.filesDir.resolve(fileName).writeText(content)
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

// 追加到内部存储
private fun appendToInternalStorage(context: Activity, fileName: String, content: String): Boolean {
    if (fileName.isEmpty() || content.isEmpty()) return false
    return try {
        context.filesDir.resolve(fileName).appendText("\n$content")
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

// 保存到外部存储
private fun saveToExternalStorage(context: Activity, fileName: String, content: String): Boolean {
    if (fileName.isEmpty() || content.isEmpty()) return false
    return try {
        val externalDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: context.filesDir
        externalDir.resolve(fileName).writeText(content)
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

// 使用原生Toast
private fun showToast(context: Activity, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}