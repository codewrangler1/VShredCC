package com.example.vshredcc

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors
import javax.net.ssl.HttpsURLConnection


class MainActivity : AppCompatActivity(){

    // Broadcast receiver to handle list refresh when favorited/unfavorited
    private val myBroadCastReceiver: BroadcastReceiver = MyBroadCastReceiver()

    val TAG: String = this.javaClass.name;
    var sview: SearchView? = null;

    private lateinit var recylerView: RecyclerView
    public var viewAdapter: RecyclerView.Adapter<*>? = null
    get() = field
    private lateinit var viewManager: RecyclerView.LayoutManager

    private var datalist: MutableList<itemData> = emptyList<itemData>().toMutableList()

    // onCreate
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.vshredcc.R.layout.activity_main)

        // register broadcast reciever
        val urlFilter = IntentFilter()
        urlFilter.addAction("updateList")
        registerReceiver(myBroadCastReceiver, urlFilter)

        // Setup Search and Recyler views
        setupInterface()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(myBroadCastReceiver)
    }

    fun setupInterface() {
        // Get Reference to Recycler View
        viewManager = LinearLayoutManager(this)
        viewAdapter = StarWarsAdapter(datalist, this)

        recylerView = findViewById<RecyclerView>(R.id.recyclerView).apply {
            // All views are the same
            setHasFixedSize(true)

            // use a linear layout manager
            layoutManager = viewManager

            // specify a viewAdapter
            adapter = viewAdapter
        }

        // Get Reference to Search View
        sview = findViewById(com.example.vshredcc.R.id.searchView) as SearchView
        // Set search hint
        sview!!.queryHint = """Search for Star Wars Characters"""

        // Setup QueryTextListener
        sview!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                Log.d(this.javaClass.name, "onQueryTextSubmit was called")
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                Log.d(this.javaClass.name, "onQueryTextChange was called")
                doSWSearch(newText)
                return true
            }
        })

    }


    private fun  doSWSearch(searchTxt: String?)
    {
        val doSWSearchRunnable: Runnable = Runnable {

            // Search string template
            // https://swapi.dev/api/people/?search=
            val searchUrlString: String = String.format("https://swapi.dev/api/people/?search=%s", searchTxt)

            val url: URL = URL(searchUrlString)
            // Create connection
            val con: HttpsURLConnection?
            try {
                con = url.openConnection() as HttpsURLConnection?
            } catch (e: IOException) {
                Log.d(this.javaClass.name, e.localizedMessage!!)
                e.printStackTrace()
                return@Runnable
            }

            // Set some paramaters
            con!!.setUseCaches(false)
            con.setDoInput(true)
            con.setConnectTimeout(30 * 1000) // 30 seconds to connect
            con.setReadTimeout(50 * 1000) // 5 seond timeout
            con.setRequestMethod("GET")

            try {
                con.connect()
                if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    Log.d(TAG, con.responseMessage)
                    return@Runnable
                }
                val baResponse = readFully(con.inputStream)
                val size = baResponse?.size
                if (size!! > 0) {
                    val stemp: String = String(baResponse, Charsets.UTF_8)
                    Log.d(TAG, stemp)
                    val jsonObject = JSONObject(stemp)
                    val count = jsonObject.getInt("count")
                    Log.i(TAG, "Count = $count\n")

                    val resArray = jsonObject.getJSONArray("results")
                    // Reset data
                    datalist.clear();

                    // add changed data
                    for (index in 0..count-1)
                    {
                        val item: JSONObject = resArray.get(index) as JSONObject

                        // Log Item details
                        Log.i(TAG, "Name = " + item.getString("name"))
                        Log.i(TAG, "Height = " + item.getString("height"))
                        Log.i(TAG, "Mass = " + item.getString("mass"))
                        Log.i(TAG, "Hair_color = " + item.getString("hair_color"))
                        Log.i(TAG, "Skin_color = " + item.getString("skin_color"))
                        Log.i(TAG, "Eye_color = " + item.getString("eye_color"))
                        Log.i(TAG, "Birth_year = " + item.getString("birth_year"))
                        Log.i(TAG, "Gender = " + item.getString("gender"))

                        val idata: itemData = itemData()
                        idata.text = item.getString("name")
                        idata.jsonObject = item

                        datalist.add(idata)
                    }
                    // Notify there is an update
                    runOnUiThread(Runnable { viewAdapter!!.notifyDataSetChanged() })

                }
            } catch (e: Exception) {
                Log.d(TAG, "Error: " + e.localizedMessage)
                // If we get this, we reached the end of page 1 (meaning there is a page 2)
                // Instructions for the coding challenge is to only do page 1, so gracefully
                // continue, after fail
                if (e.javaClass.simpleName.equals("JSONException")){
                    Log.i(TAG, "Reached End of Page 1")
                    runOnUiThread(Runnable { viewAdapter!!.notifyDataSetChanged() })
                    // Notify there is an update

                }
                return@Runnable
            } finally {
                con.disconnect()
            }


        }

        // Run thread
        Executors.newSingleThreadExecutor().execute(doSWSearchRunnable)
    }

    @Synchronized
    @Throws(IOException::class, OutOfMemoryError::class, IllegalArgumentException::class)
    fun readFully(`is`: InputStream?): ByteArray? {
        requireNotNull(`is`) { "input stream can not be null" }

        return try {
            val baf = ByteArrayOutputStream()
            val tmp = ByteArray(4096)
            var l: Int
            while (`is`.read(tmp).also { l = it } != -1) {
                baf.write(tmp, 0, l)
            }
            baf.toByteArray()
        } catch (e: java.lang.Exception) {
            Log.d(TAG, "read error", e)
            null
        } catch (e: OutOfMemoryError) {
            null
        } finally {
            `is`.close()
        }
    }
}

// Broadcast receiver
class MyBroadCastReceiver : BroadcastReceiver() {
   override fun onReceive(context: Context?, intent: Intent?) {

       // Handle the list refresh
       if (intent!!.action == "updateList")
       {
            val activity  = context as MainActivity
           activity.runOnUiThread(Runnable { activity.viewAdapter!!.notifyDataSetChanged() })
       }
   }
}