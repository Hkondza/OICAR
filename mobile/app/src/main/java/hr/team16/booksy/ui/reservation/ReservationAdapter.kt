package hr.team16.booksy.ui.reservations

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import hr.team16.booksy.R
import hr.team16.booksy.model.ReservationResponse

class ReservationAdapter(
    private val reservations: List<ReservationResponse>,
    private val onCancel: (ReservationResponse) -> Unit
) : RecyclerView.Adapter<ReservationAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvProperty: TextView = view.findViewById(R.id.tvReservationProperty)
        val tvRoom: TextView = view.findViewById(R.id.tvReservationRoom)
        val tvDates: TextView = view.findViewById(R.id.tvReservationDates)
        val tvPrice: TextView = view.findViewById(R.id.tvReservationPrice)
        val tvStatus: TextView = view.findViewById(R.id.tvReservationStatus)
        val btnCancel: Button = view.findViewById(R.id.btnCancel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reservation, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val reservation = reservations[position]
        holder.tvProperty.text = reservation.propertyName
        holder.tvRoom.text = "🛏 ${reservation.roomName}"
        holder.tvDates.text = "📅 ${reservation.checkIn} — ${reservation.checkOut}"
        holder.tvPrice.text = "💰 ${reservation.totalPrice} €"

        // Status boja
        when (reservation.status) {
            "PENDING" -> {
                holder.tvStatus.text = "⏳ Na čekanju"
                holder.tvStatus.setTextColor(0xFFFF9800.toInt())
            }
            "CONFIRMED" -> {
                holder.tvStatus.text = "✅ Potvrđeno"
                holder.tvStatus.setTextColor(0xFF0F6E56.toInt())
            }
            "CANCELLED" -> {
                holder.tvStatus.text = "❌ Otkazano"
                holder.tvStatus.setTextColor(0xFFB00020.toInt())
                holder.btnCancel.visibility = View.GONE
            }
            "DENIED" -> {
                holder.tvStatus.text = "🚫 Odbijeno"
                holder.tvStatus.setTextColor(0xFFB00020.toInt())
                holder.btnCancel.visibility = View.GONE
            }
            else -> holder.tvStatus.text = reservation.status
        }

        holder.btnCancel.setOnClickListener { onCancel(reservation) }
    }

    override fun getItemCount() = reservations.size
}