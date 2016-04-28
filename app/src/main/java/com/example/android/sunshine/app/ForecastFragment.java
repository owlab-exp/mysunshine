package com.example.android.sunshine.app;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {
    private final String LOG_TAG = ForecastFragment.class.getSimpleName();

    public ForecastFragment() {
    }

    ArrayAdapter<String> weekForecastAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //To present this fragment's menu
        //To show menu
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Log.d(LOG_TAG, "Selected menu ID = " + id);
        if(id == R.id.action_refresh) {
            //Fetch weather from remote
            new FetchWeatherTask().execute("94043");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        List<String> weekForecast = new ArrayList<String>(Arrays.asList(
                "Today-Sunny-88/63",
                "Tomorrow-Foggy-70/46",
                "Weds-Cloudy-72/63",
                "Thurs-Rainy-64/51",
                "Fri-Foggy-70/45",
                "Sat-Sunny-76/68"
        ));


        //ArrayAdapter<String> weekForecastAdapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item_forecast, R.id.list_item_forecast_textview, weekForecast);
        System.out.println("getActivity() ===> " + getActivity());
        weekForecastAdapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item_forecast, R.id.list_item_forecast_textview, weekForecast);

        //ListView listView = (ListView) getActivity().findViewById(R.id.list_item_forecast);
        // --> null!
        //ListView listView = (ListView) container.findViewById(R.id.list_item_forecast);
        // --> crash
        //System.out.println("ListView object = " + listView);
        //listView.setAdapter(weekForecastAdapter);
        return inflater.inflate(R.layout.fragment_main, container, false);

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        ListView listView = (ListView) view.findViewById(R.id.list_item_forecast);
        System.out.println("ListView object = " + listView);
        listView.setAdapter(weekForecastAdapter);
    }

    public class FetchWeatherTask extends AsyncTask<String, Integer, String[]> {
        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        //private String postCode;

        //public FetchWeatherTask(String postCode) {
        //    this.postCode = postCode;
        //}

        @Override
        protected String[] doInBackground(String... params) {
            if(params.length > 0) {
                //this.postCode = params[0];
                String jsonStr = getForecastData(params[0]);
                return getWeatherDataFromJson(jsonStr);
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String[] result) {
            weekForecastAdapter.clear();
            for(String item : result) {
                weekForecastAdapter.add(item);

            }
        }

        private String getForecastData(String postCode) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are available at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                // URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7&appid=");
                //Uri uri = uriBuilder.build();
                //Uri.Builder uriBuilder = new Uri.Builder();
                final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily";
                //Uri uri = new Uri.Builder()
                Uri uri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        //.scheme("http")
                        //.authority("api.openweathermap.org")
                        //.appendPath("data")
                        //.appendPath("2.5")
                        //.appendPath("forecast")
                        //.appendPath("daily")
                //uriBuilder.path("http://api.openweathermap.org/data/2.5/forecast/daily");
                .appendQueryParameter("q", postCode)
                .appendQueryParameter("mode", "json")
                .appendQueryParameter("units", "metric")
                .appendQueryParameter("cnt", "7")
                .appendQueryParameter("appid", "e29149c797c75f1f2f99af380a1052cd")
                .build();

                Log.d(LOG_TAG, "URI: " + uri.toString());

                URL url = new URL(uri.toString());
                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    forecastJsonStr = null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    //Log.d(LOG_TAG, line);
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    forecastJsonStr = null;
                }

                forecastJsonStr = buffer.toString();
                Log.d(LOG_TAG, "Forecast JSON String: " + forecastJsonStr);


            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attempting
                // to parse it.
                forecastJsonStr = null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            return forecastJsonStr;
        }

        private String[] getWeatherDataFromJson(String jsonStr) {
            Time dayTime = new Time();
            dayTime.setToNow();
            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);
            dayTime = new Time();

            List<String> forecastParsed = new ArrayList<>();
            try {
                JSONObject json = new JSONObject(jsonStr);
                JSONArray forecastList = json.getJSONArray("list");
                for(int i = 0; i < 7 ; i++) {
                    if(forecastList.get(i) == null) {
                        break;
                    }
                    long dateTime = 0L;
                    dateTime = dayTime.setJulianDay(julianStartDay + i);
                    String day = new SimpleDateFormat("EEE MM dd").format(dateTime);

                    JSONObject weather = forecastList.getJSONObject(i).getJSONArray("weather").getJSONObject(0);
                    String main = weather.getString("main");
                    JSONObject temp = forecastList.getJSONObject(i).getJSONObject("temp");
                    Double max  = temp.getDouble("max");
                    Double min  = temp.getDouble("min");

                    Log.d(LOG_TAG, "Day: " + day + ", Main: " + main + ", Max: " + max + ", Min: " + min);

                    forecastParsed.add(day + " - " + main + " - " + Math.round(max) + "/" + Math.round(min));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            int size = forecastParsed.size();
            String[] parsedStringArray = new String[size];
            forecastParsed.toArray(parsedStringArray);
            return parsedStringArray;
        }
    }
}

