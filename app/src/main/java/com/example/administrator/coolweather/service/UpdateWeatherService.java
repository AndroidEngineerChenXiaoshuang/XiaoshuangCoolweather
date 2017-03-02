package com.example.administrator.coolweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import com.example.administrator.coolweather.gson.Weather;
import com.example.administrator.coolweather.util.HttpUtil;
import com.example.administrator.coolweather.util.Utility;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


/**
 * Created by Administrator on 2017/3/2 0002.
 */

public class UpdateWeatherService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        onUpdateWeather();
        onUpdateWeatherPick();
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        long timer = 8*1000*60*60;
        long nowTimer = SystemClock.elapsedRealtime();
        Intent startForUpdateService = new Intent(this,UpdateWeatherService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this,0,startForUpdateService,0);
        alarmManager.cancel(pendingIntent);
        alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP,timer+nowTimer,pendingIntent);
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 更新天气数据
     */
    public void onUpdateWeather(){
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String weather_josn = sharedPreferences.getString("weather_json",null);
        if(weather_josn!=null){
            final Weather weather = Utility.handleWeatherResponse(weather_josn);
            String weather_id = weather.basic.weatherId;
            String weather_uri = "http://guolin.tech/api/weather?cityid="+weather_id+"&39e005c7a6c34018b2eec392bccbb232";
            HttpUtil.sendOkHttpRequest(weather_uri, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String reponseText = response.body().string();
                    Weather weather = Utility.handleWeatherResponse(reponseText);
                    if(weather!=null&&"ok".equals(weather.status)){
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("weather_json",reponseText);
                        editor.apply();
                    }
                }
            });

        }
    }

    /**
     * 更新必应每日一图
     */
    public void onUpdateWeatherPick(){
        String pick_uri = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(pick_uri, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String pick_uri = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(UpdateWeatherService.this).edit();
                editor.putString("picker_img",pick_uri);
                editor.apply();
            }
        });
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
