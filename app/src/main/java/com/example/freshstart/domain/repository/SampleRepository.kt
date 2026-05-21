package com.example.freshstart.domain.repository

interface SampleRepository {
    suspend fun fetchSampleMessage(): String
}
