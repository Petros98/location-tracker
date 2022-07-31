package task.gg.locationtracker.ui.routescreen

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng
import task.gg.locationtracker.databinding.ItemCoordinatesLayoutBinding

class CoordinatesAdapter : ListAdapter<LatLng, CoordinatesAdapter.ViewHolder>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemCoordinatesLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        ).apply {
            itemView.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemCoordinatesLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(latLng: LatLng) {
            binding.latitude.text = latLng.latitude.toString()
            binding.longitude.text = latLng.longitude.toString()
        }
    }

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<LatLng>() {
            override fun areItemsTheSame(oldItem: LatLng, newItem: LatLng): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: LatLng, newItem: LatLng): Boolean = true
        }
    }

}
