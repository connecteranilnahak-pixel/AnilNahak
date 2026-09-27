package com.example.ui.quickcapture

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.PebbleFlowApplication
import com.example.ui.theme.PebbleFlowTheme
import com.example.ui.viewmodel.QuickCaptureViewModel

class ShareCaptureActivity : ComponentActivity() {

    private val viewModel: QuickCaptureViewModel by viewModels {
        QuickCaptureViewModel.Factory(PebbleFlowApplication.instance.repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val intent = intent
        val action = intent.action
        val type = intent.type

        val imageUri: Uri? = if (Intent.ACTION_SEND == action && type?.startsWith("image/") == true) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(Intent.EXTRA_STREAM)
            }
        } else {
            null
        }

        if (imageUri != null) {
            viewModel.processSlipImage(this, imageUri)
        } else {
            Toast.makeText(this, "No payment slip found in share intent", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setContent {
            PebbleFlowTheme {
                val uiState by viewModel.uiState.collectAsState()

                QuickCaptureContent(
                    uiState = uiState,
                    onCategoryClick = { category ->
                        viewModel.saveAndFinish(category) {
                            finish()
                        }
                    },
                    onDismiss = {
                        finish()
                    },
                    onUpdateAmount = { newAmt ->
                        viewModel.setAmount(newAmt)
                    },
                    onUpdateMerchant = { newMerch ->
                        viewModel.setMerchant(newMerch)
                    }
                )
            }
        }
    }
}
