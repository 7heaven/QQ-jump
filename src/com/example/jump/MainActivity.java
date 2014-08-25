package com.example.jump;

import java.lang.reflect.Field;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.newrelic.agent.android.NewRelic;

public class MainActivity extends Activity {
	
	private ListView list;
	
	private Rect[] bounds;
	
	private int x;
	private int y;
	private float vx;
	private float vy;
	
	private int statusBarHeight;
	
	private boolean stop = false;
	
	private int tx;
	private int ty;
	
	private int position = 1;
	
	private ImageView item;
	private WindowManager windowManager;
	private WindowManager.LayoutParams params;
	
	private Handler handler = new Handler();
	private Runnable jumpRunnable = new Runnable(){
		@Override
		public void run(){
			if(!stop){
				if(Math.abs(ty - y) > 5 || Math.abs(vy) > 0.1F || Math.abs(vx) > 0.1F){
					x += vx;
					y += vy;
					
					vx *= 0.98F;
					
					if(y > ty){
						vy = -vy;
						vy *= 0.55F;
						vx *= 0.55F;
					}else{
						vy += 0.35F;
					}
					
					params.x = x;
					params.y = y;
					windowManager.updateViewLayout(item, params);
					
					handler.postDelayed(this, 12L);
				}else{
					position++;
					if(position < bounds.length){
						tx = bounds[position].left + bounds[position].width() / 2 - 25;
						ty = bounds[position].top - 50 - statusBarHeight;
						
						vy = -4;
						vx = (float) ((tx - x) / 32 + (Math.random() * 2 - 1));
						
						handler.post(this);
					}
				}
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		NewRelic.withApplicationToken(
				"AA275b861fa655ae3b289366538f81fc954c42ae4f"
				).start(this.getApplication());
		
		try{
			Class<?> clazz = Class.forName("com.android.internal.R$dimen");
			Object o = clazz.newInstance();
			Field field = clazz.getField("status_bar_height");
			int x = (Integer) field.get(o);
			statusBarHeight = getResources().getDimensionPixelSize(x);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		
		list = (ListView) findViewById(R.id.list);
		list.setAdapter(adapter);
		list.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View target, int position, long id){
				y = bounds[0].top - 50 - statusBarHeight;
				x = bounds[0].left + bounds[0].width() / 2 - 25;
				
				ty = bounds[1].top - 50 - statusBarHeight;
				tx = bounds[1].left + bounds[1].width() / 2 - 25;
				
				vy = -4;
				vx = (float) ((tx - x) / 32 + (Math.random() * 2 - 1));
				
				MainActivity.this.position = 1;
				
				if(item != null){
					windowManager.removeView(item);
					item = null;
				}
				item = new ImageView(MainActivity.this);
				item.setImageResource(R.drawable.ic_launcher);
				
			    params = new WindowManager.LayoutParams(50, 50);
			    params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
			    params.gravity = Gravity.LEFT | Gravity.TOP;
			    params.type = WindowManager.LayoutParams.TYPE_PHONE;
			    params.format = PixelFormat.RGBA_8888;
			    params.width = 50;
			    params.height = 50;
				
				windowManager.addView(item, params);
				
				params.x = x;
				params.y = y;
				windowManager.updateViewLayout(item, params);
				
				handler.removeCallbacks(jumpRunnable);
				handler.postDelayed(jumpRunnable, 1000L);
			}
		});
		bounds = new Rect[adapter.getCount()];
		
	}
	
	private BaseAdapter adapter = new BaseAdapter(){

		@Override
		public int getCount() {
			return 9;
		}

		@Override
		public String getItem(int position) {
			return "lalalalalalalala";
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}
		
		@Override
		public int getItemViewType(int position){
			return Math.random() * 10 > 5 ? 0 : 1;
		}
		
		@Override
		public int getViewTypeCount(){
			return 2;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			
			switch(getItemViewType(position)){
			case 0:
				convertView = LayoutInflater.from(MainActivity.this).inflate(R.layout.item_listview_message_me, null, false);
				break;
			case 1:
				convertView = LayoutInflater.from(MainActivity.this).inflate(R.layout.item_listview_message_other, null, false);
				break;
			}
			
			final TextView content = (TextView) convertView.findViewById(R.id.content);
			content.setText(getItem(position));
			
			if(position < bounds.length){
				if(bounds[position] == null || bounds[position].width() == 0 && bounds[position].height() == 0){
					content.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener(){

						@Override
						public void onGlobalLayout() {
							int[] location = new int[2];
							content.getLocationOnScreen(location);
							bounds[position] = new Rect(location[0], location[1], content.getMeasuredWidth() + location[0], content.getMeasuredHeight() + location[1]);
						}
						
					});
				}
				
			}
			
			if(position == getCount() - 1 && bounds[bounds.length - 1] != null){
				for(Rect rect : bounds){
					Log.d("rect", rect.toString());
				}
			}
			
			return convertView;
		}
		
	};
	
	@Override
	public void onResume(){
		super.onResume();
		stop = false;
	}
	
	@Override
	public void onPause(){
		super.onPause();
		stop = true;
		if(item != null){
			windowManager.removeView(item);
			item = null;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
