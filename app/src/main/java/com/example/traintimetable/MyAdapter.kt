package com.example.traintimetable


import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView


class MyAdapter(
    context: Context,
    itemLayoutId: Int,
    times: ArrayList<String>,
    types: ArrayList<Int>,
    bounds: ArrayList<String>
) : BaseAdapter() {
    private var inflater: LayoutInflater? = null
    private var layoutID = 0
    private var timelist: ArrayList<String>
    private var typelist: ArrayList<Bitmap?>
    private var boundlist: ArrayList<String>

    internal class ViewHolder {
        var time: TextView? = null
        var type: ImageView? = null
        var bound: TextView? = null
    }



    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        var convertView = convertView
        val holder: ViewHolder
        if (convertView == null) {
            convertView = inflater!!.inflate(layoutID, null)
            holder = ViewHolder()
            holder.time = convertView.findViewById(R.id.time)
            holder.type = convertView.findViewById(R.id.type)
            holder.bound = convertView.findViewById(R.id.bound)
            convertView.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
        }
        holder.type!!.setImageBitmap(typelist[position])
        holder.time!!.text = timelist[position]
        holder.bound!!.text = boundlist[position]
        return convertView
    }

    override fun getCount(): Int {
        return timelist.size
    }

    override fun getItem(position: Int): Any {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    init{
        inflater = LayoutInflater.from(context)
        layoutID = itemLayoutId
        timelist = times
        // bitmapの配列
        typelist = arrayListOf()
        // drawableのIDからbitmapに変換
        for (i in types.indices) {
            typelist.add(BitmapFactory.decodeResource(context.resources, types[i]))
        }
        boundlist = bounds
    }
}