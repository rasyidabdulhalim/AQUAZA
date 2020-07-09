package com.rasyidabdulhalim.aquaza.activities

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.view.MenuItem
import com.mikepenz.ionicons_typeface_library.Ionicons
import com.rasyidabdulhalim.aquaza.R
import com.rasyidabdulhalim.aquaza.adapters.ImagesAdapter
import com.rasyidabdulhalim.aquaza.commoners.AppUtils
import com.rasyidabdulhalim.aquaza.commoners.BaseActivity
import com.rasyidabdulhalim.aquaza.commoners.K
import com.rasyidabdulhalim.aquaza.models.Depot
import com.rasyidabdulhalim.aquaza.utils.*
import com.rasyidabdulhalim.aquaza.utils.PreferenceHelper.get
import com.zhihu.matisse.Matisse
import com.zhihu.matisse.MimeType
import com.zhihu.matisse.filter.Filter
import kotlinx.android.synthetic.main.activity_add_depot.*
import org.jetbrains.anko.toast
import timber.log.Timber

class AddDepotActivity : BaseActivity() {
    private var pickedImages = mutableListOf<Uri>()
    private lateinit var imagesAdapter: ImagesAdapter
    private val images = mutableMapOf<String, String>()
    private lateinit var KEY: String
    private var depot = Depot()
    private lateinit var prefs: SharedPreferences

    companion object {
        private const val IMAGE_PICKER = 401
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_depot)
        prefs = PreferenceHelper.defaultPrefs(this)
        if (intent.getSerializableExtra(K.DEPOT)!=null){
            depot = intent.getSerializableExtra(K.DEPOT) as Depot
        }
        initViews()
    }

    private fun initViews() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        if (depot.id!=null){
            supportActionBar?.title = "Edit Data Depot"
            nameNewDepot.text=Editable.Factory.getInstance().newEditable(depot.depotName)
            locationNewDepot.text=Editable.Factory.getInstance().newEditable(depot.location)
            phone.text=Editable.Factory.getInstance().newEditable(depot.phone)
            price.text=Editable.Factory.getInstance().newEditable(depot.price.toString())
            message.text=Editable.Factory.getInstance().newEditable(depot.description)
        }else{
            supportActionBar?.title = "Add New Depot"
        }

        photosRv.setHasFixedSize(true)
        photosRv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        imagesAdapter = ImagesAdapter()
        photosRv.adapter = imagesAdapter

        addPhoto.setDrawable(AppUtils.setDrawable(this, Ionicons.Icon.ion_android_camera, R.color.colorPrimary, 15))
        addPhoto.setOnClickListener { pickPhotos() }

        buttonNotification.setOnClickListener { addDepot() }
    }

    // Pick photos from gallery
    private fun pickPhotos() {
        if (!storagePermissionGranted()) {
            requestStoragePermission()
            return
        }

        Matisse.from(this)
                .choose(MimeType.ofImage())
                .countable(true)
                .maxSelectable(10)
                .addFilter(GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
                .gridExpectedSize(resources.getDimensionPixelSize(R.dimen.grid_expected_size))
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                .thumbnailScale(0.85f)
                .imageEngine(MyGlideEngine())
                .forResult(IMAGE_PICKER)

    }

    private fun setImages() {
        if (pickedImages.size < 1) return

        if (pickedImages.size == 1) {
            mainImage.setImageURI(pickedImages[0])
        } else {
            mainImage.setImageURI(pickedImages[0])
            photosRv.showView()

            for (i in 1..(pickedImages.size-1)) {
                imagesAdapter.addImage(pickedImages[i])
            }

        }

    }

    private fun addDepot() {
        if (!isConnected()) {
            toast("Please connect to the internet!")
            return
        }

        if (pickedImages.size < 2) {
            toast("Please select atleast 2 images...")
            return
        }

        if(!AppUtils.validated(nameNewDepot, locationNewDepot, price, message)) {
            toast("Please fill all fields!")
            return
        }

        if (depot.id!=null){
            KEY=depot.id.toString()
        }else{
            KEY = getFirestore().collection(K.DEPOTS).document().id // buat id baru
        }

        showLoading("Uploading images...")
        uploadImages()

    }

    // Upload images to firebase storage
    private fun uploadImages() {
        Timber.e("Images to be uploaded ${pickedImages.size}")

        for (i in 0..(pickedImages.size-1)) {
            val ref = getStorageReference().child(K.DEPOTS).child(KEY).child(i.toString())

            val uploadTask = ref.putFile(pickedImages[i])
            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    Timber.e("Error uploading images ${task.exception}}")
                    throw task.exception!!
                }
                // Continue with the task to get the download URL
                ref.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (i == 0) {
                        depot.image = task.result.toString()
                        images["0"] = task.result.toString()
                        Timber.e("Uploaded image one $i url is ${task.result}")

                    } else {
                        images[i.toString()] = task.result.toString()
                        Timber.e("Uploaded image $i url is ${task.result}")

                        Handler().postDelayed({
                            if(i == (pickedImages.size-1)) {
                                depot.images.putAll(images)
                                hideLoading()

                                setDetails()
                            }
                        }, 1500)
                    }

                }
            }
        }

    }

    // Set details to Firestore
    private fun setDetails() {
        Timber.e("Uploading details to Firestore")
        if(depot.id!=null){
            showLoading("Sedang Mengubah Data Cabang didalam Database...")
        }else{

            showLoading("Sedang Menambahkan Data Cabang Baru Kedalam Database...")
        }
        depot.id = KEY
        depot.sellerId = getUid()
        depot.sellerName = prefs[K.NAME]
        depot.time = System.currentTimeMillis()
        depot.depotName=nameNewDepot.text.toString().trim()
        depot.price = price.text.toString().trim()
        depot.location = locationNewDepot.text.toString().trim()
        depot.description = message.text.toString().trim()
        depot.phone=phone.text.toString().trim()
        depot.status="OPEN"
        getFirestore().collection(K.DEPOTS).document(KEY).set(depot)
                .addOnSuccessListener {
                    Timber.e("Depot successfully Added")
                    hideLoading()
                    toast("Depot successfully Added")
                }
                .addOnFailureListener {
                    Timber.e("Error uploading: $it")
                    hideLoading()

                    toast("Error Add Depot. Please try again")
                }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IMAGE_PICKER && resultCode == Activity.RESULT_OK) {
            pickedImages = Matisse.obtainResult(data)

            setImages()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            android.R.id.home -> onBackPressed()
        }

        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        AppUtils.animateEnterLeft(this)

    }

}
