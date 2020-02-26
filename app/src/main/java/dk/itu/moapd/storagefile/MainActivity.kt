package dk.itu.moapd.storagefile

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val fragment= supportFragmentManager
                .findFragmentById(R.id.fragment)
        if (fragment == null)
            supportFragmentManager
                    .beginTransaction()
                    .add(R.id.fragment, MainFragment())
                    .commit()
    }

}
