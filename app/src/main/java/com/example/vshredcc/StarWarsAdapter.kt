package com.example.vshredcc

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONObject


// ItemData class - Holds the data for each item in the list
// This is generated from the response from the search call
public class itemData {
    var text: String? = null
    var jsonObject: JSONObject? = null
}

// Adapter to handle each item in the list
public class StarWarsAdapter(private val myDataset: MutableList<itemData>, private val activity: MainActivity) :
        RecyclerView.Adapter<StarWarsAdapter.MyViewHolder>(), View.OnClickListener {

    val TAG: String = this.javaClass.name;

    // Handle onClick for list. Launches detail view
    override fun onClick(view: View?) {

        val textView = view?.findViewById<TextView>(R.id.itemtextview)
        val jsonObject: JSONObject =textView?.tag as JSONObject

        // Log selection
        Log.i(TAG, "Clicked on " + textView.text)

        // Launch detail view
        val ni = Intent(activity, DetailViewActivity::class.java)
        ni.putExtra("itemData", jsonObject.toString())
        activity.startActivity(ni)

    }

    // Define basic view holder
    class MyViewHolder(val linearLayout: LinearLayout) : RecyclerView.ViewHolder(linearLayout)


    // Replace the contents of a view (invoked by the layout manager)
    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // Get reference to TextView
        val textView = holder.itemView.findViewById<TextView>(R.id.itemtextview)

        // Set Text and data object (tag) for item
        textView.text = myDataset[position].text
        textView.tag = myDataset[position].jsonObject

        // Is Favorited?
        val preferences = activity.getSharedPreferences("favorites", Context.MODE_PRIVATE);
        val favorited: Boolean = preferences.getBoolean(myDataset[position].text, false)

        // Set image or no image, based on favorited
        if (favorited) {
            android.R.drawable.btn_star_big_on
            val drawable = activity.resources.getDrawable(android.R.drawable.btn_star_big_on, null)
            textView.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
        } else {
            textView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        }

        // Set onClick listener
        holder.linearLayout.setOnClickListener(this);
    }

    // Return the size (item count) of the dataset (invoked by the layout manager)
    override fun getItemCount() = myDataset.size

    // Create View Holder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StarWarsAdapter.MyViewHolder {

        // create a new view
        val layout = LayoutInflater.from(parent.context)
                .inflate(R.layout.search_result, parent, false) as LinearLayout

        return MyViewHolder(layout)
    }
}