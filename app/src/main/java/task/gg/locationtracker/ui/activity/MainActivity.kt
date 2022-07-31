package task.gg.locationtracker.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import task.gg.locationtracker.R
import task.gg.locationtracker.data.db.LocationEntity
import task.gg.locationtracker.databinding.ActivityMainBinding
import task.gg.locationtracker.ui.routescreen.CoordinatesAdapter
import task.gg.locationtracker.utils.LocationHelper
import java.util.*


@Suppress("DEPRECATION")
@AndroidEntryPoint
class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    private val coordinatesAdapter by lazy { CoordinatesAdapter() }
    private var routeDialog: AlertDialog? = null

    private val REQUEST_LOCATION_CODE = 123

    private val googleApiClient: GoogleApiClient by lazy {
        GoogleApiClient.Builder(this)
            .addApi(LocationServices.API)
            .addConnectionCallbacks(object : GoogleApiClient.ConnectionCallbacks {
                override fun onConnected(bundle: Bundle?) {
                    viewModel.onStartClick(this@MainActivity)
                }

                override fun onConnectionSuspended(i: Int) {
                    googleApiClient.connect()
                    viewModel.onResetState()
                }
            })
            .addOnConnectionFailedListener {
                viewModel.onResetState()
            }.build()
    }

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        viewModel.onCheckCurrentState(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setupButtons()
        setupRouteDialog()
        listenUiState()
        viewModel.onCheckCurrentState(this)
    }

    private fun setupRouteDialog() {
        routeDialog = AlertDialog.Builder(this, R.style.Theme_LocationTracker)
            .setView(RecyclerView(this).also {
                it.minimumWidth = (resources.displayMetrics.widthPixels * 0.8f).toInt()
                it.minimumHeight = resources.displayMetrics.heightPixels / 2
                it.layoutManager = LinearLayoutManager(it.context)
                it.adapter = coordinatesAdapter
                it.addItemDecoration(DividerItemDecoration(this, RecyclerView.VERTICAL))
            })
            .create()
    }

    private fun listenUiState() {
        viewModel.uiState.onEach {
            when (it) {
                UiState.Default -> {}
                UiState.RequireAction.REQUEST_PERMISSIONS -> {
                    requestPermissions()
                }
                UiState.ReadyForTracking -> {
                    showCurrentLocation()
                    showLocationButton()
                }
                UiState.RequireAction.ENABLE_LOCATION -> {
                    LocationHelper.enableLocation(this, googleApiClient, REQUEST_LOCATION_CODE)
                }
                UiState.Started -> {
                    binding.btnStopTracking.isEnabled = true
                    binding.btnStartTracking.isEnabled = false
                    showCurrentLocation()
                    showLocationButton()
                }
                UiState.Stopped -> {
                    binding.btnStopTracking.isEnabled = false
                    binding.btnStartTracking.isEnabled = true
                }
            }
            viewModel.onResetState()
        }.launchIn(lifecycleScope)
    }

    private fun requestPermissions() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) || shouldShowRequestPermissionRationale(
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        ) {
            showCustomPermissionsDialog()
        } else {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun showCustomPermissionsDialog() {
        val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle(getString(R.string.permissions_dialog_title))
        alertDialogBuilder
            .setMessage(getString(R.string.permissions_dialog_message))
            .setNegativeButton(getString(R.string.cancel)) { _, _ -> }
            .setPositiveButton(getString(R.string.settings)) { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri: Uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .create()
            .show()
    }

    private fun setupButtons() {
        binding.btnStartTracking.setOnClickListener {
            viewModel.onStartClick(this)
        }
        binding.btnStopTracking.setOnClickListener {
            viewModel.onStopClick(this)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.setOnPolylineClickListener {
            coordinatesAdapter.submitList(it.points)
            routeDialog?.show()
        }

        val colors = listOf(Color.RED, Color.BLACK, Color.BLUE, Color.GREEN, Color.GRAY)
        viewModel.routes.onEach { routes: Map<UUID, List<LocationEntity>> ->
            routes.values.forEach { route ->
                val polyline =
                    PolylineOptions().addAll(route.map { LatLng(it.latitude, it.longitude) })
                        .color(colors.random())
                        .clickable(true)
                map.addPolyline(polyline)
            }
            if (routes.values.isNotEmpty())
                map.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            routes.values.last().last().latitude,
                            routes.values.last().last().longitude,
                        ), 20F
                    )
                )
        }.launchIn(lifecycleScope)
    }

    private fun showLocationButton() {
        if (this::map.isInitialized) {
            map.uiSettings.isMyLocationButtonEnabled = true
        }
    }

    @SuppressLint("MissingPermission")
    private fun showCurrentLocation() {
        if (this::map.isInitialized) {
            map.isMyLocationEnabled = true
            showLocationButton()
            LocationHelper.listenLocationChange(
                this,
                LocationServices.getFusedLocationProviderClient(this)
            ) {
                map.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            it.latitude,
                            it.longitude
                        ), 20F
                    )
                )
            }
        }
    }
}