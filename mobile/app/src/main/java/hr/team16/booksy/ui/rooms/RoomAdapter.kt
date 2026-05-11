package hr.team16.booksy.ui.rooms

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import hr.team16.booksy.R
import hr.team16.booksy.model.Room

class RoomAdapter(
    private val rooms: List<Room>,
    private val onReserve: (Room) -> Unit
) : RecyclerView.Adapter<RoomAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvRoomName)
        val tvCapacity: TextView = view.findViewById(R.id.tvRoomCapacity)
        val tvPrice: TextView = view.findViewById(R.id.tvRoomPrice)
        val tvDates: TextView = view.findViewById(R.id.tvRoomDates)
        val btnReserve: Button = view.findViewById(R.id.btnReserve)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_room, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val room = rooms[position]
        holder.tvName.text = room.name
        holder.tvCapacity.text = "👥 Kapacitet: ${room.capacity} osoba"
        holder.tvPrice.text = "💰 ${room.pricePerNight} € / noć"
        holder.tvDates.text = "📅 ${room.availableFrom} — ${room.availableTo}"
        holder.btnReserve.setOnClickListener { onReserve(room) }
    }

    override fun getItemCount() = rooms.size
}