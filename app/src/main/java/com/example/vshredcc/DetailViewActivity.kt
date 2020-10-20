package com.example.vshredcc

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import org.json.JSONObject

class DetailViewActivity : AppCompatActivity() {

    val TAG: String = this.javaClass.name;
    private lateinit var name: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_view)

        // Handle incoming intenet
        intent.also {
            handleIntent(intent)
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun handleIntent(intent: Intent?) {
        if (intent != null) {
            val jsonString = intent.getStringExtra("itemData")

            if (jsonString != null) {
                Log.d(TAG, jsonString)

                // Create JSONObject from String
                val jsonObject: JSONObject = JSONObject(jsonString)

                window.findViewById<TextView>(R.id.tvName).text = jsonObject.getString("name")
                name = jsonObject.getString("name")
                window.findViewById<TextView>(R.id.tvHeight).text = jsonObject.getString("height")
                window.findViewById<TextView>(R.id.tvMass).text = jsonObject.getString("mass")
                window.findViewById<TextView>(R.id.tvHairColor).text = jsonObject.getString("hair_color")
                window.findViewById<TextView>(R.id.tvSkinColor).text = jsonObject.getString("skin_color")
                window.findViewById<TextView>(R.id.tvEyeColor).text = jsonObject.getString("eye_color")
                window.findViewById<TextView>(R.id.tvBirthYear).text = jsonObject.getString("birth_year")
                window.findViewById<TextView>(R.id.tvGender).text = jsonObject.getString("gender")

                val ib: ImageButton = window.findViewById<ImageButton>(R.id.togglefavorite)

                val preferences = getSharedPreferences("favorites", Context.MODE_PRIVATE);

                val favorited: Boolean = preferences.getBoolean(name, false)

                val resourceId: Int
                if (favorited) {
                    resourceId = android.R.drawable.btn_star_big_on
                } else {
                    resourceId = android.R.drawable.btn_star_big_off
                }

                val drawable = resources.getDrawable(resourceId, null)


                ib.setImageDrawable(drawable)

            }
        }
    }

    fun OnClickClose(view: View) {
        finish()
    }
    var favorited: Boolean = false;
    @SuppressLint("UseCompatLoadingForDrawables")
    fun OnToggleFavorite(view: View) {

        // Get reference to favorite button
        val ib: ImageButton = window.findViewById<ImageButton>(R.id.togglefavorite)

        // Is favorited?
        val preferences = getSharedPreferences("favorites", Context.MODE_PRIVATE);
        var favorited: Boolean = preferences.getBoolean(name, false)

        // Toggle favorited
        favorited = !favorited // toggle

        // Set image for favorite button
        val resourceId: Int
        if (favorited) {
            resourceId = android.R.drawable.btn_star_big_on
        } else {
            resourceId = android.R.drawable.btn_star_big_off
        }

        // Get image resource
        val drawable = resources.getDrawable(resourceId, null)

        // Set image for button
        ib.setImageDrawable(drawable)

        // Save favortied value for item 'name'
        preferences.edit(){
            putBoolean(name, favorited).commit()
        }

        // Let MainActivity know that there was a change
        val intent: Intent = Intent("updateList")
        sendBroadcast(intent)

    }

}