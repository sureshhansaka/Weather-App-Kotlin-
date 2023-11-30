package com.example.weatherappproject

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import android.widget.TextView
import android.widget.ImageView

class ForecastAdapter(private val forecastList: List<ForecastModel>) :
    RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForecastViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.day_weather_card, parent, false)
        return ForecastViewHolder(view)
    }

    override fun onBindViewHolder(holder: ForecastViewHolder, position: Int) {
        val forecast = forecastList[position]

        // Set data to views in the forecast item layout
        holder.dayTextView.text = forecast.day
        Picasso.get().load(forecast.iconUrl).into(holder.iconImageView)
        holder.temperatureTextView.text = forecast.temperature
        holder.descriptionTextView.text = forecast.description
    }

    override fun getItemCount(): Int {
        return forecastList.size
    }

    class ForecastViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dayTextView: TextView = itemView.findViewById(R.id.day)
        val iconImageView: ImageView = itemView.findViewById(R.id.imgIconNew)
        val temperatureTextView: TextView = itemView.findViewById(R.id.tempreture)
        val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionNew)
    }
}

