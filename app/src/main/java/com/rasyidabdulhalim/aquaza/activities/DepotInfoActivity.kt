package com.rasyidabdulhalim.aquaza.activities

import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.widget.ImageView
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.firestore.DocumentChange
import com.leavjenn.smoothdaterangepicker.date.SmoothDateRangePickerFragment
import com.mikepenz.ionicons_typeface_library.Ionicons
import com.rasyidabdulhalim.aquaza.R
import com.rasyidabdulhalim.aquaza.commoners.AppUtils
import com.rasyidabdulhalim.aquaza.commoners.AppUtils.setDrawable
import com.rasyidabdulhalim.aquaza.commoners.BaseActivity
import com.rasyidabdulhalim.aquaza.commoners.ChartLabelsFormatter
import com.rasyidabdulhalim.aquaza.commoners.K
import com.rasyidabdulhalim.aquaza.models.Depot
import com.rasyidabdulhalim.aquaza.models.Order
import com.rasyidabdulhalim.aquaza.utils.*
import com.synnapps.carouselview.ImageListener
import kotlinx.android.synthetic.main.activity_depot_info.*
import org.jetbrains.anko.toast
import timber.log.Timber
import java.text.NumberFormat
import java.util.*


class DepotInfoActivity : BaseActivity(), ImageListener, SmoothDateRangePickerFragment.OnDateRangeSetListener {
    private lateinit var depot: Depot
    private lateinit var prefs: SharedPreferences
    private var localeID: Locale = Locale("in", "ID")
    private var formatRupiah: NumberFormat = NumberFormat.getCurrencyInstance(localeID)


    private lateinit var dateRangePicker: SmoothDateRangePickerFragment
    private var startMonth = 0
    private var endMonth = 0

    var cars = 0
    var parts = 0
    var amnt = 0
    var toyota = 0
    var mazda = 0
    var bmw = 0
    var subaru = 0
    var benz = 0
    var honda = 0
    var totalPendapatan=0
    var totalGalon=0
    var carmonth = emptyArray<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_depot_info)

        prefs = PreferenceHelper.defaultPrefs(this)
        depot = intent.getSerializableExtra(K.DEPOT) as Depot

        initViews()

        showLoading("Loading records...")
        Handler().postDelayed({hideLoading()}, 2000)

        totalSales()
        loadCarInfo()
    }

    private fun initViews() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Sales Reports"

        carousel.pageCount = depot.images.size
        carousel.setImageListener(this)


        dateFilter.setImageDrawable(setDrawable(this, Ionicons.Icon.ion_android_calendar, R.color.textGray, 27))
        date.text = "Report for: Jan 01, 2018 - ${TimeFormatter().getReportTime(System.currentTimeMillis())}"
        dateFilter.hideView()
        dateRangePicker = SmoothDateRangePickerFragment.newInstance(this)
        dateRangePicker.maxDate = Calendar.getInstance()

        dateFilter.setOnClickListener { dateRangePicker.show(fragmentManager, "") }
    }

    private fun loadCarInfo() {
        getFirestore().collection(K.ORDERS)
            .whereEqualTo("depotId",depot.id)
            .whereEqualTo(K.STATUS,K.RECEIVED)
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    Timber.e("Error fetching orders $firebaseFirestoreException")
                }
                if (querySnapshot == null || querySnapshot.isEmpty) {
                    pendapatan.text= formatRupiah.format(totalPendapatan.toDouble())
                    galon.text=totalGalon.toString()
                } else {
                    for (docChange in querySnapshot.documentChanges) {
                        val order = docChange.document.toObject(Order::class.java)
                        totalPendapatan+=order.price!!.toInt()
                        totalGalon+=order.quantity!!
                    }
                    galon.text=totalGalon.toString()
                    pendapatan.text= formatRupiah.format(totalPendapatan.toDouble())
                }
            }
        sellerPhone.text = depot.phone
        sellerLocation.text = depot.location
    }

    private fun totalSales() {
        val salesEntries = mutableListOf<Entry>()

        val toy1 = Entry(0f,800000f)
        salesEntries.add(toy1)
        val toy2 = Entry(1f,950000f)
        salesEntries.add(toy2)
        val toy3 = Entry(2f,1200000f)
        salesEntries.add(toy3)
        val toy4 = Entry(3f,750000f)
        salesEntries.add(toy4)
        val nis1 = Entry(4f,1300000f)
        salesEntries.add(nis1)
        val nis2 = Entry(5f,670000f)
        salesEntries.add(nis2)
        val nis3 = Entry(6f,840000f)
        salesEntries.add(nis3)

        val salesDataSet = LineDataSet(salesEntries, "SALES (KES 25,900,000)")
        salesDataSet.axisDependency = YAxis.AxisDependency.LEFT
        salesDataSet.color = ColorTemplate.COLORFUL_COLORS.asList().random()!!

        val dataSets = mutableListOf<ILineDataSet>()
        dataSets.add(salesDataSet)

        val lineData = LineData(dataSets)
        totalSales.data = lineData
        totalSales.description = null
        totalSales.setDrawGridBackground(false)
        totalSales.setDrawBorders(false)
        totalSales.setDrawMarkers(false)
        totalSales.axisRight.setDrawLabels(false)
        totalSales.axisLeft.setDrawLabels(false)

        val labels = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun","Jul")

        val xAxis = totalSales.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f
        xAxis.isGranularityEnabled = true
        xAxis.valueFormatter = ChartLabelsFormatter(labels)

        val yAxisRight = totalSales.axisRight
        yAxisRight.setDrawLabels(false)
        yAxisRight.setDrawGridLines(false)

        totalSales.invalidate()
    }

    override fun onDateRangeSet(view: SmoothDateRangePickerFragment?, yearStart: Int, monthStart: Int, dayStart: Int, yearEnd: Int, monthEnd: Int, dayEnd: Int) {
        var months = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

        date.text = "${months[monthStart]} $dayStart, $yearStart - ${months[monthEnd]} $dayEnd, $yearEnd"
        startMonth = monthStart
        endMonth = monthEnd

        for (i in startMonth..endMonth) {
            carmonth.set(carmonth.size, months[i])
        }

        //loadData()
    }

    private fun loadData() {
        showLoading("Loading reports...")

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            android.R.id.home -> onBackPressed()
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        AppUtils.animateEnterLeft(this)
    }

    override fun setImageForPosition(position: Int, imageView: ImageView?) {
        val keys = depot.images.keys.toList()

        imageView!!.scaleType =ImageView.ScaleType.CENTER_CROP
        imageView.loadUrl(depot.images[keys[position]]!!)
    }
}
