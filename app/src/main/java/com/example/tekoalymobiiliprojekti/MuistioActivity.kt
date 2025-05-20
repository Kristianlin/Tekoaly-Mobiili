package com.example.tekoalymobiiliprojekti

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class MuistioActivity : BaseActivity() {
    override fun getSelectedBottomNavItemId(): Int = R.id.save
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Käytetään BaseActivityn layout-pohjaa ja injektoidaan siihen muistion sisältö
        setContent(R.layout.activity_muistio)

        val editText = findViewById<EditText>(R.id.muistioEditText)
        val button = findViewById<Button>(R.id.tallennaButton)

        val sharedPref = getSharedPreferences("muistio", MODE_PRIVATE)
        val savedText = sharedPref.getString("teksti", "")
        editText.setText(savedText)

        button.setOnClickListener {
            val teksti = editText.text.toString()
            sharedPref.edit().putString("teksti", teksti).apply()
            Toast.makeText(this, "Tallennettu!", Toast.LENGTH_SHORT).show()
        }
    }
}
