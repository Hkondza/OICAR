package hr.team16.booksy.ui.reservations

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import hr.team16.booksy.R
import hr.team16.booksy.api.RetrofitClient
import hr.team16.booksy.utils.SessionManager
import kotlinx.coroutines.launch

class ReservationsActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reservations)

        sessionManager = SessionManager(this)

        val rvReservations = findViewById<RecyclerView>(R.id.rvReservations)
        rvReservations.layoutManager = LinearLayoutManager(this)

        loadReservations(rvReservations)
    }

    private fun loadReservations(rvReservations: RecyclerView) {
        lifecycleScope.launch {
            try {
                val reservations = RetrofitClient.instance.getMyReservations(
                    sessionManager.getBearerToken()
                )

                val adapter = ReservationAdapter(reservations) { reservation ->
                    cancelReservation(reservation.id, rvReservations)
                }
                rvReservations.adapter = adapter

            } catch (e: Exception) {
                Toast.makeText(this@ReservationsActivity,
                    "Greška pri učitavanju rezervacija", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun cancelReservation(id: Long, rvReservations: RecyclerView) {
        lifecycleScope.launch {
            try {
                RetrofitClient.instance.cancelReservation(
                    sessionManager.getBearerToken(),
                    id,
                    mapOf("status" to "CANCELLED")
                )
                Toast.makeText(this@ReservationsActivity,
                    "Rezervacija otkazana", Toast.LENGTH_SHORT).show()
                loadReservations(rvReservations)
            } catch (e: Exception) {
                Toast.makeText(this@ReservationsActivity,
                    "Greška pri otkazivanju", Toast.LENGTH_SHORT).show()
            }
        }
    }
}