package com.udomcive.cafe

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.widget.doAfterTextChanged
import com.android.volley.DefaultRetryPolicy
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.button.MaterialButton
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : ComponentActivity() {

    private val logoUrl = "https://cafeterion.000webhostapp.com/images/categories/logo_login.png"
    private val mainScope = CoroutineScope(Dispatchers.Main)

    private lateinit var usernameEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var loginButton: MaterialButton
    private lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)
        initUI()
        loadOnlineLogo()

        usernameEditText.doAfterTextChanged { validateInput() }
        passwordEditText.doAfterTextChanged { validateInput() }

        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()

            CoroutineScope(Dispatchers.Main).launch {

                GlobalScope.launch(Dispatchers.IO) {
                    if (!isInternetAvailable()) {
                        mainScope.launch {
                            showOfflineMessage()
                        }
                        return@launch
                    }else{
                        withContext(Dispatchers.Main) {
                            makePostRequest(this@LoginActivity, username, password)
                            /*if (isAuthenticated) {
                                navigateToHome()
                            } else {
                                showInvalidCredentialsMessage()
                            }

                             */
                        }
                    }


                }
            }

        }
    }

    private fun initUI() {
        imageView = findViewById(R.id.logo_login)
        usernameEditText = findViewById(R.id.username2)
        passwordEditText = findViewById(R.id.password2)
        loginButton = findViewById(R.id.loginButton)
    }

    private fun loadOnlineLogo() {
        val requestOptions = RequestOptions()
            .placeholder(R.drawable.logo_login)
            .error(R.drawable.logo_login)

        Glide.with(this)
            .load(logoUrl)
            .apply(requestOptions)
            .into(imageView)
    }

    private fun validateInput() {
        val username = usernameEditText.text.toString()
        val password = passwordEditText.text.toString()
        loginButton.isEnabled = username.isNotEmpty() && password.isNotEmpty()
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
            return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                ?: false
        } else {
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            return activeNetworkInfo != null && activeNetworkInfo.isConnected
        }
    }

    private fun showOfflineMessage() {
        Toast.makeText(this, "You are offline!", Toast.LENGTH_LONG).show()
    }

    private fun makePostRequest(context: Context, email: String, password: String) {
        val requestQueue = Volley.newRequestQueue(context)
        val url = "https://cafeterion.000webhostapp.com/supplier/api.php" // Replace with your actual API endpoint

        // Define your request parameters here
        val params = HashMap<String, String>()
        params["email"] = email
        params["password"] = password
        params["type"] = "auth"
        params["key"] = "mynameismasterplancafeterion"


        // Set a timeout of 15 seconds (15000 milliseconds)
        val timeout = 15000
        val stringRequest = object : StringRequest(
            Request.Method.POST, url,
            Response.Listener { response ->
                // Handle the response from the server
                Log.i("Response from the server", "response: $response")
                val responseBody = response.toInt() ?: 0
                if (responseBody > 0) {
                    saveUserId(responseBody)
                    Log.i("Valid Credentials", "User provided correct credentials")
                    navigateToHome()
                } else {
                    Log.i("Wrong Credentials", "User provided wrong credentials")
                    showInvalidCredentialsMessage()
                }
            },
            Response.ErrorListener { error ->
                // Handle any errors that occur
                Log.e("Error Occurred", error.toString())
                showOfflineMessage()
            }) {
            override fun getParams(): Map<String, String> {
                return params
            }
        }

        // Set the timeout for the request
        stringRequest.retryPolicy = DefaultRetryPolicy(
            timeout,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        requestQueue.add(stringRequest)

    }


    private fun saveUserId(userId: Int) {
        val sharedPreferences: SharedPreferences =
            getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)

        val editor = sharedPreferences.edit()
        editor.putInt("userid", userId)
        editor.apply()
    }

    private fun navigateToHome() {
        val intent = Intent(this@LoginActivity, Home::class.java)
        startActivity(intent)
    }

    private fun showInvalidCredentialsMessage() {
        Toast.makeText(this, "Invalid Password or Username", Toast.LENGTH_LONG).show()
    }
}
