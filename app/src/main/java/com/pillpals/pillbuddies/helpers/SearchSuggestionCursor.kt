package com.pillpals.pillbuddies.helpers

import android.content.Context
import android.database.Cursor
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CursorAdapter
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import com.pillpals.pillbuddies.R

class SearchSuggestionCursor(
    private val mContext: Context,
    cursor: Cursor,
    private val searchView: SearchView
) : CursorAdapter(mContext, cursor, false) {
    private val mLayoutInflater: LayoutInflater

    init {
        mLayoutInflater = LayoutInflater.from(mContext)
    }

    override fun newView(context: Context, cursor: Cursor, parent: ViewGroup): View {
        return mLayoutInflater.inflate(R.layout.query_suggestion, parent, false)
    }

    override fun bindView(view: View, context: Context, cursor: Cursor) {
        val suggestion = cursor.getString(cursor.getColumnIndexOrThrow("suggestion"))

        val text = view.findViewById(R.id.querySuggestionText) as TextView
        text.text = suggestion

//        view.setOnClickListener { view ->
//            //take next action based user selected item
//            val queryText = view.findViewById(R.id.querySuggestionText) as TextView
//            searchView.setQuery(queryText.text, true)
//        }
    }
}