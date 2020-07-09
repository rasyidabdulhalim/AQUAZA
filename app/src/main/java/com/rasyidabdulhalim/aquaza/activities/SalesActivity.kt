package com.rasyidabdulhalim.aquaza.activities

import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.view.View
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import com.leavjenn.smoothdaterangepicker.date.SmoothDateRangePickerFragment
import com.mikepenz.ionicons_typeface_library.Ionicons
import com.rasyidabdulhalim.aquaza.R
import com.rasyidabdulhalim.aquaza.commoners.AppUtils
import com.rasyidabdulhalim.aquaza.commoners.AppUtils.setDrawable
import com.rasyidabdulhalim.aquaza.commoners.BaseActivity
import com.rasyidabdulhalim.aquaza.commoners.ChartLabelsFormatter
import com.rasyidabdulhalim.aquaza.commoners.K
import com.rasyidabdulhalim.aquaza.models.Order
import com.rasyidabdulhalim.aquaza.utils.PreferenceHelper
import com.rasyidabdulhalim.aquaza.utils.PreferenceHelper.get
import com.rasyidabdulhalim.aquaza.utils.TimeFormatter
import com.rasyidabdulhalim.aquaza.utils.hideView
import com.rasyidabdulhalim.aquaza.utils.random
import kotlinx.android.synthetic.main.activity_sales.*
import timber.log.Timber
import java.text.NumberFormat
import java.util.*


class SalesActivity : BaseActivity(), SmoothDateRangePickerFragment.OnDateRangeSetListener {
    private lateinit var dateRangePicker: SmoothDateRangePickerFragment
    private lateinit var prefs: SharedPreferences


    private var startMonth = 0
    private var endMonth = 0
    var carmonth = emptyArray<String>()
    private val entriesBarCabang = mutableListOf<BarEntry>()

