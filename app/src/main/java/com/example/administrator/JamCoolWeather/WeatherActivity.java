package com.example.administrator.JamCoolWeather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.administrator.JamCoolWeather.gson.Forecast;
import com.example.administrator.JamCoolWeather.gson.Weather;
import com.example.administrator.JamCoolWeather.service.UpdateWeatherService;
import com.example.administrator.JamCoolWeather.util.HttpUtil;
import com.example.administrator.JamCoolWeather.util.Utility;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/3/2 0002.
 */

public class WeatherActivity extends AppCompatActivity {

    public TextView city_name;

    public TextView update_time;

    public TextView degree;

    public TextView weather_info;

    public LinearLayout forecastLayout;

    public TextView aqi;

    public TextView pm25;

    public TextView comfort;

    public TextView car_wash;

    public TextView sport;

    public SharedPreferences sharedPreferences;

    public ImageView imageView;

    public String weatherId;

    public String weather_json;

    public SwipeRefreshLayout swipeRefreshLayout;

    public Button openHome;

    public DrawerLayout drawerLayout;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        /**
         * 只有当android设备大于或者等于5.0的系统才会执行下面的逻辑代码
         */
        if(Build.VERSION.SDK_INT>=21){
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        openHome = (Button) findViewById(R.id.openDrawer);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);

        openHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        city_name = (TextView) findViewById(R.id.title_city);
        update_time = (TextView) findViewById(R.id.title_update_time);
        degree = (TextView) findViewById(R.id.degree_text);
        weather_info = (TextView) findViewById(R.id.weather_info_text);
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        aqi = (TextView) findViewById(R.id.aqi_text);
        pm25 = (TextView) findViewById(R.id.pm25_text);
        comfort = (TextView) findViewById(R.id.comfort_text);
        car_wash = (TextView) findViewById(R.id.car_wash_text);
        sport = (TextView) findViewById(R.id.sport_text);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        imageView = (ImageView) findViewById(R.id.back_img);
        weather_json = sharedPreferences.getString("weather_json",null);
        if(weather_json!=null){
            Weather weather = Utility.handleWeatherResponse(weather_json);
            weatherId = weather.basic.weatherId;
            showWeatherInfo(weather);
        }else{
            forecastLayout.setVisibility(View.INVISIBLE);
            weatherId = getIntent().getStringExtra("weather_id");
            weatherRequest(weatherId);
        }
        String bindpic = sharedPreferences.getString("picker_img",null);
        if(bindpic!=null){
            Glide.with(this).load(bindpic).into(imageView);
        }else{
            loadPick();
        }

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe);
        swipeRefreshLayout.setColorSchemeColors(Color.RED,Color.BLUE,Color.GREEN);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                weatherRequest(weatherId);
            }
        });

    }

    public void loadPick(){
        String pickUri = "http://guolin.tech/api/bing_pic";

        HttpUtil.sendOkHttpRequest(pickUri, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call,Response response) throws IOException {
                final String result = response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("picker_img",result);
                        editor.apply();
                        Glide.with(WeatherActivity.this).load(result).into(imageView);
                    }
                });

            }
        });
    }

    /**
     * 用于将服务器返回的数据显示到界面上面
     */
    public void showWeatherInfo(Weather weather){
        Intent intent = new Intent(this, UpdateWeatherService.class);
        startService(intent);
        forecastLayout.setVisibility(View.VISIBLE);
        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        city_name.setText(cityName);
        update_time.setText(updateTime);
        String degreeText = weather.now.weathertmp+"℃";
        String weatherInfo = weather.now.more.info;
        degree.setText(degreeText);
        weather_info.setText(weatherInfo);
        forecastLayout.removeAllViews();
        for(Forecast forecast:weather.forecasts){
            View root = LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);
            TextView date = (TextView) root.findViewById(R.id.date_text);
            TextView info = (TextView) root.findViewById(R.id.info_text);
            TextView max = (TextView) root.findViewById(R.id.max_text);
            TextView min = (TextView) root.findViewById(R.id.min_text);
            date.setText(forecast.date);
            info.setText(forecast.more.info);
            max.setText(forecast.temperature.max);
            min.setText(forecast.temperature.min);
            forecastLayout.addView(root);
        }
        if(weather.aqi!=null){
            aqi.setText(weather.aqi.aqiCity.aqi);
            pm25.setText(weather.aqi.aqiCity.pm25);
        }
        String comfortInfo = "舒适度:"+weather.suggestion.comfort.info;
        String carInfo = "洗车指数:"+weather.suggestion.carWash.info;
        String sprotInfo = "运动指数:"+weather.suggestion.sport.info;
        comfort.setText(comfortInfo);
        car_wash.setText(carInfo);
        sport.setText(sprotInfo);
    }

    public void weatherRequest(final String weather_id){
        String weather_uri = "http://guolin.tech/api/weather?cityid="+weather_id+"&key=39e005c7a6c34018b2eec392bccbb232";
        HttpUtil.sendOkHttpRequest(weather_uri, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,"请求数据失败",Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String json_data = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(json_data);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(weather!=null&&"ok".equals(weather.status)){
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("weather_json",json_data);
                            editor.apply();
                            weatherId = weather.basic.weatherId;
                            showWeatherInfo(weather);
                        }else{
                            Toast.makeText(WeatherActivity.this,"请求数据失败",Toast.LENGTH_SHORT).show();
                        }
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        });
        loadPick();
    }



}
