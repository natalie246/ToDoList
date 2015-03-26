package com.nataliemaayan.taskmanager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.nataliemaayan.taskmanager.fragments.ChooseDateFragment;
import com.nataliemaayan.taskmanager.fragments.ChooseTimeFragment;

import utilities.Singleton;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.speech.RecognizerIntent;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import entities.Task;

public class MainActivity extends FragmentActivity implements DialogListener {

    private static final int EDIT_TASK = 1232;
    private static final int RESULT_SPEECH = 1234;
    Singleton singleton=null;
    private TaskListBaseAdapter currentList;
    private TextWatcher tw;
    private Task selectedTask;
    private int taskIdSelected= -1;
    private int timeHour = -1;
    private int timeMinute= -1;
    private int dateYear= -1;
    private int dateMonth= -1;
    private int dateDay= -1;

    //back key press event
    private long lastPressedTime;
    private static final int PERIOD = 2000;
    private int editTaskBoolean = -1;
    private EasyTracker easyTracker = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        easyTracker = EasyTracker.getInstance(MainActivity.this);
        easyTracker.send(MapBuilder.createEvent("TrackEventTest", "Welcome To The APP", "track_event", null).build());   
       
        // avoid keyboard show automatic 
        ListView listView = (ListView) findViewById(R.id.listView);
        
        // close keyboard and set focusable false to etNetTask
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(listView.getWindowToken(), 0);

        // If there is Tasks in the DataBase restore them 
        if (singleton.getInstance(this).getArrayList().isEmpty())
            restoreFromDb();

        currentList = new TaskListBaseAdapter(this, singleton.getInstance(this).getArrayList());

