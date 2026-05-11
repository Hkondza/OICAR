package hr.team16.booksy.ui.reservation

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import hr.team16.booksy.R
import hr.team16.booksy.api.RetrofitClient
import hr.team16.booksy.model.ReservationRequest
import hr.team16.booksy.model.ReservationResponse
import hr.team16.booksy.utils.SessionManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ReservationActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private var checkInDate: String? = null
    private var checkOutDate: String? = null
    private var pricePerNight: Double = 0.0
    private var bookedRanges: List<Pair<Long, Long>> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reservation)

        sessionManager = SessionManager(this)

        val roomId = intent.getLongExtra("roomId", -1)
        val roomName = intent.getStringExtra("roomName") ?: ""
        pricePerNight = intent.getDoubleExtra("pricePerNight", 0.0)

        val tvRoomName = findViewById<TextView>(R.id.tvRoomName)
        val tvPricePerNight = findViewById<TextView>(R.id.tvPricePerNight)
        val btnSelectDates = findViewById<Button>(R.id.btnSelectDates)
        val tvSelectedDates = findViewById<TextView>(R.id.tvSelectedDates)
        val cardTotal = findViewById<CardView>(R.id.cardTotal)
        val tvNights = findViewById<TextView>(R.id.tvNights)
        val tvTotalPrice = findViewById<TextView>(R.id.tvTotalPrice)
        val btnReserve = findViewById<Button>(R.id.btnReserve)
        val cardBookedDates = findViewById<CardView>(R.id.cardBookedDates)
        val tvBookedDates = findViewById<TextView>(R.id.tvBookedDates)

        tvRoomName.text = roomName
        tvPricePerNight.text = "💰 $pricePerNight € / noć"

        // Dohvati zauzete datume
        lifecycleScope.launch {
            try {
                val reservations = RetrofitClient.instance.getReservationsByRoom(
                    sessionManager.getBearerToken(),
                    roomId
                )

                bookedRanges = getBookedRanges(reservations)

                if (bookedRanges.isNotEmpty()) {
                    cardBookedDates.visibility = View.VISIBLE
                    tvBookedDates.text = formatBookedDates(reservations)
                }

            } catch (e: Exception) {
                // Nastavi bez zauzetih datuma
            }
        }

        btnSelectDates.setOnClickListener {
            showDateRangePicker(tvSelectedDates, cardTotal, tvNights, tvTotalPrice)
        }

        btnReserve.setOnClickListener {
            if (checkInDate == null || checkOutDate == null) {
                Toast.makeText(this, "Odaberite datume", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val response = RetrofitClient.instance.createReservation(
                        sessionManager.getBearerToken(),
                        ReservationRequest(roomId, checkInDate!!, checkOutDate!!)
                    )
                    Toast.makeText(this@ReservationActivity,
                        "Rezervacija uspješna! Status: ${response.status}",
                        Toast.LENGTH_LONG).show()
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(this@ReservationActivity,
                        "Greška. Soba možda nije dostupna.",
                        Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showDateRangePicker(
        tvSelectedDates: TextView,
        cardTotal: CardView,
        tvNights: TextView,
        tvTotalPrice: TextView
    ) {
        // Onemogući prošle datume
        val constraints = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointForward.now())
            .build()

        val picker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Odaberite termine")
            .setCalendarConstraints(constraints)
            .build()

        picker.addOnPositiveButtonClickListener { selection ->
            val startMs = selection.first
            val endMs = selection.second

            // Provjeri jesu li odabrani datumi zauzeti
            if (isOverlappingWithBooked(startMs, endMs)) {
                Toast.makeText(this,
                    "Odabrani termini se preklapaju sa zauzetim datumima!",
                    Toast.LENGTH_LONG).show()
                return@addOnPositiveButtonClickListener
            }

            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            checkInDate = sdf.format(Date(startMs))
            checkOutDate = sdf.format(Date(endMs))

            tvSelectedDates.text = "✅ Dolazak: $checkInDate  →  Odlazak: $checkOutDate"

            // Izračunaj ukupno
            val nights = ((endMs - startMs) / (1000 * 60 * 60 * 24)).toInt()
            val total = nights * pricePerNight
            tvNights.text = "Broj noći: $nights"
            tvTotalPrice.text = "Ukupno: $total €"
            cardTotal.visibility = View.VISIBLE
        }

        picker.show(supportFragmentManager, "DATE_PICKER")
    }

    private fun getBookedRanges(
        reservations: List<ReservationResponse>
    ): List<Pair<Long, Long>> {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return reservations
            .filter { it.status != "CANCELLED" && it.status != "DENIED" }
            .mapNotNull { reservation ->
                val inDate = sdf.parse(reservation.checkIn)?.time ?: return@mapNotNull null
                val outDate = sdf.parse(reservation.checkOut)?.time ?: return@mapNotNull null
                Pair(inDate, outDate)
            }
    }

    private fun isOverlappingWithBooked(startMs: Long, endMs: Long): Boolean {
        return bookedRanges.any { (bookedStart, bookedEnd) ->
            startMs < bookedEnd && endMs > bookedStart
        }
    }

    private fun formatBookedDates(reservations: List<ReservationResponse>): String {
        return reservations
            .filter { it.status != "CANCELLED" && it.status != "DENIED" }
            .joinToString("\n") { "• ${it.checkIn} → ${it.checkOut}" }
    }
}