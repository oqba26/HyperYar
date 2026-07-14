package com.oqba26.hyperyar.network

/*
import com.google.ai.client.generativeai.GenerativeModel
import com.oqba26.hyperyar.data.Product

class GeminiService(apiKey: String) {
    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = apiKey
    )

    @Suppress("unused")
    suspend fun getProductInsights(product: Product): String? {
        val prompt = "تحلیل کالا: ${product.name} با قیمت ${product.sellPrice} تومان. چه پیشنهادی برای فروش بهتر این کالا در سوپرمارکت داری؟"
        return try {
            val response = generativeModel.generateContent(prompt)
            response.text
        } catch (_: Exception) {
            null
        }
    }

    suspend fun analyzeFinancialData(prompt: String): String? {
        return try {
            val response = generativeModel.generateContent(prompt)
            response.text
        } catch (_: Exception) {
            null
        }
    }
}
*/
