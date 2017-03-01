package com.example.administrator.coolweather;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.administrator.coolweather.db.City;
import com.example.administrator.coolweather.db.County;
import com.example.administrator.coolweather.db.Province;
import com.example.administrator.coolweather.util.HttpUtil;
import com.example.administrator.coolweather.util.Utility;
import org.litepal.crud.DataSupport;
import org.litepal.tablemanager.Connector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/3/1 0001.
 */

public class ChooseAreaFragment extends Fragment {

    public static final int LEVEL_PROVINCE = 0;

    public static final int LEVEL_CITY = 1;

    public static final int LEVEL_COUNTY = 2;

    public ListView listView ;

    public ArrayAdapter arrayAdapter ;

    public List<String> data = new ArrayList<>();

    //省级列表
    public List<Province> provinces = new ArrayList<>();

    //市级列表
    public List<City> cities = new ArrayList<>();

    //县级列表
    public List<County> counties = new ArrayList<>();

    //级别
    public int currentlevel;

    public TextView title_textView;

    public Button back_btn ;

    /**
     * 被选中的省份
     */
    public Province selectedProvince;

    /**
     *被选中的市
     */
    public City selectedCity;

    /**
     * 被选中的县
     */
    public County selectCounty;

    public ProgressDialog progressDialog;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.choose_area,container,false);
        title_textView = (TextView) root.findViewById(R.id.title_id);
        back_btn = (Button) root.findViewById(R.id.back);
        listView = (ListView) root.findViewById(R.id.list_view);
        arrayAdapter = new ArrayAdapter(getContext(),android.R.layout.simple_list_item_1,data);
        listView.setAdapter(arrayAdapter);
        Connector.getDatabase();
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(currentlevel==LEVEL_PROVINCE){
                    selectedProvince = provinces.get(position);
                    queryCity();
                }else if(currentlevel==LEVEL_CITY){
                    selectedCity = cities.get(position);
                    Log.v("Jam","http://guolin.tech/api/china/"+selectedProvince.getProvinceCode()+"/"+selectedCity.getCityCode());
                    queryCounty();
                }
            }
        });
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentlevel==LEVEL_COUNTY){
                    queryCity();
                }else if(currentlevel==LEVEL_CITY){
                    queryProvinces();
                }
            }
        });
        queryProvinces();
    }

    public void queryProvinces(){
        title_textView.setText("中国");
        back_btn.setVisibility(View.GONE);
        provinces = DataSupport.findAll(Province.class);
        if(provinces.size()>0){
            data.clear();
            for(Province province:provinces){
                data.add(province.getProvinceName());
            }
            arrayAdapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentlevel = LEVEL_PROVINCE;
        }else{
            String address = "http://guolin.tech/api/china";
            queryFromServer(address,"province");
        }
    }

    public void queryCity(){
        title_textView.setText(selectedProvince.getProvinceName());
        back_btn.setVisibility(View.VISIBLE);
        cities = DataSupport.where("provinceId=?", String.valueOf(selectedProvince.getId())).find(City.class);
        if(cities.size()>0){
            data.clear();
            for(City city : cities){
                data.add(city.getCityName());
            }
            arrayAdapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentlevel = LEVEL_CITY;
        }else{
            String address = "http://guolin.tech/api/china/"+selectedProvince.getProvinceCode();
            queryFromServer(address,"city");
        }
    }

    public void queryCounty(){
        title_textView.setText(selectedCity.getCityName());
        back_btn.setVisibility(View.VISIBLE);
        counties = DataSupport.where("cityId=?",String.valueOf(selectedCity.getId())).find(County.class);
        if(counties.size()>0){
            data.clear();
            for(County county : counties){
                data.add(county.getCountyName());
            }
            arrayAdapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentlevel = LEVEL_COUNTY;
        }else{
            String address = "http://guolin.tech/api/china/"+selectedProvince.getProvinceCode()+"/"+selectedCity.getCityCode() ;
            queryFromServer(address,"county");
        }
    }

    private void queryFromServer(String address, final String type) {

        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(),"加载失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = false;
                if ("province".equals(type)) {
                    result = Utility.handleProvinceResponse(responseText);
                } else if ("city".equals(type)) {
                    result = Utility.handleCityResponse(responseText, selectedProvince.getId());
                } else if ("county".equals(type)) {
                    result = Utility.handleCountyResponse(responseText, selectedCity.getId());
                }
                if (result) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)) {
                                queryProvinces();
                            } else if ("city".equals(type)) {
                                queryCity();
                            } else if ("county".equals(type)) {
                                queryCounty();
                            }
                        }
                    });
                }
            }
        });
    }

            public void showProgressDialog() {
                if (progressDialog == null) {
                    progressDialog = new ProgressDialog(getContext());
                    progressDialog.setMessage("正在加载...");
                    progressDialog.setCanceledOnTouchOutside(false);
                }
                progressDialog.show();
            }

            public void closeProgressDialog() {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
            }

        }
