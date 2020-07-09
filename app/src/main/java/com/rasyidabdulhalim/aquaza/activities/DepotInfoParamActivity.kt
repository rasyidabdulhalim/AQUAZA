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


class DepotInfoParamActivity : BaseActivity(), ImageListener{
    private lateinit var depot: Depot
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_depot_info_param)

        prefs = PreferenceHelper.defaultPrefs(this)
        depot = intent.getSerializableExtra(K.DEPOT) as Depot

        initViews()
    }

    private fun initViews() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "View Paramater Analasisa"

        carousel.pageCount = depot.images.size
        carousel.setImageListener(this)

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
