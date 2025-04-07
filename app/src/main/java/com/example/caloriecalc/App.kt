package com.example.caloriecalc

import android.app.Application
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.database.database

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        // Инициализация Firebase
        FirebaseApp.initializeApp(this)

        // Включение оффлайн-режима
        Firebase.database.setPersistenceEnabled(true)

        // Настройка размера кэша (опционально, 10MB по умолчанию)
        Firebase.database.setPersistenceCacheSizeBytes(10_000_000)
    }
}