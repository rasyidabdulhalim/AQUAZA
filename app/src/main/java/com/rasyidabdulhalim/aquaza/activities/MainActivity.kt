package com.rasyidabdulhalim.aquaza.activities

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.mikepenz.ionicons_typeface_library.Ionicons
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.DividerDrawerItem
import com.mikepenz.materialdrawer.model.ProfileDrawerItem
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem
import com.rasyidabdulhalim.aquaza.R
import com.rasyidabdulhalim.aquaza.commoners.AppUtils
import com.rasyidabdulhalim.aquaza.commoners.AppUtils.setDrawable
import com.rasyidabdulhalim.aquaza.commoners.BaseActivity
import com.rasyidabdulhalim.aquaza.commoners.K
import com.rasyidabdulhalim.aquaza.fragments.ChatFragment
import com.rasyidabdulhalim.aquaza.fragments.DepotFragment
import com.rasyidabdulhalim.aquaza.fragments.OrderFragment
import com.rasyidabdulhalim.aquaza.fragments.OrderHistoryFragment
import com.rasyidabdulhalim.aquaza.utils.PagerAdapter
import com.rasyidabdulhalim.aquaza.utils.PreferenceHelper
import com.rasyidabdulhalim.aquaza.utils.PreferenceHelper.get
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.toast
import timber.log.Timber


