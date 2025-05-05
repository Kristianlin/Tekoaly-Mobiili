package com.example.tekoalymobiiliprojekti

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import okhttp3.OkHttpClient
import okhttp3.Request
import com.google.gson.JsonParser
import org.osmdroid.views.overlay.Polyline

class MainActivity : AppCompatActivity() {

    private lateinit var map: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().load(applicationContext, getSharedPreferences("osmdroid", MODE_PRIVATE))
        setContentView(R.layout.activity_main)

        map = findViewById(R.id.mapView3)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setBuiltInZoomControls(true)
        map.setMultiTouchControls(true)

        val mapController = map.controller
        mapController.setZoom(13.0)
        val startPoint = GeoPoint(60.1699, 24.9384) // Helsinki
        val endPoint = GeoPoint(60.2055, 24.6559)   // Espoo
        mapController.setCenter(startPoint)

        drawRoute(startPoint, endPoint) // <- Kutsu metodia täällä
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }

    private fun drawRoute(start: GeoPoint, end: GeoPoint) {
        val url = "https://router.project-osrm.org/route/v1/driving/" +
                "${start.longitude},${start.latitude};${end.longitude},${end.latitude}" +
                "?overview=full&geometries=geojson"

        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        Thread {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val json = JsonParser.parseString(response.body?.string()).asJsonObject
                    val coords = json["routes"].asJsonArray[0]
                        .asJsonObject["geometry"]
                        .asJsonObject["coordinates"]
                        .asJsonArray

                    val geoPoints = coords.map { coordElement ->
                        val coordArray = coordElement.asJsonArray
                        val lon = coordArray[0].asDouble
                        val lat = coordArray[1].asDouble
                        GeoPoint(lat, lon)
                    }

                    runOnUiThread {
                        val line = Polyline().apply {
                            setPoints(geoPoints)
                            color = android.graphics.Color.BLUE
                            width = 8f
                        }
                        map.overlays.add(line)
                        map.invalidate()
                    }
                }
            }
        }.start()
    }
}