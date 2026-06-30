package com.wisatakita.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.wisatakita.app.data.DestinationRepository
import com.wisatakita.app.databinding.ActivityListBinding
import kotlinx.coroutines.launch

class ListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.progressBar.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE

        lifecycleScope.launch {
            val result = DestinationRepository(this@ListActivity).getDestinationsWithSource()
            val destinations = result.destinations
            binding.progressBar.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
            binding.tvDataSource.text = "${result.sourceLabel} - ${destinations.size} destinasi"

            val adapter = DestinationAdapter(destinations) { destination ->
                val intent = Intent(this@ListActivity, DetailActivity::class.java)
                intent.putExtra("DESTINATION_ID", destination.id)
                startActivity(intent)
            }
            binding.recyclerView.layoutManager = LinearLayoutManager(this@ListActivity)
            binding.recyclerView.adapter = adapter
        }
    }
}
