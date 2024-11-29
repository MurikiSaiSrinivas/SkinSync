package com.oo.skinsync

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.ai.client.generativeai.GenerativeModel
import com.google.gson.Gson
import com.oo.skinsync.databinding.ActivityMainBinding
import com.oo.skinsync.models.ColorSuggestionResponse
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val sharedPreferences by lazy {
        getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
    }
    var activeCircle: CircleColorView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Initialize the binding object
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.profileButton.setOnClickListener{
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }


        binding.genButton.setOnClickListener{
            val generativeModel = GenerativeModel(
                modelName = "gemini-1.5-flash",
                apiKey = "AIzaSyDz1jaVMwfvBHJNNHj4lfr7wvXJNmfD2tY"
            )

            val inputField = binding.locationFiledText.text.toString()

            if(inputField == ""){
                Toast.makeText(this, "Please enter the desired photoshoot location.",Toast.LENGTH_LONG).show()
            }
            else {
                val prompt = "Updated Prompt for Gemini AI Color Analysis:\n" +
                        "\"Analyze and suggest the best complementary colors for a photoshoot based on a given location and personal attributes. The input details are:\n" +
                        "Location:" + inputField + "\n" +
                        "Skin Color: " + sharedPreferences.getInt("face_color", 23) + "\n" +
                        "Lip Color: " + sharedPreferences.getInt("lip_color", 23) + "\n" +
                        "Eye Color: " + sharedPreferences.getInt("left_eye_color", 23) + "\n" +
                        "Please return the output in a structured JSON format with the following structure:\n" +
                        "\n" +
                        "Your response should be in json (example):\n" +
                        "{\n" +
                        "  \"suggestedColors\": [\n" +
                        "    {\"color\": <integer in ARGB format>, \"reason\": \"<brief explanation of the color choice>\"},\n" +
                        "    {\"color\": <integer in ARGB format>, \"reason\": \"<brief explanation of the color choice>\"},\n" +
                        "    {\"color\": <integer in ARGB format>, \"reason\": \"<brief explanation of the color choice>\"},\n" +
                        "    {\"color\": <integer in ARGB format>, \"reason\": \"<brief explanation of the color choice>\"}\n" +
                        "  ]\n" +
                        "}\n" +
                        "Ensure that the color suggestions are harmonious with the given attributes and the location's aesthetic. The reasons should be concise, highlighting why each color is visually suitable for the photoshoot.\""

                Log.d("MainActivity", prompt)
                MainScope().launch {
                    val response = generativeModel.generateContent(prompt)

                    val jsonText = response.text?.replaceFirst("json", "", ignoreCase = true)
                        ?.replace("\n", "")?.replace("```", "")?.trim()
                    if (jsonText != null) {
                        Log.d("MainActivity", jsonText)
                    }
                    val gson = Gson()
                    val json = gson.fromJson(jsonText, ColorSuggestionResponse::class.java)

                    json.suggestedColors.forEachIndexed { index, colorSuggestion ->
                        // Create a new CircleColorView instance
                        val circleView = CircleColorView(this@MainActivity)

                        circleView.id = index
                        // Set the color of the CircleColorView
                        circleView.color = colorSuggestion.color

                        circleView.setOnClickListener {
                            // Deactivate the previously active circle, if any
                            activeCircle?.isActive = false

                            // Set the clicked circle as active
                            circleView.isActive = true
                            activeCircle = circleView

                            // Update the response text based on the clicked circle
                            binding.responseText.text = colorSuggestion.reason
                        }

                        // Create LayoutParams for the circle view
                        val layoutParams = LinearLayout.LayoutParams(
                            40.dpToPx(),
                            40.dpToPx()
                        ).apply {
                            // Add margin between each view (10dp for example)
                            marginEnd = 10.dpToPx()
                        }
                        binding.colorContainer.addView(circleView, layoutParams)
                    }
                }
            }
        }

    }

    // Extension function to convert dp to px
    fun Int.dpToPx(): Int {
        val density = resources.displayMetrics.density
        return (this * density).toInt()
    }
}