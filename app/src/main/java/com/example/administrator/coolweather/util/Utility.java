package com.example.administrator.coolweather.util;

import android.text.TextUtils;

import com.example.administrator.coolweather.db.City;
import com.example.administrator.coolweather.db.County;
import com.example.administrator.coolweather.db.Province;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 这是一个解析服务器返回数据的工具类
 */

public class Utility {
    /**
     * 这个方法是用来解析服务器返回的省级数据
     * @param reponse
     * @return 返回的结果代表着解析是否成功
     */
    public static boolean handleProvinceResponse(String reponse){
        try {
            if(!TextUtils.isEmpty(reponse)){
                JSONArray jsonArray = new JSONArray(reponse);
                for(int i=0;i<jsonArray.length();i++){
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceCode(jsonObject.getInt("id"));
                    province.setProvinceName(jsonObject.getString("name"));
                    province.save();
                }
                return true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }
    /**
     * 这个方法是用来解析服务器返回的市区信息
     */
    public static boolean handleCityResponse(String reponse,int provinceId){
        if(!TextUtils.isEmpty(reponse)){
            try {
                JSONArray jsonArray = new JSONArray(reponse);
                for(int i=0;i<jsonArray.length();i++){
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    City city = new City();
                    city.setCityName(jsonObject.getString("name"));
                    city.setCityCode(jsonObject.getInt("id"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 这是用来解析服务器返回的县级数据
     * @param reponse
     * @param cityId
     * @return
     */
    public static boolean handleCountyResponse(String reponse,int cityId){
        if(!TextUtils.isEmpty(reponse)){
            try {
                JSONArray jsonArray = new JSONArray(reponse);
                for(int i=0;i<jsonArray.length();i++){
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    County county = new County();
                    county.setCountyName(jsonObject.getString("name"));
                    county.setCityId(cityId);
                    county.setWeatherId(jsonObject.getString("weather_id"));
                    county.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        return false;
    }


}
