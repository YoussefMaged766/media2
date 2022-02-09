package com.example.media2


import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.service.carrier.CarrierMessagingService
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.OptionalPendingResult
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.common.api.Status


class google_activity : AppCompatActivity() , View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    private val TAG = MainActivity::class.java.simpleName
    private val RC_SIGN_IN = 420
    private var mGoogleApiClient: GoogleApiClient? = null
    private var mProgressDialog: ProgressDialog? = null
    private var btnSignIn: SignInButton? = null
    private var btnSignOut: Button? = null
    private var llProfileLayout: LinearLayout? = null
    private lateinit var imgProfilePic: ImageView
    private var txtName: TextView? = null
    private  var txtEmail:TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_google)

        initializeControls()
        initializeGPlusSettings()

    }
    private fun initializeControls() {
        btnSignIn = findViewById<View>(R.id.btn_sign_in) as SignInButton
        btnSignOut = findViewById<View>(R.id.btn_sign_out) as Button
        llProfileLayout = findViewById<View>(R.id.llProfile) as LinearLayout
        imgProfilePic = findViewById<View>(R.id.imgProfilePic) as ImageView

        txtName = findViewById<View>(R.id.txtName) as TextView
        txtEmail = findViewById<View>(R.id.txtEmail) as TextView
        btnSignIn!!.setOnClickListener(this)
        btnSignOut!!.setOnClickListener(this)
    }

    private fun initializeGPlusSettings() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        mGoogleApiClient = GoogleApiClient.Builder(this)
            .enableAutoManage(this, this)
            .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
            .build()

        // Customizing G+ button
        btnSignIn!!.setSize(SignInButton.SIZE_STANDARD)
        btnSignIn!!.setScopes(gso.scopeArray)
    }


    private fun signIn() {
        val signInIntent: Intent? = mGoogleApiClient?.let { Auth.GoogleSignInApi.getSignInIntent(it) }
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }


    private fun signOut() {
        mGoogleApiClient?.let {
            Auth.GoogleSignInApi.signOut(it).setResultCallback(
                object : ResultCallback<Status?> {
                    override fun onResult(p0: Status) {
                        updateUI(false)
                    }
                })
        }
    }

    private fun handleGPlusSignInResult(result: GoogleSignInResult) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess())
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
//            val acct: GoogleSignInAccount? = result.getSignInAccount()
//
//            //Fetch values
//            val personName = acct?.displayName
//            val personPhotoUrl = acct?.photoUrl.toString()
//            val email = acct?.email
//            val familyName = acct?.familyName
//            Log.e(
//                TAG, "Name: " + personName +
//                        ", email: " + email +
//                        ", Image: " + personPhotoUrl +
//                        ", Family Name: " + familyName
//            )
            val acct1 = GoogleSignIn.getLastSignedInAccount(this)
            if (acct1!=null){
                val personName = acct1?.displayName
                val personPhotoUrl = acct1?.photoUrl.toString()
                val email = acct1?.email
                val familyName = acct1?.familyName
                Log.d(TAG,"name" +personName)
                txtName!!.text = personName
                txtEmail!!.text = email
                Glide.with(getApplicationContext()).load(personPhotoUrl)
                    .into(imgProfilePic)
            }
            //Set values


            //Set profile pic with the help of Glide

            updateUI(true)
        } else {
            // Signed out, show unauthenticated UI.
            updateUI(false)
        }
    }

    override fun onClick(v: View) {
        val id: Int = v.getId()
        when (id) {
            R.id.btn_sign_in -> signIn()
            R.id.btn_sign_out -> signOut()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val result: GoogleSignInResult? = data?.let {
                Auth.GoogleSignInApi.getSignInResultFromIntent(
                    it
                )
            }
            if (result != null) {
                handleGPlusSignInResult(result)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val opr: OptionalPendingResult<GoogleSignInResult> =
            mGoogleApiClient?.let { Auth.GoogleSignInApi.silentSignIn(it) } as OptionalPendingResult<GoogleSignInResult>
        if (opr.isDone()) {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            Log.e(TAG, "Got cached sign-in")
            val result: GoogleSignInResult = opr.get()
            handleGPlusSignInResult(result)
        } else {
            // If the user has not previously signed in on this device or the sign-in has expired,
            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
            // single sign-on will occur in this branch.
            showProgressDialog()
            opr.setResultCallback(object : CarrierMessagingService.ResultCallback<GoogleSignInResult?>,
                ResultCallback<GoogleSignInResult> {
                override fun onResult(googleSignInResult: GoogleSignInResult) {
                    hideProgressDialog()
                    handleGPlusSignInResult(googleSignInResult)
                }

                override fun onReceiveResult(result: GoogleSignInResult) {
                    TODO("Not yet implemented")
                }
            })
        }
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.e(TAG, "onConnectionFailed:$connectionResult")
    }

    private fun showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog(this)
            mProgressDialog!!.setMessage("loading")
            mProgressDialog!!.setIndeterminate(true)
        }
        mProgressDialog!!.show()
    }

    private fun hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog!!.isShowing()) {
            mProgressDialog!!.hide()
        }
    }

    private fun updateUI(isSignedIn: Boolean) {
        if (isSignedIn) {
            btnSignIn!!.visibility = View.GONE
            btnSignOut!!.visibility = View.VISIBLE
            llProfileLayout!!.visibility = View.VISIBLE
        } else {
            btnSignIn!!.visibility = View.VISIBLE
            btnSignOut!!.visibility = View.GONE
            llProfileLayout!!.visibility = View.GONE
        }
    }
}