package com.example.iu_pdm_pedometer;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.format.Time;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

public class DisplayUserData extends Activity {
	public final static String TOPTIME = "com.example.iu_pdm_pedometer.TOPTIME";
	private int time_training = 30;
	public final static int TIME_TRAINING_INDEX = 6;
	@SuppressLint("NewApi")

	private TextView tvDisplayTime;
	static final int TIME_DIALOG_ID = 999;
	@SuppressLint("NewApi")
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_display_user_data);
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
			getActionBar().setDisplayHomeAsUpEnabled(true); 
		}
		
		Intent i = getIntent();

		float[] data = i.getFloatArrayExtra(MainActivity.THE_DATA);
		float imc = data[MainActivity.WEIGHT_INDEX]/((data[MainActivity.HEIGHT_INDEX]/100)*(data[MainActivity.HEIGHT_INDEX]/100)); 
		String message="";
		 
		if (imc < 18.5)
			message = "under your better weight";
		else if( imc >= 18.5 && imc < 25)
			message = "in your correct weight"; 
		else if( imc >= 25 && imc < 30){
			message = "over your  better weight";
			time_training = 40;
		}
		else if (imc >= 30){
			message = "obese. Careful!";
			time_training = 50; 
		}	
		TextView tv = (TextView) findViewById(R.id.info); 
		tv.setText("Your IMC is " + imc +
				"\n\nYou are probably "+message +
				"\n\nTraining time estimated: " + time_training + " minutes/day"+
				"\nCan you select your top time?"); 
		//setContentView(tv); 
		// Show the Up button in the action bar.
		setupActionBar();
	}
	public void saveNGo(View view){
		TimePicker tp = (TimePicker) findViewById(R.id.timePicker);
		Intent intent = new Intent(this, StartCount.class);
		int hour = tp.getCurrentHour();
		int minute = tp.getCurrentMinute();
		Time top_time = new Time(); 
		// Set the correct date here
		top_time.setToNow();
		top_time.set(Time.SECOND, minute, hour, Time.MONTH_DAY, Time.MONTH, Time.YEAR);
		int [] time = new int[7];
		time[0] = Time.SECOND;
		time[1] = top_time.minute;	
		time[2] = top_time.hour;
		time[3] = Time.MONTH_DAY;
		time[4] = Time.MONTH;
		time[5] = Time.YEAR;
		
		time[TIME_TRAINING_INDEX] = time_training;
		intent.putExtra(TOPTIME, time);								
		//System.out.println("theTime = "+ top_time.hour + ":" + top_time.minute);
		startActivity(intent);
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
		getMenuInflater().inflate(R.menu.display_user_data, menu);
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
}
