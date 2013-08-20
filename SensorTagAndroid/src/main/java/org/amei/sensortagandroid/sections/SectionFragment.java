package org.amei.sensortagandroid.sections;

import android.app.Activity;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.amei.sensortagandroid.R;
import org.amei.sensortagandroid.SensorTagActivity;
import org.amei.sensortagandroid.SensorTagService;

/**
 * Created by valueBLE on 12/08/13.
 */
public abstract class SectionFragment extends Fragment {
    protected boolean mConnected = false;

    protected TextView mNotConnectedTextView;

    protected void setConnected(boolean connected) {
        setConnected(connected, false);
    }

    protected void setConnected(boolean connected, boolean force) {
        if (force || mConnected != connected) {
            mConnected = connected;
            if (mConnected) {
                mNotConnectedTextView.setVisibility(View.GONE);
                getSectionLayout().setVisibility(View.VISIBLE);
            } else {
                mNotConnectedTextView.setVisibility(View.VISIBLE);
                getSectionLayout().setVisibility(View.GONE);
            }
        }
    }

    private void registerReceiver() {
        IntentFilter temperatureFilter = new IntentFilter(SensorTagService.ACTION_GATT_CONNECTED);
        temperatureFilter.addAction(SensorTagService.ACTION_GATT_DISCONNECTED);
        registerSectionReceiver(temperatureFilter);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SensorTagActivity.BUNDLE_KEY_IS_CONNECTED, mConnected);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mConnected = savedInstanceState.getBoolean(SensorTagActivity.BUNDLE_KEY_IS_CONNECTED, false);
        }

        // Always need to register our receiver.
        registerReceiver();
        View rootView = inflater.inflate(getLayoutResource(), container, false);
        mNotConnectedTextView = (TextView) rootView.findViewById(R.id.section_not_connected);

        onCreateViewHook(rootView);

        setConnected(mConnected, true);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDestroyView() {
        unregisterSectionReceiver();
        super.onDestroyView();
    }

    abstract protected int getLayoutResource();
    abstract protected void onCreateViewHook(View rootView);
    abstract protected LinearLayout getSectionLayout();
    abstract protected void registerSectionReceiver(IntentFilter filter);
    abstract protected void unregisterSectionReceiver();
}
