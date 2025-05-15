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
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.Road
import org.osmdroid.bonuspack.routing.RoadManager
import android.widget.Toast
import android.graphics.Color
import kotlin.math.*
import kotlin.random.Random
import android.content.Context


class KarttaActivity : BaseActivity() {

    private val greetings = listOf(
        "Liikkumisen iloa!",
        "Avoimin mielin, kevein askelin!",
        "Hyvää treeniä!",
        "Sinä liikut, kehosi kiittää!",
        "Tsemppiä reitille!"
    )

    private lateinit var map: MapView
    lateinit var kilometrit : TextView
    private lateinit var binding: ActivityKarttaBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_kartta)
        setContent(R.layout.activity_kartta)

        val greeting = greetings.random()  //tervehdys toiminta
        val greetingText = findViewById<TextView>(R.id.greetingText)
        greetingText.text = greeting

        /*val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
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
        }*/

        Configuration.getInstance().load(applicationContext, getSharedPreferences("osmdroid", MODE_PRIVATE))


        map = findViewById(R.id.mapView3)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setBuiltInZoomControls(true)
        map.setMultiTouchControls(true)

        val mapController = map.controller
        mapController.setZoom(13.0)

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

        // Kun käyttäjän sijainti on ensimmäisen kerran saatu, suoritetaan tämä osio
        locationOverlay.runOnFirstFix {
            val currentLocation = locationOverlay.myLocation
            val startPoint = GeoPoint(currentLocation.latitude, currentLocation.longitude)

            // Haetaan käyttäjän syöttämä aika ja tahti
            val aika = intent.getDoubleExtra("Aika", 0.0)
            val tahti = intent.getStringExtra("Tahti") ?: "Rauhallisesti"
            val haluttuMatkaKm = laskeMatka(tahti, aika)

            // Haetaan useista reiteistä satunnaisilla suunnilla
            val roadManager = OSRMRoadManager(this, "TekoalyApp")
            val candidateRoutes = mutableListOf<Pair<List<GeoPoint>, Road>>()
            roadManager.setMean(OSRMRoadManager.MEAN_BY_FOOT)

            // Piirretään edestakaisin reitti, jos reitti on liian lyhyt
            if (haluttuMatkaKm < 1.2) {
                val suunta = Random.nextDouble(0.0, 360.0)
                val edesTakaisin = haluttuMatkaKm / 2.5
                val puolimatka = LuoPaatepiste(startPoint, edesTakaisin, suunta)
                val waypoints = arrayListOf(startPoint, puolimatka, startPoint)

                val road = roadManager.getRoad(waypoints)

                runOnUiThread {
                    if (road.mStatus == Road.STATUS_OK) {
                        drawRoute(waypoints)
                        kilometrit.text = "%.2f".format(road.mLength)
                    } else {
                        Toast.makeText(this, "Reitin haku epäonnistui", Toast.LENGTH_SHORT).show()
                    }
                }
                return@runOnFirstFix
            }

            val numberOfCandidates = 12
            repeat(numberOfCandidates) {
                val angle1 = Random.nextDouble(0.0, 360.0)
                val angle2 = (angle1 + Random.nextDouble(60.0, 120.0)) % 360  // eri suuntaan kuin 1. piste
                val angle3 = (angle2 + Random.nextDouble(60.0, 90.0)) % 360
                val angle4 = (angle3 + Random.nextDouble(60.0, 90.0)) % 360

                val legDistance = haluttuMatkaKm / 5.0 * 0.8  // viisi etappia: 4 välietappia + paluu

                val waypoint1 = LuoPaatepiste(startPoint, legDistance, angle1)
                val waypoint2 = LuoPaatepiste(waypoint1, legDistance, angle2)
                val waypoint3 = LuoPaatepiste(waypoint2, legDistance, angle3)
                val waypoint4 = LuoPaatepiste(waypoint3, legDistance, angle4)

                val waypoints = arrayListOf(startPoint, waypoint1, waypoint2, waypoint3, waypoint4, startPoint)
                val road = roadManager.getRoad(waypoints)

                if (road.mStatus == Road.STATUS_OK) {
                    candidateRoutes.add(Pair(waypoints, road))
                }
            }

            // Valitaan reitti, jonka pituus on lähimpänä haluttua matkaa
            val best = candidateRoutes.minByOrNull { abs(it.second.mLength - haluttuMatkaKm) }

            if (best != null) {
                runOnUiThread {
                    drawRoute(best.first)
                    kilometrit.text = "%.2f".format(best.second.mLength)
                }
            } else {
                runOnUiThread {
                    Toast.makeText(this, "Reitin haku epäonnistui", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Piirretään reitti kartalle ja luodaan infokuplat
    private fun drawRoute(points: List<GeoPoint>) {
        Thread {
            try {
                val roadManager = OSRMRoadManager(this, "TekoalyApp")
                roadManager.setMean(OSRMRoadManager.MEAN_BY_FOOT)
                val road: Road = roadManager.getRoad(ArrayList(points))

                runOnUiThread {
                    if (road.mStatus != Road.STATUS_OK) {
                        Toast.makeText(this, "Reitin haku epäonnistui", Toast.LENGTH_SHORT).show()
                        return@runOnUiThread
                    }

                    val roadOverlay = RoadManager.buildRoadOverlay(road)
                    roadOverlay.color = Color.BLUE
                    roadOverlay.width = 8f
                    map.overlays.add(roadOverlay)

                    // Lisätään markerit ohjeiden kohdalle
                    for ((index, node) in road.mNodes.withIndex()) {
                        val nodeMarker = Marker(map)
                        nodeMarker.position = node.mLocation
                        nodeMarker.title = "Ohje ${index + 1}"
                        nodeMarker.snippet = node.mInstructions
                        nodeMarker.subDescription = Road.getLengthDurationText(this, node.mLength, node.mDuration)
                        nodeMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        map.overlays.add(nodeMarker)
                    }

                    map.invalidate()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "Virhe reittiä laskettaessa: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }

    // Hakee uuden sijainnin  annetusta lähtöpisteestä
    fun LuoPaatepiste(start: GeoPoint, distanceKm: Double, bearingDegrees: Double): GeoPoint {
        val R = 6371.0
        val bearing = Math.toRadians(bearingDegrees)
        val lat1 = Math.toRadians(start.latitude)
        val lon1 = Math.toRadians(start.longitude)

        val lat2 = asin(sin(lat1) * cos(distanceKm / R) +
                cos(lat1) * sin(distanceKm / R) * cos(bearing))
        val lon2 = lon1 + atan2(
            sin(bearing) * sin(distanceKm / R) * cos(lat1),
            cos(distanceKm / R) - sin(lat1) * sin(lat2)
        )

        return GeoPoint(Math.toDegrees(lat2), Math.toDegrees(lon2))
    }

    //Laskee matkan ja näyttää sen valitussa kentässä
    private fun laskeMatka(tahti: String, aika: Double): Double {

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
            return matkaKm
        } else {
            kilometrit.text = "0"
            return 0.0
        }
    }
}