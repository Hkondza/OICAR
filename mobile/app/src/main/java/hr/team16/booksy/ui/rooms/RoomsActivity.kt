package hr.team16.booksy.ui.rooms

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import hr.team16.booksy.R
import hr.team16.booksy.api.RetrofitClient
import hr.team16.booksy.ui.reservation.ReservationActivity
import hr.team16.booksy.utils.SessionManager
import kotlinx.coroutines.launch

class RoomsActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rooms)

        sessionManager = SessionManager(this)

        val propertyId = intent.getLongExtra("propertyId", -1)
        val propertyName = intent.getStringExtra("propertyName") ?: "Sobe"

        findViewById<TextView>(R.id.tvPropertyName).text = propertyName

        val rvRooms = findViewById<RecyclerView>(R.id.rvRooms)
        rvRooms.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            try {
                val rooms = RetrofitClient.instance.getRoomsByProperty(sessionManager.getBearerToken(),propertyId)
                val adapter = RoomAdapter(rooms) { room ->
                    val intent = Intent(this@RoomsActivity, ReservationActivity::class.java)
                    intent.putExtra("roomId", room.id)
                    intent.putExtra("roomName", room.name)
                    intent.putExtra("pricePerNight", room.pricePerNight.toDouble())
                    intent.putExtra("availableFrom",room.availableFrom)
                    intent.putExtra("availableTo",room.availableTo)
                    startActivity(intent)
                }
                rvRooms.adapter = adapter
            } catch (e: Exception) {
                Toast.makeText(this@RoomsActivity,
                    "Greška pri učitavanju soba", Toast.LENGTH_SHORT).show()
            }
        }
    }
}