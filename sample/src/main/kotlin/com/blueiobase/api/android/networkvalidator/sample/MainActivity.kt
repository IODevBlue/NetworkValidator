package com.blueiobase.api.android.networkvalidator.sample

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.blueiobase.api.android.networkvalidator.NetworkValidator
import com.blueiobase.api.android.networkvalidator.networkValidator

class MainActivity : AppCompatActivity() {

    private val githubButton: ImageButton by lazy { findViewById(R.id.github_button) }
    private val tv: TextView by lazy { findViewById(R.id.text_loading) }
    private val progress: ProgressBar by lazy { findViewById(R.id.progress) }
    private val wifiIv: ImageView by lazy { findViewById(R.id.wifi_iv) }
    private val mobileDataIv: ImageView by lazy { findViewById(R.id.mobile_data_iv) }
    private val airplaneModeIv: ImageView by lazy { findViewById(R.id.airplane_mode_iv) }

    private val progressAnim by lazy { progress.animate() }

    private val thread = HandlerThread("Waiting Thread")
    private val handler by lazy { Handler(Looper.getMainLooper()) }
    private var validator: NetworkValidator? = null

    private val repoLink = "https://github.com/IODevBlue/NetworkValidator"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        githubButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(repoLink)
            startActivity(intent)
        }
        thread.start()
        networkValidator {
            evaluateCurrentNetwork(this@networkValidator, isOnline())
            setOnNetworkStateChangedListener { isOnline, _ ->
                evaluateCurrentNetwork(this@networkValidator, isOnline)
            }

            setOnAirplaneModeSwitchListener {
                evaluateCurrentNetwork(this@networkValidator, false)
            }
            validator = this
        }
    }

    override fun onResume() {
        super.onResume()
        validator?.registerAirplaneModeSwitchListener()
    }

    override fun onPause() {
        super.onPause()
        validator?.unregisterAirplaneModeSwitchListener()
    }

    private fun evaluateCurrentNetwork(netVal: NetworkValidator, bool: Boolean) {
        runOnUiThread {
            tv.text = resources.getString(R.string.detect_network)
            showProgress()
            handler.postDelayed(
                {
                    hideProgress()
                    updateNetworkState(bool)
                    changeColorWifi(netVal)
                    changeColorMobileData(netVal)
                    changeColorAirplaneMode(netVal)
                }, 1500)
        }
    }

    private fun updateNetworkState(bool: Boolean) {
        if(bool) {
            tv.text = resources.getString(R.string.network_available)
        } else {
            tv.text = resources.getString(R.string.network_unavailable)
        }
    }

    private fun hideProgress()  {
        progressAnim.let {
            it.setInterpolator(AccelerateDecelerateInterpolator()).translationY((-progress.height).toFloat())
            it.alpha(0F)
        }
    }

    private fun showProgress()  {
        progressAnim.let {
            it.setInterpolator(AccelerateDecelerateInterpolator()).translationY(0f)
            it.alpha(1F)
        }
    }

    private fun changeColorMobileData(netVal: NetworkValidator) {
        if (netVal.isCellularAvailable()) {
            DrawableCompat.wrap(mobileDataIv.drawable).setTint(ContextCompat.getColor(this, R.color.green))
        } else {
            DrawableCompat.wrap(mobileDataIv.drawable).setTint(ContextCompat.getColor(this, R.color.red))
        }
    }

    private fun changeColorWifi(netVal: NetworkValidator) {
        if (netVal.isWifiAvailable()) {
            DrawableCompat.wrap(wifiIv.drawable).setTint(ContextCompat.getColor(this, R.color.green))
        } else {
            DrawableCompat.wrap(wifiIv.drawable).setTint(ContextCompat.getColor(this, R.color.red))
        }
    }

    private fun changeColorAirplaneMode(netVal: NetworkValidator) {
        if (netVal.isAirplaneModeActive()) {
            DrawableCompat.wrap(airplaneModeIv.drawable).setTint(ContextCompat.getColor(this, R.color.green))
        } else {
            DrawableCompat.wrap(airplaneModeIv.drawable).setTint(ContextCompat.getColor(this, R.color.red))
        }
    }
}