package com.example.weatherappproject

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

import android.location.Address
import android.location.Geocoder
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import java.io.IOException
import java.util.Locale

class CurrentLoationWeather : AppCompatActivity() {


    private lateinit var forecastRecyclerView: RecyclerView

    lateinit var locationRequest: LocationRequest

    val locationClient : FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

    private var currentLocation : Location? = null



    private lateinit var lbllocation: TextView

    private lateinit var description: TextView
    private lateinit var humidity: TextView
    private lateinit var pressure: TextView
    private lateinit var temp: TextView
    private lateinit var windSpeed: TextView
    private lateinit var imgIcon: ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_current_loation_weather)

        forecastRecyclerView = findViewById(R.id.forcast)
        forecastRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        fetchForecastData(currentLocation?.latitude ?: 0.0, currentLocation?.longitude ?: 0.0)

        lbllocation = findViewById(R.id.lbllocation)

        description = findViewById(R.id.description)
        humidity = findViewById(R.id.humidity)
        pressure = findViewById(R.id.pressure)
        temp = findViewById(R.id.temp)
        windSpeed = findViewById(R.id.windSpeed)
        imgIcon = findViewById(R.id.imgIcon)

        CheckPermission()
    }

    fun CheckPermission(){

        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
            accessLocation()
        }else{
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),100)
        }
    }

    @SuppressLint("MissingPermission")
    fun accessLocation(){
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY,100).build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                p0.locations.lastOrNull()?.let { location ->
                    fetchLocationName(location.latitude, location.longitude)
                    fetchWeatherData(location.latitude, location.longitude)
                }
            }
        }
        locationClient.requestLocationUpdates(locationRequest,locationCallback,Looper.getMainLooper())
    }

    private fun fetchWeatherData(latitude: Double, longitude: Double) {
        val apiKey = "39038539455c25ce4322edfa3af922bf"
        val apiUrl = "https://api.openweathermap.org/data/2.5/weather?lat=$latitude&lon=$longitude&appid=$apiKey"

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

        val weatherTempKelvin = jsonResponse.getJSONObject("main").getDouble("temp")
        val weatherTempCelsius = weatherTempKelvin - 273.15

        // Update UI with weather details
        temp.text = "${String.format("%.2f", weatherTempCelsius)} °C"

        val imageURL = "https://openweathermap.org/img/w/" + jsonResponse.getJSONArray("weather").getJSONObject(0).getString("icon")+".png"

        Picasso.get().load(imageURL).into(imgIcon)
    }

    private fun fetchLocationName(latitude: Double, longitude: Double) {
        val geocoder = Geocoder(this, Locale.getDefault())

        try {
            val addresses: List<Address> = geocoder.getFromLocation(latitude, longitude, 1) as List<Address>
            if (addresses.isNotEmpty()) {
                val address: Address = addresses[0]
                val locationName = address.locality ?: address.subAdminArea ?: address.adminArea ?: ""

                runOnUiThread {
                    lbllocation.text = "$locationName"
                }
            } else {
                // Handle the case where no addresses are found
                lbllocation.text = "Location not found"
            }
        } catch (e: IOException) {
            e.printStackTrace()
            // Handle the exception
            lbllocation.text = "Error fetching location name"
        }
    }

    private fun fetchForecastData(latitude: Double, longitude: Double) {
        // Use a forecast API endpoint to get the forecast data
        // Modify the URL and parsing logic accordingly

        // Sample URL for forecast
        val apiKey = "39038539455c25ce4322edfa3af922bf"
        val forecastApiUrl = "https://api.openweathermap.org/data/2.5/forecast?lat=$latitude&lon=$longitude&appid=$apiKey"

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

                for (i in 0 until forecastJsonArray.length()) {
                    val forecastJson = forecastJsonArray.getJSONObject(i)

                    // Parse forecast data and create ForecastModel objects
                    val day = "Day" // Replace this with actual day data
                    val iconUrl = "https://openweathermap.org/img/w/${forecastJson.getJSONArray("weather").getJSONObject(0).getString("icon")}.png"
                    val temperature = "${forecastJson.getJSONObject("main").getDouble("temp")} °C"
                    val description = forecastJson.getJSONArray("weather").getJSONObject(0).getString("description")

                    val forecastModel = ForecastModel(day, iconUrl, temperature, description)
                    forecastList.add(forecastModel)

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