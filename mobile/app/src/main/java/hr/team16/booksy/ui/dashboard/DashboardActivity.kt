package hr.team16.booksy.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import hr.team16.booksy.R
import hr.team16.booksy.ui.login.LoginActivity
import hr.team16.booksy.ui.properties.PropertiesActivity
import hr.team16.booksy.ui.reservations.ReservationsActivity
import hr.team16.booksy.utils.SessionManager

class DashboardActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        sessionManager = SessionManager(this)

        val tvEmail = findViewById<TextView>(R.id.tvEmail)
        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val cardProperties = findViewById<CardView>(R.id.cardProperties)
        val cardReservations = findViewById<CardView>(R.id.cardReservations)

        val email = sessionManager.getEmail()
        tvEmail.text = email
        tvWelcome.text = "Dobrodošao, ${email?.split("@")?.get(0)}! 👋"

        btnLogout.setOnClickListener {
            sessionManager.clearSession()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        cardProperties.setOnClickListener {
            startActivity(Intent(this, PropertiesActivity::class.java))
        }

        cardReservations.setOnClickListener {
            startActivity(Intent(this, ReservationsActivity::class.java))
        }
    }
}