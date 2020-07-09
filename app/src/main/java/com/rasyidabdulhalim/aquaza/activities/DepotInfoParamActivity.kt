package com.rasyidabdulhalim.aquaza.activities

import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import com.rasyidabdulhalim.aquaza.R
import com.rasyidabdulhalim.aquaza.commoners.AppUtils
import com.rasyidabdulhalim.aquaza.commoners.BaseActivity
import com.rasyidabdulhalim.aquaza.commoners.K
import com.rasyidabdulhalim.aquaza.models.Depot
import com.rasyidabdulhalim.aquaza.utils.PreferenceHelper
import com.rasyidabdulhalim.aquaza.utils.loadUrl
import com.synnapps.carouselview.ImageListener
import kotlinx.android.synthetic.main.activity_depot_info.*


class DepotInfoParamActivity : BaseActivity(), ImageListener {
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
        when (item?.itemId) {
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

        imageView!!.scaleType = ImageView.ScaleType.CENTER_CROP
        imageView.loadUrl(depot.images[keys[position]]!!)
    }
}
