package com.example.iu_pdm_pedometer;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.support.v4.app.NavUtils;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;

public class StartCount extends Activity implements SensorEventListener {
	private SensorManager mSensorManager;
	private Sensor mAcc;
	private float prev_step;
	private int nsteps = 0; 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start_count);
		// Show the Up button in the action bar.
		Intent i = getIntent();
		int [] time = i.getIntArrayExtra(DisplayUserData.TOPTIME);
		
		TextView tv = (TextView) findViewById(R.id.timeset);
		tv.setText("Top hour: " + time[2]+":"+time[1]);
		
		TextView ns = (TextView) findViewById(R.id.step_count);
		ns.setText("Steps: " + nsteps);
		ns.setHeight(30);
		
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
	    mAcc = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		setupActionBar();
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.start_count, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	  @Override
	  public final void onSensorChanged(SensorEvent event) {
	    // The light sensor returns a single value.
	    // Many sensors return 3 values, one for each axis.
	    float step_sensor = event.values[0];
	    //float ac2 = event.values[1];
	    //float ac3 = event.values[2];
	    if (step_sensor >= 12.0 && prev_step < 12.0){
	    	// Do something
	    	nsteps++; 
			TextView ns = (TextView) findViewById(R.id.step_count);
			ns.setText("Steps: " + nsteps);
			ns.setHeight(30);
	    	System.out.println("Step! : " + step_sensor + " ----> " + nsteps);
	    }
	    	prev_step = step_sensor; 
	    
	     
	    // Do something with this sensor value.
	  }

	  @Override
	  protected void onResume() {
	    super.onResume();
	    mSensorManager.registerListener(this, mAcc, SensorManager.SENSOR_DELAY_NORMAL);
	  }

	  @Override
	  protected void onPause() {
	    super.onPause();
	    mSensorManager.unregisterListener(this);
	  }

}
