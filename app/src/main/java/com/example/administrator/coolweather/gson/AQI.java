package com.example.administrator.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Administrator on 2017/3/2 0002.
 */

public class AQI {

    @SerializedName("city")
    public AQICity aqiCity;


    public class AQICity{

        public String aqi;

        public String pm25;
    }
}
