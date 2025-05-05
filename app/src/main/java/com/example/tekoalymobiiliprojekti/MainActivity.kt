package com.example.tekoalymobiiliprojekti

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    lateinit var button4 : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        button4 = findViewById(R.id.button4)

        button4.setOnClickListener {

            var intent = Intent(this, KarttaActivity::class.java)
            startActivity(intent)

        }
    }

}