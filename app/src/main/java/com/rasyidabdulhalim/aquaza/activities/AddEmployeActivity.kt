package com.rasyidabdulhalim.aquaza.activities

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.view.MenuItem
import android.view.View
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.messaging.FirebaseMessaging
import com.mikepenz.ionicons_typeface_library.Ionicons
import com.rasyidabdulhalim.aquaza.R
import com.rasyidabdulhalim.aquaza.commoners.AppUtils
import com.rasyidabdulhalim.aquaza.commoners.AppUtils.drawableToBitmap
import com.rasyidabdulhalim.aquaza.commoners.AppUtils.setDrawable
import com.rasyidabdulhalim.aquaza.commoners.BaseActivity
import com.rasyidabdulhalim.aquaza.commoners.K
import com.rasyidabdulhalim.aquaza.models.User
import com.rasyidabdulhalim.aquaza.utils.PreferenceHelper
import com.rasyidabdulhalim.aquaza.utils.TimeFormatter
import com.rasyidabdulhalim.aquaza.utils.setDrawable
import kotlinx.android.synthetic.main.activity_add_employee.*
import org.jetbrains.anko.toast
import timber.log.Timber

class AddEmployeActivity : BaseActivity(){
    private var imageUri: Uri? = null
    private var imageSelected = false
    private lateinit var status: Array<String>
    private lateinit var depot: Array<String>
    private lateinit var shift: Array<String>
    private lateinit var registerSuccessful: Bitmap
    private var employee = User()
    private lateinit var KEY: String
    private lateinit var prefs: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_employee)
        prefs = PreferenceHelper.defaultPrefs(this)
        if (intent.getSerializableExtra(K.USER)!=null){
            employee = intent.getSerializableExtra(K.USER) as User
        }
        val successfulIcon = setDrawable(this, Ionicons.Icon.ion_checkmark_round, R.color.white, 25)
        registerSuccessful = drawableToBitmap(successfulIcon)
        initViews()
    }

    private fun initViews() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (employee.id!=null){
            supportActionBar?.title = "Edit Data Employee"
            registerFirstname.text= Editable.Factory.getInstance().newEditable(employee.name.toString())
            registerPhone.text= Editable.Factory.getInstance().newEditable(employee.phone.toString())
            registerEmail.text= Editable.Factory.getInstance().newEditable(employee.email.toString())
            address.text= Editable.Factory.getInstance().newEditable(employee.address.toString())
            registerPassword.setVisibility(View.GONE)
            registerConfirmPassword.setVisibility(View.GONE)

        }else{
            supportActionBar?.title = "Add New Employee"
        }
        registerFirstname.setDrawable(setDrawable(this, Ionicons.Icon.ion_person, R.color.secondaryText, 18))
        address.setDrawable(setDrawable(this, Ionicons.Icon.ion_ios_navigate_outline, R.color.secondaryText, 18))
        registerPhone.setDrawable(setDrawable(this, Ionicons.Icon.ion_android_call, R.color.secondaryText, 18))
        registerEmail.setDrawable(setDrawable(this, Ionicons.Icon.ion_ios_email, R.color.secondaryText, 18))
        registerPassword.setDrawable(setDrawable(this, Ionicons.Icon.ion_android_lock, R.color.secondaryText, 18))
        registerConfirmPassword.setDrawable(setDrawable(this, Ionicons.Icon.ion_android_lock, R.color.secondaryText, 18))
        registerButton.setOnClickListener{
            validatedDataEmployee()
        }
        initArrays()
    }

    // Initial arrays for spinners
    private fun initArrays() {
        status = resources.getStringArray(R.array.status)
        depot = resources.getStringArray(R.array.depot)
        shift = resources.getStringArray(R.array.shift)
    }

    private fun validatedDataEmployee() {
        // Check if all fields are filled
        if (!AppUtils.validated(registerFirstname,registerPhone, address, registerEmail, registerPassword, registerConfirmPassword)) return

        val email = registerEmail.text.toString().trim()
        val pw = registerPassword.text.toString().trim()
        val confirmPw = registerConfirmPassword.text.toString().trim()
        if (pw != confirmPw) {
            registerConfirmPassword.error = "Does not match password"
            return
        }
        registerButton.startAnimation()
        getFirebaseAuth().createUserWithEmailAndPassword(email, pw)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    registerButton.doneLoadingAnimation(AppUtils.getColor(this, R.color.pink), registerSuccessful)
                    Timber.e("Add New Employee: Success!")
                    val user = task.result!!.user
                    addEmployee(user)
                } else {
                    try {
                        throw task.exception!!
                    } catch (weakPassword: FirebaseAuthWeakPasswordException){
                        registerButton.revertAnimation()
                        registerPassword.error = "Please enter a stronger password"

                    } catch (userExists: FirebaseAuthUserCollisionException) {
                        registerButton.revertAnimation()
                        this.toast("Account already exists. Please log in.")

                    } catch (malformedEmail: FirebaseAuthInvalidCredentialsException) {
                        registerButton.revertAnimation()
                        registerEmail.error = "Incorrect email format"

                    } catch (e: Exception) {
                        registerButton.revertAnimation()
                        Timber.e( "signingIn: Failure - $e}")
                        this.toast("Error signing up. Please try again.")
                    }
                }
            }


    }
    private fun addEmployee(user: FirebaseUser) {
        val newUser = User()
        newUser.name = registerFirstname.text.toString().trim()
        newUser.email = user.email
        newUser.dateCreated = TimeFormatter().getNormalYear(System.currentTimeMillis())
        newUser.id = user.uid
        newUser.phone = registerPhone.text.toString().trim()
        newUser.status=statusType.selectedItem.toString()
        newUser.address=address.text.toString().trim()
        newUser.mydepot=depotType.selectedItem.toString()
        newUser.myshift=shiftType.selectedItem.toString()

        if (employee.id!=null){
            KEY=employee.id.toString()
        }else{
            KEY = getFirestore().collection(K.USERS).document().id // buat id baru
        }

        val ref = getStorageReference().child("avatars").child(KEY)
        val uploadTask = ref.putFile(imageUri!!)
        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                throw task.exception!!
            }
            Timber.e("Image uploaded")

            // Continue with the task to getBitmap the download URL
            ref.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                newUser.avatar =  task.result.toString()
                user.sendEmailVerification()
                FirebaseMessaging.getInstance().subscribeToTopic(K.TOPIC_GLOBAL)
                getFirestore().collection(K.USERS).document(KEY).set(newUser).addOnSuccessListener {
                    Timber.e("Adding user: $newUser")
                    registerButton.doneLoadingAnimation(AppUtils.getColor(this, R.color.pink), registerSuccessful)
                    startActivity(Intent(this, MyEmployeActivity::class.java))
                    AppUtils.animateEnterRight(this)
                    this.finish()
                }

            } else {
                registerButton.revertAnimation()
                toast("Error signing up. Please try again.")
                Timber.e("Error signing up: ${task.exception}")
            }
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
