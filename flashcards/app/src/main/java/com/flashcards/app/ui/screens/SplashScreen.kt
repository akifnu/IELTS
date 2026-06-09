package com.flashcards.app.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.flashcards.app.util.GoogleSignInHelper
import com.flashcards.app.viewmodel.SplashViewModel
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    onSignedIn: () -> Unit,
    onNeedOnboarding: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val scene = viewModel.currentScene
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val kenBurns = remember { Animatable(1.06f) }
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val scroll = rememberScrollState()

    LaunchedEffect(state.sceneIndex) {
        kenBurns.snapTo(1.06f)
        kenBurns.animateTo(1f, tween(durationMillis = 14_000, easing = FastOutSlowInEasing))
    }

    fun proceed() {
        viewModel.continueAsGuest()
        if (viewModel.uiState.value.onboarded) onSignedIn() else onNeedOnboarding()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A12)),
    ) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(context)
                .data("file:///android_asset/splash/${scene.assetFile}")
                .crossfade(true)
                .build(),
            contentDescription = scene.credit,
            contentScale = ContentScale.Crop,
            loading = { SplashPhotoFallback() },
            error = { SplashPhotoFallback() },
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = kenBurns.value
                    scaleY = kenBurns.value
                },
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color(0x14000000),
                            0.45f to Color(0x59000000),
                            1.0f to Color(0xB8000000),
                        ),
                    ),
                ),
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .heightIn(max = screenHeight * 0.88f)
                .verticalScroll(scroll)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 28.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            SplashLine(visible = state.animateContent, delayMs = 0) {
                Text(
                    "SHINE",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                )
            }
            Spacer(Modifier.height(14.dp))
            SplashLine(visible = state.animateContent, delayMs = 80) {
                Text(
                    scene.essence.uppercase(),
                    color = Color.White.copy(alpha = 0.92f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.12f))
                        .border(1.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                )
            }
            Spacer(Modifier.height(12.dp))
            SplashLine(visible = state.animateContent, delayMs = 180) {
                Text(
                    scene.headline,
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 32.sp,
                )
            }
            Spacer(Modifier.height(10.dp))
            SplashLine(visible = state.animateContent, delayMs = 300) {
                Text(
                    scene.subline,
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = 24.sp,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Spacer(Modifier.height(12.dp))
            SplashLine(visible = state.animateContent, delayMs = 420) {
                Text(
                    viewModel.personalLine(),
                    color = Color.White.copy(alpha = 0.78f),
                    fontStyle = FontStyle.Italic,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Spacer(Modifier.height(22.dp))
            SplashLine(visible = state.animateContent, delayMs = 540) {
                Button(
                    onClick = { proceed() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF1E1B4B),
                    ),
                ) {
                    Text("Begin anew", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 4.dp))
                }
            }
            Spacer(Modifier.height(14.dp))
            SplashLine(visible = state.animateContent, delayMs = 640) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                val cred = GoogleSignInHelper.signIn(context)
                                if (cred != null) {
                                    viewModel.signInGoogle(
                                        cred.id,
                                        cred.displayName,
                                        cred.profilePictureUri?.toString(),
                                        onDone = { proceed() },
                                        onError = { },
                                    )
                                } else {
                                    proceed()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White.copy(alpha = 0.95f),
                            contentColor = Color(0xFF1F1F1F),
                        ),
                    ) {
                        Text("Continue with Google", fontWeight = FontWeight.SemiBold)
                    }
                    TextButton(
                        onClick = { proceed() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.White.copy(alpha = 0.88f)),
                    ) {
                        Text("Continue as guest")
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            SplashLine(visible = state.animateContent, delayMs = 740) {
                TextButton(onClick = { viewModel.cycleScene() }) {
                    Text(
                        "See another beginning →",
                        color = Color.White.copy(alpha = 0.65f),
                        fontSize = 13.sp,
                    )
                }
            }
            SplashLine(visible = state.animateContent, delayMs = 820) {
                Text(
                    scene.credit,
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun SplashPhotoFallback() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF1E1B4B), Color(0xFF5B5EF7)),
                ),
            ),
    )
}

@Composable
private fun SplashLine(
    visible: Boolean,
    delayMs: Int,
    content: @Composable () -> Unit,
) {
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(850, delayMillis = delayMs, easing = FastOutSlowInEasing),
        label = "splashAlpha",
    )
    val offsetY by animateFloatAsState(
        targetValue = if (visible) 0f else 18f,
        animationSpec = tween(850, delayMillis = delayMs, easing = FastOutSlowInEasing),
        label = "splashOffset",
    )
    Box(
        modifier = Modifier.graphicsLayer {
            this.alpha = alpha
            translationY = offsetY
        },
    ) {
        content()
    }
}
