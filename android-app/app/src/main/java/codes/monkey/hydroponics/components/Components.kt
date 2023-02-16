package codes.monkey.hydroponics.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp

@Composable
fun <T> ListView(
    loading: Boolean = false,
    values: List<T>,
    content: @Composable (T) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(Constants.GUTTER_PADDING.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val indicatorHeight = (Constants.GUTTER_WIDTH * 2).dp
        if (loading) {
            item {
                CircularProgressIndicator(modifier = Modifier.size(indicatorHeight))
            }
        } else {
            item {
                Box(modifier = Modifier.size(indicatorHeight)) { }
            }
        }
        items(values) {
            content(it)
        }
    }
}

@Composable
fun <T> IconCard(
    cardHeight: Int = 80,
    item: T,
    onClick: (T) -> Unit,
    icon: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(Constants.GUTTER_PADDING.dp)
            .height(cardHeight.dp)
            .clickable { onClick(item) },
        shape = RoundedCornerShape(corner = CornerSize(Constants.CORNER_SIZE.dp))
    ) {
        Row(modifier =
        Modifier
            .padding(Constants.GUTTER_PADDING.dp)
            .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Surface(modifier = Modifier
                .padding(Constants.GUTTER_PADDING.dp)
                .size((cardHeight * 0.8).dp),
                shape = RectangleShape,
                shadowElevation = Constants.ELEVATION.dp
            ){
                icon()
            }

            Column(modifier = Modifier.padding(Constants.GUTTER_PADDING.dp)) {
                content()
            }
        }
    }

}

@Composable
fun LabelledData(data: List<Pair<String, String>>) {
    data.forEachIndexed { index, field ->
        Text(
            text = "${field.first}: ${field.second}",
            style = if (index == 0) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium
        )
    }
}