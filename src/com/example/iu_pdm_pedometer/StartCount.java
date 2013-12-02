package com.example.iu_pdm_pedometer;

import java.util.Date;

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
	
	public static final int THRESHOLD_TIME = 20; 
	public static final int THRESHOLD_TIME_WALKING = 600; // ten minutes	
	private SensorManager mSensorManager;
	private Sensor mAcc;
	private float prev_step;
	private int nsteps = 0; 
	private int valid_steps = 0; 
	private short rings = 3; 
	private int interval = 0; 
	private Date initial_time; 
	private Date next_time; 
	private Date initial_interval; 
	private Date next_interval; 
	private Date prevStep_time;
	private boolean walking = false; 
	
	
	public Date getPrevStep_time() {
		return prevStep_time;
	}

	public void setPrevStep_time(Date prevStep_time) {
		this.prevStep_time = prevStep_time;
	}

	public Date getInitial_interval() {
		return initial_interval;
	}

	public void setInitial_interval(Date initial_interval) {
		this.initial_interval = initial_interval;
	}

	public Date getNext_interval() {
		return next_interval;
	}

	public void setNext_interval(Date next_interval) {
		this.next_interval = next_interval;
	}

	public boolean isWalking() {
		return walking;
	}

	public void setWalking(boolean walking) {
		this.walking = walking;
	}

	public Date getNext_time() {
		return next_time;
	}

	public void setNext_time(Date next_time) {
		this.next_time = next_time;
	}

	public Date getInitial_time() {
		return initial_time;
	}

	public void setInitial_time(Date initial_time) {
		this.initial_time = initial_time;
	}

	public float getPrev_step() {
		return prev_step;
	}

	public void setPrev_step(float prev_step) {
		this.prev_step = prev_step;
	}

	public int getNsteps() {
		return nsteps;
	}

	public void setNsteps(int nsteps) {
		this.nsteps = nsteps;
	}

	public int getValid_steps() {
		return valid_steps;
	}

	public void setValid_steps(int valid_steps) {
		this.valid_steps = valid_steps;
	}

	public short getRings() {
		return rings;
	}

	public void setRings(short rings) {
		this.rings = rings;
	}

	public int getInterval() {
		return interval;
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start_count);
		// Show the Up button in the action bar.
		Intent i = getIntent();
		int [] time = i.getIntArrayExtra(DisplayUserData.TOPTIME);
		
		setInterval(time[DisplayUserData.TIME_TRAINING_INDEX]/10); 
		
		TextView tv = (TextView) findViewById(R.id.timeset);
		tv.setText("Top hour: " + time[2]+":"+time[1] +" in " + getInterval() +
				" intervals máx\n Please put your phone horizontal on your belt. ");
		
		TextView ns = (TextView) findViewById(R.id.step_count);
		ns.setText("Steps: " + nsteps);
		ns.setTextSize(40);
		
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
	    mAcc = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	    
	    setInitial_time(new Date()); 
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
	    float step_sensor = event.values[0];
	    //float ac2 = event.values[1];
	    //float ac3 = event.values[2];
	    if (step_sensor >= 12.0 && prev_step < 12.0){
	    	setNext_time(new Date());
	    	
	    	System.out.println(getNext_time().getTime()*1000);
	    	if (!isWalking()){
	    		if((getNext_time().getTime()/1000 - getInitial_time().getTime()/1000) >= THRESHOLD_TIME){
	    			setNsteps(0); 
	    			setInitial_time(new Date()); 
	    		}
	    		else{
	    			setNsteps(getNsteps()+1);
	    			setWalking(true); 
	    		}
	    	}
	    	else{
	    		if((getNext_time().getTime()/1000 - getInitial_time().getTime()/1000) >= THRESHOLD_TIME){
	    			setNsteps(0); 
	    			setInitial_time(new Date()); 
	    			setWalking(false); 
	    		}
	    		else{
	    			setNsteps(getNsteps()+1); 
	    		}
	    	}
			TextView ns = (TextView) findViewById(R.id.step_count);
			ns.setText("Steps: " + nsteps);
			ns.setTextSize(40);
	    	System.out.println("Step! : " + step_sensor + " ----> " + getNsteps());
	    }
	    	prev_step = step_sensor; 
	    setPrevStep_time(new Date()); 
	     
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
