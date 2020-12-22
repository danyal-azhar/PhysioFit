/*
 * Copyright 2019 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tensorflow.lite.fyp.physiofit

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.tensorflow.lite.examples.posenet.R

class CameraActivity : AppCompatActivity() {

  var type = 0

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.tfe_pn_activity_camera)


    type = intent.getIntExtra("type", 0)


    var bundle = Bundle()
    bundle.putInt("type", type)
    val fragInfo = PosenetActivity()
    fragInfo.arguments = bundle

    savedInstanceState ?: supportFragmentManager.beginTransaction()
      .replace(R.id.container, fragInfo)
      .commit()
  }
}
