package com.example.workouttimerinterval;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private EditText workInputHours, workInputMinutes, workInputSeconds, setsInput;
    private EditText restInputHours, restInputMinutes, restInputSeconds;
    private TextView helperText, countDownText, setStatus;
    private Button btn_start, btn_stop;
    int hoursLeft, minutesLeft, secondsLeft;
    int totalSecondsLeft, CurrentProgress = 1,  totalSets;
    private ProgressBar progressBar;
    int numCycles = 0, workPeriod = 0, restPeriod = 0;
    boolean rest = false, work = false, timerInitiated = false;
    private CountDownTimer workTimer, restTimer;
    private MediaPlayer music;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //user inputs work
        workInputHours = findViewById(R.id.workInputHours);
        workInputMinutes = findViewById(R.id.workInputMinutes);
        workInputSeconds = findViewById(R.id.workInputSeconds);

        //user inputs rest
        restInputHours = findViewById(R.id.restInputHours);
        restInputMinutes = findViewById(R.id.restInputMinutes);
        restInputSeconds = findViewById(R.id.restInputSeconds);

        //sets, progress, visual timer, helper text
        setsInput = findViewById(R.id.setsInput);
        setStatus = findViewById(R.id.setStatus);
        progressBar = findViewById(R.id.progressBar);
        countDownText = findViewById(R.id.countDownText);
        helperText = findViewById(R.id.helperText);

        // buttons
        btn_start = findViewById(R.id.btn_start);
        btn_stop = findViewById(R.id.btn_stop);

        //set texts
        helperText.setText("COUNT DOWN READY");

        startTimer(); //start timer
    }
    private void startTimer() {
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Media player
                // Sound Effect from <a href="https://pixabay.com/sound-effects/?utm_source=link-attribution&amp;utm_medium=referral&amp;utm_campaign=music&amp;utm_content=96243">Pixabay</a>
                MediaPlayer music = MediaPlayer.create(MainActivity.this, R.raw.beep1);

                //check work is not empty
                ResetInputValuesIfEmpty();

                //get user input values work
                workPeriod += Integer.parseInt(workInputHours.getText().toString()) * 60 * 60 * 1000;
                workPeriod += Integer.parseInt(workInputMinutes.getText().toString()) * 60 * 1000;
                workPeriod += Integer.parseInt(workInputSeconds.getText().toString()) * 1000;

                //get user input values rest
                restPeriod += Integer.parseInt(restInputHours.getText().toString()) * 60 * 60 * 1000;
                restPeriod += Integer.parseInt(restInputMinutes.getText().toString()) * 60 * 1000;
                restPeriod += Integer.parseInt(restInputSeconds.getText().toString()) * 1000;

                progressBar.setMax(restPeriod + workPeriod);
                //check sets is not empty
                if(TextUtils.isEmpty(setsInput.getText().toString()))
                {
                    Toast.makeText(getApplicationContext(),"Enter Sets",Toast.LENGTH_LONG).show();
                    return;
                }

                //EditText is not empty -1 to account for first set
                int sets = Integer.parseInt(setsInput.getText().toString()) -1;

                //initiate Timer for checker when resetting values
                timerInitiated = true;

                // set progress bar method
                setProgressBarValues();

                // set status
                totalSets = sets + 1;
                setStatus.setText("Set " + CurrentProgress + "/" + totalSets);

                // Initialize CountDownTimers for each period
                // work timer
                //progressBar.setMax(workPeriod);
                workTimer = new CountDownTimer(workPeriod, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        updateTimeRemaining(millisUntilFinished);

                        //change background colour for helper text
                        helperText.setTextColor(Color.parseColor("#FF0000"));
                        helperText.setText("WORK");

                        // update status text
                        setStatus.setText("Set " + CurrentProgress + "/" + totalSets);

                        // Update progress bar
                        progressBar.setProgress((int)millisUntilFinished);

                    }
                    @Override
                    public void onFinish() {
                        // Media player
                        music.start();
                        Log.d("CountDownTimer", "Work period finished");
                        if (numCycles <= sets) {
                            numCycles++;
                            CurrentProgress++;
                            rest = true;
                            work = false;
                            setProgressBarValues();
                            //change background colour for helpertext
                            helperText.setTextColor(Color.parseColor("#0000FF"));
                            helperText.setText("REST");
                            restTimer.start();

                        } else {
                            finishTimer("Count down complete");
                        }
                    }
                };
                //progressBar.setMax(restPeriod);
                // rest timer
                restTimer = new CountDownTimer(restPeriod, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        updateTimeRemaining(millisUntilFinished);
                        setProgressBarValues(); // set progress bar values

                        // Update progress bar
                        progressBar.setProgress((int)millisUntilFinished);
                    }

                    @Override
                    public void onFinish() {
                        // Media player
                        music.start();
                        Log.d("CountDownTimer", "Rest period finished");
                        if (numCycles <= sets) {
                            rest = false;
                            work = true;
                            setProgressBarValues(); //reset progress bar values
                            Log.d("CountDownTimer", "Set");

                            //change background colour for helper text
                            helperText.setTextColor(Color.parseColor("#FF0000"));
                            helperText.setText("WORK");
                            workTimer.start();

                        } else {
                            // update status text
                            helperText.setTextColor(Color.parseColor("#FF018786"));
                            finishTimer("Count down finished!");

                            rest = false;
                            work = false;
                        }
                    }
                };
                restTimer.cancel(); // Keep rest timer paused initially
                numCycles = 0; // Initialize cycle count
                btn_start.setEnabled(false);
                btn_stop.setEnabled(true);
                workTimer.start(); // Start the first timer
            }
        });
        // button stop is pressed
        btn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Enable the start button and disable the stop button
                workTimer.cancel();
                restTimer.cancel();
                btn_start.setEnabled(true);
                btn_stop.setEnabled(false);

                //change background colour for helper text
                finishTimer("Work Out Timer Cancelled");
            }
        });
    }

    // reset user input values to 00
    private void ResetInputValuesIfEmpty() {
        //check work is not empty
        if(TextUtils.isEmpty(workInputHours.getText().toString())) { workInputHours.setText("00"); }
        if(TextUtils.isEmpty(workInputMinutes.getText().toString())) { workInputMinutes.setText("00"); }
        if(TextUtils.isEmpty(workInputSeconds.getText().toString())) { workInputSeconds.setText("00"); }

        //check rest is not empty
        if(TextUtils.isEmpty(restInputHours.getText().toString())) { restInputHours.setText("00"); }
        if(TextUtils.isEmpty(restInputMinutes.getText().toString())) { restInputMinutes.setText("00"); }
        if(TextUtils.isEmpty(restInputSeconds.getText().toString())) { restInputSeconds.setText("00"); }
    }

    // clear user inputs
    private void ClearUserInput() {
        // clear set input
        setsInput.getText().clear();
        // clear work times
        workInputHours.getText().clear();
        workInputMinutes.getText().clear();
        workInputSeconds.getText().clear();

        // clear rest times
        restInputHours.getText().clear();
        restInputMinutes.getText().clear();
        restInputSeconds.getText().clear();

        // clear timer
        countDownText.setText("00:00:00");

        // set status
        CurrentProgress = 1; // reset counter
        setStatus.setText("Set " + CurrentProgress + "/" + 1); // update sets
    }

    // check progress bar for rest or work periods
    private void setProgressBarValues() {
        if (rest) {
            progressBar.setMax(restPeriod);
        } else { progressBar.setMax(workPeriod); }
    }
    private void finishTimer(String message) {
        helperText.setTextColor(Color.parseColor("#FF018786"));
        helperText.setText(message); //end message
        btn_start.setEnabled(true);
        btn_stop.setEnabled(false);
        ClearUserInput();

        // clear progress bar and setStatus
        progressBar.setProgress(0); //set progress bar 0
        setStatus.setText("Set " + totalSets + "/" + totalSets); // update sets

        // cancel timers to allow new start
        workTimer.cancel();
        restTimer.cancel();
    }

    // update time in each onTick() method for count down timer
    private void updateTimeRemaining(long millisUntilFinished) {
        totalSecondsLeft = (int) millisUntilFinished / 1000;
        hoursLeft = totalSecondsLeft / 3600;
        minutesLeft = (totalSecondsLeft % 3600) / 60;
        secondsLeft = totalSecondsLeft % 60;

        // Update progress bar
        progressBar.setProgress((int)millisUntilFinished/1000);

        // set to text
        countDownText.setText(String.format("%02d",hoursLeft)+":"+String.format("%02d",minutesLeft)+":"+String.format("%02d",secondsLeft));
    }

    protected void onDestroy() {
        super.onDestroy();
        // cancel timers
        workTimer.cancel();
        restTimer.cancel();

        //cancel media player
        music.release();
        music = null;
    }

}
