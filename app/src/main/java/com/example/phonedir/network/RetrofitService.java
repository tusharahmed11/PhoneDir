package com.example.phonedir.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitService {
    private static OkHttpClient.Builder httpClient;
    private static Retrofit.Builder builder;
    private static int ConnectionTimeout = 600;
    private static int ReadTimeout = 600;


    /**
     *
     * @param serviceClass
     * @param serverURL
     * @param withHeader
     * @param <S>
     * @return
     */
    //Retrofit service method for all api calling
    public static <S> S createService(Class<S> serviceClass, String serverURL, boolean withHeader) {

        Gson gson = new GsonBuilder().setLenient().create();
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(logging);
        ConnectionTimeout = 600;
        httpClient.connectTimeout(ConnectionTimeout, TimeUnit.SECONDS );
        ReadTimeout = 600;
        httpClient.readTimeout(ReadTimeout, TimeUnit.SECONDS );

        if (withHeader) {
            httpClient.addInterceptor(

                    chain -> {
                        final Request request = chain.request().newBuilder()
                                .addHeader("Accept", "application/json")
                                .addHeader("Content-Type", "application/json-patch+json")
                                .build();

                        return chain.proceed(request);
                    }
            );
        }

        builder = new Retrofit.Builder()
                .baseUrl(serverURL)
                .addConverterFactory(GsonConverterFactory.create(gson));

        Retrofit retrofit = builder.client(httpClient.build()).build();
        return retrofit.create(serviceClass);
    }
}