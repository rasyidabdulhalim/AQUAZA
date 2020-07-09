package com.rasyidabdulhalim.aquaza.activities

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.v7.app.AlertDialog
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.mapbox.android.core.location.*
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponent
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.rasyidabdulhalim.aquaza.R
import com.rasyidabdulhalim.aquaza.commoners.AppUtils
import com.rasyidabdulhalim.aquaza.commoners.BaseActivity
import com.rasyidabdulhalim.aquaza.commoners.K
import com.rasyidabdulhalim.aquaza.models.Depot
import com.rasyidabdulhalim.aquaza.utils.*
import com.rasyidabdulhalim.aquaza.models.Order
import com.synnapps.carouselview.ImageListener
import kotlinx.android.synthetic.main.depot_activity.*
import com.rasyidabdulhalim.aquaza.utils.PreferenceHelper.get
import kotlinx.android.synthetic.main.depot_activity.locationNewDepot
import kotlinx.android.synthetic.main.depot_activity.toolbar
import org.jetbrains.anko.toast
import timber.log.Timber
import java.text.NumberFormat
import java.util.*

class DepotActivity : BaseActivity(), ImageListener, View.OnClickListener, DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener,
    OnMapReadyCallback, PermissionsListener {
    private lateinit var depot: Depot
    private lateinit var datePicker: DatePickerDialog
    private lateinit var timePicker: TimePickerDialog
    private lateinit var datePicked: String
    private lateinit var timePicked: String
    private lateinit var prefs: SharedPreferences
    private lateinit var KEY: String
    private val order = Order()
    private var localeID: Locale = Locale("in", "ID")
    private var formatRupiah : NumberFormat = NumberFormat.getCurrencyInstance(localeID)
    private var mapView: MapView? = null
    private var mapboxMap: MapboxMap? = null
    private var locationComponent: LocationComponent? = null
    private var permissionsManager: PermissionsManager? = null
    private var locationEngine: LocationEngine? = null
    private var callback: DepotActivity.LocationChangeListeningCallback? = null
    private var subTotal:Double = 0.0

    private inner class LocationChangeListeningCallback : LocationEngineCallback<LocationEngineResult> {

        override fun onSuccess(result: LocationEngineResult?) {
            result?.lastLocation ?: return
            if (result.lastLocation != null) {
                val lat = result.lastLocation?.latitude!!
                val lng = result.lastLocation?.longitude!!
                val latLng = LatLng(lat, lng)
                mapboxMap!!.locationComponent.forceLocationUpdate(result.lastLocation)
                val position = CameraPosition.Builder()
                    .target(latLng)
                    .tilt(10.0)
                    .build()
                mapboxMap!!.animateCamera(CameraUpdateFactory.newCameraPosition(position))
                lngtv.text=lng.toString()
                lattv.text=lat.toString()
                //   Toast.makeText(this@MainActivity, "Location update : $latLng", Toast.LENGTH_SHORT).show()
            }

        }

        override fun onFailure(exception: Exception) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, getString(R.string.token))
        setContentView(R.layout.depot_activity)
        mapView = findViewById(R.id.mapView)
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync(this)

        prefs = PreferenceHelper.defaultPrefs(this)
        depot = intent.getSerializableExtra(K.DEPOT) as Depot

        initViews()

        initActions()
        loadCarInfo()
    }

    private fun initViews() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = null

        if (depot.id!=null){
            locationNewDepot.text= Editable.Factory.getInstance().newEditable(prefs[K.ADDRESS,""])
            priceTextView.text=formatRupiah.format(depot.price?.toDouble())
            totalPriceTextView.text= formatRupiah.format(depot.price?.toDouble())
        }
        toolbarTitle()

        if (depot.sellerId == getUid()) isMyCar() else notMyCar()

        contactSeller.setOnClickListener(this)
        testDrive.setOnClickListener(this)
        editdepot.setOnClickListener(this)

        val cal = Calendar.getInstance()
        datePicker = DatePickerDialog(this, this, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
        timePicker = TimePickerDialog(this, this, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true)
    }

    private fun toolbarTitle() {
        toolbarLayout.title = ""
        appBar.addOnOffsetChangedListener(object : AppBarLayout.OnOffsetChangedListener{
            var showTitle = true
            var scrollRange = -1

            override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
                if (scrollRange == -1) {
                    scrollRange = appBar.totalScrollRange
                }

                if (scrollRange + verticalOffset == 0) {
                    toolbarLayout.setCollapsedTitleTextColor(Color.WHITE)
                    toolbarLayout.title = "${depot.depotName}"
                    showTitle = true
                } else if (showTitle) {
                    toolbarLayout.title = ""
                    showTitle = false

                }
            }
        })
    }

    private fun loadCarInfo() {
        sellerPhone.text = depot.phone
        sellerLocation.text = depot.location
    }

    override fun setImageForPosition(position: Int, imageView: ImageView?) {
        val keys = depot.images.keys.toList()

        imageView!!.scaleType =ImageView.ScaleType.CENTER_CROP
        imageView.loadUrl(depot.images[keys[position]]!!)
    }

    // Show delete button & hide other options
    private fun isMyCar() {
        testDrive.hideView()
        contactSeller.hideView()
        editdepot.showView()
    }

    // Hide delete button and show other options
    private fun notMyCar() {
        editdepot.hideView()
        testDrive.showView()
        contactSeller.showView()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            android.R.id.home -> onBackPressed()
        }

        return true
    }

    private fun initActions() {
        moreImageView.setOnClickListener { Toast.makeText(this, "Clicked More.", Toast.LENGTH_SHORT).show() }
        plusImageView.setOnClickListener{
            try {
                var value = Integer.parseInt(qtyEditText.text.toString())
                value++
                qtyEditText.setText(value.toString())

                val itemPrice = depot.price
                subTotal = (value * itemPrice!!.toInt()).toDouble()

                totalPriceTextView.text = formatRupiah.format(subTotal.toDouble())
            } catch (ignored: Exception) { }
        }
        minusImageView.setOnClickListener {
            var value = Integer.parseInt(qtyEditText.text.toString())
            if (value > 1) {
                value -= 1
            }
            qtyEditText.setText(value.toString())

            val itemPrice = depot.price?.toInt()
            subTotal = (value * itemPrice!!).toDouble()
            totalPriceTextView.text = formatRupiah.format(subTotal.toDouble())
        }
        qtyEditText.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {
                try {
                    var value = Integer.parseInt(s.toString())
                    val itemPrice = depot.price?.toInt()
                    subTotal = (value * itemPrice!!).toDouble()
                    totalPriceTextView.text = formatRupiah.format(subTotal.toDouble())
                }catch (ignored:Exception){}
            }

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                try {
                    var value = Integer.parseInt(s.toString())
                    val itemPrice = depot.price?.toInt()
                    subTotal = (value * itemPrice!!).toDouble()
                    totalPriceTextView.text = formatRupiah.format(subTotal.toDouble())
                }catch (ignored:Exception){}
            }
        })
    }
    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.contactSeller -> {
                val i = Intent(this, ChatActivity::class.java)
                i.putExtra(K.MY_ID, getUid())
                i.putExtra(K.OTHER_ID, depot.sellerId)
                i.putExtra(K.CHAT_NAME, depot.sellerName)
                startActivity(i)
                AppUtils.animateFadein(this)
            }

            R.id.testDrive -> {
                val view = layoutInflater.inflate(R.layout.depot_make_order,null,false)

                val quantity = view.findViewById<EditText>(R.id.orderQuantity)
                val location = view.findViewById<EditText>(R.id.typenotification)
                val desc = view.findViewById<EditText>(R.id.noteOrder)
                val price = view.findViewById<TextView>(R.id.priceOrder)

                quantity.text=qtyEditText.text
                location.text=locationNewDepot.text
                desc.text=note.text
                price.text=totalPriceTextView.text
                AlertDialog.Builder(this).setTitle("Information Pemesanan").setView(view).setPositiveButton("ORDER") { _, _->
                    if (!AppUtils.validated(quantity,location,desc)){
                        return@setPositiveButton
                    }
                    makeOrder()
                }.setNegativeButton("CANCEL",null).create().show()

            }

            R.id.editdepot -> {
                val i = Intent(this, AddDepotActivity::class.java)
                i.putExtra(K.DEPOT, depot)
                startActivity(i)
                AppUtils.animateFadein(this)
            }

        }
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        val cal = Calendar.getInstance()
        cal.set(year, month, dayOfMonth)
        datePicked = TimeFormatter().getNormalYear(cal)
        timePicker.show()
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        timePicked = "$hourOfDay:$minute"
    }

    private fun makeOrder() {
        Timber.e("Uploading Orders to Firestore")
        showLoading("Pesanan Sedang Di Proses...")
        KEY = getFirestore().collection(K.ORDERS).document().id

        order.id = KEY
        order.buyerId = getUid()
        order.buyerName = prefs[K.NAME]
        order.depotId = depot.id
        order.depotName = depot.sellerName
        order.time = System.currentTimeMillis()
        order.quantity = (qtyEditText.text.toString()).toInt()
        order.price = (order.quantity!! * 4000).toString()
        order.location = locationNewDepot.text.toString().trim()
        order.description = note.text.toString().trim()
        order.description = note.text.toString().trim()
        order.status = K.ONREQUEST
        order.lng=lngtv.text.toString().toDouble()
        order.lat=lattv.text.toString().toDouble()
        getFirestore().collection(K.ORDERS).document(KEY).set(order)
            .addOnSuccessListener {
                Timber.e("Order Success,Silahkan Tunggu")
                hideLoading()

                toast("Terimakasih Telah Memesan")
                val i = Intent(this, MainActivity::class.java)
                startActivity(i)
            }
            .addOnFailureListener {
                Timber.e("Error uploading: $it")
                hideLoading()

                toast("Order Gagal. Coba Lagi")
            }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        AppUtils.animateEnterLeft(this)
    }

    @SuppressLint("MissingPermission")
    private fun initLocationEngine() {
        locationEngine = LocationEngineProvider.getBestLocationEngine(this)
        val request = LocationEngineRequest
            .Builder(CompanionString.DEFAULT_INTERVAL_IN_MILLISECONDS)
            .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
            .setMaxWaitTime(CompanionString.DEFAULT_MAX_WAIT_TIME)
            .build()
        locationEngine!!.requestLocationUpdates(request, callback!!, mainLooper)
        locationEngine!!.getLastLocation(callback!!)
    }

    @SuppressLint("MissingPermission")
    private fun enableLocationComponent(loadedMapStyle: Style) { // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            val locationComponentActivationOptions = LocationComponentActivationOptions.builder(this, loadedMapStyle)
                .useDefaultLocationEngine(false)
                .build()
            locationComponent = mapboxMap!!.locationComponent
            mapboxMap!!.locationComponent.apply {
                activateLocationComponent(locationComponentActivationOptions)
                isLocationComponentEnabled = true                       // Enable to make component visible
                cameraMode = CameraMode.TRACKING                        // Set the component's camera mode
                renderMode = RenderMode.COMPASS                         // Set the component's render mode
            }
            initLocationEngine()
        } else {
            permissionsManager = PermissionsManager(this)
            permissionsManager!!.requestLocationPermissions(this)
        }
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
        callback = LocationChangeListeningCallback()
        mapboxMap.setStyle(Style.MAPBOX_STREETS) { style ->
            enableLocationComponent(style)
        }
    }

    override fun onExplanationNeeded(permissionsToExplain: List<String>) {
        Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show()
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            enableLocationComponent(mapboxMap!!.style!!)
        } else {
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show()
            finish()
        }
    }
}
