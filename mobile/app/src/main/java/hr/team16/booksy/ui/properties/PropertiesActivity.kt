package hr.team16.booksy.ui.properties

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import hr.team16.booksy.R
import hr.team16.booksy.api.RetrofitClient
import hr.team16.booksy.ui.rooms.RoomsActivity
import hr.team16.booksy.utils.SessionManager
import kotlinx.coroutines.launch

class PropertiesActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_properties)

        sessionManager = SessionManager(this)

        val rvProperties = findViewById<RecyclerView>(R.id.rvProperties)
        rvProperties.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            try {
                val properties = RetrofitClient.instance.getProperties(sessionManager.getBearerToken())
                val adapter = PropertyAdapter(properties) { property ->
                    val intent = Intent(this@PropertiesActivity, RoomsActivity::class.java)
                    intent.putExtra("propertyId", property.id)
                    intent.putExtra("propertyName", property.name)
                    startActivity(intent)
                }
                rvProperties.adapter = adapter
            } catch (e: Exception) {
                Toast.makeText(this@PropertiesActivity,
                    "Greška pri učitavanju apartmana", Toast.LENGTH_SHORT).show()
            }
        }
    }
}