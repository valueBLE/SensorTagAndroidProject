package org.amei.sensortagandroid.sections;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.amei.sensortagandroid.R;
import org.amei.sensortagandroid.SensorTagService;

/**
 * Created by valueBLE on 08/08/13.
 */
public class HumidityFragment extends SectionFragment {
    private TextView mRelativeValueTextView;
    private TextView mTemperatureValueTextView;
    private LinearLayout mHumidityDetails;
    private HumidityReceiver mReceiver;

    public class HumidityReceiver extends BroadcastReceiver {
        public HumidityReceiver() {
            // Android needs the empty constructor.
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (SensorTagService.ACTION_GATT_CONNECTED.equals(action)) {
                setConnected(true);
            } else if (SensorTagService.ACTION_GATT_DISCONNECTED.equals(action)) {
                setConnected(false);
            } else if (SensorTagService.ACTION_HUMIDITY_DATA_AVAILABLE.equals(action)) {
                setConnected(true);
                double humidityTemperature = intent.getDoubleExtra(SensorTagService.DATA_HUMIDITY_TEMPERATURE, 0);
                double humidityRelative = intent.getDoubleExtra(SensorTagService.DATA_HUMIDITY_RELATIVE, 0);
                mRelativeValueTextView.setText(Double.toString(humidityRelative));
                mTemperatureValueTextView.setText(Double.toString(humidityTemperature));
            }
        }
    };

    @Override
    protected int getLayoutResource() {
        return R.layout.fragment_sensor_tag_humidity;
    }

    @Override
    protected void onCreateViewHook(View rootView) {
        mTemperatureValueTextView = (TextView) rootView.findViewById(R.id.humidity_details_temperature_value);
        mRelativeValueTextView = (TextView) rootView.findViewById(R.id.humidity_details_relative_value);
        mHumidityDetails = (LinearLayout) rootView.findViewById(R.id.humidity_details);
    }

    @Override
    protected LinearLayout getSectionLayout() {
        return mHumidityDetails;
    }

    @Override
    protected void registerSectionReceiver(IntentFilter filter) {
        filter.addAction(SensorTagService.ACTION_HUMIDITY_DATA_AVAILABLE);
        mReceiver = new HumidityReceiver();
        getActivity().registerReceiver(mReceiver, filter);
    }

    @Override
    protected void unregisterSectionReceiver() {
        getActivity().unregisterReceiver(mReceiver);
    }
}
