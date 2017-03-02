package com.example.administrator.coolweather;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.example.administrator.coolweather.gson.Forecast;
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


    public String weather_json;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
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
        weather_json = sharedPreferences.getString("weather_json",null);
        if(weather_json!=null){
            Weather weather = Utility.handleWeatherResponse(weather_json);
            showWeatherInfo(weather);
        }else{
            forecastLayout.setVisibility(View.INVISIBLE);
            String weatherId = getIntent().getStringExtra("weather_id");
            weatherRequest(weatherId);
        }

    }

    public void showWeatherInfo(Weather weather){
        forecastLayout.setVisibility(View.VISIBLE);
        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime;
        city_name.setText(cityName);
        update_time.setText(updateTime);
        String degreeText = weather.now.weathertmp+"℃";
        String weatherInfo = weather.now.more.info;
        degree.setText(degreeText);
        weather_info.setText(weatherInfo);
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

    public void weatherRequest(String weather_id){
        String weather_uri = "http://guolin.tech/api/weather?cityid="+weather_id+"&key=39e005c7a6c34018b2eec392bccbb232";
        HttpUtil.sendOkHttpRequest(weather_uri, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,"请求数据失败",Toast.LENGTH_SHORT).show();
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
                            showWeatherInfo(weather);
                        }else{
                            Toast.makeText(WeatherActivity.this,"请求数据失败",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }



}
