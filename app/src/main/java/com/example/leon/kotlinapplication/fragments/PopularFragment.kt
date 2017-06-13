package com.example.leon.kotlinapplication.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.leon.kotlinapplication.R
import com.example.leon.kotlinapplication.activities.MainActivity
import com.example.leon.kotlinapplication.adapter.MovieAdapter
import kotlin.properties.Delegates
import android.support.v7.widget.GridLayoutManager
import com.android.volley.Request
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.android.volley.Response
import com.example.leon.kotlinapplication.jsonParser
import com.example.leon.kotlinapplication.model.List
import io.realm.*
import io.realm.exceptions.RealmPrimaryKeyConstraintException
import io.realm.internal.IOException
import java.io.FileNotFoundException
import com.example.leon.kotlinapplication.R.id.recyclerView
import android.support.v7.widget.DefaultItemAnimator




/**
 * A simple [Fragment] subclass.
 * Use the [PopularFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PopularFragment(var a: MainActivity) : Fragment() {


    var realm: Realm by Delegates.notNull()
    var adapter = MovieAdapter()
    var refreshLayout = SwipeRefreshLayout(a)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        var rootView = inflater!!.inflate(R.layout.fragment_popular, container, false)
        var recyclerView = rootView.findViewById(R.id.recyclerView2) as RecyclerView
        refreshLayout = rootView.findViewById(R.id.refreshContainer) as SwipeRefreshLayout


        // Initialize realm
        Realm.init(context)
        realm = Realm.getDefaultInstance()
        realm.refresh()



        // Set up recycler view
        adapter = MovieAdapter()
        recyclerView.adapter = adapter
        val itemAnimator = DefaultItemAnimator()
        itemAnimator.addDuration = 300
        itemAnimator.removeDuration = 300
        recyclerView.itemAnimator = itemAnimator
        recyclerView.layoutManager = GridLayoutManager(activity, 2)

        // Set up refresh listener
        refreshLayout.setOnRefreshListener { httpRequest() }

        // load data from https://api.themoviedb.org/3
        httpRequest()
        return rootView
    }

    // httpRequest() makes a GET Request to https://api.themoviedb.org/3
    // The response is a json String
    private fun httpRequest() {
        val queue = Volley.newRequestQueue(activity)
        val url = getString(R.string.base_url) +
                "movie/popular?api_key=" +
                getString(R.string.key) +
                "&language=en-US&page=1"

        // Request a string response from the provided URL.
        val stringRequest = StringRequest(Request.Method.GET, url, object : Response.Listener<String> {
            override fun onResponse(response: String) {
                // Display the first 500 characters of the response string.
                Log.d("Resonse", response)
                fetchRequest(response)
            }
        }, object : Response.ErrorListener {
            override fun onErrorResponse(error: VolleyError) {
                error.printStackTrace()
                updateUIfromRealm()
            }
        })

        queue.add(stringRequest)
        queue.start()
    }


    // updates the recycler view with the data from the realm
    private fun updateUIfromRealm() {
        var results: RealmResults<List> = realm.where(List::class.java).equalTo("id", 0).findAll()
        Log.d("PopularFragment", " updateRealm(): Size of Popular Movie Lists:" + results.size)

        if (adapter != null) {
            adapter.addData(results.get(0).results.sort("popularity", Sort.DESCENDING))
        } else {
            Log.d("PopularFragment", "adapter is null")
        }
        if (refreshLayout != null) {
            refreshLayout.isRefreshing = false
        }
    }

    fun deleteRealm() {
        Realm.init(activity)
        var realm: Realm = Realm.getDefaultInstance()
        realm.executeTransaction {
            realm.deleteAll()
        }

    }

    // fetches the json string response to the realm database
    // calls updateUIfromRealm afterwards to notify the user about the new data
    private fun fetchRequest(response: String) {
        try {
            realm.executeTransaction {
                //val input: InputStream = assets.open("response.json")
                Log.d("PopularFragment:", "Response: " + response)
                var modedResponse: String = jsonParser(response)
                        .insertValueInt("id", 0)
                        .insertValueString("name", "popularMovies")
                        .makeArray().json

                Log.d("PopularFragment:", "moded Response: " + modedResponse)
                realm.createOrUpdateAllFromJson(List::class.java, modedResponse)
                updateUIfromRealm()
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            throw RuntimeException(e)
        } catch (e: RealmPrimaryKeyConstraintException) {
            e.printStackTrace()
        }

    }

}




