// app/src/main/java/com/example/dreamfunds/SupabaseClient.kt
package com.example.dreamfunds

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.googleNativeLogin
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage

object SupabaseClientProvider {

    private const val SUPABASE_URL      = "https://otszqqawzeqiaesoqouf.supabase.co"
    private const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im90c3pxcWF3emVxaWFlc29xb3VmIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzQwOTc1ODcsImV4cCI6MjA4OTY3MzU4N30.5u5h4XvBVH0VUfX95eiFA6R4Xipn8zMdpN07-zO7WRM"

    const val GOOGLE_WEB_CLIENT_ID = "989421160016-tu2dtqfj8uef749nofq3m4losv108o4m.apps.googleusercontent.com"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_ANON_KEY
    ) {
        install(Auth) {
            // Must match AndroidManifest deep link scheme + host
            // and the redirectUrl in AuthRepository.register()
            scheme = "dreamfunds"
            host   = "auth"
        }
        install(Postgrest)
        install(Realtime)
        install(Storage)
        install(ComposeAuth) {
            googleNativeLogin(serverClientId = GOOGLE_WEB_CLIENT_ID)
        }
    }
}