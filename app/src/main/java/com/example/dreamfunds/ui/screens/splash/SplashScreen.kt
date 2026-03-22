package com.example.dreamfunds.ui.screens.splash


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.dreamfunds.R

@Composable
fun SplashScreen() {
    Box(
        modifier         = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Image(
                painter            = painterResource(id = R.drawable.dreamfunds_logo),
                contentDescription = "DreamFunds Logo",
                modifier           = Modifier.size(120.dp),
                contentScale       = ContentScale.Fit,
            )
            Text(
                text       = "DreamFunds",
                style      = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.primary,
            )
            CircularProgressIndicator(
                modifier    = Modifier.size(24.dp),
                strokeWidth = 2.dp,
                color       = MaterialTheme.colorScheme.primary,
            )
        }
    }
}