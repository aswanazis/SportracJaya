package com.tajayajaya.sportracjaya.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.tajayajaya.sportracjaya.R
import com.tajayajaya.sportracjaya.db.Run
import com.tajayajaya.sportracjaya.extras.Constants.ACTION_PAUSE_SERVICE
import com.tajayajaya.sportracjaya.extras.Constants.ACTION_START_OR_RESUME_SERVICE
import com.tajayajaya.sportracjaya.extras.Constants.ACTION_STOP_SERVICE
import com.tajayajaya.sportracjaya.extras.Constants.MAP_ZOOM
import com.tajayajaya.sportracjaya.extras.Constants.POLYLINE_COLOR
import com.tajayajaya.sportracjaya.extras.Constants.POLYLINE_WIDTH
import com.tajayajaya.sportracjaya.extras.TrackingUtility
import com.tajayajaya.sportracjaya.services.Polyline
import com.tajayajaya.sportracjaya.services.TrackingService
import com.tajayajaya.sportracjaya.viewmodels.MainViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_tracking.btnFinishRun
import kotlinx.android.synthetic.main.fragment_tracking.btnToggleRun
import kotlinx.android.synthetic.main.fragment_tracking.mapView
import kotlinx.android.synthetic.main.fragment_tracking.tvTimer
import java.util.Calendar
import javax.inject.Inject
import kotlin.math.round

const val CANCEL_TRACKING_DIALOG_TAG = "CancelDialog"
@AndroidEntryPoint
class TrackingFragment : Fragment(R.layout.fragment_tracking) {

    private val viewModel: MainViewModel by viewModels()
    private var isTracking = false
    private var pathPoints = mutableListOf<Polyline>()

    private var map: GoogleMap? = null

    private var curTimeInMillis = 0L

    private var menu: Menu? = null

    @set:Inject
    private var weight = 80f

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView.onCreate(savedInstanceState)
        btnToggleRun.setOnClickListener {
            toggleRun()
        }

        if (savedInstanceState != null){
            val cancelTrackingDIalog = parentFragmentManager.findFragmentByTag(
                CANCEL_TRACKING_DIALOG_TAG) as CancelTrackingDialog?
            cancelTrackingDIalog?.setYesListener {
                stopRun()
            }
        }

        btnFinishRun.setOnClickListener {
            zoomToSeeWholeTrack()
            endRunAndSaveToDb()
        }

        mapView.getMapAsync {
            map = it
            addAllPolylines()
        }

