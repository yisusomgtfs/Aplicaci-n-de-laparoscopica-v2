package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint as AndroidPaint
import android.util.Base64
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import java.io.ByteArrayOutputStream

data class PathPoint(val offset: Offset, val isStart: Boolean)

@Composable
fun SignatureCanvas(
    modifier: Modifier = Modifier,
    initialBase64: String? = null,
    onSignatureChanged: (base64: String?) -> Unit
) {
    val points = remember { mutableStateListOf<PathPoint>() }
    var loadedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isDirty by remember { mutableStateOf(false) }

    // Decode initial bitmap if provided
    LaunchedEffect(initialBase64) {
        if (!initialBase64.isNullOrBlank() && !isDirty) {
            try {
                val clean = if (initialBase64.contains(",")) initialBase64.substringAfter(",") else initialBase64
                val decoded = Base64.decode(clean, Base64.DEFAULT)
                loadedBitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.size)
            } catch (_: Exception) {
                loadedBitmap = null
            }
        } else if (initialBase64.isNullOrBlank() && !isDirty) {
            loadedBitmap = null
            points.clear()
        }
    }

    Box(
        modifier = modifier
            .height(130.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color.White)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp))
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            isDirty = true
                            loadedBitmap = null
                            points.add(PathPoint(offset, isStart = true))
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            points.add(PathPoint(change.position, isStart = false))
                        },
                        onDragEnd = {
                            val bmp = renderPointsToBitmap(points, 400, 160)
                            val b64 = bitmapToBase64(bmp)
                            onSignatureChanged(b64)
                        }
                    )
                }
        ) {
            if (loadedBitmap != null) {
                val img = loadedBitmap!!.asImageBitmap()
                drawImage(img)
            } else {
                val path = Path()
                for (i in points.indices) {
                    val p = points[i]
                    if (p.isStart) {
                        path.moveTo(p.offset.x, p.offset.y)
                    } else {
                        path.lineTo(p.offset.x, p.offset.y)
                    }
                }
                drawPath(
                    path = path,
                    color = Color.Black,
                    style = Stroke(
                        width = 4f,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
        }

        // Clear button
        if (points.isNotEmpty() || loadedBitmap != null) {
            IconButton(
                onClick = {
                    points.clear()
                    loadedBitmap = null
                    isDirty = true
                    onSignatureChanged(null)
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(28.dp)
                    .background(Color(0xFFE74C3C), RoundedCornerShape(4.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Limpiar Firma",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

private fun renderPointsToBitmap(points: List<PathPoint>, width: Int, height: Int): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    val paint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.BLACK
        strokeWidth = 3f
        style = AndroidPaint.Style.STROKE
        strokeCap = AndroidPaint.Cap.ROUND
        strokeJoin = AndroidPaint.Join.ROUND
    }

    val androidPath = android.graphics.Path()
    for (p in points) {
        if (p.isStart) {
            androidPath.moveTo(p.offset.x, p.offset.y)
        } else {
            androidPath.lineTo(p.offset.x, p.offset.y)
        }
    }
    canvas.drawPath(androidPath, paint)
    return bitmap
}

private fun bitmapToBase64(bitmap: Bitmap): String {
    val baos = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
    val bytes = baos.toByteArray()
    return Base64.encodeToString(bytes, Base64.NO_WRAP)
}
