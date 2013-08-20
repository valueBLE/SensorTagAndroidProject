package org.amei.sensortagandroid;

import java.util.Locale;


import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.amei.sensortagandroid.sections.AccelerometerFragment;
import org.amei.sensortagandroid.sections.GyroscopeFragment;
import org.amei.sensortagandroid.sections.HumidityFragment;
import org.amei.sensortagandroid.sections.MagnetometerFragment;
import org.amei.sensortagandroid.sections.TemperatureFragment;

public class SensorTagActivity extends FragmentActivity implements ConfigurationDialogFragment.OnLeConnectionRequestHandler{
    private static final String TAG = "org.amei.sensortagandroid";

    public static final int SECTION_NUMBER_TEMPERATURE = 0;
    public static final int SECTION_NUMBER_HUMIDITY = 1;
    public static final int SECTION_NUMBER_ACCELEROMETER = 2;
    public static final int SECTION_NUMBER_PRESSURE = 3;
    public static final int SECTION_NUMBER_MAGNETOMETER = 4;
    public static final int SECTION_NUMBER_GYROSCOPE = 5;
    public static final int SECTION_NUMBER_TOTAL = 6;

    public static final String BUNDLE_KEY_IS_CONNECTED = "connected";

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
     * will keep every loaded fragment in memory. If this becomes too memory
     * intensive, it may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    // Connection details to share amongst the fragments.
    private boolean mIsConnected = false;
    public class GattReceiver extends BroadcastReceiver {
        public GattReceiver() {
            // Android needs the empty constructor.
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (SensorTagService.ACTION_GATT_CONNECTED.equals(action)) {
                mIsConnected = true;
            } else if (SensorTagService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mIsConnected = false;
            } else if (SensorTagService.
                    ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
            }
        }
    };

    // Bind to the local sensor tag service.
    private SensorTagService mBoundService;
    private boolean mIsBound = false;

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            SensorTagService.LocalBinder binder = (SensorTagService.LocalBinder) service;
            mBoundService = binder.getService();
            mIsBound = true;
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            mBoundService = null;
            mIsBound = false;
        }
    };

    void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because we want a specific service implementation that
        // we know will be running in our own process (and thus won't be
        // supporting component replacement by other applications).
        Intent intent = new Intent(this, SensorTagService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    void doUnbindService() {
        if (mIsBound) {
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
        }
    }
    private void registerReceiver() {
        IntentFilter temperatureFilter = new IntentFilter(SensorTagService.ACTION_GATT_CONNECTED);
        temperatureFilter.addAction(SensorTagService.ACTION_GATT_DISCONNECTED);
        temperatureFilter.addAction(SensorTagService.ACTION_GATT_SERVICES_DISCOVERED);
        GattReceiver receiver = new GattReceiver();
        registerReceiver(receiver, temperatureFilter);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mIsBound) {
            doUnbindService();
            mIsBound = false;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Always need to bind our service.
        doBindService();
        // Always need to register our receiver.
        registerReceiver();
    }

    // Connect to the local sensor tag service.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sensor_tag_main);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the app.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.sensor_tag, menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.action_configure:
                showConfigurationDialog();
                return true;
            default:
                return super.onMenuItemSelected(featureId, item);
        }
    }

    private void showConfigurationDialog() {
        FragmentManager fm = getSupportFragmentManager();
        ConfigurationDialogFragment configurationDialog = new ConfigurationDialogFragment();
        configurationDialog.show(fm, "fragment_edit_name");
    }

    @Override
    public void onLeDeviceConnectionRequest(BluetoothDevice device) {
        mBoundService.connectLeDevice(device);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            Bundle args = new Bundle();
            args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
            args.putBoolean(BUNDLE_KEY_IS_CONNECTED, mIsConnected);
            switch (position) {
                case SECTION_NUMBER_TEMPERATURE:
                    Fragment temperatureFragment = new TemperatureFragment();
                    temperatureFragment.setArguments(args);
                    return temperatureFragment;
                case SECTION_NUMBER_HUMIDITY:
                    Fragment humidityFragment = new HumidityFragment();
                    humidityFragment.setArguments(args);
                    return humidityFragment;
                case SECTION_NUMBER_ACCELEROMETER:
                    Fragment accelerometerFragment = new AccelerometerFragment();
                    accelerometerFragment.setArguments(args);
                    return accelerometerFragment;
                case SECTION_NUMBER_MAGNETOMETER:
                    Fragment magnetometerFragment = new MagnetometerFragment();
                    magnetometerFragment.setArguments(args);
                    return magnetometerFragment;
                case SECTION_NUMBER_GYROSCOPE:
                    Fragment gyroscopeFragment = new GyroscopeFragment();
                    gyroscopeFragment.setArguments(args);
                    return gyroscopeFragment;
                default:
                    Fragment fragment = new DummySectionFragment();
                    fragment.setArguments(args);
                    return fragment;
            }
        }

        @Override
        public int getCount() {
            return SECTION_NUMBER_TOTAL;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case SECTION_NUMBER_TEMPERATURE:
                    return getString(R.string.title_section_temperature).toUpperCase(l);
                case SECTION_NUMBER_HUMIDITY:
                    return getString(R.string.title_section_humidity).toUpperCase(l);
                case SECTION_NUMBER_ACCELEROMETER:
                    return getString(R.string.title_section_accelerometer).toUpperCase(l);
                case SECTION_NUMBER_PRESSURE:
                    return getString(R.string.title_section_pressure).toUpperCase(l);
                case SECTION_NUMBER_MAGNETOMETER:
                    return getString(R.string.title_section_magnetometer).toUpperCase(l);
                case SECTION_NUMBER_GYROSCOPE:
                    return getString(R.string.title_section_gyroscope).toUpperCase(l);
                default:
                    return null;
            }
        }
    }

    /**
     * A dummy fragment representing a section of the app, but that simply
     * displays dummy text.
     */
    public static class DummySectionFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        public static final String ARG_SECTION_NUMBER = "section_number";

        public DummySectionFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_sensor_tag_dummy, container, false);
            TextView dummyTextView = (TextView) rootView.findViewById(R.id.section_label);
            dummyTextView.setText(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

}
