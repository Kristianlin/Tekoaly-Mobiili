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

        editText = findViewById(R.id.muistioEditText)
        val button = findViewById<Button>(R.id.tallennaButton)
        val clearButton = findViewById<Button>(R.id.tyhjennaButton)
        val dateButton = findViewById<Button>(R.id.pvmButton)
        val dateTextView = findViewById<TextView>(R.id.valittuPaivaTextView)
        listView = findViewById(R.id.muistioListView)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, merkinnat)
        listView.adapter = adapter

        val sharedPref = getSharedPreferences("muistio", MODE_PRIVATE)
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        loadAllEntries(sharedPref)

        dateButton.setOnClickListener {
            DatePickerDialog(this,
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    selectedDate = dateFormat.format(calendar.time)
                    dateTextView.text = "Valittu: $selectedDate"
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

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

            val uusiMerkinta = "$selectedDate: $teksti"
            merkinnat.add(0, uusiMerkinta)
            adapter.notifyDataSetChanged()
            saveAllEntries(sharedPref)
            editText.text.clear()
        }

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

    // ✅ Tämä pitää olla onCreate:n ULKOPUOLELLA
    override fun getSelectedBottomNavItemId(): Int {
        return R.id.save // käytä oikeaa ID:tä navibarista, esim. R.id.navigation_muistio
    }

    private fun saveAllEntries(sharedPref: android.content.SharedPreferences) {
        val jsonArray = JSONArray()
        for (merkinta in merkinnat) {
            jsonArray.put(merkinta)
        }
        sharedPref.edit().putString("kaikki_merkinnat", jsonArray.toString()).apply()
    }

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