    private var localeID: Locale = Locale("in", "ID")
    private var formatRupiah: NumberFormat = NumberFormat.getCurrencyInstance(localeID)
    var totalPriceOrder = 0
    var totalOrder = 0
    var totalPriceOrderByDepot = 0
    var totalOrderByDepot = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sales)
        prefs = PreferenceHelper.defaultPrefs(this)
        initViews()

        showLoading("Loading records...")
        Handler().postDelayed({ hideLoading() }, 1000)

        if (prefs[K.STATUS, ""] == "Owner") {
            loadOrderSummary()
            entriesBarCabang.add(BarEntry(0f, 1000.toFloat()))
            entriesBarCabang.add(BarEntry(1f, 2000.toFloat()))
            carSalesBar(entriesBarCabang)
            partSales(entriesBarCabang)
        } else if (prefs[K.STATUS, ""] == "Driver") {
            loadOrderSummaryDriver()
            grafikTotalOrder.visibility = View.GONE
            grafikPemesanan.visibility = View.GONE
        } else {
            loadOrderSummaryUser()
            grafikTotalOrder.visibility = View.GONE
            grafikPemesanan.visibility = View.GONE
        }

    }

    private fun initViews() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Sales Reports"

        dateFilter.setImageDrawable(
            setDrawable(
                this,
                Ionicons.Icon.ion_android_calendar,
                R.color.textGray,
                27
            )
        )
        date.text =
            "Report for: Jan 01, 2018 - ${TimeFormatter().getReportTime(System.currentTimeMillis())}"
        dateFilter.hideView()

        dateRangePicker = SmoothDateRangePickerFragment.newInstance(this)
        dateRangePicker.maxDate = Calendar.getInstance()

        dateFilter.setOnClickListener { dateRangePicker.show(fragmentManager, "") }
    }

    private fun loadOrderSummary() {
        getFirestore().collection(K.ORDERS)
            .whereEqualTo(K.STATUS, K.RECEIVED)
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    Timber.e("Error fetching orders $firebaseFirestoreException")
                }
                if (querySnapshot == null || querySnapshot.isEmpty) {
                    priceTotalOrder.text = formatRupiah.format(totalPriceOrder.toDouble())
                    totalOrderSummary.text = totalOrder.toString()
                } else {
                    for (docChange in querySnapshot.documentChanges) {
                        val order = docChange.document.toObject(Order::class.java)
                        totalPriceOrder += order.price!!.toInt()
                        totalOrder += order.quantity!!
                    }
                    priceTotalOrder.text = formatRupiah.format(totalPriceOrder.toDouble())
                    totalOrderSummary.text = totalOrder.toString()

                    totalSales()
                }
            }
    }

    private fun loadOrderSummaryDriver() {
        getFirestore().collection(K.ORDERS)
            .whereEqualTo("driverId", getUid())
            .whereEqualTo(K.STATUS, K.RECEIVED)
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    Timber.e("Error fetching orders $firebaseFirestoreException")
                }
                if (querySnapshot == null || querySnapshot.isEmpty) {
                    priceTotalOrder.text = formatRupiah.format(totalPriceOrder.toDouble())
                    totalOrderSummary.text = totalOrder.toString()
                } else {
                    for (docChange in querySnapshot.documentChanges) {
                        val order = docChange.document.toObject(Order::class.java)
                        totalPriceOrder += order.price!!.toInt()
                        totalOrder += order.quantity!!
                        right_text.text = "Received Money"
                        left_text.text = "Delivered Water"
                    }
                    priceTotalOrder.text = formatRupiah.format(totalPriceOrder.toDouble())
                    totalOrderSummary.text = totalOrder.toString()

                    totalSales()
                }
            }
    }

    private fun loadOrderSummaryUser() {
        getFirestore().collection(K.ORDERS)
            .whereEqualTo("buyerId", getUid())
            .whereEqualTo(K.STATUS, K.RECEIVED)
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    Timber.e("Error fetching orders $firebaseFirestoreException")
                }
                if (querySnapshot == null || querySnapshot.isEmpty) {
                    priceTotalOrder.text = formatRupiah.format(totalPriceOrder.toDouble())
                    totalOrderSummary.text = totalOrder.toString()
                } else {
                    for (docChange in querySnapshot.documentChanges) {
                        val order = docChange.document.toObject(Order::class.java)
                        totalPriceOrder += order.price!!.toInt()
                        totalOrder += order.quantity!!
                    }
                    priceTotalOrder.text = formatRupiah.format(totalPriceOrder.toDouble())
                    totalOrderSummary.text = totalOrder.toString()

                    totalSales()
                }
            }
    }

    private fun totalSales() {
        val salesEntries = mutableListOf<Entry>()

        val toy1 = Entry(0f, 100000f)
        salesEntries.add(toy1)
        val toy2 = Entry(1f, 200000f)
        salesEntries.add(toy2)
        val toy3 = Entry(2f, 300000f)
        salesEntries.add(toy3)
        val toy4 = Entry(3f, 250000f)
        salesEntries.add(toy4)
        val nis1 = Entry(4f, 430000f)
        salesEntries.add(nis1)
        val nis2 = Entry(5f, 370000f)
        salesEntries.add(nis2)
        val nis3 = Entry(6f, 440000f)
        salesEntries.add(nis3)

        val salesDataSet = LineDataSet(salesEntries, "SALES (" + priceTotalOrder.text + ")")
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

        val labels = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul")

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

    private fun carSalesBar(entries: MutableList<BarEntry>) {

        val labels = arrayOf("AQUAZA1", "AQUAZA2")

        val set = BarDataSet(entries, "Sales (Cabang)")
        set.colors = ColorTemplate.COLORFUL_COLORS.asList()

        val data = BarData(set)
        carSalesBar.data = data
        carSalesBar.setFitBars(true)
        carSalesBar.description = null

        val xAxis = carSalesBar.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.valueFormatter = ChartLabelsFormatter(labels)
        xAxis.granularity = 1f
        xAxis.isGranularityEnabled = true

        val yAxisRight = carSalesBar.axisRight
        yAxisRight.setDrawLabels(false)
        yAxisRight.setDrawGridLines(false)

        carSalesBar.invalidate()
    }

    private fun partSales(entries: MutableList<BarEntry>) {

        val labels = arrayOf("AQUAZA1", "AQUAZA2")

        val set = BarDataSet(entries, "Sales (Total Penjualan)")
        set.colors = ColorTemplate.COLORFUL_COLORS.asList()

        val data = BarData(set)
        partSalesBar.data = data
        partSalesBar.setFitBars(true)
        partSalesBar.description = null

        val xAxis = partSalesBar.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.valueFormatter = ChartLabelsFormatter(labels)
        xAxis.granularity = 1f
        xAxis.isGranularityEnabled = true

        val yAxisRight = partSalesBar.axisRight
        yAxisRight.setDrawLabels(false)
        yAxisRight.setDrawGridLines(false)

        partSalesBar.invalidate()
    }

    override fun onDateRangeSet(
        view: SmoothDateRangePickerFragment?,
        yearStart: Int,
        monthStart: Int,
        dayStart: Int,
        yearEnd: Int,
        monthEnd: Int,
        dayEnd: Int
    ) {
        var months = arrayOf(
            "Jan",
            "Feb",
            "Mar",
            "Apr",
            "May",
            "Jun",
            "Jul",
            "Aug",
            "Sep",
            "Oct",
            "Nov",
            "Dec"
        )

        date.text =
            "${months[monthStart]} $dayStart, $yearStart - ${months[monthEnd]} $dayEnd, $yearEnd"
        startMonth = monthStart
        endMonth = monthEnd

        for (i in startMonth..endMonth) {
            carmonth.set(carmonth.size, months[i])
        }

        //loadData()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> onBackPressed()
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        AppUtils.animateEnterLeft(this)
    }
}
