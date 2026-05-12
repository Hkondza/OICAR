package hr.team16.booksy.ui.reservation

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.view.CalendarView
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import com.kizitonwose.calendar.view.ViewContainer
import hr.team16.booksy.R
import hr.team16.booksy.api.RetrofitClient
import hr.team16.booksy.model.ReservationRequest
import hr.team16.booksy.model.ReservationResponse
import hr.team16.booksy.model.Room
import hr.team16.booksy.utils.SessionManager
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*

class ReservationActivity : AppCompatActivity() {


    private var avalibleFrom: String? = null
    private var avalibleTo: String? = null
    private lateinit var room: Room
    private lateinit var sessionManager: SessionManager
    private var checkInDate: LocalDate? = null
    private var checkOutDate: LocalDate? = null
    private var pricePerNight: Double = 0.0
    private var bookedDates: Set<LocalDate> = emptySet()
    private var roomId: Long = -1
    private lateinit var calendarView: CalendarView
    private lateinit var tvSelectedDates: TextView
    private lateinit var cardTotal: CardView
    private lateinit var tvNights: TextView
    private lateinit var tvTotalPrice: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reservation)

        sessionManager = SessionManager(this)



        roomId = intent.getLongExtra("roomId", -1)
        val roomName = intent.getStringExtra("roomName") ?: ""
        pricePerNight = intent.getDoubleExtra("pricePerNight", 0.0)

        avalibleTo = intent.getStringExtra("availableTo")
        avalibleFrom = intent.getStringExtra("availableFrom")

        val tvRoomName = findViewById<TextView>(R.id.tvRoomName)
        val tvPricePerNight = findViewById<TextView>(R.id.tvPricePerNight)
        calendarView = findViewById(R.id.calendarView)
        tvSelectedDates = findViewById(R.id.tvSelectedDates)
        cardTotal = findViewById(R.id.cardTotal)
        tvNights = findViewById(R.id.tvNights)
        tvTotalPrice = findViewById(R.id.tvTotalPrice)
        val btnReserve = findViewById<Button>(R.id.btnReserve)

        tvRoomName.text = roomName
        tvPricePerNight.text = "💰 $pricePerNight € / noć"

        setupCalendar()

        // Dohvati zauzete datume
        lifecycleScope.launch {
            try {
                val reservations = RetrofitClient.instance.getReservationsByRoomId(
                    sessionManager.getBearerToken(),
                    roomId
                )
                bookedDates = getBookedDates(reservations)
                calendarView.notifyCalendarChanged()
            } catch (e: Exception) {
                Toast.makeText(this@ReservationActivity,
                    "Ne mogu učitati zauzete datume", Toast.LENGTH_SHORT).show()
            }
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
                        ReservationRequest(
                            roomId,
                            checkInDate.toString(),
                            checkOutDate.toString()
                        )
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

    private fun setupCalendar() {
        val currentMonth = YearMonth.from(LocalDate.parse(avalibleFrom))
        val endMonth = YearMonth.from(LocalDate.parse(avalibleTo))
        val daysOfWeek = daysOfWeek()

        calendarView.setup(currentMonth, endMonth, daysOfWeek.first())
        calendarView.scrollToMonth(currentMonth)

        calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)

            override fun bind(container: DayViewContainer, data: CalendarDay) {
                val tvDay = container.tvDay
                tvDay.text = data.date.dayOfMonth.toString()

                if (data.position != DayPosition.MonthDate) {
                    tvDay.setTextColor(Color.LTGRAY)
                    tvDay.background = null
                    container.view.setOnClickListener(null)
                    return
                }

                val today = LocalDate.now()
                val date = data.date

                when {
                    date.isBefore(today) -> {
                        tvDay.setTextColor(Color.LTGRAY)
                        tvDay.background = null
                        container.view.setOnClickListener(null)
                    }

                    bookedDates.contains(date) -> {
                        tvDay.setTextColor(Color.WHITE)
                        tvDay.background = createCircleBackground(Color.parseColor("#B00020"))
                        container.view.setOnClickListener(null)
                    }

                    date == checkInDate || date == checkOutDate -> {
                        tvDay.setTextColor(Color.WHITE)
                        tvDay.background = createCircleBackground(Color.parseColor("#0F6E56"))
                        container.view.setOnClickListener { onDateClicked(date) }
                    }

                    checkInDate != null && checkOutDate != null &&
                            date.isAfter(checkInDate) && date.isBefore(checkOutDate) -> {
                        tvDay.setTextColor(Color.WHITE)
                        tvDay.background = createCircleBackground(Color.parseColor("#4CAF50"))
                        container.view.setOnClickListener { onDateClicked(date) }
                    }

                    else -> {
                        tvDay.setTextColor(Color.parseColor("#1A3557"))
                        tvDay.background = null
                        container.view.setOnClickListener { onDateClicked(date) }
                    }
                }
            }
        }

        calendarView.monthHeaderBinder =
            object : MonthHeaderFooterBinder<MonthHeaderContainer> {
                override fun create(view: View) = MonthHeaderContainer(view)

                override fun bind(container: MonthHeaderContainer, data: CalendarMonth) {
                    val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale("hr"))
                    container.tvMonthYear.text =
                        data.yearMonth.format(formatter).uppercase()

                    container.btnPrev.setOnClickListener {
                        calendarView.findFirstVisibleMonth()?.let {
                            calendarView.smoothScrollToMonth(it.yearMonth.minusMonths(1))
                        }
                    }

                    container.btnNext.setOnClickListener {
                        calendarView.findFirstVisibleMonth()?.let {
                            calendarView.smoothScrollToMonth(it.yearMonth.plusMonths(1))
                        }
                    }
                }
            }
    }

    private fun onDateClicked(date: LocalDate) {
        when {
            checkInDate == null -> {
                checkInDate = date
                calendarView.notifyCalendarChanged()
                tvSelectedDates.text = "Odaberite datum odlaska"
            }

            checkOutDate == null -> {
                if (date.isBefore(checkInDate) || date == checkInDate) {
                    checkInDate = date
                    calendarView.notifyCalendarChanged()
                    return
                }

                val hasBookedInRange = bookedDates.any { booked ->
                    booked.isAfter(checkInDate) && booked.isBefore(date)
                }

                if (hasBookedInRange) {
                    Toast.makeText(this,
                        "Raspon sadrži zauzete datume!", Toast.LENGTH_SHORT).show()
                    checkInDate = null
                    calendarView.notifyCalendarChanged()
                    tvSelectedDates.text = "Odaberite datum dolaska i odlaska"
                    cardTotal.visibility = View.GONE
                    return
                }

                checkOutDate = date
                calendarView.notifyCalendarChanged()

                tvSelectedDates.text =
                    "✅ Dolazak: $checkInDate  →  Odlazak: $checkOutDate"

                val nights = checkInDate!!.until(checkOutDate!!).days.toLong()
                val total = nights * pricePerNight
                tvNights.text = "Broj noći: $nights"
                tvTotalPrice.text = "Ukupno: $total €"
                cardTotal.visibility = View.VISIBLE
            }

            else -> {
                checkInDate = date
                checkOutDate = null
                calendarView.notifyCalendarChanged()
                tvSelectedDates.text = "Odaberite datum odlaska"
                cardTotal.visibility = View.GONE
            }
        }
    }

    private fun createCircleBackground(color: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(color)
        }
    }

    private fun getBookedDates(reservations: List<ReservationResponse>): Set<LocalDate> {
        val dates = mutableSetOf<LocalDate>()
        reservations
            .filter { it.status != "CANCELLED" && it.status != "DENIED" }
            .forEach { reservation ->
                var date = LocalDate.parse(reservation.checkIn)
                val endDate = LocalDate.parse(reservation.checkOut)
                while (date.isBefore(endDate)) {
                    dates.add(date)
                    date = date.plusDays(1)
                }
            }
        return dates
    }
}

class DayViewContainer(v: View) : ViewContainer(v) {
    val tvDay: TextView = v.findViewById(R.id.tvDay)
}

class MonthHeaderContainer(v: View) : ViewContainer(v) {
    val tvMonthYear: TextView = v.findViewById(R.id.tvMonthYear)
    val btnPrev: Button = v.findViewById(R.id.btnPrevMonth)
    val btnNext: Button = v.findViewById(R.id.btnNextMonth)
}