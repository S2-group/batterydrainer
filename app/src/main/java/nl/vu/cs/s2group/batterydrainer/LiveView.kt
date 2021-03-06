/*
 * MIT License
 *
 * Copyright (c) 2022 Software and Sustainability Group - VU
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package nl.vu.cs.s2group.batterydrainer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.BATTERY_SERVICE
import android.content.Context.POWER_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.*
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import nl.vu.cs.s2group.batterydrainer.Utils.Battery.getBatteryStatusText
import timber.log.Timber
import java.lang.StrictMath.abs
import java.util.*
import kotlin.math.floor


/**
 * A simple [Fragment] subclass.
 * Use the [LiveView.newInstance] factory method to
 * create an instance of this fragment.
 */
class LiveView : Fragment(R.layout.fragment_live_view) {
    private lateinit var batteryManager: BatteryManager
    private lateinit var powerManager: PowerManager
    private lateinit var broadcastReceiver: BroadcastReceiver
    private val mHandler: Handler = Handler(Looper.getMainLooper())
    private val timeLength = 120 //seconds
    private var graphNextXValue = 0.0
    private var lastKnownVoltage : Int = 0    // milliVolts
    private var lastKnownLevel : Double = 0.0 // percentage

    private val wattSeries   : LineGraphSeries<DataPoint> = LineGraphSeries()
    private val currentSeries: LineGraphSeries<DataPoint> = LineGraphSeries()

    private lateinit var currentNowTextView         : TextView
    private lateinit var voltageTextView            : TextView
    private lateinit var wattsTextView              : TextView
    private lateinit var remainingBatteryTextView   : TextView
    private lateinit var estimatedLifeTimeTextView  : TextView

    private val mGraphUpdater = object : Runnable {
        private val mInterval = 1000 // milliseconds
        private val maxDataPoints = ((1000/mInterval.toDouble()) * 60 * 5).toInt() // Keep a record of 5 minutes

        override fun run() {
            var currentNow = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW) //Instantaneous battery current in microamperes

            val status = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS)
            if(status == BatteryManager.BATTERY_STATUS_DISCHARGING) {   //some models report with inverted sign
                currentNow = -abs(currentNow)
            }