        // when etNewTask pressed then show the extra buttons 
        EditText newTask = (EditText) findViewById(R.id.etNewTask);
        newTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).isActive()) {
                    LinearLayout linearLayout = (LinearLayout) findViewById(R.id.extraOptions);
                    if (linearLayout.getVisibility()== View.VISIBLE ){
                        linearLayout.setVisibility(LinearLayout.GONE);
                    }
                    else linearLayout.setVisibility(LinearLayout.VISIBLE);
                }
            }
        });


        // inflate extra bar when text change 
        tw = new TextWatcher() {
            LinearLayout linearLayout = (LinearLayout) findViewById(R.id.extraOptions);
            public void afterTextChanged(Editable s){
                if (s.length()>0)
                linearLayout.setVisibility(LinearLayout.VISIBLE);
                else linearLayout.setVisibility(LinearLayout.GONE);
            }
            public void  beforeTextChanged(CharSequence s, int start, int count, int after){
                if (count<0)
                linearLayout.setVisibility(LinearLayout.GONE);
            }
            public void  onTextChanged (CharSequence s, int start, int before,int count) {
                if (count<0)
                linearLayout.setVisibility(LinearLayout.GONE);
            }
        };
        EditText et = (EditText) findViewById(R.id.etNewTask);
        et.addTextChangedListener(tw);
    }

	@Override
	protected void onStart() {
		super.onStart();
		
		EasyTracker.getInstance(this).activityStart(this);
	}

	@Override
	protected void onStop() {
		super.onStop();

		EasyTracker.getInstance(this).activityStop(this);
	}

    @Override
    protected void onResume(){
        super.onResume();
        updateListView();
        currentList.notifyDataSetChanged();
    }

    // handle with back key
    // when pressed twice the application closed
     
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            switch (event.getAction()) {
                case KeyEvent.ACTION_DOWN:
                    if (event.getDownTime() - lastPressedTime < PERIOD) {
                        finish();
                    } else {
                        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.extraOptions);
                        linearLayout.setVisibility(LinearLayout.GONE);
                        Toast.makeText(getApplicationContext(), "Press again to exit.",
                                Toast.LENGTH_SHORT).show();
                        lastPressedTime = event.getEventTime();
                    }
                    return true;
            }
        }
        return false;
    }

    /**
     * All types of Dialogs
     */
    // Time-Picker Dialog 
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void showTimePickerDialog(View v) {
        DialogFragment newFragment = new ChooseTimeFragment();

        // add details if user want to edit exist task
        if (selectedTask != null){
            Bundle dataBundle = new Bundle();
            dataBundle.putInt("timeHour", selectedTask._timeHour);
            dataBundle.putInt("timeMinute", selectedTask._timeMinute);
            newFragment.setArguments(dataBundle);
        }
        newFragment.show(getFragmentManager(), "timePicker");
    }

    //show alert dialog
    public void alertDialog(View v) {
        DialogFragment newFragment = new AlertDialog();

        // add details if user want to edit exist task
        newFragment.show(getFragmentManager(), "alertPicker");
    }

    // Date-Picker Dialog 
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new ChooseDateFragment();

        // add details if user want to edit exist task
        if (selectedTask != null)
        {
            Bundle dataBundle = new Bundle();
            dataBundle.putInt("dateYear", selectedTask._dateYear);
            dataBundle.putInt("dateMonth", selectedTask._dateMonth);
            dataBundle.putInt("dateDay", selectedTask._dateDay);
            newFragment.setArguments(dataBundle);
            System.out.println("save bundle----------"+dateDay+"."+dateMonth+"."+dateYear);
        }
        newFragment.show(getFragmentManager(), "datePicker");
    }
  
    /**
     *
     * Returning data from FragmentDialogs
     * (Time dialog)
     * (Date dialog)
     */
    @Override
    public void onFinishEditDialog(Intent data) {
        // For TimePicker Fragment 
        if (data.getExtras().containsKey("hour"))
            timeHour = data.getExtras().getInt("hour");
        	if(selectedTask != null)selectedTask._timeHour = data.getExtras().getInt("hour");
        if (data.getExtras().containsKey("minute"))
            timeMinute = data.getExtras().getInt("minute");
        	if(selectedTask != null)selectedTask._timeMinute = data.getExtras().getInt("minute");

        // For DatePicker Fragment 
        if (data.getExtras().containsKey("year"))
            dateYear = data.getExtras().getInt("year");
        	if(selectedTask != null)selectedTask._dateYear = data.getExtras().getInt("year");
        if (data.getExtras().containsKey("month"))
            dateMonth = data.getExtras().getInt("month");
        	if(selectedTask != null)selectedTask._dateMonth = data.getExtras().getInt("month");
        if (data.getExtras().containsKey("day"))
            dateDay = data.getExtras().getInt("day");
        	if(selectedTask != null)selectedTask._dateDay = data.getExtras().getInt("day");

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case EDIT_TASK: {
                if (resultCode == EDIT_TASK) {
                    // Receive String from Speech recognition
                    ArrayList<String> text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    EditText editText_newTask = (EditText) findViewById(R.id.etNewTask);
                    editText_newTask.setText(text.get(0));
                }
                break;
            }
           
            case RESULT_SPEECH: {
                if ( data != null) {
                    // Receive String from Speech recognition
                    ArrayList<String> text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    EditText editText_newTask = (EditText) findViewById(R.id.etNewTask);
                    editText_newTask.setText(text.get(0));
                }
                break;
            }

        }
    }

    public void restoreFromDb(){
        List<Task> list = singleton.getInstance(this).getDb().getAllTasks();
        for(Task task : list){
            String str = new String( task.getTaskMessage() );
            singleton.getInstance(this).getArrayList().add(0,task);
        }
        updateListView();
    }

    public void updateListView(){
        ListView lv = (ListView) findViewById(R.id.listView);
        TaskListBaseAdapter currentList = new TaskListBaseAdapter(this, singleton.getInstance(this).getArrayList());
        lv.setAdapter(currentList);
    }

    // place all the details from the task in upper EditText and local variables that will send by intent to dialogs 
    public void editTask (View view) {

        // close the upper bar 
        LinearLayout linearLayout1 = (LinearLayout) findViewById(R.id.extraOptions);
        if (linearLayout1.getVisibility()== View.VISIBLE )
            linearLayout1.setVisibility(LinearLayout.GONE);

        // Get the specific Task in this place 
        ListView listView = (ListView) findViewById(R.id.listView);
        int position = listView.getPositionForView(view);
        selectedTask = (Task) listView.getItemAtPosition(position);


        // load all variables from task (if have) to local variables (use in dialogs) 
        // ID
        taskIdSelected = selectedTask.get_id();
        // Message
        EditText message = (EditText) findViewById(R.id.etNewTask);
        message.setText( selectedTask._taskMessage );
       
        // Date
        if (selectedTask._dateYear != 0 )
        dateYear = selectedTask._dateYear;
        if (selectedTask._dateMonth != 0 )
        dateMonth = selectedTask._dateMonth;
        if (selectedTask._dateDay != 0 )
        dateDay = selectedTask._dateDay;
        // Time
        if (selectedTask._timeHour != 0 )
        timeHour = selectedTask._timeHour;
        if (selectedTask._timeMinute != 0 )
        timeMinute = selectedTask._timeMinute;

        // close keyboard
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(listView.getWindowToken(), 0);

        editTaskBoolean=taskIdSelected;
        currentList.notifyDataSetChanged();
    }


    // if user check the dateButton
    @SuppressWarnings("deprecation")
		private void createAlarmAtDate(Task task){
	
	        Intent intent = new Intent();
	        intent.setAction("com.maayanatalie.taskmanager.ManageTaskReceiver");
	        intent.putExtra("taskMessage", task._taskMessage );
	        intent.putExtra("taskId", task.get_id());
	
	        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, task.getID(), intent, 0);
	
	        // getting all parameters of create Date object from task 
	        Date date = new Date();
	
	        if (task._dateDay != -1 && task._dateMonth != -1 && task._dateYear != -1){
	            date.setYear(   task._dateYear  );
	            date.setMonth(  task._dateMonth );
	            date.setDate(   task._dateDay   );
	        }
	        if (task._timeHour != -1 && task._timeMinute != -1){
	            date.setHours(  task._timeHour  );
	            date.setMinutes(task._timeMinute);
	        }
	        
	        BroadcastReceiver receiver = new ManageTaskReceiver();
	        
	        this.registerReceiver( receiver, new IntentFilter("com.blah.blah.somemessage") );
	
	        AlarmManager manager = (AlarmManager)(this.getSystemService( Context.ALARM_SERVICE ));
	        manager.set( AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 1000*5,  pendingIntent);
		}

    	public long millisecondsUntilDate(){
	        Calendar cal = Calendar.getInstance();
	        if (timeHour != -1 && timeMinute != -1 && dateDay == -1 && dateMonth == -1 && dateYear == -1){
	            cal.set(cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH),timeHour,timeMinute);
        }

        if (dateDay != -1 && dateMonth != -1 && dateYear != -1 && timeHour != -1 && timeMinute != -1) {
            cal.set(dateYear, dateMonth, dateDay, timeHour, timeMinute);
        }

        if (dateDay != -1 && dateMonth != -1 && dateYear != -1 && timeHour == -1 && timeMinute == -1) {
            cal.set(dateYear,dateMonth,dateDay,10,0);
        }
        Calendar now = Calendar.getInstance();
        long diff_in_ms = cal.getTimeInMillis()-now.getTimeInMillis();
        return diff_in_ms;
    }

    public void done(View view) {
    	
    	CheckBox checkBox = (CheckBox)view;
        
    	if(checkBox.isChecked())
        {
        	ListView listView = (ListView) findViewById(R.id.listView);
            int position = listView.getPositionForView(view);
            Task selectedTask = (Task) listView.getItemAtPosition(position);
            Toast.makeText(MainActivity.this, "DONE : " + " " +selectedTask.getTaskMessage(), Toast.LENGTH_LONG).show();
            currentList.notifyDataSetChanged();

        }	
    }
    
    public void delete(View view) {
        ListView listView = (ListView) findViewById(R.id.listView);
        int position = listView.getPositionForView(view);
        Task selectedTask = (Task) listView.getItemAtPosition(position);

        showDelteAlert("DELETE", selectedTask.getTaskMessage(), view);
    }

    public void DeleteFromDb(Task taskToDelete, int position){
        singleton.getInstance(this).getArrayList().remove(position);
        singleton.getInstance(this).getDb().deleteTask( taskToDelete);
        updateListView();
    }


    public void saveTask(View view){
        // only if user put text for the task - we continue to save it 
        EditText description = (EditText) findViewById(R.id.etNewTask);
        Task task;
        
        if (!description.getText().toString().isEmpty())
        {
            if (editTaskBoolean == -1 )
            {
            	// not in edit mode 
                // Create ID for the Task by get currentTimeMillis of this moment 
                int nowUseAsId = (int) System.currentTimeMillis();
                if (nowUseAsId<0) nowUseAsId*=-1;

                // Text Message 
                String taskMessage = description.getText().toString();

                task = new Task(nowUseAsId, taskMessage, dateYear, dateMonth, dateDay, timeHour, timeMinute);
                System.out.println("********************************USUAL*********************************************************");
                System.out.println(timeHour+":"+timeMinute+" "+dateDay+"/"+dateMonth+"/"+dateYear+" -- "+taskMessage);

                singleton.getInstance(this).getArrayList().add(0, task);

                //-----continue checking from here -> to register date & cancel alarmManager after if click done
                saveToDb(task);            
            }
            else
            {
                // is in edit mode
                // try to update this task in DB

                // Text Message 
                String taskMessage = description.getText().toString();
                task = new Task(taskIdSelected, taskMessage, dateYear, dateMonth, dateDay, timeHour, timeMinute);
                System.out.println("********************************EDITED*********************************************************");
                System.out.println(timeHour+":"+timeMinute+" "+dateDay+"/"+dateMonth+"/"+dateYear+" -- "+taskMessage);
                updateTaskInDb(task);
                updateTaskInArray(task);
                
                // initialize
                editTaskBoolean = -1;
            }
            
            createAlarmAtDate(task);
            // initialize Task Message
            description.setText("");
            initialize_variables();

            updateListView();
        }
    }

    public void initialize_variables(){
        // initialize variables 
       
        // Time
        timeHour= -1;
        timeMinute= -1;
        // Date
        dateYear= -1;
        dateMonth= -1;
        dateDay= -1;

    }

    public void saveToDb(Task newTask){
        singleton.getInstance(this).getDb().addTask(newTask);
    }
    public void updateTaskInDb(Task editTask){
        singleton.getInstance(this).getDb().updateTask(editTask);
    }
    public void updateTaskInArray(Task editTask){
        for (int i=0 ; i< currentList.getCount();i++)
        {
            if (((Task)currentList.getItem(i)).getID()==editTaskBoolean)
            {
                ((Task)currentList.getItem(i)).setTaskMessage(editTask.getTaskMessage());
            }
        }
    }

    public void showDelteAlert(String title, String message, final View view)
    {

        android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(
                this);

        // set title
        alertDialogBuilder.setTitle(title);

        // set dialog message
        alertDialogBuilder
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // if this button is clicked, close
                        // current activity
                        ListView listView = (ListView) findViewById(R.id.listView);
                        int position = listView.getPositionForView(view);
                        Task selectedTask = (Task) listView.getItemAtPosition(position);
                        DeleteFromDb(selectedTask, position);
                        currentList.notifyDataSetChanged();

                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing

                        dialog.cancel();
                    }
                });

        // create alert dialog
        android.app.AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();

    }

    public void showEditAlert(String title, String message, final View view)
    {

        android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(
                this);

        // set title
        alertDialogBuilder.setTitle(title);

        // set dialog message
        alertDialogBuilder
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // if this button is clicked, close
                        // current activity
                        ListView listView = (ListView) findViewById(R.id.listView);
                        int position = listView.getPositionForView(view);
                        Task selectedTask = (Task) listView.getItemAtPosition(position);

                        currentList.notifyDataSetChanged();

                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing

                        dialog.cancel();
                    }
                });

        // create alert dialog
        android.app.AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }
}