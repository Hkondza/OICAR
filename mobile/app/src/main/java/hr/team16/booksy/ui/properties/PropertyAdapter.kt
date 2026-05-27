package hr.team16.booksy.ui.properties

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import hr.team16.booksy.R
import hr.team16.booksy.model.Property

class PropertyAdapter(
    private val properties: List<Property>,
    private val onViewRooms: (Property) -> Unit
) : RecyclerView.Adapter<PropertyAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivImage: ImageView = view.findViewById(R.id.ivPropertyImage)
        val tvName: TextView = view.findViewById(R.id.tvPropertyName)
        val tvCity: TextView = view.findViewById(R.id.tvPropertyCity)
        val tvAddress: TextView = view.findViewById(R.id.tvPropertyAddress)
        val btnViewRooms: Button = view.findViewById(R.id.btnViewRooms)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_property, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val property = properties[position]
        holder.tvName.text = property.name
        holder.tvCity.text = "📍 ${property.city}, ${property.country}"
        holder.tvAddress.text = property.address
        holder.btnViewRooms.setOnClickListener { onViewRooms(property) }

        if (!property.imageUrl.isNullOrEmpty()) {
            holder.ivImage.visibility = View.VISIBLE
            Glide.with(holder.ivImage.context)
                .load(property.imageUrl)
                .centerCrop()
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(holder.ivImage)
        } else {
            holder.ivImage.visibility = View.GONE
        }
    }

    override fun getItemCount() = properties.size
}