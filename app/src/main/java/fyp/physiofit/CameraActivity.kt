
package fyp.physiofit

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import fyp.physiofit.PosenetActivity
import fyp.physiofit.R

class CameraActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_camera)
    savedInstanceState ?: supportFragmentManager.beginTransaction()
      .replace(
        R.id.container,
        PosenetActivity()
      )
      .commit()
  }
}
