package my.taskreminder;

import java.util.Calendar;

import my.taskreminder.util.DateTimeFormater;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class TaskInfoDialogActivity extends Activity {

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.taskinfodialog);
		//Get button and assign on click action
		//dont need reference any more
		View b=findViewById(R.id.okbutton);
		CharSequence name, msg;
		Long time;
		name=getIntent().getCharSequenceExtra("taskName");
		msg=getIntent().getCharSequenceExtra("taskMeassage");
		name=name!=null?name:"not set";
		msg=msg!=null?msg:"not set";
		time = getIntent().getLongExtra("taskTime", Calendar.getInstance().getTimeInMillis());
		TextView v_name, v_measege, v_time;
		v_name=(TextView)findViewById(R.id.task_name);
		v_measege=(TextView)findViewById(R.id.task_message);
		v_time=(TextView)findViewById(R.id.task_time);
		
		v_name.setText(name);
		v_measege.setText(msg);
		v_time.setText(DateTimeFormater.longToTime(time));
		
		
		b.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				TaskInfoDialogActivity.this.finish();
				
			}
		});
	}

}