        subscribeToObservers()
    }

    private fun subscribeToObservers() {
        TrackingService.isTracking.observe(viewLifecycleOwner, Observer {
            updateTracking(it)
        })

        TrackingService.pathPoints.observe(viewLifecycleOwner, Observer {
            pathPoints = it
            addLatestPolyline()
            moveCameraToUser()
        })

        TrackingService.timeRunInMillis.observe(viewLifecycleOwner, Observer {
            curTimeInMillis = it
            val formattedTime = TrackingUtility.getFormattedStopWatchTime(curTimeInMillis, true)
            tvTimer.text = formattedTime
        })
    }

    private fun toggleRun() {
        if (isTracking) {
            menu?.getItem(0)?.isVisible = true
            sendCommandToService(ACTION_PAUSE_SERVICE)
        } else {
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.toolbar_tracking_menu, menu)
        this.menu = menu
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        if (curTimeInMillis > 0L) {
            this.menu?.getItem(0)?.isVisible = true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.miCancelTracking -> {
                showCancelTrackingDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showCancelTrackingDialog() {
        CancelTrackingDialog().apply {
            setYesListener {
                stopRun()
            }
        }.show(parentFragmentManager, CANCEL_TRACKING_DIALOG_TAG)
    }

    private fun stopRun() {
        tvTimer.text = "00:00:00:00"
        sendCommandToService(ACTION_STOP_SERVICE)
        findNavController().navigate(R.id.action_trackingFragment_to_runFragment)
    }

    private fun updateTracking(isTracking: Boolean) {
        this.isTracking = isTracking
        if (!isTracking && curTimeInMillis > 0L) {
            btnToggleRun.text = "Start"
            btnFinishRun.visibility = View.VISIBLE
        } else if (isTracking) {
            btnToggleRun.text = "Stop"
            menu?.getItem(0)?.isVisible = true
            btnFinishRun.visibility = View.GONE
        }
    }

    private fun moveCameraToUser() {
        if (pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()) {
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoints.last().last(),
                    MAP_ZOOM
                )
            )
        }
    }

    private fun zoomToSeeWholeTrack() {
        val bounds = LatLngBounds.Builder()
        for (polyline in pathPoints) {
            for (pos in polyline) {
                bounds.include(pos)
            }
        }

        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                mapView.width,
                mapView.height,
                (mapView.height * 0.05f).toInt()
            )
        )
    }

    private fun endRunAndSaveToDb() {
        map?.snapshot { bmp ->
            var distanceInMeters = 0
            for (polyline in pathPoints) {
                distanceInMeters += TrackingUtility.calculatePolylineLength(polyline).toInt()
            }
            val avgSpeed =
                round((distanceInMeters / 1000f) / (curTimeInMillis / 1000f / 60 / 60) * 10) / 10f
            val dateTimestamp = Calendar.getInstance().timeInMillis
            val caloriesBurned = ((distanceInMeters / 1000f) * weight).toInt()
            val run =
                Run(bmp, dateTimestamp, avgSpeed, distanceInMeters, curTimeInMillis, caloriesBurned)
            viewModel.insertRun(run)
            Snackbar.make(
                requireActivity().findViewById(R.id.rootView),
                "Run saved successfully",
                Snackbar.LENGTH_LONG
            ).show()
            stopRun()
        }
    }

    private fun addAllPolylines() {
        for (polyline in pathPoints) {
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .addAll(polyline)
            map?.addPolyline(polylineOptions)
        }
    }

    private fun addLatestPolyline() {
        if (pathPoints.isNotEmpty() && pathPoints.last().size > 1) {
            val preLastLatLng = pathPoints.last()[pathPoints.last().size - 2]
            val lastLatLng = pathPoints.last().last()
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .add(preLastLatLng)
                .add(lastLatLng)
            map?.addPolyline(polylineOptions)
        }
    }

    private fun sendCommandToService(action: String) =
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }

}


/*
private val viewModel: MainViewModel by viewModels()
private var isTracking = false
private var pathPoints = mutableListOf<Polyline>()
private var map: GoogleMap? = null
private var curTimeInMillis = 0L
private var menu: Menu? = null
private var weight = 80f //currently we care getting the default value of the weight

override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
): View? {
    setHasOptionsMenu(true)
    return super.onCreateView(inflater, container, savedInstanceState)
}

override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    mapView.onCreate(savedInstanceState)
    btnToggleRun.setOnClickListener {
        toggleRun()
    }
    btnFinishRun.setOnClickListener {
        zoomToSeeWholeTrack()
        endRunAndSaveToDb()
    }
    mapView.getMapAsync {
        map = it
        addAllPolyLines() //called only when fragment created
    }
    subscribeToObservers()
}

//function to subscribe live dataObject
private fun subscribeToObservers() {
//        observable data for map
    TrackingService.isTracking.observe(viewLifecycleOwner, Observer {
        updateTracking(it)
    })
    TrackingService.pathPoints.observe(viewLifecycleOwner, Observer {
        pathPoints = it
        addLatestPolyLine()
        moveCameraToUser()
    })
//        observable data for the stopwatch
    TrackingService.timeRunInMillis.observe(viewLifecycleOwner, Observer {
        curTimeInMillis = it
        val formattedTime = TrackingUtility.getFormattedStopWatchTime(curTimeInMillis, false)
        tvTimer.text = formattedTime
    })
}

//used to start to stop toggleRun or service
private fun toggleRun() {
    if (isTracking) {
        menu?.get(0)?.isVisible = true
        sendCommandToService(ACTION_PAUSE_SERVICE)
    } else {
        sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
    }
}

override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
    super.onCreateOptionsMenu(menu, inflater)
    inflater.inflate(R.menu.toolbar_tracking_menu, menu)
    this.menu = menu
}

override fun onPrepareOptionsMenu(menu: Menu) {
    super.onPrepareOptionsMenu(menu)
    if (curTimeInMillis > 0L) {
        this.menu?.getItem(0)?.isVisible = true
    }
}

override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
        R.id.miCancelTracking -> {
            showCancelTrackingDialog()
        }
    }
    return super.onOptionsItemSelected(item)
}

private fun showCancelTrackingDialog() {
    val dialog = MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
        .setTitle("Cancel the Run?")
        .setMessage("Are you sure to cancel the current run and delete all its data?")
        .setIcon(R.drawable.ic_delete)
        .setPositiveButton("Yes") { _, _ ->
            stopRun()
        }
        .setNegativeButton("No") { dialogInterface, _ ->
            dialogInterface.cancel()
        }
        .create()
    dialog.show()
}

private fun stopRun() {
    sendCommandToService(ACTION_STOP_SERVICE)
    findNavController().navigate(R.id.action_trackingFragment_to_runFragment)
}

//updating data from service and reacting from to those changes
private fun updateTracking(isTracking: Boolean) {
    this.isTracking = isTracking
    if (!isTracking) {
        btnToggleRun.text = "START"
        btnFinishRun.visibility = View.VISIBLE
    } else {
        btnToggleRun.text = "STOP"
        menu?.get(0)?.isVisible = true
        btnFinishRun.visibility = View.GONE
    }
}

/**
 * Used to connect the 2 last polyLines (last point with the secondLast point)
 * but when we rotate the device it won't work
 */
