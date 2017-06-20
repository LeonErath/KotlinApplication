package com.example.leon.kotlinapplication.adapter

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.leon.kotlinapplication.R
import com.example.leon.kotlinapplication.jsonParser
import com.example.leon.kotlinapplication.model.List
import com.example.leon.kotlinapplication.model.Movie
import io.realm.Realm
import io.realm.RealmQuery
import io.realm.RealmResults
import io.realm.exceptions.RealmPrimaryKeyConstraintException
import io.realm.internal.IOException
import java.io.FileNotFoundException
import kotlin.properties.Delegates

/**
 * Created by Leon on 15.06.17.
 */
class QueryAdapter(c: Context) {
    var realm: Realm by Delegates.notNull()
    var c: Context
    val TAG = QueryAdapter::class.java.simpleName

    init {
        Realm.init(c)
        realm = Realm.getDefaultInstance()
        this.c = c
    }

    fun getDetail(id: Int): Movie {
        val url = c.getString(R.string.base_url) +
                "movie/$id?api_key=" +
                c.getString(R.string.key) +
                "&language=en-US"
        Log.d(TAG, url)


        val urlCast = c.getString(R.string.base_url) +
                "movie/$id/credits?api_key=" +
                c.getString(R.string.key) +
                "&language=en-US"
        Log.d(TAG, urlCast)
        val urlTrailer = c.getString(R.string.base_url) +
                "movie/$id/videos?api_key=" +
                c.getString(R.string.key) +
                "&language=en-US"
        Log.d(TAG, urlTrailer)
        val urlRecom = c.getString(R.string.base_url) +
                "movie/$id/recommendations?api_key=" +
                c.getString(R.string.key) +
                "&language=en-US"
        Log.d(TAG, urlRecom)
        httpRequestDetail(url, urlCast, urlTrailer, urlRecom, id)
        return findMovie(id)
    }


    fun getCinema(page: Int, loadData: LoadData) {
        val queue = Volley.newRequestQueue(c)
        val url = c.getString(R.string.base_url) +
                "movie/now_playing?api_key=" +
                c.getString(R.string.key) +
                "&language=en-US&page=$page"

        // Request a string response from the provided URL.
        val stringRequest = StringRequest(Request.Method.GET, url, Response.Listener<String> { response ->
            // Display the first 500 characters of the response string.
            Log.i(TAG, "Response:" + response)
            fetchRequestCinema(response, loadData)
        }, Response.ErrorListener { error ->
            error.printStackTrace()
        })

        queue.add(stringRequest)
        queue.start()
    }

    fun getPopular(page: Int, loadData: LoadData) {
        val queue = Volley.newRequestQueue(c)
        val url = c.getString(R.string.base_url) +
                "movie/popular?api_key=" +
                c.getString(R.string.key) +
                "&language=en-US&page=$page"

        // Request a string response from the provided URL.
        val stringRequest = StringRequest(Request.Method.GET, url, Response.Listener<String> { response ->
            // Display the first 500 characters of the response string.
            Log.i(TAG, "Response:" + response)
            fetchRequestPopular(response, loadData)
        }, Response.ErrorListener { error ->
            error.printStackTrace()
        })

        queue.add(stringRequest)
        queue.start()
    }


    fun deleteFromMyList(adapter: MovieFlatAdapter, movie: Movie) {
        realm.executeTransaction {
            val results: RealmResults<List> = realm.where(List::class.java).equalTo("id", 2).findAll()
            if (results.size > 0) {
                Log.d(TAG, "MyList is not empty -> updates List")
                val List = results[0]
                List.results.remove(movie)
                List.total_results--
                adapter.removeMovie(movie)
            }
        }
    }

    fun putInMyList(movie: Movie) {
        realm.executeTransaction {
            val results: RealmResults<List> = realm.where(List::class.java).equalTo("id", 2).findAll()
            if (results.size > 0) {
                Log.d(TAG, "MyList is not empty -> updates List")
                val List = results[0]
                var check = false
                for (movie2 in List.results) {
                    if (movie!!.id == movie2.id) check = true
                }

                if (!check) {
                    List.results.add(movie)
                    List.total_results++
                }

            } else {
                Log.d(TAG, "MyList is empty -> creates new List")
                val List = List()
                List.id = 2
                List.name = "MyList"
                List.results.add(movie)
                List.total_results++
                realm.copyToRealmOrUpdate(List)
            }

            getDetail(movie.id)
        }

    }

    private fun httpRequestDetail(url: String, urlCast: String, urlTrailer: String, urlRecom: String, id: Int) {

        val queue = Volley.newRequestQueue(c)

        val stringRequestRecom = StringRequest(Request.Method.GET, urlRecom, Response.Listener<String> { response ->
            Log.d(TAG, response)
            fetchRequest(jsonParser(response).parseRecommendation(id))
        }, Response.ErrorListener { error -> error.printStackTrace() })


        queue.add(makeRequest(url))
        queue.add(makeRequest(urlCast))
        queue.add(makeRequest(urlTrailer))
        queue.add(stringRequestRecom)

    }

    // fetches the json string response to the realm database
    // calls updateUIfromRealm afterwards to notify the user about the new data
    private fun fetchRequestPopular(response: String, loadData: LoadData) {
        try {
            realm.executeTransaction {
                val modedResponse: String = jsonParser(response)
                        .insertValueInt("id", 0)
                        .insertValueString("name", "popularMovies")
                        .makeArray().json

                Log.i(TAG, "moded Response: " + modedResponse)
                realm.createOrUpdateAllFromJson(List::class.java, modedResponse)
            }
            loadData.update(0)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            throw RuntimeException(e)
        } catch (e: RealmPrimaryKeyConstraintException) {
            e.printStackTrace()
        }

    }

    private fun fetchRequestCinema(response: String, loadData: LoadData) {
        try {
            realm.executeTransaction {
                val modedResponse: String = jsonParser(response)
                        .insertValueInt("id", 1)
                        .insertValueString("name", "cinemaMovies")
                        .makeArray().json

                Log.i(TAG, "moded Response: " + modedResponse)
                realm.createOrUpdateAllFromJson(List::class.java, modedResponse)
            }
            loadData.update(1)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            throw RuntimeException(e)
        } catch (e: RealmPrimaryKeyConstraintException) {
            e.printStackTrace()
        }

    }

    private fun makeRequest(url: String): StringRequest {
        val stringRequest = StringRequest(Request.Method.GET, url, Response.Listener<String> { response ->
            Log.d(TAG, response)
            fetchRequest(response)
        }, Response.ErrorListener { error -> error.printStackTrace() })
        return stringRequest
    }

    private fun fetchRequest(response: String) {
        realm.executeTransaction {
            realm.createOrUpdateObjectFromJson(Movie::class.java, response)
        }
    }


    private fun findMovie(movieid: Int): Movie {
        var query: RealmQuery<Movie> = realm.where(Movie::class.java)
        var results: RealmResults<Movie> = query.equalTo("id", movieid).findAll()
        Log.d(TAG, " " + results.size)

        return results.first()
    }


}

interface LoadData {
    fun update(type: Int)
}