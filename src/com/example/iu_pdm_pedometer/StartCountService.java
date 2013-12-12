package com.example.iu_pdm_pedometer;

import java.io.IOException;
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
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class StartCountService extends Service implements SensorEventListener{
	public static final int THRESHOLD_TIME = 20; 
	public static final int THRESHOLD_TIME_WALKING = 60; // ten minutes	
	public static final String BROADCAST_ACTION = "com.example.iu_pdm_pedometer.bc";
	private final Handler handler = new Handler();
	Intent intent; 
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
	private boolean finish = false; 
	private boolean lock_valid = true;
	int total_distance = 0; 
	float step_longitude = 0;
	private MediaPlayer mMediaPlayer; 
	LocalBroadcastManager broadcaster;
	static final public String NOTIF = "com.IU_PDM_PEDOMETER";
	
	public void sendResult(int [] data) {
	    Intent intent = new Intent(NOTIF);
	 
	    intent.putExtra(NOTIF, data);
	    broadcaster.sendBroadcast(intent);
	}
    @Override
    public void onCreate() {
            super.onCreate();
            broadcaster = LocalBroadcastManager.getInstance(this);
            intent = new Intent(BROADCAST_ACTION);        
    }
    private Runnable sendUpdatesToUI = new Runnable() {
    	public void run() {
    		DisplayLoggingInfo();                    
    		handler.postDelayed(this, 1000); // 1 seconds
    	}
    };  
    private void DisplayLoggingInfo() {
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
			intent.putExtra("DATA", data);
    	sendBroadcast(intent);
    }
    @Override
    public void onDestroy() {                
    handler.removeCallbacks(sendUpdatesToUI);                
            super.onDestroy();
    }    
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
		//mSensorManager.unregisterListener(this);
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
		int	effective_time_walked = 0;
		int time_walked = 0; 
		int t_distance = 0; 
		int t_effective_distance = 0; 
		int t_intervals = 0; 
		int data[] = new int [6]; 

		if (step_sensor >= 12.0 && prev_step < 12.0){
			setNext_time(getTime());
			total_distance ++; 

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
			effective_time_walked = getValid_time()/60;
			time_walked = (getNext_time() - getInitial_interval())/60; 
			t_distance = total_distance/100; 
			//t_effective_distance = getValid_steps()*step_longitude; 
			t_intervals = getValid_time()/THRESHOLD_TIME_WALKING; 
			/*TextView ns = (TextView) findViewById(R.id.step_count);
			TextView extra = (TextView) findViewById(R.id.some_info);
			ns.setText("Steps: " + getNsteps() + " (Valid: " +getValid_steps()+")");
			ns.setTextSize(30);
			extra.setText("Time walked: " + (getNext_time() - getInitial_interval() )/60 + " minutes"   
					+ "\nEffective time walked: " + getValid_time()/60 + " minutes" 
					+ "\nTotal/Eff. dist:      "  + (int) (total_distance/100) + "/" + (int) (getValid_steps()*step_longitude/100) + " m"
					+ "\nIntervals: "+getValid_time()/60 + "/" + getInterval());
			extra.setTextSize(20); */
			System.out.println("Step! : " + step_sensor + " ----> " + getNsteps() +
					"\nEffective time walked: " + getValid_time()/60 + " minutes");
			
			data[0] = getNsteps(); 
			data[1] = getValid_steps(); 
			data[2] = time_walked; 
			data[3] = effective_time_walked; 
			data[4] = t_distance; 
			data[5] = t_intervals; 
			sendResult(data);
		}
		prev_step = step_sensor; 
		setPrevStep_time(getTime()); 
		//saveData(getValid_steps());

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
	protected void saveData(int n_steps){
/*		SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putInt(getString(R.string.last_steps), n_steps);
		editor.putInt(getString(R.string.last_distance), total_distance);
		editor.commit();*/

	}
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
