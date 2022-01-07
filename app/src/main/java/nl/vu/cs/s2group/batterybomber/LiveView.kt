package nl.vu.cs.s2group.batterybomber

import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.BATTERY_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import timber.log.Timber
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Use the [LiveView.newInstance] factory method to
 * create an instance of this fragment.
 */
class LiveView : Fragment(R.layout.fragment_live_view) {
    private lateinit var batteryManager: BatteryManager
    private lateinit var broadcastReceiver: BroadcastReceiver
    private val series: LineGraphSeries<DataPoint> = LineGraphSeries()
    private var graphNextXValue = 0.0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val textView = view.findViewById(R.id.someTextView) as TextView
        val graph = view.findViewById(R.id.liveGraph) as GraphView

        batteryManager = requireContext().getSystemService(BATTERY_SERVICE) as BatteryManager
        broadcastReceiver = BatteryManagerBroadcastReceiver { intent ->
            val propertyCurrentNow = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
            val propertyLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -99)
            textView.text = "Current: $propertyCurrentNow, Level: $propertyLevel"
            series.appendData(DataPoint(graphNextXValue++, propertyLevel.toDouble()), graphNextXValue > 40, 40)
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
        }
        requireContext().registerReceiver(broadcastReceiver, filter)


        graph.addSeries(series)
        graph.viewport.isXAxisBoundsManual = true;
        graph.viewport.setMinX(0.0);
        graph.viewport.setMaxX(40.0);

        graph.viewport.isYAxisBoundsManual = true;
        graph.viewport.setMinY(0.0);
        graph.viewport.setMaxY(100.0);
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireContext().unregisterReceiver(broadcastReceiver)
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
