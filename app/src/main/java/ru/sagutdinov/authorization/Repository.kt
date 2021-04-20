package ru.sagutdinov.authorization

import android.content.Intent
import androidx.core.content.ContextCompat.startActivity
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.sagutdinov.App
import ru.sagutdinov.CounterChangeDto
import ru.sagutdinov.PostDto
import ru.sagutdinov.authorization.api.API
import ru.sagutdinov.authorization.api.AuthRequestParams
import ru.sagutdinov.authorization.api.ErrorInterceptor
import ru.sagutdinov.authorization.api.RegistrationRequestParams


object Repository {

    // Ленивое создание Retrofit экземпляра
    private val retrofit: Retrofit by lazy {
        val client=OkHttpClient()
            .newBuilder()
            .addInterceptor(ErrorInterceptor())
            .build()
        Retrofit.Builder()
            .client(client)
            .baseUrl("https://srv-ncms.herokuapp.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    // Ленивое создание API
    private val API: API by lazy {
        retrofit.create(ru.sagutdinov.authorization.api.API::class.java)
    }

    suspend fun authenticate(login: String, password: String) = API.authenticate(
        AuthRequestParams(login, password)
    )

    suspend fun register(login: String, password: String) =
        API.register(
            RegistrationRequestParams(
                login,
                password
            )
        )

    suspend fun getPosts(token: String?): List<PostDto>? {
        val list=token?.let {
            API.getPosts(
                "bearer $it"
            )
        }
        if (list.isNullOrEmpty()){
            val intent = Intent(
                App.INSTANCE,
                AuthentificateActivity::class.java
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(App.context,intent,null)
        }
        return list
    }

    suspend fun changeCounter(token:String,counterChangeDto: CounterChangeDto):Any=
        API.changeCounter("bearer $token",counterChangeDto)

}