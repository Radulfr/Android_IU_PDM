package com.example.iu_pdm_pedometer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.NavUtils;
import android.support.v4.content.LocalBroadcastManager;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;

public class StartCount extends Activity  {
	
	public static final int THRESHOLD_TIME = 20; 
	public static final int THRESHOLD_TIME_WALKING = 60; // ten minutes	
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
	private int prevStep_time =0;
	private boolean walking = false; 
	private Intent intent_service;
	private boolean finish = false; 
	private boolean lock_valid = true;
	int total_distance = 0; 
	float step_longitude = 0;
	private MediaPlayer mMediaPlayer; 
	BroadcastReceiver receiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start_count);
		
		intent_service = new Intent(this, StartCountService.class);
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mAcc = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

		Intent i = getIntent();
		int [] time = i.getIntArrayExtra(DisplayUserData.TOPTIME);
		String balance = "good";
		setInterval(time[DisplayUserData.TIME_TRAINING_INDEX]/10); 
		
		// SET ALARM -------------------------------------------------------------
        Calendar cal = Calendar.getInstance();
        cal.set(cal.HOUR_OF_DAY, time[DisplayUserData.TIME_HOUR_INDEX]-1);
        cal.set(cal.MINUTE, time[DisplayUserData.TIME_MINUTE_INDEX]);
        
        Intent intent = new Intent(this, AlarmReceiverActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
            12345, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager am = 
            (AlarmManager)getSystemService(Activity.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
                pendingIntent);
        //SET ALARM END ---------------------------------------------------------------------
        // LOAD PREV DATA---------------------------------------------------------------------
		SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();

		float lw = sharedPref.getFloat(getString(R.string.last_weight), (float)0.0);
		float cw = i.getFloatExtra(DisplayUserData.WEIGHT, (float) 0.0);
		
		int dist = sharedPref.getInt(getString(R.string.last_distance), 0);
		int ps = sharedPref.getInt(getString(R.string.last_steps), 0);
		
		step_longitude = i.getIntExtra(DisplayUserData.STEP, 1);
		//longitud
		
		editor.putFloat(getString(R.string.last_weight), cw);
		editor.commit();
		
		if(cw > lw)
			balance = "Bad balance. You must walk more!";
		else if (lw > cw)
			balance = "Good balance! Congratulations! Go on!";
		else
			balance = "Keep working!";
		// LOAD PREV DATA END---------------------------------------------------------------------
		// INFO TOAST-----------------------------------------------------------------
		Context context = getApplicationContext();
		CharSequence text2 = "Prev distance: " + dist/100 + " m\nPrev steps: " + ps;
		int duration = Toast.LENGTH_LONG;
		Toast toast = Toast.makeText(context, text2, duration);
		toast.show();
		// INFO TOAST END ----------------------------------------------------------------
		// UI MESSAGES-------------------------------------------------------------------------
		TextView tv = (TextView) findViewById(R.id.timeset);
		
		TextView ns = (TextView) findViewById(R.id.step_count);
		ns.setText("Steps: " + nsteps);
		ns.setTextSize(40);
		CharSequence text = "Prev. weight: " +lw +" kg\nCurrent weight: " +cw + " kg\n" + balance ;
		tv.setText(text);
		tv.setTextSize(20);
		// UI MESSAGES END-------------------------------------------------------------------------
		
		receiver = new BroadcastReceiver() {
	        @Override
	        public void onReceive(Context context, Intent intent) {
	        	int data [] = new int[6];
	        	String intervals; 
	        	data = intent.getIntArrayExtra(StartCountService.NOTIF);
	            //int s = intent.getIntExtra(, -1);
	        	
				TextView ns = (TextView) findViewById(R.id.step_count);
				TextView extra = (TextView) findViewById(R.id.some_info);
				ns.setText("Steps: " + data[0] + " (Valid: " +data[1]+")");
				ns.setTextSize(30);
				
				if(data[5] == getInterval())
					intervals = "COMPLETED! WELL DONE!";
				else
					intervals = "\nIntervals: "+data[5] + "/" + getInterval();
				
				extra.setText("Time walked: " + data[2] + " minutes"   
						+ "\nEffective time walked: " + data[3] + " minutes" 
						+ "\nEffective distance:      "  + data[1]*step_longitude/100 + " m"
						+ intervals);
				extra.setTextSize(20); 
				saveData(data[1]);
	            // do something here.
	        }
	    };
		
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
		startService(intent_service);
		setInitial_time(getTime()); 
	    setInitial_interval(getTime()); 
		setupActionBar();
	}
	@Override
	protected void onStart() {
	    super.onStart();
	    LocalBroadcastManager.getInstance(this).registerReceiver((receiver), new IntentFilter(StartCountService.NOTIF));
	}

	@Override
	protected void onStop() {
	    LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
	    super.onStop();
	}	
   
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

