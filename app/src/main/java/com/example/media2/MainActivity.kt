package com.example.media2

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import org.json.JSONException
import java.util.*


private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    private var callbackManager: CallbackManager? = null
    private lateinit var loginButton: LoginButton
    private lateinit var btn_logout: Button
    private lateinit var txtname: TextView
    private lateinit var imgProfilePic: ImageView
    private lateinit var btn_google: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        calling()
        callbackManager = CallbackManager.Factory.create()
        loginButton.setPermissions(listOf("email", "public_profile"))

        buttons_listners()
        val accessToken = AccessToken.getCurrentAccessToken()
        val isLoggedIn = accessToken != null && !accessToken.isExpired





    }

    private fun calling() {
        imgProfilePic = findViewById(R.id.imgProfilePic1)
        txtname = findViewById(R.id.txtname_fac)
        btn_google = findViewById(R.id.btngo_google)
        btn_logout = findViewById(R.id.btn_logoutfb)
        loginButton = findViewById(R.id.login_button)

    }

    private fun buttons_listners() {
        loginButton.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {

            override fun onSuccess(result: LoginResult) {


                val userId = result.accessToken.userId
                Log.d(TAG, "onSuccess: userId $userId")
//               loginButton.setReadPermissions(Arrays.asList("user_status"))
                val bundle = Bundle()
                bundle.putString("fields", "id, email, name, gender,birthday")
                updateUI(true)


                //Graph API to access the data of user's facebook account
                val request = GraphRequest.newMeRequest(
                    result.accessToken
                ) { fbObject, response ->
                    Log.v("Login Success", response.toString())

                    //For safety measure enclose the request with try and catch
                    try {

                        val profilePicture_url =
                            "https://graph.facebook.com/" + userId + "/picture?type=large"
                        Log.e(TAG, "picture" + profilePicture_url)
                        Glide.with(getApplicationContext()).load(profilePicture_url)
                            .into(imgProfilePic)
                        Log.d(TAG, "onSuccess: fbObject $fbObject")

                        val Name = fbObject?.getString("name")
                        val email = fbObject?.getString("email")
                        txtname.text = Name
                        loginButton.visibility = View.GONE


//                        Log.d(TAG, "onSuccess: firstName $firstName")
//                        Log.d(TAG, "onSuccess: lastName $lastName")
//                        Log.d(TAG, "onSuccess: gender $gender")
                        Log.d(TAG, "onSuccess: email $email")

                    } //If no data has been retrieve throw some error
                    catch (e: JSONException) {

                    }

                }
//                Set the bundle's data as Graph's object data
                request.parameters(bundle)

                //Execute this Graph request asynchronously
                request.executeAsync()

            }

            override fun onCancel() {
                Log.d(TAG, "onCancel: called")
                Toast.makeText(applicationContext, "login cancel", Toast.LENGTH_SHORT).show()


            }

            override fun onError(error: FacebookException) {
                Log.d(TAG, "onError: called")
                error.printStackTrace()
            }
        })

        btn_logout.setOnClickListener {
            FacebookSdk.sdkInitialize(this.getApplicationContext())
            LoginManager.getInstance().logOut()
            updateUI(false)

        }
        btn_google.setOnClickListener {
            var i = Intent(this, google_activity::class.java)
            startActivity(i)
        }
    }

    private fun updateUI(isSignedIn: Boolean) {
        if (isSignedIn) {
            loginButton!!.visibility = View.GONE
            btn_logout!!.visibility = View.VISIBLE
            imgProfilePic!!.visibility = View.VISIBLE
            txtname!!.visibility = View.VISIBLE
        } else {
            loginButton!!.visibility = View.VISIBLE
            btn_logout!!.visibility = View.GONE
            imgProfilePic!!.visibility = View.GONE
            txtname!!.visibility = View.GONE
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager?.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)

    }

}

private fun GraphRequest.parameters(bundle: Bundle) {

}



