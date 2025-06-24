package com.pokrikinc.mixpokrikcutter.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.svg.SvgDecoder

@Composable
fun CategoryCard(
    title: String,
    img: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Card(
        modifier = modifier,
        elevation = CardDefaults.elevatedCardElevation(1.dp), // меньше тени
        shape = RoundedCornerShape(4.dp), // меньше радиус
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .height(100.dp), // Используем минимально возможную высоту
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(img)
                    .decoderFactory(SvgDecoder.Factory())
                    .memoryCacheKey(img) // Use URL as cache key
                    .build(),
                contentDescription = "Изображение категории",
                modifier = Modifier
                    .fillMaxHeight()
                    .width(50.dp), // Ограничиваем ширину
                contentScale = ContentScale.Fit // Или ContentScale.Crop при необходимости
            )

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f) // Занимает оставшиеся 50%
            )
        }
    }
}