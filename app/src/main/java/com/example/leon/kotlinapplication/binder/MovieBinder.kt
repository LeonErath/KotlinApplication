package com.example.leon.kotlinapplication.binder

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.ahamed.multiviewadapter.SelectableBinder
import com.ahamed.multiviewadapter.SelectableViewHolder
import com.example.leon.kotlinapplication.R
import com.example.leon.kotlinapplication.activities.DetailActivity
import com.example.leon.kotlinapplication.model.Movie
import com.squareup.picasso.Picasso


/**
 * Created by Leon on 07.06.17.
 */

open class MovieBinder : SelectableBinder<Movie, MovieBinder.ViewHolder>() {

    override fun bind(holder: ViewHolder?, movie: Movie?, b: Boolean) {
        holder?.tvMovie!!.text = movie?.title

        val uri:Uri = Uri
                .parse(holder.context.getString(R.string.image_base_url)
                        + "/w342"
                        +movie?.poster_path)
        Picasso.with(holder.context)
                .load(uri)
                .into(holder.imageV)
    }


    override fun canBindData(item: Any?): Boolean {
        return item is Movie
    }

    override fun create(inflater: LayoutInflater?, parent: ViewGroup?): ViewHolder {
        return ViewHolder(inflater?.inflate(R.layout.movie_item, parent, false)!!)
    }

    override fun getSpanSize(maxSpanCount: Int): Int {
        return 2
    }

    class ViewHolder(itemView: View) : SelectableViewHolder<Movie>(itemView) {

        val context:Context = itemView.context

         var tvMovie: TextView
         var imageV: ImageView

        init {
            tvMovie = itemView.findViewById(R.id.textViewMovie) as TextView
            imageV = itemView.findViewById(R.id.imageView) as ImageView

            setItemClickListener { view, item ->
                val intent = Intent(context, DetailActivity::class.java)
                intent.putExtra("movieid", item.id)
                context.startActivity(intent)
            }

        }


    }

}