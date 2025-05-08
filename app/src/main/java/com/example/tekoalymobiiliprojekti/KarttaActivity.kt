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
import android.Manifest // manifestiin lisätty luvat -Henry
import android.content.Intent
import android.content.pm.PackageManager // -Henry
import android.widget.TextView
import androidx.core.app.ActivityCompat // -Henry
import com.example.tekoalymobiiliprojekti.databinding.ActivityKarttaBinding
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay // sijainnin hakemiseen -Henry
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider // -Henry
import com.example.tekoalymobiiliprojekti.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class KarttaActivity : AppCompatActivity() {
    private lateinit var map: MapView
    lateinit var kilometrit : TextView
    private lateinit var binding: ActivityKarttaBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kartta)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNav.selectedItemId = R.id.map

        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.map -> true // Olet jo täällä
                R.id.home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                else -> false
            }
        }

        Configuration.getInstance().load(applicationContext, getSharedPreferences("osmdroid", MODE_PRIVATE))


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

        // tarkistaa onko käyttäjältä lupa pyydetty sijainnin käyttämiseen // -Henry
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        } else {
            // jos lupa myönnetty otetaan käyttöön // -Henry
            setupMyLocation()
        }

        kilometrit = findViewById(R.id.kilometritTxt)

        var tahti : String = intent.getStringExtra("Tahti").toString()
        var aika : Double = intent.getDoubleExtra("Aika", 0.0)

        val matka = laskeMatka(tahti, aika)
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }

    // Käsittelee lupapyynnön // -Henry
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            setupMyLocation()
        }
    }

    // Asettaa sijainnin käyttäjälle // -Henry
    private fun setupMyLocation() {
        val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), map)
        locationOverlay.enableMyLocation()
        locationOverlay.enableFollowLocation()
        map.overlays.add(locationOverlay)
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

    //Laskee matkan ja näyttää sen valitussa kentässä
    private fun laskeMatka(tahti: String, aika: Double){

        val vauhtiMap = mapOf(
            "Rauhallisesti" to 4.0,  // km/h
            "Verkkaasti" to 6.0,
            "Juosten" to 10.0
        )

        val nopeus = vauhtiMap[tahti]

        if (nopeus != null) {
            val aikaTunneissa = aika / 60.0
            val matkaKm = nopeus * aikaTunneissa
            val matkaMetreina = matkaKm * 1000


            kilometrit.text = "%.2f".format(matkaMetreina / 1000)
        } else {
            kilometrit.text = "0"
        }
}
}
