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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public class PlacesAPI {

    private static final String TAG = PlacesAPI.class.getSimpleName();

    private Context context;
    private String API_KEY;

    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String TYPE_DETAILS = "details";
    private static final String OUT_JSON = "/json";

    public PlacesAPI(Context context) {
        this.context = context;
        this.API_KEY = this.context.getString(R.string.google_maps_key);
    }

    public ArrayList<SearchedPlace> autocomplete(String input) {

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

    private interface GetPlaceDetailsApi {

        @Headers("Content-Type: application/json")
        @GET(TYPE_DETAILS + OUT_JSON)
        Call<Object> getPlaceDetailsById(@Query("placeid") String placeId, @Query("key") String key);
    }

    public void getPlaceDetailsById(String placeId, Callback callback) {

        Retrofit retrofit = new Retrofit.Builder().baseUrl(PLACES_API_BASE + "/")
                .addConverterFactory(GsonConverterFactory.create()).build();

        GetPlaceDetailsApi getPlaceDetailsApi = retrofit.create(GetPlaceDetailsApi.class);
        Call call = getPlaceDetailsApi.getPlaceDetailsById(placeId, API_KEY);
        call.enqueue(callback);
    }
}