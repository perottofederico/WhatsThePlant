package com.example.whatstheplant.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.whatstheplant.R
import com.example.whatstheplant.ui.theme.darkGreen
import com.example.whatstheplant.ui.theme.green
import com.example.whatstheplant.ui.theme.lightGreen

//--------------------------------------------------------------------------------------------------

@Composable
fun MyBigButton(text: String, onClick: () -> Unit) {

    ElevatedButton(
        onClick = { onClick() },
        modifier = Modifier
            .padding(8.dp),
        colors = ButtonDefaults.buttonColors(darkGreen),
        elevation = ButtonDefaults.elevatedButtonElevation(
            defaultElevation = 8.dp
        )
    )
    {
        Text(
            text = text,
            color = Color.White,
            fontSize = 18.sp
        )
    }
}

//--------------------------------------------------------------------------------------------------

@Composable
fun Logo() {
    Image(
        painter = painterResource(id = R.drawable.logo),
        contentDescription = "Logo",
        modifier = Modifier
            .size(250.dp)
            .padding(top = 20.dp)
    )
}

//--------------------------------------------------------------------------------------------------

@Composable
fun MyTitle(text: String) {
    Text(
        text = text,
        fontSize = 20.sp,
        color = green
    )
}

//--------------------------------------------------------------------------------------------------

@Composable
fun MyText(text: String) {
    Text(
        text = text,
        fontSize = 15.sp,
        color = green
    )
}







