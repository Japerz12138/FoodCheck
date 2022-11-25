package com.nyit.japerz.quickie.utils

import com.nyit.japerz.quickie.QRScannerActivity

internal object MlKitErrorHandler {

  @Suppress("UNUSED_PARAMETER", "FunctionOnlyReturningConstant")
  fun isResolvableError(activity: QRScannerActivity, exception: Exception) = false // always false when bundled
}