            val currentAverage = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE) //Average battery current in microamperes
            val watts = if(currentNow >= 0)  0.0 else (lastKnownVoltage.toDouble() / 1000) * (abs(currentNow).toDouble()/1000/1000) //Only negative current means discharging

            val energy   = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_ENERGY_COUNTER) //Remaining energy in nanowatt-hours
            val capacity = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER) //Remaining battery capacity in microampere-hours
            val capacityPercentage = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) //Remaining battery capacity as an integer percentage of total capacity

            /*
             * currentAverage always reports 0
             * energy         always reports 0
             * capacityPercentage == lastKnownLevel
             * Usable metrics: currentNow, watts, capacity
             */

            currentNowTextView.text         = "%4d mA".format(currentNow/1000)
            voltageTextView.text            = "%4d mV".format(lastKnownVoltage)
            wattsTextView.text              = "%2.2f W".format(watts)
            remainingBatteryTextView.text   = "%2d%% (%4d mAH)".format(lastKnownLevel.toInt(), capacity/1000)

            val estimatedLifeTime = abs((capacity.toDouble()/1000)/(currentNow.toDouble()/1000))
            val hours = floor(estimatedLifeTime)
            val minutes = ((estimatedLifeTime - hours)*60)
            estimatedLifeTimeTextView.text  = if(currentNow >= 0) "NA (Charging)" else "%2d hours and %2d minutes".format(hours.toInt(), minutes.toInt())

            wattSeries.appendData(DataPoint(graphNextXValue, watts), graphNextXValue > timeLength, maxDataPoints)
            currentSeries.appendData(DataPoint(graphNextXValue, if(currentNow >= 0) 0.0 else (abs(currentNow)/1000).toDouble()), graphNextXValue > timeLength, maxDataPoints)
            graphNextXValue++

            mHandler.postDelayed(this, mInterval.toLong())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val wattGraph    = view.findViewById(R.id.liveGraphWatts    ) as CustomGraphView
        val currentGraph = view.findViewById(R.id.liveGraphCurrent  ) as CustomGraphView
        currentNowTextView          = view.findViewById(R.id.currentNowTextView         ) as TextView
        voltageTextView             = view.findViewById(R.id.voltageTextView            ) as TextView
        wattsTextView               = view.findViewById(R.id.wattsTextView              ) as TextView
        remainingBatteryTextView    = view.findViewById(R.id.remainingBatteryTextView   ) as TextView
        estimatedLifeTimeTextView   = view.findViewById(R.id.estimatedLifeTimeTextView  ) as TextView

        batteryManager = requireContext().getSystemService(BATTERY_SERVICE) as BatteryManager
        powerManager   = requireContext().getSystemService(POWER_SERVICE  ) as PowerManager
        broadcastReceiver = BatteryManagerBroadcastReceiver { intent ->
            lastKnownVoltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)

            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            lastKnownLevel = (level * 100).toDouble() / scale
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
        }
        requireContext().registerReceiver(broadcastReceiver, filter)


        wattGraph.addSeries(wattSeries)
        wattGraph.title = "Watt consumption (W)"
        wattGraph.isTitleBold = true
        wattGraph.titleTextSize = Utils.spToPx(16.0f, requireContext()).toFloat()
        wattGraph.viewport.isXAxisBoundsManual = true;
        wattGraph.viewport.setMinX(0.0);
        wattGraph.viewport.setMaxX(timeLength.toDouble());

        wattGraph.viewport.isYAxisBoundsManual = true;
        wattGraph.viewport.setMinY(0.0);
        wattGraph.viewport.setMaxY(10.0);
        wattGraph.gridLabelRenderer.isHorizontalLabelsVisible = false
        wattGraph.gridLabelRenderer.reloadStyles()

        currentGraph.addSeries(currentSeries)
        currentGraph.title = "Current discharge (mA)"
        currentGraph.isTitleBold = true
        currentGraph.titleTextSize = Utils.spToPx(16.0f, requireContext()).toFloat()
        currentGraph.viewport.isXAxisBoundsManual = true;
        currentGraph.viewport.setMinX(0.0);
        currentGraph.viewport.setMaxX(timeLength.toDouble());

        currentGraph.viewport.isYAxisBoundsManual = true;
        currentGraph.viewport.setMinY(0.0);
        currentGraph.viewport.setMaxY(3000.0);
        currentGraph.gridLabelRenderer.reloadStyles()

        mGraphUpdater.run()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireContext().unregisterReceiver(broadcastReceiver)
        mHandler.removeCallbacks(mGraphUpdater);
        Timber.d("LiveView destroyed!")
    }
}

class CustomGraphView : GraphView {
    var isTitleBold: Boolean = false

    private val mPaintTitle = Paint()

    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    override fun drawTitle(canvas : Canvas) {
        /* FIXME: for some reason we have to invoke super.drawTitle() in order for the text to appear
         * in the correct position on the y-axis, despite that the code is the same. Is this some Java/Kotlin integration problem?
         * To avoid ugly rendering, we make the parent rendering invisible
         */
        val realTitleColor = titleColor
        super.setTitleColor(Color.TRANSPARENT)
        super.drawTitle(canvas)

        if (title != null && title.length > 0) {
            mPaintTitle.color = realTitleColor
            mPaintTitle.textSize = titleTextSize
            mPaintTitle.textAlign = Paint.Align.CENTER
            mPaintTitle.isFakeBoldText = isTitleBold

            val x = canvas.width.toFloat() / 2
            val y = mPaintTitle.textSize
            canvas.drawText(title, x, y, mPaintTitle)
        }
        super.setTitleColor(realTitleColor)
    }
}

private class BatteryManagerBroadcastReceiver(
    private val onReceiveIntent: (Intent) -> Unit,
) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        onReceiveIntent(intent)
    }
}
