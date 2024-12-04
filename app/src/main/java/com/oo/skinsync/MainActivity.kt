package com.oo.skinsync

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.ai.client.generativeai.GenerativeModel
import com.google.gson.Gson
import com.oo.skinsync.api.RetrofitClient
import com.oo.skinsync.databinding.ActivityMainBinding
import com.oo.skinsync.models.ColorSuggestionResponse
import com.oo.skinsync.models.SerpApiResponse
import com.oo.skinsync.models.ShoppingResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val sharedPreferences by lazy {
        getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
    }
    var activeCircle: CircleColorView? = null
    private val serpApiKey = "Your Serp API key goes here"
    private val geminiApiKey = "Your Gemini API key goes here"


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

        val imageUriString = sharedPreferences.getString("profile_image_uri", null)
        if (imageUriString != null) {
            val imageUri = Uri.parse(imageUriString)
            binding.profileButton.setImageURI(imageUri)
        }
        else{
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }


        // Gemini api key here
        binding.genButton.setOnClickListener{

            showLoadingSpinner()
            val generativeModel = GenerativeModel(
                modelName = "gemini-1.5-flash",
                apiKey = geminiApiKey
            )

            val inputField = binding.locationFiledText.text.toString()

            if(inputField == ""){
                Toast.makeText(this, "Please enter the desired photoshoot location.",Toast.LENGTH_LONG).show()
            }
            else {
                val prompt = "Updated Prompt for Gemini AI Color Analysis:\n" +
                        "\"Analyze and suggest the best complementary colors for a photoshoot based on a given location and personal attributes. The input details are:\n" +
                        "Location:" + inputField + "\n" +
                        "gender: "+ sharedPreferences.getString("sex","Female") +"\n" +
                        "age:" + sharedPreferences.getString("age","23") + "\n" +
                        "Skin Color: " + sharedPreferences.getInt("face_color", 23) + "\n" +
                        "Lip Color: " + sharedPreferences.getInt("lip_color", 23) + "\n" +
                        "Eye Color: " + sharedPreferences.getInt("left_eye_color", 23) + "\n" +
                        "Please return the output in a structured JSON format with the following structure:\n" +
                        "\n" +
                        "Your response should be in json (example):\n" +
                        "{\n" +
                        "  \"suggestedColors\": [\n" +
                        "    {\"color\": <Hex Code>, \"reason\": \"<brief explanation of the color choice>\",\"dresses\": [\"<dress name>\", \"<dress name>\", ...]}\n"+
                        "    {\"color\": <Hex Code>, \"reason\": \"<brief explanation of the color choice>\",\"dresses\": [\"<dress name>\", \"<dress name>\", ...]}\n"+
                        "    {\"color\": <Hex Code>, \"reason\": \"<brief explanation of the color choice>\",\"dresses\": [\"<dress name>\", \"<dress name>\", ...]}\n"+
                        "    {\"color\": <Hex Code>, \"reason\": \"<brief explanation of the color choice>\",\"dresses\": [\"<dress name>\", \"<dress name>\", ...]}\n" +
                        "  ]\n" +
                        "}\n" +
                        "Ensure that the color suggestions are harmonious with the given attributes and the location's aesthetic. For each suggested color, provide a list of dress names that match the color, gender ("+sharedPreferences.getString("sex","Female") +"), and age ("+sharedPreferences.getString("age","23")+"). These dress names should represent clothing items from popular online shopping sites such as Amazon, Myntra, Zara, and others. Each dress name should be concise and descriptive (e.g., 'Elegant Evening Gown').The reasons for color choices should be concise, highlighting why each color is visually suitable for the photoshoot."

                Log.d("MainActivity", prompt)
                binding.colorContainer.removeAllViews()
                MainScope().launch {
                    val response = generativeModel.generateContent(prompt)
                    val bool = false

                    if(bool){
                        binding.responseText.text = response.text
                    }
                    else {
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
                            circleView.color = Color.parseColor(colorSuggestion.color)

                            circleView.setOnClickListener {
                                showLoadingSpinner()
                                // Deactivate the previously active circle, if any
                                activeCircle?.isActive = false

                                // Set the clicked circle as active
                                circleView.isActive = true
                                activeCircle = circleView

                                // Update the response text based on the clicked circle
                                binding.responseText.text = colorSuggestion.reason +"\n" + "Type of dresses can wear:\n" + colorSuggestion.dresses
                                binding.productContainer.removeAllViews()

                                colorSuggestion.dresses.forEachIndexed { index,dress ->
//                                    Log.d("DressInMainActivity", "${index}:${dress}")
                                    fetchShoppingResults(dress)
                                }
                                hideLoadingSpinner()
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

            hideLoadingSpinner()
        }

    }

    // Extension function to convert dp to px
    fun Int.dpToPx(): Int {
        val density = resources.displayMetrics.density
        return (this * density).toInt()
    }

    private fun fetchShoppingResults(query: String) {
        val call = RetrofitClient.instance.getShoppingResults(
            apiKey = serpApiKey,
            query = query
        )

        call.enqueue(object : Callback<SerpApiResponse> {
            override fun onResponse(
                call: Call<SerpApiResponse>,
                response: Response<SerpApiResponse>
            ) {
                if (response.isSuccessful) {
                    val results = response.body()?.shopping_results?.take(4) ?: emptyList()
                    Log.d("SerpAPI", "Results for $query: $results")
                    // You can now process or display the results in your UI
                    updateUI(query, results)
                } else {
                    Log.e("SerpAPI", "Error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<SerpApiResponse>, t: Throwable) {
                Log.e("SerpAPI", "Failed to fetch data: ${t.message}")
            }
        })
    }

    private fun updateUI(dress: String, results: List<ShoppingResult>) {

        // Create a HorizontalScrollView to allow horizontal scrolling
        val horizontalScrollView = HorizontalScrollView(this).apply {
            // Set the width and height of the HorizontalScrollView
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val horizontalLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginEnd = 20
            }
        }

        // Update the UI with the shopping results
        results.forEach { result ->

            val cardView = LayoutInflater.from(this).inflate(R.layout.product_card_layout, null) as CardView

            // Set the image (thumbnail)
            val imageView = cardView.findViewById<ImageView>(R.id.product_thumbnail)
            Glide.with(this).load(result.thumbnail).into(imageView)

            // Set the product details (title, price, link, source)
            val productDetails = "${result.title}\n${result.price}\nFrom: ${result.source}"
            val textView = cardView.findViewById<TextView>(R.id.product_details)
            textView.text = productDetails

            cardView.setOnClickListener {
                val productLink = result.product_link

                // Create an intent to open the product link in the browser
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(productLink))
                startActivity(intent)
            }
            horizontalLayout.addView(cardView)
        }
        horizontalScrollView.addView(horizontalLayout)
        binding.productContainer.addView(horizontalScrollView)
    }

    private fun showLoadingSpinner() {
        binding.dimOverlay.visibility = View.VISIBLE
        binding.loadingSpinner.visibility = View.VISIBLE
    }

    private fun hideLoadingSpinner() {
        CoroutineScope(Dispatchers.Main).launch {
            delay(5000) // 11 seconds
            binding.dimOverlay.visibility = View.INVISIBLE
            binding.loadingSpinner.visibility = View.INVISIBLE
        }
    }
}