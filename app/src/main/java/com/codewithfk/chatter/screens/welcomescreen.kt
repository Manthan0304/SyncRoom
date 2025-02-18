import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codewithfk.chatter.R
@Preview
@Composable
fun WelcomeScreen(
    username: String = "",
    onStartClick: () -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // 20% of the screen for the welcome text
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.2f)
                .background(Color.White)
                .padding(16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = "Welcome,\n$username...",
                style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            )
        }

        // 80% of the screen for the image and content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.8f)
        ) {
            // Background Image
            Image(
                painter = painterResource(id = R.drawable.logoimg),
                contentDescription = "Background Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
            )

            // Circular Logo at the Top Center
            Image(
                painter = painterResource(id = R.drawable.infinitylogo),
                contentDescription = "Infinity Logo",
                modifier = Modifier
                    .size(130.dp)
                    .align(Alignment.TopCenter)
                    .offset(y = -40.dp)
            )

            // Get Started Button at the bottom center
            Button(
                onClick = onStartClick,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
                    .height(56.dp)
                    .fillMaxWidth(0.8f), // Takes 80% of the width
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF121235))
            ) {
                Text(
                    text = "Get Started",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
