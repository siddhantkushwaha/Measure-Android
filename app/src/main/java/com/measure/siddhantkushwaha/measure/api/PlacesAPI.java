package com.measure.siddhantkushwaha.measure.api;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.measure.siddhantkushwaha.measure.R;
import com.measure.siddhantkushwaha.measure.pojo.SearchedPlace;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class PlacesAPI {

    private static final String TAG = PlacesAPI.class.getSimpleName();

    private Context context;
    private String API_KEY;

    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String TYPE_DETAILS = "/details";
    private static final String OUT_JSON = "/json";

    public PlacesAPI(Context context) {
        this.context = context;
        this.API_KEY = this.context.getString(R.string.google_maps_key);
    }

    public ArrayList<SearchedPlace> autocomplete (String input) {

        ArrayList<SearchedPlace> resultList = null;
        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();

        try {
            StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);
            sb.append("?key=" + API_KEY);
            sb.append("&input=" + URLEncoder.encode(input, "utf8"));

            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            Log.e(TAG, "Error processing Places API URL", e);
            return resultList;
        } catch (IOException e) {
            Log.e(TAG, "Error connecting to Places API", e);
            return resultList;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {

            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

            resultList = new ArrayList<>();
            for (int i = 0; i < predsJsonArray.length(); i++) {
                SearchedPlace searchedPlace = new SearchedPlace();
                searchedPlace.setDescription(predsJsonArray.getJSONObject(i).getString("description"));
                searchedPlace.setPlace_id(predsJsonArray.getJSONObject(i).getString("place_id"));
                resultList.add(searchedPlace);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Cannot process JSON results", e);
        }

        return resultList;
    }

    public LatLng findPlaceDetails(String placeId) {

        LatLng latLng = null;
        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();

        try {
            StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_DETAILS + OUT_JSON);
            sb.append("?key=" + API_KEY);
            sb.append("&placeid=" + URLEncoder.encode(placeId, "utf8"));

            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            Log.e(TAG, "Error processing Places API URL", e);
            return latLng;

        } catch (IOException e) {
            Log.e(TAG, "Error connecting to Places API", e);
            return latLng;

        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {

            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONObject result = jsonObj.getJSONObject("result");
            JSONObject geometry = result.getJSONObject("geometry");
            JSONObject location = geometry.getJSONObject("location");
            Double latitude = location.getDouble("lat");
            Double longitude = location.getDouble("lng");

            latLng = new LatLng(latitude, longitude);

            return latLng;

        } catch (JSONException e) {
            Log.e(TAG, "Cannot process JSON results", e);
            return latLng;
        }
    }
//
//    private interface GetPlaceDetailsApi {
//
//
//    }
}