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
public class MagnetometerFragment extends SectionFragment {
    private TextView mXValueTextView;
    private TextView mYValueTextView;
    private TextView mZValueTextView;
    private LinearLayout mMagnetometerDetails;
    private Receiver mReceiver;

    public class Receiver extends BroadcastReceiver {
        public Receiver() {
            // Android needs the empty constructor.
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (SensorTagService.ACTION_GATT_CONNECTED.equals(action)) {
                setConnected(true);
            } else if (SensorTagService.ACTION_GATT_DISCONNECTED.equals(action)) {
                setConnected(false);
            } else if (SensorTagService.ACTION_MAGNETOMETER_DATA_AVAILABLE.equals(action)) {
                setConnected(true);
                float x = intent.getFloatExtra(SensorTagService.DATA_MAGNETOMETER_X, 0);
                float y = intent.getFloatExtra(SensorTagService.DATA_MAGNETOMETER_Y, 0);
                float z = intent.getFloatExtra(SensorTagService.DATA_MAGNETOMETER_Z, 0);
                mXValueTextView.setText(Float.toString(x));
                mYValueTextView.setText(Float.toString(y));
                mZValueTextView.setText(Float.toString(z));
            }
        }
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.fragment_sensor_tag_magnetometer;
    }

    @Override
    protected void onCreateViewHook(View rootView) {
        mXValueTextView = (TextView) rootView.findViewById(R.id.magnetometer_details_x_value);
        mYValueTextView = (TextView) rootView.findViewById(R.id.magnetometer_details_y_value);
        mZValueTextView = (TextView) rootView.findViewById(R.id.magnetometer_details_z_value);
        mMagnetometerDetails = (LinearLayout) rootView.findViewById(R.id.magnetometer_details);
    }

    @Override
    protected LinearLayout getSectionLayout() {
        return mMagnetometerDetails;
    }

    @Override
    protected void registerSectionReceiver(IntentFilter filter) {
        filter.addAction(SensorTagService.ACTION_MAGNETOMETER_DATA_AVAILABLE);
        mReceiver = new Receiver();
        getActivity().registerReceiver(mReceiver, filter);
    }

    @Override
    protected void unregisterSectionReceiver() {
        getActivity().unregisterReceiver(mReceiver);
    }
}
