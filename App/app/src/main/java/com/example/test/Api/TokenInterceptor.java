package com.example.test.Api;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import androidx.preference.PreferenceManager;

import com.example.test.Helper.ContextHelper;
import com.example.test.Model.AuthResponse;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

import javax.net.ssl.HttpsURLConnection;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Callback;

public class TokenInterceptor implements Interceptor {
    String accessToken = "";
    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ContextHelper.getContext());
    Request request = null;



    @Override
    public Response intercept(Chain chain) throws IOException {

        Request newRequest = chain.request().newBuilder()
//                .addHeader("Content-Type:","application/json")
                .header("Authorization", "Bearer " + accessToken)
                .build();
        accessToken = sharedPreferences.getString("accessToken", "");
        Response response = chain.proceed(newRequest);

        if (response.code() == HttpsURLConnection.HTTP_UNAUTHORIZED){
            String refreshToken = sharedPreferences.getString("refreshToken", "");

            if(!TextUtils.isEmpty(accessToken) && !TextUtils.isEmpty(refreshToken)){
                AuthResponse oldAuth = new AuthResponse();
                oldAuth.setAccessToken(accessToken);
                oldAuth.setRefreshToken(refreshToken);
                refreshToken(oldAuth);
                accessToken = sharedPreferences.getString("accessToken", "");
                newRequest = chain.request().newBuilder()
//                .addHeader("Content-Type:","application/json")
                        .header("Authorization", "Bearer " + accessToken)
                        .build();
                response = chain.proceed(newRequest);
                if (response.code() == HttpsURLConnection.HTTP_UNAUTHORIZED){
                    //TODO: chuyen ve dang nhap
                }
            }
        }


        return response;
    }

    synchronized void refreshToken(AuthResponse auth){
        ApiService.apiService.refreshToken(auth).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, retrofit2.Response<AuthResponse> response) {
                SharedPreferences.Editor editor =  sharedPreferences.edit();

                if (response.isSuccessful()){
                    AuthResponse newAuth = response.body();
                    editor.remove("accessToken");
                    editor.remove("refreshToken");
                    editor.putString("accessToken", newAuth.accessToken);
                    editor.putString("refreshToken", newAuth.refreshToken);


                    editor.apply();
                }
                else {
                    editor.remove("accessToken");
                    editor.remove("refreshToken");
                    editor.apply();

                }

            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {

            }
        });
    }
}



