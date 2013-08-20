package org.amei.sensortagandroid;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Created by valueBLE on 08/08/13.
 *
 * TODO Not really complete yet.
 */
public class ConfigurationDialogFragment extends DialogFragment {
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;
    private LeDeviceArrayAdapter mLeDeviceListAdapter;
    private ProgressBar mProgress;

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    private OnLeConnectionRequestHandler mCallback;

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!mLeDeviceListAdapter.containsDevice(device)) {
                                mLeDeviceListAdapter.add(device);
                                mLeDeviceListAdapter.notifyDataSetChanged();
                            }
                        }
                    });
                }
            };

    // Container activity should implement this.
    // Container Activity must implement this interface
    public interface OnLeConnectionRequestHandler {
        public void onLeDeviceConnectionRequest(BluetoothDevice device);
    }

    public ConfigurationDialogFragment() {
        // Empty constructor required for DialogFragment
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mCallback = (OnLeConnectionRequestHandler) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnLeConnectionRequestHandler.");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Initialize the handler.
        mHandler = new Handler();

        // Initialize the list.
        mLeDeviceListAdapter = new LeDeviceArrayAdapter(this.getActivity(), R.layout.list_view_bluetooth_device);

        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        View view = inflater.inflate(R.layout.fragment_dialog_configuration, container);
        getDialog().setTitle(getString(R.string.dialog_title_configuration));
        ((Button) view.findViewById(R.id.button_scan)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // When button is clicked, call up to owning activity.
                doSensorTagScan();
            }
        });

        // Set up the list view.
        final ListView listView = (ListView) view.findViewById(R.id.configuration_list_view);
        listView.setAdapter(mLeDeviceListAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                doStopLeScan();
                BluetoothDevice device = mLeDeviceListAdapter.getItem(position);
                mCallback.onLeDeviceConnectionRequest(device);
                dismiss();
            }
        });

        // Set up the progress bar.
        mProgress = (ProgressBar) view.findViewById(R.id.progressBar);

        return view;
    }

    private void doStopLeScan() {
        mScanning = false;
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
        mProgress.setVisibility(View.GONE);
    }

    private void doSensorTagScan() {
        // Clear all entries.
        mLeDeviceListAdapter.clear();

        // Show the progress.
        mProgress.setVisibility(View.VISIBLE);

        // Stops scanning after a pre-defined scan period.
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                doStopLeScan();
            }
        }, SCAN_PERIOD);

        mScanning = true;
        mBluetoothAdapter.startLeScan(mLeScanCallback);

    }

    class LeDeviceArrayAdapter extends ArrayAdapter<BluetoothDevice> {

        public LeDeviceArrayAdapter(Context context, int resource) {
            super(context, resource);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            LeDeviceHolder holder = null;

            if (row == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater.inflate(R.layout.list_view_bluetooth_device, parent, false);

                holder = new LeDeviceHolder();
                holder.textView = (TextView) row.findViewById(R.id.le_device_view);

                row.setTag(holder);
            }
            else {
                holder = (LeDeviceHolder) row.getTag();
            }

            BluetoothDevice device = getItem(position);
            holder.textView.setText(getString(R.string.list_view_bluetooth_device_text, device.getName(), device.getAddress()));

            return row;
        }

        public boolean containsDevice(BluetoothDevice device) {
            boolean deviceExists = false;

            for (int i = 0; i < getCount(); i++) {
                BluetoothDevice existingDevice = getItem(i);
                if (existingDevice.getAddress().equalsIgnoreCase(device.getAddress())) {
                    deviceExists = true;
                    break;
                }
            }

            return deviceExists;
        }
    }

    static class LeDeviceHolder {
        TextView textView;
    }
}
