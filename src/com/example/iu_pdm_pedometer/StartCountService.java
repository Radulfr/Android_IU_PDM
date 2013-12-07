package com.example.iu_pdm_pedometer;

import java.util.Date;


import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;

public class StartCountService extends Service implements SensorEventListener{
	public static final int THRESHOLD_TIME = 20; 
	public static final int THRESHOLD_TIME_WALKING = 600; // ten minutes	
	private SensorManager mSensorManager;
	private Sensor mAcc;
	private float prev_step;
	private int nsteps = 0; 
	private int valid_steps = 0; 
	private int valid_time = 0; 
	private short rings = 3; 
	private int interval = 0; 
	private int initial_time; 
	private int next_time; 
	private int initial_interval; 
	private int next_interval; 
	private int prevStep_time;
	private boolean walking = false; 
	

	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand( intent, flags, startId );

		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
	    mAcc = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	    
	    setInitial_time(getTime()); 
	    setInitial_interval(getTime()); 
	    float [] data = intent.getFloatArrayExtra("DATA");
			prev_step = data[0];
			nsteps = (int) data[1];
			valid_steps = (int) data[2];
			valid_time = (int) data[3]; 
			interval = (int) data[4];
			initial_time = (int) data[5];
			next_time = (int) data[6];
			initial_interval = (int) data[7];
			next_interval = (int) data[8];
			prevStep_time = (int) data[9];
			
			if (data[10] > 0)
				walking = true;
			else
				walking = false;
			
		mSensorManager.registerListener(this, mAcc, SensorManager.SENSOR_DELAY_FASTEST);
		
		return START_NOT_STICKY;
	}
	
	public int getValid_time() {
		return valid_time;
	}

	public void setValid_time(int valid_time) {
		this.valid_time = valid_time;
	}

	public int getPrevStep_time() {
		return prevStep_time;
	}

	public void setPrevStep_time(int prevStep_time) {
		this.prevStep_time = prevStep_time;
	}

	public int getInitial_interval() {
		return initial_interval;
	}

	public void setInitial_interval(int i) {
		this.initial_interval = i;
	}

	public int getNext_interval() {
		return next_interval;
	}

	public void setNext_interval(int next_interval) {
		this.next_interval = next_interval;
	}

	public boolean isWalking() {
		return walking;
	}

	public void setWalking(boolean walking) {
		this.walking = walking;
	}

	public int getNext_time() {
		return next_time;
	}

	public void setNext_time(int next_time) {
		this.next_time = next_time;
	}

	public int getInitial_time() {
		return initial_time;
	}

	public void setInitial_time(int initial_time) {
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
	
	public StartCountService() {
		super();
	}

	public int getTime(){
		return (int) (new Date().getTime()/1000);
	}
	
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
	    mAcc = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	  public final void onSensorChanged(SensorEvent event) {
	    float step_sensor = event.values[0];
	    //float ac2 = event.values[1];
	    //float ac3 = event.values[2];
	    if (step_sensor >= 12.0 && prev_step < 12.0){
	    	setNext_time(getTime());
	    	
	    	if (!isWalking()){
	    		if((getNext_time() - getInitial_time()) >= THRESHOLD_TIME){ 
	    			//maybe not neccessary
	    			setNsteps(0); 
	    			setInitial_time(getTime()); 
	    		}
	    		else{
	    			setNsteps(getNsteps()+1);
	    			setInitial_interval(getTime()); 
	    			setWalking(true); 
	    		}
	    	}
	    	else{
	    		if((getNext_time() - getInitial_time()) >= THRESHOLD_TIME){
	    			setNsteps(0); 
	    			setInitial_time(getTime());
	    			setNext_time(getTime());

	    			setWalking(false); 
	    		}
	    		else{
	    			setNsteps(getNsteps()+1); 
	    			setInitial_time(getTime());
	    			if ((getNext_time() - getInitial_interval()) >= THRESHOLD_TIME_WALKING){
	    				setValid_time((getNext_time() - getInitial_interval()) + getValid_time());
	    				if (getValid_time() >= getInterval()*THRESHOLD_TIME_WALKING)
	    					System.out.println("YOU HAVE FINISHED!");
	    				else
	    					System.out.println("Interval reached: " + (getValid_time()/600));
	    			}
	    		}
	    	}
	    	/*
			TextView ns = (TextView) findViewById(R.id.step_count);
			TextView extra = (TextView) findViewById(R.id.some_info);
			ns.setText("Steps: " + nsteps);
			ns.setTextSize(40);
			extra.setText("Time walked: " + (getNext_time() - getInitial_interval() )/60 + " minutes"   
			+ "\nEffective time walked: " + getValid_time()/60 + " minutes" +
					"\nIntervals completed: "+getValid_time()/600 + "/" + getInterval());
			extra.setTextSize(25);
			*/ 
	    	System.out.println("Step! : " + step_sensor + " ----> " + getNsteps() +
	    			"\nEffective time walked: " + getValid_time()/60 + " minutes");
	    }
	    	prev_step = step_sensor; 
	    setPrevStep_time(getTime()); 

	  }

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
