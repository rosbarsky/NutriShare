package net.nutrishare.app.view.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import net.nutrishare.app.R
import net.nutrishare.app.viewmodel.SplashViewModel

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private val splashViewModel: SplashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        lifecycleScope.launch {
            splashViewModel.checkUserSession()
        }
        Handler(Looper.getMainLooper()).postDelayed({
            splashViewModel.sessionCheckResult.observe(this) { isLoggedIn ->
                if (isLoggedIn) {
                    startActivity(Intent(this,MainActivity::class.java)).apply {
                        finish()
                    }
                } else {
                    startActivity(Intent(this,AuthActivity::class.java)).apply {
                        finish()
                    }
                }
            }

        },3000)
    }
}