private fun addLatestPolyLine() {
    if (pathPoints.isNotEmpty() && pathPoints.last().size > 1) {
        val preLastLatLng = pathPoints.last()[pathPoints.last().size - 2]
        val lastLatLng = pathPoints.last().last()
        val polyLineOptions = PolylineOptions()
            .color(POLYLINE_COLOR)
            .width(POLYLINE_WIDTH)
            .add(preLastLatLng)
            .add(lastLatLng)
        map?.addPolyline(polyLineOptions)
    }
}

//move the camera to the user position whenever new position in the polyline list or pathpoint list
private fun moveCameraToUser() {
    if (pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()) {
        map?.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                pathPoints.last().last(), //coz this is the last position path we got
                MAP_ZOOM
            )
        )
    }
}

private fun zoomToSeeWholeTrack() {
    val bound = LatLngBounds.Builder()
    for(polyline in pathPoints) {
        for(pos in polyline) {
            bound.include(pos)
        }
    }

    map?.moveCamera(
        CameraUpdateFactory.newLatLngBounds(
            bound.build(),
            mapView.width,
            mapView.height,
            (mapView.height * 0.05f).toInt()
        )
    )
}

//    saving data to db
private fun endRunAndSaveToDb() {
    map?.snapshot { bmp ->
        var distanceInMeters = 0
        for(polyline in pathPoints) {
            distanceInMeters += TrackingUtility.calculatePolylineLength(polyline).toInt()
        }
        val avgSpeed = round((distanceInMeters / 1000f) / (curTimeInMillis / 1000f / 60 / 60) * 10) / 10f
        val dateTimestamp = Calendar.getInstance().timeInMillis
        val caloriesBurned = ((distanceInMeters / 1000f) * weight).toInt()
        val run = Run(bmp, dateTimestamp, avgSpeed, distanceInMeters, curTimeInMillis, caloriesBurned)
        viewModel.insertRun(run)
        Snackbar.make(
            requireActivity().findViewById(R.id.rootView),
            "Run saved successfully",
            Snackbar.LENGTH_LONG
        ).show()
        stopRun()
    }
}

// to prevent all the data after rotation we are creating this function
private fun addAllPolyLines() {
    for (polyline in pathPoints) {
        val polylineOptions = PolylineOptions()
            .color(POLYLINE_COLOR)
            .width(POLYLINE_WIDTH)
            .addAll(polyline)
        map?.addPolyline(polylineOptions)
    }
}

private fun sendCommandToService(action: String) =
    Intent(requireContext(), TrackingService::class.java).also {
        it.action = action
        requireContext().startService(it)
    }

override fun onResume() {
    super.onResume()
    mapView?.onResume()
}

override fun onStart() {
    super.onStart()
    mapView?.onStart()
}

override fun onStop() {
    super.onStop()
    mapView?.onStop()
}

override fun onPause() {
    super.onPause()
    mapView?.onPause()
}

override fun onLowMemory() {
    super.onLowMemory()
    mapView?.onLowMemory()
}

override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    mapView?.onSaveInstanceState(outState)
}
}
*/