package com.example.tekoalymobiiliprojekti

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import com.example.tekoalymobiiliprojekti.databinding.ActivityMainBinding

class MainActivity : BaseActivity() {

    //Lupaus alustuksista
    lateinit var reittiBtn: Button
    lateinit var aika: EditText
    private lateinit var binding: ActivityMainBinding

    override fun getSelectedBottomNavItemId(): Int = R.id.home
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent(R.layout.activity_main)

        //Alustukset
        aika = findViewById(R.id.aikaExt)
        reittiBtn = findViewById(R.id.laskeReittiBtn)

        var spinnerTahti: Spinner = findViewById(R.id.tahtiSpinner)

        //Spinnerin komponenttien haku ja valinta mahdollisuudet
        val arrayAdapter: ArrayAdapter<CharSequence> =
            ArrayAdapter.createFromResource(
                this,
                R.array.tahti,
                android.R.layout.simple_spinner_item
            )
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
        spinnerTahti.adapter = arrayAdapter

        //Nappia painettaessa tiedot siirretään KarttaAktiviteettiin.
        reittiBtn.setOnClickListener {

            val valittuAika: String = aika.text.toString()
            val valittuTahti = spinnerTahti.selectedItem.toString()

            // Muistuttaa käyttäjää asettamaan ajan
            if (valittuAika.isEmpty()) {
                Toast.makeText(this, "Anna aika!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intentMatka = Intent(this, KarttaActivity::class.java).apply {
                putExtra("Aika", valittuAika.toDouble())
                putExtra("Tahti", valittuTahti)
            }
            startActivity(intentMatka)
        }
    }
}
