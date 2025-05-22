package com.example.tekoalymobiiliprojekti

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*
import androidx.appcompat.app.AlertDialog


class MuistioActivity : BaseActivity() {

    private lateinit var selectedDate: String
    private lateinit var editText: EditText
    private lateinit var listView: ListView
    private lateinit var adapter: ArrayAdapter<String>
    private val merkinnat = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent(R.layout.activity_muistio)

        // Haetaan käyttöliittymän elementit XML-tiedostosta
        editText = findViewById(R.id.muistioEditText)
        val button = findViewById<Button>(R.id.tallennaButton)
        val clearButton = findViewById<Button>(R.id.tyhjennaButton)
        val dateButton = findViewById<Button>(R.id.pvmButton)
        val dateTextView = findViewById<TextView>(R.id.valittuPaivaTextView)
        listView = findViewById(R.id.muistioListView)

        // Yhdistetään lista ja sen sisältö adapteriin
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, merkinnat)
        listView.adapter = adapter

        // Jaetaan muistioon tallennettu tieto käyttöön
        val sharedPref = getSharedPreferences("muistio", MODE_PRIVATE)

        // Asetetaan kalenteri ja päivämääräformaatti
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        loadAllEntries(sharedPref)

        // Päivämäärän valintapainikkeen toiminta
        dateButton.setOnClickListener {
            DatePickerDialog(this,
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    selectedDate = dateFormat.format(calendar.time)
                    dateTextView.text = "Valittu: $selectedDate" // Näytetään käyttäjälle
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Tallenna-painikkeen toiminta
        button.setOnClickListener {
            if (!::selectedDate.isInitialized) {
                Toast.makeText(this, "Valitse ensin päivämäärä", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val teksti = editText.text.toString().trim()
            if (teksti.isEmpty()) {
                Toast.makeText(this, "Kirjoita jotain", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Muodostetaan uusi merkintä ja lisätään listan alkuun
            val uusiMerkinta = "$selectedDate: $teksti"
            merkinnat.add(0, uusiMerkinta)
            adapter.notifyDataSetChanged()
            saveAllEntries(sharedPref)
            editText.text.clear()
        }

        // Tyhjennä kaikki -painikkeen toiminta
        clearButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Tyhjennä kaikki?")
                .setMessage("Haluatko varmasti poistaa kaikki merkinnät?")
                .setPositiveButton("Kyllä") { _, _ ->
                    merkinnat.clear()
                    adapter.notifyDataSetChanged()
                    sharedPref.edit().remove("kaikki_merkinnat").apply()
                    Toast.makeText(this, "Kaikki merkinnät poistettu", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Peruuta", null)
                .show()
        }
    }

    // Tämä metodi palauttaa mikä nappi on aktiivinen alavalikossa (BaseActivity vaatii tämän)
    override fun getSelectedBottomNavItemId(): Int {
        return R.id.save // käytä oikeaa ID:tä navibarista, esim. R.id.navigation_muistio
    }

    // Tallennetaan kaikki merkinnät SharedPreferences-muotoon (JSON-listana)
    private fun saveAllEntries(sharedPref: android.content.SharedPreferences) {
        val jsonArray = JSONArray()
        for (merkinta in merkinnat) {
            jsonArray.put(merkinta)
        }
        sharedPref.edit().putString("kaikki_merkinnat", jsonArray.toString()).apply()
    }

    // Ladataan aiemmin tallennetut merkinnät muistista ja lisätään listaan
    private fun loadAllEntries(sharedPref: android.content.SharedPreferences) {
        merkinnat.clear()
        val savedJson = sharedPref.getString("kaikki_merkinnat", "[]")
        val jsonArray = JSONArray(savedJson ?: "[]")
        for (i in 0 until jsonArray.length()) {
            merkinnat.add(jsonArray.getString(i))
        }
        adapter.notifyDataSetChanged()
    }
}
