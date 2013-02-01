package my.taskreminder;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TextView;

public class TaskListActivity extends TabActivity {

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tasklist);
		Resources res = getResources(); // Resource object to get Drawables
	    TabHost tabHost = getTabHost();  // The activity TabHost
	    TabHost.TabSpec spec;  // Resusable TabSpec for each tab
	    Intent intent;  // Reusable Intent for each tab

	    // Create an Intent to launch an Activity for the tab (to be reused)
	    intent = new Intent().setClass(this, ListViewTabActivity.class);
	    intent.putExtra("TAB", ListViewTabActivity.TAB_TODAY);
	    spec = tabHost.newTabSpec(ListViewTabActivity.TAB_TODAY+"")
	    		.setIndicator(res.getText(R.string.today))
	    		.setContent(intent);
	    tabHost.addTab(spec);
	    
	    intent = new Intent().setClass(this, ListViewTabActivity.class);
	    intent.putExtra("TAB", ListViewTabActivity.TAB_WEEK);
	    spec = tabHost.newTabSpec(ListViewTabActivity.TAB_WEEK+"")
	    		.setIndicator(res.getText(R.string.week))
	    		.setContent(intent);
	    tabHost.addTab(spec);
	    
	    intent = new Intent().setClass(this, ListViewTabActivity.class);
	    intent.putExtra("TAB", ListViewTabActivity.TAB_ALL);
	    spec = tabHost.newTabSpec(ListViewTabActivity.TAB_ALL+"")
	    		.setIndicator(res.getText(R.string.all))
	    		.setContent(intent);
	    tabHost.addTab(spec);
	    
	    intent = new Intent().setClass(this, ListViewTabActivity.class);
	    intent.putExtra("TAB", ListViewTabActivity.TAB_MISSED);
	    spec = tabHost.newTabSpec(ListViewTabActivity.TAB_MISSED+"")
	    		.setIndicator(res.getText(R.string.missed))
	    		.setContent(intent);
	    tabHost.addTab(spec);
	    final TextView tv = (TextView) tabHost.getTabWidget().getChildAt(3).findViewById(android.R.id.title);        
	    tv.setTextColor(this.getResources().getColorStateList(R.color.missed_colors));
	    tabHost.setCurrentTab(0);
	}

}
