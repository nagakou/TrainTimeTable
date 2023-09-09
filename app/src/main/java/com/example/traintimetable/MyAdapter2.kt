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


class MyAdapter2(
    context: Context,
    itemLayoutId: Int,
    numberRings: ArrayList<Int>,
    stationNames: Array<String>
) : BaseAdapter() {
    private var inflater: LayoutInflater? = null
    private var layoutID = 0
    private var numberRingList: ArrayList<Bitmap?>
    private var stationNamesList: Array<String>

    internal class ViewHolder {
        var numberRing: ImageView? = null
        var stationNames: TextView? = null
    }



    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        var convertView = convertView
        val holder: ViewHolder
        if (convertView == null) {
            convertView = inflater!!.inflate(layoutID, null)
            holder = ViewHolder()
            holder.numberRing = convertView.findViewById(R.id.NumberRing)
            holder.stationNames = convertView.findViewById(R.id.StationNames)
            convertView.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
        }
        holder.numberRing!!.setImageBitmap(numberRingList[position])
        holder.stationNames!!.text = stationNamesList[position]
        return convertView
    }

    override fun getCount(): Int {
        return stationNamesList.size
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
        // bitmapの配列
        numberRingList = arrayListOf()
        // drawableのIDからbitmapに変換
        for (i in numberRings.indices) {
            numberRingList.add(BitmapFactory.decodeResource(context.resources, numberRings[i]))
        }
        stationNamesList = stationNames
    }

}