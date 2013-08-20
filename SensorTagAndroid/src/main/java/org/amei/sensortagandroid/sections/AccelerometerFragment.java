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
public class AccelerometerFragment extends SectionFragment {
    private TextView mXValueTextView;
    private TextView mYValueTextView;
    private TextView mZValueTextView;
    private LinearLayout mAccelerometerDetails;
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
            } else if (SensorTagService.ACTION_ACCELEROMETER_DATA_AVAILABLE.equals(action)) {
                setConnected(true);
                double x = intent.getDoubleExtra(SensorTagService.DATA_ACCELEROMETER_X, 0);
                double y = intent.getDoubleExtra(SensorTagService.DATA_ACCELEROMETER_Y, 0);
                double z = intent.getDoubleExtra(SensorTagService.DATA_ACCELEROMETER_Z, 0);
                mXValueTextView.setText(Double.toString(x));
                mYValueTextView.setText(Double.toString(y));
                mZValueTextView.setText(Double.toString(z));
            }
        }
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.fragment_sensor_tag_accelerometer;
    }

    @Override
    protected void onCreateViewHook(View rootView) {
        mXValueTextView = (TextView) rootView.findViewById(R.id.accelerometer_details_x_value);
        mYValueTextView = (TextView) rootView.findViewById(R.id.accelerometer_details_y_value);
        mZValueTextView = (TextView) rootView.findViewById(R.id.accelerometer_details_z_value);
        mAccelerometerDetails = (LinearLayout) rootView.findViewById(R.id.accelerometer_details);
    }

    @Override
    protected LinearLayout getSectionLayout() {
        return mAccelerometerDetails;
    }

    @Override
    protected void registerSectionReceiver(IntentFilter filter) {
        filter.addAction(SensorTagService.ACTION_ACCELEROMETER_DATA_AVAILABLE);
        mReceiver = new Receiver();
        getActivity().registerReceiver(mReceiver, filter);
    }

    @Override
    protected void unregisterSectionReceiver() {
        getActivity().unregisterReceiver(mReceiver);
    }
}
