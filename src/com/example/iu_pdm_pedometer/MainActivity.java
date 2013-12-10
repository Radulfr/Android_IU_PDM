package com.example.iu_pdm_pedometer;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends Activity {
	public final static String THE_DATA = "com.example.iu_pdm_pedometer.USERDATA";
	public final static int HEIGHT_INDEX = 0;
	public final static int WEIGHT_INDEX = 1;
	public final static int LEGSIZE_INDEX = 2;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void sendData(View view){
		SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		Intent i = new Intent(this, DisplayUserData.class); 
		EditText height = (EditText) findViewById(R.id.Height);
		EditText weight = (EditText) findViewById(R.id.Weight);
		EditText leg = (EditText) findViewById(R.id.Leglongitude);
		
		float [] values = new float[3];
		
		Float h = Float.parseFloat(height.getText().toString());
		Float w = Float.parseFloat(weight.getText().toString());
		Float l = Float.parseFloat(leg.getText().toString());
		
		System.out.println(h + " - " + w + " " + l);
		

		editor.putFloat(getString(R.string.main_h), h);
		editor.putFloat(getString(R.string.main_w), w);
		editor.putFloat(getString(R.string.main_l), l);
		editor.commit();
		
		values[HEIGHT_INDEX] = h; 
		values[WEIGHT_INDEX] = w;
		values[LEGSIZE_INDEX] = l;
		i.putExtra(THE_DATA, values);
		startActivity(i);
	}
}
