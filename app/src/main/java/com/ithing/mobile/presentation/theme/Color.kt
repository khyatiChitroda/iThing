package com.ithing.mobile.presentation.theme

import androidx.compose.ui.graphics.Color

// Web design tokens (ithing-redesign-main/webapp, <html class="theme-2">)
// --color-theme-1: rgb(30,58,138)  => #1E3A8A
// --color-theme-2: rgb(23,37,84)   => #172554
// --color-primary: rgb(23,37,84)   => #172554
val Theme1Blue = Color(0xFF1E3A8A)
val Theme2Navy = Color(0xFF172554)

// Brand Colors (matching website design)
val NavyBlue = Theme2Navy // Primary brand navy (web --color-primary)
val WebsiteBlue = Theme1Blue // Accent blue used in web utility classes (e.g., .blue-color)
val AccentBlue = Color(0xFF2563EB) // Secondary accent (used sparingly)
val LightBlue = Color(0xFF3B82F6) // Lighter accent

// Background Colors
val LightGrayBg = Color(0xFFF5F6FA) // Website card background
val CardBackground = Color(0xFFE2E8F0) // White card background
val BorderColor = Color(0xFFE2E8F0) // Borders and dividers
val ScreenBackground =  Color(0xFFFFFFFF) // White screen background for mobile shell

// Web text/placeholder helpers (Tailwind slate palette)
val Slate600 = Color(0xFF475569) // text-slate-600
val Slate400 = Color(0xFF94A3B8) // placeholder:text-slate-400

// Dark Theme
val DarkBackground = Color(0xFF0F172A) // Dark main background
val DarkCardBg = Color(0xFF1E293B) // Dark card background
val DarkBorder = Color(0xFF334155) // Dark borders
val DarkScreenBg = Color(0xFF1E293B) // Dark screen background

// Text Colors
val DarkText = Color(0xFF1F2937) // Primary text
val MutedText = Color(0xFF6B7280) // Secondary text
val LightText = Color(0xFF9CA3AF) // Tertiary text

// Status Colors
val SuccessGreen = Color(0xFF10B981)
val WarningYellow = Color(0xFFF59E0B)
val ErrorRed = Color(0xFFEF4444)
val InfoBlue = Color(0xFF3B82F6)

// Supporting
val White = Color(0xFFFFFFFF)
val Black = Color(0xFF000000)
val Transparent = Color(0x00000000)
