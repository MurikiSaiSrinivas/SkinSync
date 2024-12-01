package com.oo.skinsync

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.oo.skinsync.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Initialize the binding object
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPreferences = getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
        val imageUriString = sharedPreferences.getString("profile_image_uri", null)
        val editor = sharedPreferences.edit()


        // Set an onClickListener on the button
        binding.cameraButton.setOnClickListener{
            val intent = Intent(this, CameraActivity::class.java)
            startActivity(intent)
        }

        if (imageUriString != null) {
            val imageUri = Uri.parse(imageUriString)
            binding.profileImageView.setImageURI(imageUri)
        }

        binding.nameSave.setOnClickListener{
            editor.putString("name", binding.nameText.text.toString())
            editor.apply()
        }

        binding.ageSave.setOnClickListener {
            editor.putString("age", binding.ageField.text.toString())
            editor.apply()
        }

        binding.radioFemale.setOnClickListener{
            editor.putString("sex", binding.radioFemale.text.toString())
            editor.apply()
        }

        binding.radioMale.setOnClickListener{
            editor.putString("sex", binding.radioMale.text.toString())
            editor.apply()
        }

        binding.cheekColorView.color = sharedPreferences.getInt("face_color",123)
        binding.lipColorView.color = sharedPreferences.getInt("lip_color",123)
        binding.lEyeColorView.color = sharedPreferences.getInt("left_eye_color",123)
        binding.rEyeColorView.color = sharedPreferences.getInt("right_eye_color",123)
        binding.nameText.setText(sharedPreferences.getString("name", ""))
        binding.ageField.setText(sharedPreferences.getString("age", ""))
        if(sharedPreferences.getString("sex", "Female") == "Female"){
            binding.radioFemale.isChecked = true
        }
        else{
            binding.radioMale.isChecked = true
        }
    }
}