package org.golfcat.team.project

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.auth.Auth

object SupabaseConfig {
    const val URL = "https://qyhtllcncbnzykzbvlhu.supabase.co"
    const val ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InF5aHRsbGNuY2JuenlremJ2bGh1Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3Nzk2NDUxMTYsImV4cCI6MjA5NTIyMTExNn0.T97-3knhVMg4-Ec6XSjHKI8xrDrdYMJryklcyQkXTJc"
}

// Use lazy initialization to avoid crashing during Android Studio Preview.
// The default SettingsSessionManager in Supabase Auth fails to initialize in the Layoutlib environment.
val supabase by lazy {
    createSupabaseClient(
        supabaseUrl = SupabaseConfig.URL,
        supabaseKey = SupabaseConfig.ANON_KEY
    ) {
        install(Postgrest)
        install(Auth)
    }
}
