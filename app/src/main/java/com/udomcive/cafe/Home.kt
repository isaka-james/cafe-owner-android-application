package com.udomcive.cafe

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import com.google.gson.Gson
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.cardview.widget.CardView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.navigation.NavigationView
import org.json.JSONArray

import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.animation.AnimationUtils
import android.widget.Toast


import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException


import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.io.IOException
import java.net.URLEncoder
import java.util.concurrent.TimeUnit


class Home : AppCompatActivity() {

    // the splash for fetching datas
    private val SPLASH_DELAY: Long = 3000


    // handles the onback press
    private var doubleBackToExitPressedOnce = false
    private val doubleBackToExitToastDuration = 2000 // 2 seconds


    private var jsonWay: String? = null
    private var jsonDelivered: String? = null
    private var jsonOrders: String? = null
    private var jsonUser: String? = null





    /*
    *
    *
    *   FETCHING THE DATA FROM THE API HERE
    *
    *
    */

    //private var selectedTime: String = ""
    //private val configureTimeButton: Button







    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        // Use a Handler to switch to the splash screen layout after a delay
        // Use a Handler to switch to the splash screen layout after a delay
        Handler(Looper.getMainLooper()).postDelayed({
            setContentView(R.layout.splash_screen)





            // Start a coroutine to fetch data asynchronously
            CoroutineScope(Dispatchers.Main).launch {

                GlobalScope.launch(Dispatchers.IO) {
                    val isConnected = testInternet()

                    withContext(Dispatchers.Main) {
                        if (!isConnected) {
                            goOfflinePage()
                        }else{
                            val deferredWay = async(Dispatchers.IO) { fetchData("pending","normal") }
                            val deferredDelivered = async(Dispatchers.IO) { fetchData("delivered","normal") }
                            val deferredUser = async(Dispatchers.IO) { fetchData("user","normal") }


                            // Wait for all data fetching tasks to complete
                            jsonWay = deferredWay.await()
                            jsonDelivered = deferredDelivered.await()
                            jsonUser = deferredUser.await()

                            // Now you have the fetched data, you can use it as needed
                            // For example, you can update UI elements with this data
                            // ...

                            // Switch to the main activity layout on the UI thread
                            HomePage()

                        }
                    }
                }

            }
        }, SPLASH_DELAY)










    }



    /* The side navigation start functions */

    // share function
    private fun shareApp() {
        val appLink = "https://cafeterion.000webhostapp.com/app.php" // The link of the app
        val message = "Check out this awesome ordering  app: $appLink"

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, message)

        startActivity(Intent.createChooser(intent, "Share the App"))

    }



    private fun parseOrdersFromJSON(): List<Order> {
        val orders = mutableListOf<Order>()

        try {
            val jsonArray = JSONArray(jsonOrders)
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val orderId = jsonObject.getString("orderId") // Parse orderId
                val imageResource = jsonObject.getString("imageResource")
                val restaurantName = jsonObject.getString("restaurantName")
                val orderDate = jsonObject.getString("orderDate")
                val orderStatus = jsonObject.getString("orderStatus")
                val deliveryTime = jsonObject.getString("deliveryTime")

                val order = Order(orderId, imageResource, restaurantName, orderDate, orderStatus,deliveryTime)
                orders.add(order)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return orders
    }


    private fun orders(outerLayout: View) {


        CoroutineScope(Dispatchers.Main).launch {

            GlobalScope.launch(Dispatchers.IO) {
                val isConnected = testInternet()

                withContext(Dispatchers.Main) {
                    if (!isConnected) {
                        goOfflinePage()
                    }else{
                        //val deferredOrder = async(Dispatchers.IO) { fetchData("orders","normal") }

                        // Wait for all data fetching tasks to complete
                        //jsonOrders = deferredOrder.await()

                        // Now you have the fetched data, you can use it as needed
                        val outerLinearLayout = outerLayout.findViewById<LinearLayout>(R.id.outerLinearLayout)

                        // Get the list of orders (you can use your JSON parsing logic here)
                        //val orders = parseOrdersFromJSON()

                        /* Iterate through the orders and create order items dynamically
                        for (order in orders) {
                            // Inflate the order item layout
                            val orderItemLayout = layoutInflater.inflate(R.layout.order_card, null)

                            // Find views inside the order item layout and set their values
                            val orderImageView = orderItemLayout.findViewById<ImageView>(R.id.orderImageView)
                            val restaurantNameTextView = orderItemLayout.findViewById<TextView>(R.id.restaurantNameTextView)
                            val orderDateTextView = orderItemLayout.findViewById<TextView>(R.id.orderDateTextView)
                            val orderStatusTextView = orderItemLayout.findViewById<TextView>(R.id.orderStatusTextView)
                            val deliveryTimeTextView = orderItemLayout.findViewById<TextView>(R.id.orderDeliveryTime)


                            // val cancelButton = orderItemLayout.findViewById<Button>(R.id.cancelButton)

                            // Load image using Glide (replace with your image loading logic)
                            Glide.with(this@Home)
                                .load(order.imageResource) // Use the appropriate image resource
                                .placeholder(R.drawable.logo_login) // Replace with your placeholder image
                                .error(R.drawable.error_image) // Replace with your error image
                                .into(orderImageView)


                            restaurantNameTextView.text = order.restaurantName
                            //restaurantNameTextView.typeface = Typeface.createFromAsset(assets, "caveat_bush.ttf")
                            orderDateTextView.text = "Time Ordered: ${order.orderDate}"
                            orderStatusTextView.text = "Status: ${order.orderStatus}"
                            deliveryTimeTextView.text = "Time of Delivery: ${order.deliveryTime}"

                            // Set the ID of the order item based on the orderId from JSON
                            orderItemLayout.id = order.orderId.hashCode()

                            // Add the order item to the outer LinearLayout
                            outerLinearLayout.addView(orderItemLayout)

                            // ADD LATER THE CANCEL BUTTON FOR CANCELLING ORDERS


                        }

                         */

                    }
                }
            }

        }


    }



    private fun feedback(){
        val frameLayout = findViewById<FrameLayout>(R.id.container)
        frameLayout.removeAllViews()

        // Inflate the XML layout (a.xml)
        val inflater = LayoutInflater.from(this)
        val aLayout = inflater.inflate(R.layout.feedback, null)

        // Add the inflated layout to the FrameLayout
        frameLayout.addView(aLayout)


    }


    private fun account() {
        /**********************************************************************
         *        X X X X X X X X X X X X X X X X X X X X
         *        X HANDLING THE ACCOUNT HERE  X X
         *        X X X X X X X X X X X X X X X X X X X
         */
        val gson = Gson()
        val user = gson.fromJson(jsonUser, User::class.java)

        val frameLayout = findViewById<FrameLayout>(R.id.container)
        frameLayout.removeAllViews()

        // Inflate the XML layout (activity_account.xml)
        val inflater = LayoutInflater.from(this)
        val aLayout = inflater.inflate(R.layout.activity_account, null)

        // Find TextViews and set user information
        val usernameTextView = aLayout.findViewById<TextView>(R.id.usernameTextView)
        val emailTextView = aLayout.findViewById<TextView>(R.id.emailTextView)
        val phoneTextView = aLayout.findViewById<TextView>(R.id.phoneTextView)
        val locationTextView = aLayout.findViewById<TextView>(R.id.locationTextView)

        usernameTextView.text = user.username
        emailTextView.text = user.email
        phoneTextView.text = user.phone
        locationTextView.text = "Location: " + user.location

        // Add the inflated layout to the FrameLayout
        frameLayout.addView(aLayout)

        // Now, the contents of a.xml are added to the container
    }









    private fun createCardViewPending(pending: SingleProduct): CardView {
        val cardView = CardView(this)
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(10, 10, 10, 10)
        cardView.layoutParams = layoutParams

        val cardContent = layoutInflater.inflate(R.layout.onway_foods, null)

        val imageView = cardContent.findViewById<ImageView>(R.id.image_view_onway)
        val titleTextView = cardContent.findViewById<TextView>(R.id.titleTextViewOnWay)
        val locationTextView = cardContent.findViewById<TextView>(R.id.locationTextViewOnWay)
        val priceTextView = cardContent.findViewById<TextView>(R.id.priceTextViewOnWay)
        val markOrdered = cardContent.findViewById<MaterialButton>(R.id.deliver)
        val timeText = cardContent.findViewById<TextView>(R.id.delivering_time)
        val customerComment = cardContent.findViewById<TextView>(R.id.customer_comment)
        val customerPhone = cardContent.findViewById<TextView>(R.id.customer_phone)
        val from = cardContent.findViewById<TextView>(R.id.fromFood)
        val customer = cardContent.findViewById<TextView>(R.id.customer_name)

        // Load image using Glide (replace with your image loading logic)
        Glide.with(this)
            .load(pending.image_url) // Use the appropriate image resource
            .placeholder(R.drawable.logo_login) // Replace with your placeholder image
            .error(R.drawable.error_image) // Replace with your error image
            .into(imageView)

        var showLocation: String? = null
        if(pending.location==""){
            showLocation = pending.oglocation
        }else{
            showLocation = pending.location
        }
        // Populate views with order data
        titleTextView.text = "${pending.name} "
        locationTextView.text = "Destination: ${showLocation}"
        locationTextView.textSize = resources.getDimension(R.dimen.description_text)
        locationTextView.typeface= Typeface.createFromAsset(assets, "dancing_script.ttf")
        timeText.text= "Delivery at: ${pending.time}"
        timeText.typeface= Typeface.createFromAsset(assets, "dancing_script.ttf")
        priceTextView.text = "Tsh. ${pending.price} /="
        customerComment.text = "Customer Comment: ${pending.comment}"
        customerPhone.text = "Customer Phone: ${pending.phone}"
        from.text = "From: ${pending.from}"
        customer.text = "Customer Name: ${pending.customer}"
        // Set the cardView's id using the "id" from the JSON
        cardView.id = pending.id.hashCode()

        cardView.addView(cardContent)

        markOrdered.setOnClickListener {
            markItemOrder(pending.id.hashCode())
        }

        return cardView
    }













    private fun createCardViewDelivered(delivery: SingleProduct): CardView {

        val cardView = CardView(this)
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(10, 10, 10, 10)
        cardView.layoutParams = layoutParams

        val cardContent = layoutInflater.inflate(R.layout.delivered_foods, null)

        val imageView = cardContent.findViewById<ImageView>(R.id.image_view_delivered)
        val titleTextView = cardContent.findViewById<TextView>(R.id.titleTextViewDelivered)
        val locationTextView = cardContent.findViewById<TextView>(R.id.locationTextViewDelivered)
        val priceTextView = cardContent.findViewById<TextView>(R.id.priceTextViewDelivered)
        val timeText = cardContent.findViewById<TextView>(R.id.delivering_time)
        val customerComment = cardContent.findViewById<TextView>(R.id.customer_comment)
        val customerPhone = cardContent.findViewById<TextView>(R.id.customer_phone)
        val from = cardContent.findViewById<TextView>(R.id.fromFood)
        val customer = cardContent.findViewById<TextView>(R.id.customer_name)

        // Load image using Glide (replace with your image loading logic)
        Glide.with(this)
            .load(delivery.image_url) // Use the appropriate image resource
            .placeholder(R.drawable.logo_login) // Replace with your placeholder image
            .error(R.drawable.error_image) // Replace with your error image
            .into(imageView)

        var showLocation: String? = null
        if(delivery.location==""){
            showLocation = delivery.oglocation
        }else{
            showLocation = delivery.location
        }
        // Populate views with order data
        titleTextView.text = "${delivery.name} "
        locationTextView.text = "Destination: ${delivery.location}"
        locationTextView.textSize = resources.getDimension(R.dimen.description_text)
        locationTextView.typeface= Typeface.createFromAsset(assets, "dancing_script.ttf")
        timeText.text= "Delivery at: ${delivery.time}"
        timeText.typeface= Typeface.createFromAsset(assets, "dancing_script.ttf")
        priceTextView.text = "Tsh. ${delivery.price} /="
        customerComment.text = "Customer Comment: ${delivery.comment}"
        customerPhone.text = "Customer Phone: ${delivery.phone}"
        from.text = "From: ${delivery.from}"
        customer.text = "Customer Name: ${delivery.customer}"

        // Set the cardView's id using the "id" from the JSON
        cardView.id = delivery.id.hashCode()

        cardView.addView(cardContent)

        return cardView
    }









    private fun testInternet(): Boolean {
        val clientAuth = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()

        val url = "https://cafeterion.000webhostapp.com/test.html"

        val requestAuth = Request.Builder()
            .url(url)
            .post(RequestBody.create(MediaType.parse("application/json"), ""))
            .build()

        try {
            val responseAuth = clientAuth.newCall(requestAuth).execute()

            return when {
                responseAuth.isSuccessful -> {
                    true
                }
                else -> false
            }
        } catch (e: IOException) {
            val userid = getUserId()
            Log.e("Bad Internet", "Error: ${e.message} User = $userid")
            return false
        }
    }


    private fun goOfflinePage(){
        setContentView(R.layout.no_internet)
        val retry = findViewById<Button>(R.id.retryButton)
        val loadingAnime = findViewById<ImageView>(R.id.noInternetPic)
        val message = findViewById<TextView>(R.id.usernameTextView)

        retry.setOnClickListener{

            message.text = "loading .."
            val rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate_animation)
            loadingAnime.startAnimation(rotateAnimation)


            CoroutineScope(Dispatchers.Main).launch {

                GlobalScope.launch(Dispatchers.IO) {
                    val isConnected = testInternet()

                    withContext(Dispatchers.Main) {
                        if (!isConnected) {
                            goOfflinePage()
                        }else{
                            val deferredTrend = async(Dispatchers.IO) { fetchData("pending","normal") }
                            val deferredDelivered = async(Dispatchers.IO) { fetchData("delivered","normal") }
                            val deferredUser = async(Dispatchers.IO) { fetchData("user","normal")}

                            // Wait for all data fetching tasks to complete
                            jsonWay = deferredTrend.await()
                            jsonDelivered = deferredDelivered.await()
                            jsonUser = deferredUser.await()

                            // Now you have the fetched data, you can use it as needed
                            // For example, you can update UI elements with this data
                            // ...

                            // Switch to the main activity layout on the UI thread
                            HomePage()

                        }
                    }
                }

            }

        }

    }





    private suspend fun fetchData(type: String,which: String): String {

        val userId = getUserId()

        val client = OkHttpClient()

        var url: String? = null

        if(which == "normal"){
            url = when (type) {
                "pending" -> "https://cafeterion.000webhostapp.com/supplier/pending.php?userid=${userId}"
                "delivered" -> "https://cafeterion.000webhostapp.com/supplier/delivered.php?userid=${userId}"
                "user" -> "https://cafeterion.000webhostapp.com/supplier/user_info.php?userid=$userId"
                else -> ""
            }
        }else if(which=="marksold"){
            val solditem = URLEncoder.encode(type, "UTF-8")
            url = "https://cafeterion.000webhostapp.com/supplier/marksold.php?item=${solditem}"
        }


        val jsonBody = """
        {
            "type": "$type",
            "key": "mynameismasterplancafeterion"
        }
    """.trimIndent()

        val request = Request.Builder()
            .url(url)
            .post(RequestBody.create(MediaType.parse("application/json"), jsonBody))
            .build()

        val response = client.newCall(request).execute()

        return if (response.isSuccessful) {
            val jsonData = response.body()?.string().toString()
            Log.i("Data received: ", "Data for $type fetched successfully")
            jsonData // Return the fetched data as a non-nullable String
        } else {
            Log.e("Data not received: ", "Failed to fetch data for $type")
            "" // Return an empty string in case of failure, but it should be a valid String
        }
    }



    private fun markItemOrder(id: Int){

        val context = applicationContext // Get the application context

        GlobalScope.launch(Dispatchers.IO) {
            val isConnected = testInternet()

            withContext(Dispatchers.Main) {
                if (!isConnected) {
                    Toast.makeText(context, "You are currently Offline!", Toast.LENGTH_LONG).show()
                } else {
                    //val deferredSold = async(Dispatchers.IO) { fetchData(id.toString(), "marksold") }

                    // Wait for all data fetching tasks to complete
                    //val soldResponse = deferredSold.await()

                    Toast.makeText(context, "Successfully Delivered!", Toast.LENGTH_LONG).show()
                }
            }
        }

    }



    private fun HomePage() {
        setContentView(R.layout.activity_home)
        // Find the LinearLayout for them
        val pendingFoodsLayout = findViewById<LinearLayout>(R.id.pending_orders)
        val deliveredFoodsLayout = findViewById<LinearLayout>(R.id.delivered_orders)
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        val headerView = navigationView.getHeaderView(0)
        val usernameOfUser = headerView.findViewById<TextView>(R.id.usernameCurrentUser)
        val phoneOfUser = headerView.findViewById<TextView>(R.id.phoneCurrentUser)

        val gson = Gson()



        /*
         *   THE
         *   ORDERS ON THE WAY
         */
        // Parse the JSON data into an array of on the way objects
        val onway = gson.fromJson(jsonWay, Array<SingleProduct>::class.java)

        // Create and add CardViews for each trending
        onway.forEach { pending ->
            // Create a CardView
            val cardView = createCardViewPending(pending)
            pendingFoodsLayout.addView(cardView)
        }

        /*
         *   THE
         *   RECENTLY
         */

        // let's go for the recently
        val delivered = gson.fromJson(jsonDelivered, Array<SingleProduct>::class.java)

        // Create and add CardViews for each recently
        delivered.forEach { delivery ->
            // Create a CardView
            val cardView = createCardViewDelivered(delivery)
            deliveredFoodsLayout.addView(cardView)
        }

        // NAVIGATION ANIMATIONS
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)

        // Toggle for the navigation drawer
        val toggle = ActionBarDrawerToggle(
            this@Home, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Handle navigation item clicks here
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.home_side_nav -> {
                    HomePage()
                }
                R.id.account_side_nav -> {
                    account()
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
                R.id.orders_side_nav -> {
                    // Inflate the outer_order.xml layout
                    val outerOrderLayout = LayoutInflater.from(this@Home).inflate(R.layout.outer_cover_orders, null)


                    // Find the FrameLayout in your activity_home.xml where you want to add the outer LinearLayout
                    val frameLayout = findViewById<FrameLayout>(R.id.container)
                    frameLayout.removeAllViews()

                    // Add the outer LinearLayout to the FrameLayout
                    frameLayout.addView(outerOrderLayout)

                    orders(outerOrderLayout)
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
                R.id.feedback_side_nav -> {
                    feedback()
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
                R.id.share_side_nav -> {
                    shareApp()
                }
                // Add cases for other items as needed
            }
            true
        }

        // when menu button is clicked it opens navigation
        val menuIcon: ImageView = findViewById(R.id.menu_icon)
        menuIcon.setOnClickListener {
            // Open the navigation drawer when the menu button is clicked
            drawerLayout.openDrawer(GravityCompat.START)
        }


        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Set the listener for item selection
        bottomNavigation.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_item_home -> {
                    HomePage()
                    true
                }
                R.id.menu_item_orders -> {
                    // Inflate the outer_order.xml layout
                    val outerOrderLayout = LayoutInflater.from(this@Home).inflate(R.layout.outer_cover_orders, null)

                    // Find the FrameLayout in your activity_home.xml where you want to add the outer LinearLayout
                    val frameLayout = findViewById<FrameLayout>(R.id.container)
                    frameLayout.removeAllViews()

                    // Add the outer LinearLayout to the FrameLayout
                    frameLayout.addView(outerOrderLayout)

                    orders(outerOrderLayout)
                    true
                }
                R.id.menu_item_account -> {
                    account()
                    true
                }
                else -> false
            }
        }


        // for the side view inplace the variables
        val gsonUser = Gson()
        val userProfile = gsonUser.fromJson(jsonUser, User::class.java)
        usernameOfUser.text = userProfile.username
        phoneOfUser.text = userProfile.phone

    }









    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            // Pressed twice, exit the app
            finishAffinity() // Finish all activities in the task
            System.exit(0) // Exit the app
            return
        }

        this.doubleBackToExitPressedOnce = true
        //Toast.makeText(this, "Press BACK again to go to the home page", Toast.LENGTH_SHORT).show()

        // Reset the flag after a delay if not pressed again
        Handler().postDelayed({
            doubleBackToExitPressedOnce = false
        }, doubleBackToExitToastDuration.toLong())

        // go home if pressed once
        HomePage()
    }


    private fun getUserId(): Int {
        val sharedPreferences: SharedPreferences =
            getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)

        // Default value is set to 0 in case "userid" preference doesn't exist
        return sharedPreferences.getInt("userid", 0)
    }










}
