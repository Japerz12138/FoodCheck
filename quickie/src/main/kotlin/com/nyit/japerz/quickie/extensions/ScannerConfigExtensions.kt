package com.nyit.japerz.quickie.extensions

import com.nyit.japerz.quickie.config.ParcelableScannerConfig
import com.nyit.japerz.quickie.config.ScannerConfig

internal fun ScannerConfig.toParcelableConfig() =
  ParcelableScannerConfig(
    formats = formats,
    stringRes = stringRes,
    drawableRes = drawableRes,
    hapticFeedback = hapticFeedback,
    showTorchToggle = showTorchToggle,
    horizontalFrameRatio = horizontalFrameRatio,
    useFrontCamera = useFrontCamera,
  )