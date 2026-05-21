package com.example.freshstart.data.repository

import com.example.freshstart.domain.repository.SampleRepository
import kotlinx.coroutines.delay

class SampleRepositoryImpl : SampleRepository {
    override suspend fun fetchSampleMessage(): String {
        delay(500)
        return "MVVM skeleton ready"
    }
}
