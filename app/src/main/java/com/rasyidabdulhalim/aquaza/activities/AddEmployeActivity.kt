package com.rasyidabdulhalim.aquaza.activities

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.view.MenuItem
import android.view.View
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.mikepenz.ionicons_typeface_library.Ionicons
import com.rasyidabdulhalim.aquaza.R
import com.rasyidabdulhalim.aquaza.adapters.ImagesAdapter
import com.rasyidabdulhalim.aquaza.commoners.AppUtils
import com.rasyidabdulhalim.aquaza.commoners.AppUtils.setDrawable
import com.rasyidabdulhalim.aquaza.commoners.BaseActivity
import com.rasyidabdulhalim.aquaza.commoners.K
import com.rasyidabdulhalim.aquaza.models.User
import com.rasyidabdulhalim.aquaza.utils.*
import com.zhihu.matisse.Matisse
import com.zhihu.matisse.MimeType
import com.zhihu.matisse.filter.Filter
import kotlinx.android.synthetic.main.activity_add_employee.*
import org.jetbrains.anko.toast
import timber.log.Timber
import kotlin.collections.set

class AddEmployeActivity : BaseActivity() {
    private var pickedImages = mutableListOf<Uri>()
    private lateinit var imagesAdapter: ImagesAdapter
    private val images = mutableMapOf<String, String>()
    private lateinit var KEY: String
    private var employee = User()
    private lateinit var prefs: SharedPreferences

