package com.example.iu_pdm_pedometer;

import java.util.Calendar;
import java.util.Date;


import android.os.Bundle;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;
import android.support.v4.app.NavUtils;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;

public class StartCount extends Activity implements SensorEventListener {
	
	public static final int THRESHOLD_TIME = 20; 
	public static final int THRESHOLD_TIME_WALKING = 600; // ten minutes	
	private SensorManager mSensorManager=null;
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
	private Intent intent_service;
	
	  private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
	        @Override
	        public void onReceive(Context context, Intent intent) {
	        	updateData(intent);       
	        }


	  };    
	  private void updateData(Intent intent) {
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

			TextView ns = (TextView) findViewById(R.id.step_count);
			TextView extra = (TextView) findViewById(R.id.some_info);
			ns.setText("Steps: " + nsteps);
			ns.setTextSize(40);
			extra.setText("Time walked: " + (getNext_time() - getInitial_interval() )/60 + " minutes"   
			+ "\nEffective time walked: " + getValid_time()/60 + " minutes" +
					"\nIntervals completed: "+getValid_time()/600 + "/" + getInterval());
			extra.setTextSize(25); 
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start_count);
		intent_service = new Intent(this, StartCountService.class);
		//BAD SOLUTION FOR AVOID SERVICE 
		 //getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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
		
        Calendar cal = Calendar.getInstance();
        System.out.println(cal.HOUR_OF_DAY + " : " + cal.MINUTE);
        //cal.add(Calendar.SECOND, ((time[2] -2) - cal.HOUR_OF_DAY)*3600);
        
        cal.set(cal.HOUR_OF_DAY, time[2]-1);
        cal.set(cal.MINUTE, time[1]);
        
        System.out.println("HSeconds: " + ((time[2] -1) - cal.HOUR_OF_DAY)*3600 + " MSeconds: " +(time[1] - cal.MINUTE)*60) ;
        
        Intent intent = new Intent(this, AlarmReceiverActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
            12345, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager am = 
            (AlarmManager)getSystemService(Activity.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
                pendingIntent);
        
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
	    mAcc = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	    
	    setInitial_time(getTime()); 
	    setInitial_interval(getTime()); 
		setupActionBar();
	}
	public int getTime(){
		return (int) (new Date().getTime()/1000);
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
			TextView ns = (TextView) findViewById(R.id.step_count);
			TextView extra = (TextView) findViewById(R.id.some_info);
			ns.setText("Steps: " + nsteps);
			ns.setTextSize(40);
			extra.setText("Time walked: " + (getNext_time() - getInitial_interval() )/60 + " minutes"   
			+ "\nEffective time walked: " + getValid_time()/60 + " minutes" +
					"\nIntervals completed: "+getValid_time()/600 + "/" + getInterval());
			extra.setTextSize(25); 
	    	System.out.println("Step! : " + step_sensor + " ----> " + getNsteps() +
	    			"\nEffective time walked: " + getValid_time()/60 + " minutes");
	    }
	    	prev_step = step_sensor; 
	    setPrevStep_time(getTime()); 

	  }

	  @Override
	  protected void onResume() {
	    super.onResume();
	   // if (mSensorManager == null)
	    	mSensorManager.registerListener(this, mAcc, SensorManager.SENSOR_DELAY_NORMAL);
	    //stopService(intent_service);
	    //Get data back
	  }

	  @Override
	  protected void onPause() {
		  
		  float [] data = new float[11];
			data[0] = prev_step;
			data[1] = nsteps; 
			data[2] = valid_steps; 
			data[3] = valid_time;  
			data[4] = interval; 
			data[5] = initial_time; 
			data[6] = next_time; 
			data[7] = initial_interval; 
			data[8] = next_interval; 
			data[9] = prevStep_time;
			if (walking)
				data[10] = 1;
			else
				data[10] = 0;
			intent_service.putExtra("DATA", data);
			//startService(intent_service);
			//registerReceiver(broadcastReceiver, new IntentFilter(StartCountService.BROADCAST_ACTION));
			mSensorManager.registerListener(this, mAcc, SensorManager.SENSOR_DELAY_FASTEST);
			super.onPause();
			// If I comment this line, the app still working on second plane
			//mSensorManager.unregisterListener(this);
	  }

}
