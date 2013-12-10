package com.example.iu_pdm_pedometer;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
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
	public final static String WEIGHT = "com.example.iu_pdm_pedometer.WEIGHT";
	public final static String STEP = "com.example.iu_pdm_pedometer.STEP";
	private int time_training = 30;
	public final static int TIME_HOUR_INDEX = 0;
	public final static int TIME_MINUTE_INDEX = 1;
	public final static int TIME_TRAINING_INDEX = 3;
	float weight;
	int step;



	@SuppressLint("NewApi")

	private TextView tvDisplayTime;
	static final int TIME_DIALOG_ID = 999;
	public static final int NEW_WEIGHT_INDEX = 4;
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
		weight = data[MainActivity.WEIGHT_INDEX];
		step = (int) data[MainActivity.LEGSIZE_INDEX];
		
		SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putFloat(getString(R.string.new_weight), data[MainActivity.WEIGHT_INDEX]);
		editor.commit();

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
		tv.setTextSize(15);
		//setContentView(tv); 
		// Show the Up button in the action bar.
		setupActionBar();
	}
	public void saveNGo(View view){
		TimePicker tp = (TimePicker) findViewById(R.id.timePicker);
		Intent intent = new Intent(this, StartCount.class);
		//Intent intent = new Intent(this, StartCountService.class);
		int hour = tp.getCurrentHour();
		int minute = tp.getCurrentMinute();
		Time top_time = new Time(); 
		// Set the correct date here
		top_time.setToNow();
		top_time.set(Time.SECOND, minute, hour, Time.MONTH_DAY, Time.MONTH, Time.YEAR);
		int [] time = new int[4];
		
		time[TIME_HOUR_INDEX] = top_time.hour;
		time[TIME_MINUTE_INDEX] = top_time.minute;
		time[TIME_TRAINING_INDEX] = time_training;
		

		intent.putExtra(TOPTIME, time);		
		intent.putExtra(WEIGHT, weight);
		intent.putExtra(STEP, step);
		System.out.println("theTime = "+ top_time.hour + ":" + top_time.minute);
		startActivity(intent);
		// NEW ------------------------------------------
		//startService(intent);
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
