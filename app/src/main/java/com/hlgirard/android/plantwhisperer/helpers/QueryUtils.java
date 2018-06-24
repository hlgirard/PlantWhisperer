package com.hlgirard.android.plantwhisperer.helpers;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import data.MoistureHistory;
import data.MoistureHistoryRepository;
import data.Plant;

public class QueryUtils {

    private MoistureHistoryRepository mHistoryRepo;
    private static final String LOG_TAG = "QueryUtils";

    // TODO: implement OAuth to get the API token in real time
    private static final String API_key = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJqdGkiOiJtb2lzdHVyZV9idWNrZXRfdG9rZW4iLCJ1c3IiOiJobGdpcmFyZCJ9.NmS6c0g5AiHswXwLYvhj3GT1INV6p0UMCfXRCKIxZVM";

    private QueryUtils() {
    }

    public static ArrayList<MoistureHistory> extractHistoryData(int plantId, String jsonResponse) throws JSONException {

        ArrayList<MoistureHistory> moistureHistoryList = new ArrayList<>();

            JSONArray root = new JSONArray(jsonResponse);

            for (int i=0; i < root.length(); i++) {
                JSONObject element = root.getJSONObject(i);
                long time = element.getLong("ts");
                int moisture = element.getInt("val");
                moistureHistoryList.add(new MoistureHistory(plantId, moisture, time));

            }

        return moistureHistoryList;

    }

    public static ArrayList<MoistureHistory> fetchHistoryData(int plantId, String requestUrl) throws JSONException {

        URL url = createUrl(requestUrl);

        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error closing input stream", e);
        }

        return extractHistoryData(plantId, jsonResponse);

    }

    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error with creating URL ", e);
        }
        return url;
    }

    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        Log.v("makeHttpRequest", "Trying to connect to API with URL " + url);

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Authorization", "Bearer " + API_key);
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the plant history JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }
}
