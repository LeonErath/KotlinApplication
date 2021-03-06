package com.leon.app.watchlist.binder

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.ahamed.multiviewadapter.SelectableBinder
import com.ahamed.multiviewadapter.SelectableViewHolder
import com.leon.app.watchlist.*
import com.leon.app.watchlist.activities.DetailActivity
import com.leon.app.watchlist.activities.MainActivity
import com.leon.app.watchlist.adapter.MovieFlatAdapter
import com.leon.app.watchlist.model.Movie
import java.util.*
import java.util.concurrent.TimeUnit


open class MovieFlatBinder(adapter2: MovieFlatAdapter, activity: MainActivity) : SelectableBinder<Movie, MovieFlatBinder.ViewHolder>() {
    val TAG: String? = MovieFlatBinder::class.simpleName

    val adapater: MovieFlatAdapter = adapter2
    val mainActivity: MainActivity = activity


    private fun Long.toDay(): String {
        if (this > 1) {
            return "$this Days"
        } else {
            return "$this Day"
        }
    }

    override fun bind(holder: ViewHolder?, movie: Movie?, p2: Boolean) {
        if (holder != null && movie != null) {
            with(holder) {
                with(movie) {
                    tvMovie.text = title
                    tvTagline.text = tagline
                    val time: Long = Calendar.getInstance().timeInMillis
                    buttonTime.text = TimeUnit.MILLISECONDS.toDays(time - movie.timeAdded).toDay()
                }
            }
        }
    }


    override fun canBindData(item: Any?): Boolean {
        return item is Movie
    }

    override fun create(inflater: LayoutInflater?, parent: ViewGroup?): ViewHolder {
        return ViewHolder(inflater?.inflate(R.layout.movie_item_flat, parent, false)!!, adapater, mainActivity)
    }

    override fun getSpanSize(maxSpanCount: Int): Int {
        return 1
    }

    class ViewHolder(itemView: View, adapter: MovieFlatAdapter, activity: MainActivity) : SelectableViewHolder<Movie>(itemView) {

        val mainActivity: MainActivity = activity
        val context: Context = itemView.context
        val adapter: MovieFlatAdapter = adapter

        var tvMovie: TextView
        var tvTagline: TextView
        var buttonTime: Button
        var buttonWatched: Button
        var buttonRemove: Button



        init {
            tvMovie = itemView.findViewById(R.id.textViewMovie) as TextView
            tvTagline = itemView.findViewById(R.id.textViewTagline) as TextView
            buttonTime = itemView.findViewById(R.id.buttonTime) as Button
            buttonRemove = itemView.findViewById(R.id.buttonRemove) as Button
            buttonWatched = itemView.findViewById(R.id.buttonWatched) as Button

            val queryAdapter = QueryAdapter(context)

            buttonRemove.setOnClickListener {
                queryAdapter.removeClickDetail(movie = item)
                mainActivity.removeFromFavorite(item)
                Bus.send(MovieEventRemove(item))
            }

            setItemClickListener { view, item ->
                val intent = Intent(context, DetailActivity::class.java)
                intent.putExtra("movieid", item.id)
                context.startActivity(intent)
            }

            setItemLongClickListener { view, movie ->
                when (item.evolution) {
                    0 -> {
                        queryAdapter.movieClickDetail(movie = item)
                        mainActivity.addToFavorite(item)
                        Bus.send(MovieEventAdd(item))
                        Log.e("MovieFlatBinder", "Technically should not be able")
                    }
                    1 -> {
                        queryAdapter.removeClickDetail(movie = item)
                        mainActivity.removeFromFavorite(item)
                        Bus.send(MovieEventRemove(item))
                    }
                }
                adapter.removeMovie(item)
                Bus.send(MovieEventRemove(item))
                true
            }
        }


    }

}