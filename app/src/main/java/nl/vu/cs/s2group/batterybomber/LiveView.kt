package nl.vu.cs.s2group.batterybomber

import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.BATTERY_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import timber.log.Timber
import java.lang.StrictMath.abs
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Use the [LiveView.newInstance] factory method to
 * create an instance of this fragment.
 */
class LiveView : Fragment(R.layout.fragment_live_view) {
    private lateinit var batteryManager: BatteryManager
    private lateinit var broadcastReceiver: BroadcastReceiver
    private val mHandler: Handler = Handler(Looper.getMainLooper())
    private val series: LineGraphSeries<DataPoint> = LineGraphSeries()
    private val timeLength = 120 //seconds
    private var graphNextXValue = 0.0
    private var lastKnownVoltage : Int = 0    // milliVolts
    private var lastknownLevel : Double = 0.0 // percentage

    private lateinit var textView: TextView

    private val mGraphUpdater = object : Runnable {
        private val mInterval = 1000 // milliseconds

        override fun run() {
            val propertyCurrentNow = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW) //microampers
            val watts = if(propertyCurrentNow > 0)  0.0 else (lastKnownVoltage.toDouble() / 1000) * (abs(propertyCurrentNow).toDouble()/1000/1000) //Only negative current means discharging

            //textView.text = "${propertyCurrentNow/1000} miliamps, ${lastKnownVoltage} milivolts, ${watts} watts, $lastknownLevel %"
            textView.text = "%d milliAmps, %d milliVolts, %.3f Watts, %.1f %%".format(propertyCurrentNow/1000, lastKnownVoltage, watts, lastknownLevel)

            series.appendData(DataPoint(graphNextXValue++, watts), graphNextXValue > timeLength, timeLength)
            mHandler.postDelayed(this, mInterval.toLong())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val graph = view.findViewById(R.id.liveGraph) as GraphView
        textView = view.findViewById(R.id.someTextView) as TextView

        batteryManager = requireContext().getSystemService(BATTERY_SERVICE) as BatteryManager
        broadcastReceiver = BatteryManagerBroadcastReceiver { intent ->
            lastKnownVoltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)

            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            lastknownLevel = (level * 100).toDouble() / scale
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
        }
        requireContext().registerReceiver(broadcastReceiver, filter)


        graph.addSeries(series)
        graph.viewport.isXAxisBoundsManual = true;
        graph.viewport.setMinX(0.0);
        graph.viewport.setMaxX(timeLength.toDouble());

        graph.viewport.isYAxisBoundsManual = true;
        graph.viewport.setMinY(0.0);
        graph.viewport.setMaxY(5.0);
        mGraphUpdater.run()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireContext().unregisterReceiver(broadcastReceiver)
        mHandler.removeCallbacks(mGraphUpdater);
        Timber.d("LiveView destroyed!")
    }
}

private class BatteryManagerBroadcastReceiver(
    private val onReceiveIntent: (Intent) -> Unit,
) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        onReceiveIntent(intent)
    }
}