    companion object {
        private const val IMAGE_PICKER = 401
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_employee)
        prefs = PreferenceHelper.defaultPrefs(this)
        if (intent.getSerializableExtra(K.USER) != null) {
            employee = intent.getSerializableExtra(K.USER) as User
        }
        initViews()
    }

    private fun initViews() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        if (employee.id != null) {
            supportActionBar?.title = "Edit Data Employee"
            nameEmployee.text = Editable.Factory.getInstance().newEditable(employee.name.toString())
            phoneEmployee.text =
                Editable.Factory.getInstance().newEditable(employee.phone.toString())
            emailEmployee.text =
                Editable.Factory.getInstance().newEditable(employee.email.toString())
            locationNewDepot.text =
                Editable.Factory.getInstance().newEditable(employee.address.toString())
            passwordEmployee.setVisibility(View.GONE)
            confirmPasswordEmployee.setVisibility(View.GONE)
            buttonNotification.setText("Simpan Perubahan")

            KEY = employee.id.toString()

        } else {
            supportActionBar?.title = "Add New Employee"

            KEY = getFirestore().collection(K.USERS).document().id // buat id baru

        }
        nameEmployee.setDrawable(
            setDrawable(
                this,
                Ionicons.Icon.ion_person,
                R.color.secondaryText,
                18
            )
        )
        locationNewDepot.setDrawable(
            setDrawable(
                this,
                Ionicons.Icon.ion_ios_navigate_outline,
                R.color.secondaryText,
                18
            )
        )
        phoneEmployee.setDrawable(
            setDrawable(
                this,
                Ionicons.Icon.ion_android_call,
                R.color.secondaryText,
                18
            )
        )
        emailEmployee.setDrawable(
            setDrawable(
                this,
                Ionicons.Icon.ion_ios_email,
                R.color.secondaryText,
                18
            )
        )
        passwordEmployee.setDrawable(
            setDrawable(
                this,
                Ionicons.Icon.ion_android_lock,
                R.color.secondaryText,
                18
            )
        )
        confirmPasswordEmployee.setDrawable(
            setDrawable(
                this,
                Ionicons.Icon.ion_android_lock,
                R.color.secondaryText,
                18
            )
        )

        photosRv.setHasFixedSize(true)
        photosRv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        imagesAdapter = ImagesAdapter()
        photosRv.adapter = imagesAdapter

        addPhoto.setDrawable(
            AppUtils.setDrawable(
                this,
                Ionicons.Icon.ion_android_camera,
                R.color.colorPrimary,
                15
            )
        )
        addPhoto.setOnClickListener { pickPhotos() }

        buttonNotification.setOnClickListener { validatedData() }
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

            for (i in 1..(pickedImages.size - 1)) {
                imagesAdapter.addImage(pickedImages[i])
            }

        }

    }

    private fun validatedData() {
        val email = emailEmployee.text.toString().trim()
        val pw = confirmPasswordEmployee.text.toString().trim()
        val confirmPw = confirmPasswordEmployee.text.toString().trim()
        if (!isConnected()) {
            toast("Please connect to the internet!")
            return
        }
        // Check if password and confirm password match
        if (pw != confirmPw) {
            confirmPasswordEmployee.error = "Does not match password"
            return
        }

        if (pickedImages.size < 1) {
            toast("Please Upload Avatar")
            return
        }

        if (employee.id != null) {
            if (!AppUtils.validated(nameEmployee, phoneEmployee, locationNewDepot, emailEmployee)) {
                toast("Please fill all fields!")
                return
            }else{
                showLoading("Uploading images...")
                uploadImages()
            }
        } else {
            if (!AppUtils.validated(nameEmployee,phoneEmployee,locationNewDepot,emailEmployee,passwordEmployee,confirmPasswordEmployee)) {
                toast("Please fill all fields!")
                return
            }else{
                getFirebaseAuth().createUserWithEmailAndPassword(email, pw)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            Timber.e("signingIn: Success!")
                            val user = task.result!!.user
                            KEY = user.uid
                            showLoading("Uploading images...")
                            uploadImages()
                        } else {
                            try {
                                throw task.exception!!
                            } catch (weakPassword: FirebaseAuthWeakPasswordException) {
                                buttonNotification.revertAnimation()
                                confirmPasswordEmployee.error = "Please enter a stronger password"

                            } catch (userExists: FirebaseAuthUserCollisionException) {
                                buttonNotification.revertAnimation()
                                toast("Account already exists. Please log in.")

                            } catch (malformedEmail: FirebaseAuthInvalidCredentialsException) {
                                buttonNotification.revertAnimation()
                                emailEmployee.error = "Incorrect email format"

                            } catch (e: Exception) {
                                buttonNotification.revertAnimation()
                                Timber.e("signingIn: Failure - $e}")
                                toast("Error signing up. Please try again.")
                            }
                        }
                    }
            }
        }
    }

    // Upload images to firebase storage
    private fun uploadImages() {
        Timber.e("Images to be uploaded ${pickedImages.size}")
        val ref = getStorageReference().child("avatars").child(KEY)

        val uploadTask = ref.putFile(pickedImages[0])
        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                Timber.e("Error uploading images ${task.exception}}")
                throw task.exception!!
            }
            // Continue with the task to get the download URL
            ref.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                employee.avatar = task.result.toString()
                images["0"] = task.result.toString()
                Timber.e("Uploaded image ${task.result}")
                setDetails()
            }
        }
    }

    // Set details to Firestore
    private fun setDetails() {
        Timber.e("Uploading details to Firestore")
        if (employee.id != null) {
            showLoading("Sedang Mengubah Data Karyawan didalam Database...")
        } else {
            showLoading("Sedang Menambahkan Data Karyawan Baru Kedalam Database...")
        }
        employee.id = KEY
        employee.name = nameEmployee.text.toString()
        employee.phone = phoneEmployee.text.toString()
        employee.address = locationNewDepot.text.toString()
        employee.email = emailEmployee.text.toString()
        employee.status = statusEmployee.selectedItem.toString()
        employee.mydepot = depotType.selectedItem.toString()
        employee.myshift = shiftEmployee.selectedItem.toString()
        employee.dateCreated = TimeFormatter().getNormalYear(System.currentTimeMillis())
        getFirestore().collection(K.USERS).document(KEY).set(employee)
            .addOnSuccessListener {
                Timber.e("Data Berhasil Disimpan")
                hideLoading()
                toast("Data Karyawan Berhasil Disimpan")
            }
            .addOnFailureListener {
                Timber.e("Error uploading: $it")
                hideLoading()

                toast("Error. Please try again")
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
        when (item?.itemId) {
            android.R.id.home -> onBackPressed()
        }

        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        AppUtils.animateEnterLeft(this)

    }

}