/*	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public final void onSensorChanged(SensorEvent event) {
		float step_sensor = event.values[0];

		if (step_sensor >= 12.0 && prev_step < 12.0){
			setNext_time(getTime());
			total_distance += step_longitude; 

			if (!isWalking()){
				lock_valid = true;
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
					
					if ((getNext_time() - getInitial_interval()) >= THRESHOLD_TIME_WALKING){
						
						if (lock_valid == true){
							lock_valid = false;
							setValid_time(getNext_time() - getInitial_interval()  + getValid_time());
							setValid_steps(getValid_steps() + getNsteps() - 10);
						}
						setValid_time((getNext_time() -getInitial_time()) + getValid_time());
						setValid_steps(getValid_steps()+1);
						if ((getValid_time() >= getInterval()*THRESHOLD_TIME_WALKING) & (finish==false)){
							System.out.println("YOU HAVE FINISHED!");
							playSound(this, getAlarmUri());
							finish=true;
							Context context = getApplicationContext();
							CharSequence text2 = "CONGRATULATIONS! YOU'VE FINISH!" ;
							int duration = Toast.LENGTH_LONG;
							Toast toast = Toast.makeText(context, text2, duration);
							toast.show();
						}
					}
					setInitial_time(getTime());
				}
			}

			TextView ns = (TextView) findViewById(R.id.step_count);
			TextView extra = (TextView) findViewById(R.id.some_info);
			ns.setText("Steps: " + getNsteps() + " (Valid: " +getValid_steps()+")");
			ns.setTextSize(30);
			extra.setText("Time walked: " + (getNext_time() - getInitial_interval() )/60 + " minutes"   
					+ "\nEffective time walked: " + getValid_time()/60 + " minutes" 
					+ "\nTotal/Eff. dist:      "  + (int) (total_distance/100) + "/" + (int) (getValid_steps()*step_longitude/100) + " m"
					+ "\nIntervals: "+getValid_time()/60 + "/" + getInterval());
			extra.setTextSize(20); 
			System.out.println("Step! : " + step_sensor + " ----> " + getNsteps() +
					"\nEffective time walked: " + getValid_time()/60 + " minutes");
		}
		prev_step = step_sensor; 
		setPrevStep_time(getTime()); 
		saveData(getValid_steps());

	}*/

	@Override
	protected void onResume() {
		super.onResume();
		// if (mSensorManager == null)
		//mSensorManager.registerListener(this, mAcc, SensorManager.SENSOR_DELAY_NORMAL);
		//stopService(intent_service);
		//Get data back
	}

	protected void saveData(int n_steps){
		SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putInt(getString(R.string.last_steps), n_steps);
		editor.putInt(getString(R.string.last_distance), total_distance);
		editor.commit();

	}
	@Override
	protected void onPause() {

	/*	float [] data = new float[11];
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
		startService(intent_service);
		mSensorManager.unregisterListener(this);*/
		//registerReceiver(broadcastReceiver, new IntentFilter(StartCountService.BROADCAST_ACTION));
		//mSensorManager.registerListener(this, mAcc, SensorManager.SENSOR_DELAY_FASTEST);
		super.onPause();
		// If I comment this line, the app still working on second plane
		
	}
    private void playSound(Context context, Uri alert) {
        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(context, alert);
            final AudioManager audioManager = (AudioManager) context
                    .getSystemService(Context.AUDIO_SERVICE);
            if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                mMediaPlayer.prepare();
                mMediaPlayer.start();
            }
        } catch (IOException e) {
            System.out.println("OOPS");
        }
    }

    //Get an alarm sound. Try for an alarm. If none set, try notification, 
    //Otherwise, ringtone.
    private Uri getAlarmUri() {
        Uri alert = RingtoneManager
                .getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alert == null) {
            alert = RingtoneManager
                    .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            if (alert == null) {
                alert = RingtoneManager
                        .getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            }
        }
        return alert;
    }	

}
