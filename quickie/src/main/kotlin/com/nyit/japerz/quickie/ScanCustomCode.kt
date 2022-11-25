package com.nyit.japerz.quickie

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.nyit.japerz.quickie.QRResult.QRError
import com.nyit.japerz.quickie.QRResult.QRMissingPermission
import com.nyit.japerz.quickie.QRResult.QRSuccess
import com.nyit.japerz.quickie.QRResult.QRUserCanceled
import com.nyit.japerz.quickie.QRScannerActivity.Companion.EXTRA_CONFIG
import com.nyit.japerz.quickie.QRScannerActivity.Companion.RESULT_ERROR
import com.nyit.japerz.quickie.QRScannerActivity.Companion.RESULT_MISSING_PERMISSION
import com.nyit.japerz.quickie.config.ScannerConfig
import com.nyit.japerz.quickie.extensions.getRootException
import com.nyit.japerz.quickie.extensions.toParcelableConfig
import com.nyit.japerz.quickie.extensions.toQuickieContentType

public class ScanCustomCode : ActivityResultContract<ScannerConfig, QRResult>() {

  override fun createIntent(context: Context, input: ScannerConfig): Intent {
    return Intent(context, QRScannerActivity::class.java).apply {
      putExtra(EXTRA_CONFIG, input.toParcelableConfig())
    }
  }

  override fun parseResult(resultCode: Int, intent: Intent?): QRResult {
    return when (resultCode) {
      RESULT_OK -> QRSuccess(intent.toQuickieContentType())
      RESULT_CANCELED -> QRUserCanceled
      RESULT_MISSING_PERMISSION -> QRMissingPermission
      RESULT_ERROR -> QRError(intent.getRootException())
      else -> QRError(IllegalStateException("Unknown activity result code $resultCode"))
    }
  }
}