package io.github.linxiks.pindex.core.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.unit.sp

private val Base = Typography()

val PindexTypography = Typography(
    titleLarge = Base.titleLarge.copy(fontSize = 22.sp),
    headlineSmall = Base.headlineSmall.copy(fontSize = 26.sp),
    titleMedium = Base.titleMedium.copy(fontSize = 18.sp),
    bodyLarge = Base.bodyLarge.copy(fontSize = 16.sp),
    bodyMedium = Base.bodyMedium.copy(fontSize = 14.sp),
    labelLarge = Base.labelLarge.copy(fontSize = 14.sp),
    labelMedium = Base.labelMedium.copy(fontSize = 12.sp),
)
