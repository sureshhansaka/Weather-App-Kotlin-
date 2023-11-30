package com.example.weatherappproject

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.material.textfield.TextInputEditText
import com.squareup.picasso.Picasso
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DifferentLocationWeather : AppCompatActivity() {

    lateinit var locationRequest: LocationRequest

    val locationClient : FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }
    private lateinit var forecastRecyclerView: RecyclerView
    private lateinit var lbllocation: TextView
    private lateinit var cityEdit : TextInputEditText;
    private lateinit var description: TextView
    private lateinit var humidity: TextView
    private lateinit var pressure: TextView
    private lateinit var temp: TextView
    private lateinit var windSpeed: TextView
    private lateinit var imgIcon: ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_different_location_weather)




        forecastRecyclerView = findViewById(R.id.forcast)
        forecastRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)



        lbllocation = findViewById(R.id.idLbllocation)
        cityEdit = findViewById(R.id.idEdtCity)
        description = findViewById(R.id.idDescription)
        humidity = findViewById(R.id.idHumidity)
        pressure = findViewById(R.id.idPressure)
        temp = findViewById(R.id.idTemp)
        windSpeed = findViewById(R.id.idWindSpeed)
        imgIcon = findViewById(R.id.idImgIcon)


        CheckPermission()
        // Adding TextWatcher to TextInputEditText
        cityEdit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val cityName = s.toString()
                if (cityName.isNotEmpty()) {
                    fetchWeatherData(cityName)
                    fetchForecastData(cityName)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed for this example
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not needed for this example
            }
        })

    }
    fun CheckPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // No need to request location updates in this case
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
        }
    }




    private fun fetchWeatherData(cityName: String) {
        val apiKey = "39038539455c25ce4322edfa3af922bf"
        val apiUrl = "https://api.openweathermap.org/data/2.5/weather?q=$cityName&appid=$apiKey"

        Thread {
            try {
                val url = URL(apiUrl)
                val urlConnection: HttpURLConnection = url.openConnection() as HttpURLConnection
                val inputStream = urlConnection.inputStream
                val bufferedReader = BufferedReader(InputStreamReader(inputStream))
                val stringBuilder = StringBuilder()

                bufferedReader.forEachLine {
                    stringBuilder.append(it)
                }

                val jsonResponse = JSONObject(stringBuilder.toString())

                runOnUiThread {
                    updateWeatherUI(jsonResponse)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }
    private fun updateWeatherUI(jsonResponse: JSONObject) {
        val weatherDescription = jsonResponse.getJSONArray("weather").getJSONObject(0).getString("description")
        val weatherHumidity = jsonResponse.getJSONObject("main").getInt("humidity")
        val weatherPressure = jsonResponse.getJSONObject("main").getInt("pressure")
        val weatherTemp = jsonResponse.getJSONObject("main").getDouble("temp")
        val weatherWindSpeed = jsonResponse.getJSONObject("wind").getDouble("speed")

        // Update UI with weather details
        description.text = "$weatherDescription"
        humidity.text = "Humidity: $weatherHumidity%"
        pressure.text = "Pressure: $weatherPressure hPa"
        temp.text = "$weatherTemp °C"
        windSpeed.text = "Wind Speed: $weatherWindSpeed m/s"

        fetchLocationName(jsonResponse);

        val imageURL = "https://openweathermap.org/img/w/" + jsonResponse.getJSONArray("weather").getJSONObject(0).getString("icon")+".png"

        Picasso.get().load(imageURL).into(imgIcon)
    }



    private fun fetchLocationName(jsonResponse: JSONObject) {
        val cityName = jsonResponse.optString("name", "")

        runOnUiThread {
            lbllocation.text = if (cityName.isNotEmpty()) cityName else "Location not found"
        }
    }
    private fun fetchForecastData(cityName: String) {

        val apiKey = "39038539455c25ce4322edfa3af922bf"
        val forecastApiUrl = "https://api.openweathermap.org/data/2.5/weather?q=$cityName&appid=$apiKey"

        Thread {
            try {
                val url = URL(forecastApiUrl)
                val urlConnection: HttpURLConnection = url.openConnection() as HttpURLConnection
                val inputStream = urlConnection.inputStream
                val bufferedReader = BufferedReader(InputStreamReader(inputStream))
                val stringBuilder = StringBuilder()

                bufferedReader.forEachLine {
                    stringBuilder.append(it)
                }

                val forecastJsonArray = JSONObject(stringBuilder.toString()).getJSONArray("list")

                val forecastList = mutableListOf<ForecastModel>()

                // Get the current date in milliseconds
                val currentDateMillis = System.currentTimeMillis()

                for (i in 0 until forecastJsonArray.length()) {
                    val forecastJson = forecastJsonArray.getJSONObject(i)

                    val forecastTimestamp = forecastJson.getLong("dt") * 1000

                    if (forecastTimestamp >= currentDateMillis && forecastList.size < 5) {
                        val date = Date(forecastTimestamp)


                        // Format the date to get the day of the week
                        val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault())
                        val day = dayFormat.format(date)
                        val iconUrl = "https://openweathermap.org/img/w/${forecastJson.getJSONArray("weather").getJSONObject(0).getString("icon")}.png"
                        val temperature = "${forecastJson.getJSONObject("main").getDouble("temp")} °C"
                        val description = forecastJson.getJSONArray("weather").getJSONObject(0).getString("description")

                        val temperatureKelvin = forecastJson.getJSONObject("main").getDouble("temp")
                        val temperatureCelsius = temperatureKelvin - 273.15
                        val formattedTemperature = "${String.format("%.2f", temperatureCelsius)} °C"

                        val forecastModel = ForecastModel(day, iconUrl, formattedTemperature, description)
                        forecastList.add(forecastModel)
                    }

                }

                runOnUiThread {
                    // Create and set adapter for the forecast RecyclerView
                    val forecastAdapter = ForecastAdapter(forecastList)
                    forecastRecyclerView.adapter = forecastAdapter
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }


}