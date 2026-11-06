package com.murrel.pocketledger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.murrel.pocketledger.ui.PocketLedgerApp
import com.murrel.pocketledger.ui.theme.PocketLedgerTheme

/**
 * The single Activity for the whole app. With Jetpack Compose there is
 * no XML layout and no Fragments — this Activity just hosts Compose,
 * and Navigation Compose swaps the screens inside it.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PocketLedgerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PocketLedgerApp()
                }
            }
        }
    }
}
