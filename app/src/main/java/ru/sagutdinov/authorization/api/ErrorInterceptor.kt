package ru.sagutdinov.authorization.api

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import ru.sagutdinov.App
import ru.sagutdinov.authorization.API_SHARED_FILE
import ru.sagutdinov.authorization.AUTHENTICATED_SHARED_KEY

class ErrorInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
        val response = chain.proceed(request)
        when (response.code()) {
            400 -> {
                //TODO
            }
            401 -> {
                App.context.getSharedPreferences(API_SHARED_FILE, Context.MODE_PRIVATE)
                    .edit()
                    .putString(AUTHENTICATED_SHARED_KEY,"")
                    .commit()
            }
        }
        return response
    }

}



