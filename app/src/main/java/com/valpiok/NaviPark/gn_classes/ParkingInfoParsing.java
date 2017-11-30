package com.valpiok.NaviPark.gn_classes;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by SERHIO on 18.09.2017.
 */

public class ParkingInfoParsing {

    private String json_parking;
    private Boolean success;

    public String error;
    public String ParkTitls[];
    public String ParkAddress[];
    public String ParkPlacesQtv[];
    public Integer ParkIds[];

    public HashMap<Integer, HashMap<Integer, HashMap<String, String>>> Tarrifs = new HashMap<>();
    public HashMap<Integer, Integer> ValidTarrifs = new HashMap<>();

    public ParkingInfoParsing(String json_park, Boolean success) {
        this.json_parking = json_park;
        this.success = success;
        this.parse_json();
    }

    private void parse_json() {

        try {
            JSONObject json = new JSONObject(this.json_parking);
            if (!this.success) {
                this.error = json.getString("error");
                return;
            }

            Integer park_count = (json.has("count")) ? json.getInt("count") : 1;
            Boolean HasItems = (json.has("items")) ? true : false;

            this.ParkTitls = new String[park_count];
            this.ParkAddress = new String[park_count];
            this.ParkPlacesQtv = new String[park_count];
            this.ParkIds = new Integer[park_count];

            JSONArray parkArray = new JSONArray();
            if (HasItems) {
                parkArray = json.getJSONArray("items");
            }

            String param_park;

            for (int i = 0; i < park_count; i++) {
                param_park = (HasItems) ? parkArray.getString(i) : this.json_parking;
                this.parse_parking(param_park, i);
            }
        } catch (Exception e) {
            Log.e("PIP parse_json()", e.getMessage());
        }

    }

    private void parse_parking(String json_park, Integer numb) {
        try {
            JSONObject json = new JSONObject(json_park);
            this.ParkIds[numb] = Integer.parseInt(json.getString("p_id"));
            this.ParkTitls[numb] = json.getString("p_title");
            this.ParkAddress[numb] = json.getString("p_address");
            this.ParkPlacesQtv[numb] = json.getString("p_places_qty");

            json = json.getJSONObject("tariffs");
            Integer count = json.getInt("count");
            JSONArray tarrifs = json.getJSONArray("items");

            HashMap <Integer, HashMap<String, String>> TarrifList = new HashMap<>();

            Integer TarriffID;
            String TValidFrom, TValidTo;

            TarriffTimeControl timeControl = new TarriffTimeControl();

            for (int i = 0; i < count; i++) {
                json = new JSONObject(tarrifs.getString(i));
                HashMap<String, String> Tarrif = new HashMap<>();
                Long max_time = json.getLong("max_time");
                Tarrif.put("tariff_title", json.getString("tariff_title"));
                TValidFrom = json.getString("validity_from");
                Tarrif.put("validity_from", TValidFrom);
                TValidTo = json.getString("validity_to");
                Tarrif.put("validity_to", TValidTo);
                Tarrif.put("price", json.getString("price"));
                Tarrif.put("max_time", max_time.toString());
                Tarrif.put("unit_type", json.getString("unit_type"));
                TarriffID = Integer.parseInt(json.getString("tariff_id"));
                TarrifList.put(TarriffID, Tarrif);
                if (timeControl.check_valid_tarrif(TValidFrom, TValidTo)) {
                    this.ValidTarrifs.put(TarriffID, numb);
                }
            }
            this.Tarrifs.put(numb, TarrifList);

        } catch (Exception e) {
            Log.e("PIP parse_parking()", e.getMessage());
        }
    }

}