class MainActivity : BaseActivity(), AHBottomNavigation.OnTabSelectedListener,
        AHBottomNavigation.OnNavigationPositionListener, ViewPager.OnPageChangeListener {

    private var doubleBackToExit = false

    private lateinit var drawer: Drawer

    private lateinit var depotFragment: DepotFragment
    private lateinit var orderFragment: OrderFragment
    private lateinit var orderHistoryFragment: OrderHistoryFragment
    private lateinit var chatFragment: ChatFragment
    private lateinit var prefs: SharedPreferences

    companion object {
        private const val HOME = "Aquaza"
        private const val DAFTAR_PESANAN = "Daftar Pesanan"
        private const val DEPOT = "Depot"
        private const val ORDER = "Pesanan"
        private const val HISTORY = "History Pemesanan"
        private const val CHATS = "Chats Depot"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        prefs = PreferenceHelper.defaultPrefs(this)

        Timber.e("UID: ${getUid()}")

        depotFragment = DepotFragment()
        orderFragment = OrderFragment()
        orderHistoryFragment = OrderHistoryFragment()
        chatFragment = ChatFragment()

        initViews()
    }

    private fun initViews() {
        setSupportActionBar(toolbar)
        supportActionBar?.title = HOME
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        setupBottomNav()
        setupViewPager()
        if(prefs[K.STATUS, ""]==K.OWNER){
            setupDrawerOwner()
        }else if (prefs[K.STATUS, ""]==K.DRIVER){
            setupDrawerDriver()
        }else{
            setupDrawer()
        }
    }

    //Setup the bottom navigation bar
    private fun setupBottomNav() {
        val homeIcon = setDrawable(this, Ionicons.Icon.ion_ios_home, R.color.secondaryText, 18)
        val partsIcon = setDrawable(this, Ionicons.Icon.ion_gear_a, R.color.secondaryText, 18)
        val notificationIcon = setDrawable(this, Ionicons.Icon.ion_ios_bell, R.color.secondaryText, 18)
        val chatIcon = setDrawable(this, Ionicons.Icon.ion_chatbubbles, R.color.secondaryText, 18)

        bottomNav.addItem(AHBottomNavigationItem(DEPOT, homeIcon))
        bottomNav.addItem(AHBottomNavigationItem(ORDER, partsIcon))
        bottomNav.addItem(AHBottomNavigationItem(HISTORY, notificationIcon))
        bottomNav.addItem(AHBottomNavigationItem(CHATS, chatIcon))

        bottomNav.defaultBackgroundColor = Color.WHITE
        bottomNav.inactiveColor = ContextCompat.getColor(this, R.color.inactiveColor)
        bottomNav.accentColor = ContextCompat.getColor(this, R.color.colorPrimary)
        bottomNav.isBehaviorTranslationEnabled = false
        bottomNav.titleState = AHBottomNavigation.TitleState.ALWAYS_SHOW
        bottomNav.setUseElevation(true, 5f)

        bottomNav.setOnTabSelectedListener(this)
        bottomNav.setOnNavigationPositionListener(this)
    }

    //Setup the view pager
    private fun setupViewPager() {
        val adapter = PagerAdapter(supportFragmentManager, this)

        adapter.addAllFrags(depotFragment, orderFragment, orderHistoryFragment, chatFragment)
        adapter.addAllTitles(HOME, DAFTAR_PESANAN, HISTORY, CHATS)

        viewPager.adapter = adapter
        viewPager.addOnPageChangeListener(this)
        viewPager.offscreenPageLimit = 4
    }

    private fun setupDrawer() {
        val accountHeader = AccountHeaderBuilder().withActivity(this)
                .withSelectionListEnabled(false)
                .withHeaderBackground(R.drawable.bg_drawer)
                .addProfiles(ProfileDrawerItem()
                        .withName(prefs[K.NAME, ""])
                        .withEmail(prefs[K.PHONE, ""])
                        .withIcon(R.drawable.app_logo))
                .build()

        val default = SecondaryDrawerItem().withIdentifier(0).withName("Home").withIcon(Ionicons.Icon.ion_ios_home)
        //val orders = SecondaryDrawerItem().withIdentifier(1).withName("Jadwalkan Pemesanan").withIcon(Ionicons.Icon.ion_ios_cart)
        val watchlist = SecondaryDrawerItem().withIdentifier(4).withName("Depot Langganan").withIcon(Ionicons.Icon.ion_star)
        val sales = SecondaryDrawerItem().withIdentifier(5).withName("Order Reports").withIcon(Ionicons.Icon.ion_android_list)
        val settings = SecondaryDrawerItem().withIdentifier(6).withName("Settings").withIcon(Ionicons.Icon.ion_ios_gear)
        val exit = SecondaryDrawerItem().withIdentifier(7).withName("Logout").withIcon(Ionicons.Icon.ion_log_out)

        drawer = DrawerBuilder().withActivity(this)
                .withToolbar(toolbar)
                .withAccountHeader(accountHeader)
                .addDrawerItems(default, DividerDrawerItem(),sales, watchlist, DividerDrawerItem(), settings, exit)
                .withOnDrawerItemClickListener { _, _, drawerItem ->
                    when(drawerItem) {
                        //orders -> launchActivity(SettingsActivity::class.java)
                        sales -> launchActivity(SalesActivity::class.java)
                        watchlist -> launchActivity(WatchlistActivity::class.java)
                        settings -> launchActivity(SettingsActivity::class.java)
                        exit -> logOut()
                    }
                    true
                }
                .build()
    }
    private fun setupDrawerOwner() {
        val accountHeader = AccountHeaderBuilder().withActivity(this)
            .withSelectionListEnabled(false)
            .withHeaderBackground(R.drawable.bg_drawer)
            .addProfiles(ProfileDrawerItem()
                .withName(prefs[K.NAME, ""])
                .withEmail(prefs[K.PHONE, ""])
                .withIcon(R.drawable.app_logo))
            .build()

        val default = SecondaryDrawerItem().withIdentifier(0).withName("HOME").withIcon(Ionicons.Icon.ion_ios_home)
        val myDepot = SecondaryDrawerItem().withIdentifier(2).withName("DEPOT").withIcon(Ionicons.Icon.ion_ios_cart)
        val myEmployee = SecondaryDrawerItem().withIdentifier(3).withName("KARYAWAN").withIcon(Ionicons.Icon.ion_upload)
        val myKonsumen = SecondaryDrawerItem().withIdentifier(4).withName("KONSUMEN").withIcon(Ionicons.Icon.ion_star)
        val sales = SecondaryDrawerItem().withIdentifier(5).withName("LAPORAN PENJUALAN").withIcon(Ionicons.Icon.ion_android_list)
        val settings = SecondaryDrawerItem().withIdentifier(6).withName("PENGATURAN").withIcon(Ionicons.Icon.ion_ios_gear)
        val exit = SecondaryDrawerItem().withIdentifier(7).withName("KELUAR").withIcon(Ionicons.Icon.ion_log_out)

        drawer = DrawerBuilder().withActivity(this)
            .withToolbar(toolbar)
            .withAccountHeader(accountHeader)
            .addDrawerItems(default, DividerDrawerItem(), myDepot, myEmployee, myKonsumen,sales, DividerDrawerItem(), settings, exit)
            .withOnDrawerItemClickListener { _, _, drawerItem ->
                when(drawerItem) {
                    myDepot -> launchActivity(MyDepotsActivity::class.java)
                    myEmployee -> launchActivity(MyEmployeActivity::class.java)
                    sales -> launchActivity(SalesActivity::class.java)
                    myKonsumen -> launchActivity(MyKonsumenActivity::class.java)
                    settings -> launchActivity(SettingsActivity::class.java)
                    exit -> logOut()
                }
                true
            }
            .build()
    }
    private fun setupDrawerDriver() {
        val accountHeader = AccountHeaderBuilder().withActivity(this)
            .withSelectionListEnabled(false)
            .withHeaderBackground(R.drawable.bg_drawer)
            .addProfiles(ProfileDrawerItem()
                .withName(prefs[K.NAME, ""])
                .withEmail(prefs[K.PHONE, ""])
                .withIcon(R.drawable.app_logo))
            .build()

        val default = SecondaryDrawerItem().withIdentifier(0).withName("Home").withIcon(Ionicons.Icon.ion_ios_home)
        val boardEmployee = SecondaryDrawerItem().withIdentifier(4).withName("Board Employee").withIcon(Ionicons.Icon.ion_star)
        val driverReport = SecondaryDrawerItem().withIdentifier(5).withName("Driver Reports").withIcon(Ionicons.Icon.ion_android_list)
        val settings = SecondaryDrawerItem().withIdentifier(6).withName("Settings").withIcon(Ionicons.Icon.ion_ios_gear)
        val exit = SecondaryDrawerItem().withIdentifier(7).withName("Logout").withIcon(Ionicons.Icon.ion_log_out)

        drawer = DrawerBuilder().withActivity(this)
            .withToolbar(toolbar)
            .withAccountHeader(accountHeader)
            .addDrawerItems(default, DividerDrawerItem(), driverReport, boardEmployee, DividerDrawerItem(), settings, exit)
            .withOnDrawerItemClickListener { _, _, drawerItem ->
                when(drawerItem) {
                    driverReport -> launchActivity(SalesActivity::class.java)
                    boardEmployee -> launchActivity(NotificationsActivity::class.java)
                    settings -> launchActivity(SettingsActivity::class.java)
                    exit -> logOut()
                }
                true
            }
            .build()
    }
    private fun launchActivity(intentClass: Class<*>) {
        val intent = Intent(this, intentClass)
        startActivity(intent)
        overridePendingTransition(R.anim.enter_b, R.anim.exit_a)

        Handler().postDelayed({
            drawer.closeDrawer()
            drawer.setSelection(0)
        }, 300)

    }

    private fun logOut() {
        Handler().postDelayed({
            alert("Are you sure you want to log out?") {
                title = "Log out"
                positiveButton("LOG OUT") {
                    val firebaseAuth = FirebaseAuth.getInstance()
                    firebaseAuth.signOut()
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(K.TOPIC_GLOBAL)

                    startActivity(Intent(this@MainActivity, AuthActivity::class.java))
                    AppUtils.animateEnterLeft(this@MainActivity)
                    finish()
                }
                negativeButton("CANCEL") {}
            }.show()
        }, 300)
    }

    override fun onTabSelected(position: Int, wasSelected: Boolean): Boolean {
        viewPager.setCurrentItem(position, true)

        when(position) {
            0 -> supportActionBar?.title = HOME
            1 -> supportActionBar?.title = DAFTAR_PESANAN
            2 -> supportActionBar?.title = HISTORY
            3 -> supportActionBar?.title = CHATS
        }

        return true
    }

    override fun onPositionChange(y: Int) {
        viewPager.setCurrentItem(y, true)
    }

    override fun onPageScrollStateChanged(state: Int) {

    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

    }

    override fun onPageSelected(position: Int) {
        bottomNav.currentItem = position
    }

    override fun onBackPressed() {
        if (doubleBackToExit) {
            super.onBackPressed()
        } else {
            doubleBackToExit = true
            toast("Tap back again to exit")

            Handler().postDelayed({doubleBackToExit = false}, 1500)
        }
    }

}
