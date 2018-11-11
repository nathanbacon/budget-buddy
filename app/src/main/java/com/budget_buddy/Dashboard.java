package com.budget_buddy;

import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.budget_buddy.animations.ExperienceBarAnimation;
import com.budget_buddy.charts.GoalProgressBar;

import com.budget_buddy.utils.Data.MyCallback;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.google.firebase.database.DataSnapshot;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Dashboard extends AppCompatActivity {

    BBUser currentUser = BBUser.GetInstance();

    ProgressBar experienceBar;
    TextView experienceProgressText;
    ExperienceBarAnimation experienceBarAnimation;

    HorizontalBarChart progressBar;
    BarChart chart;
    final List<BarEntry> entries = new ArrayList<BarEntry>();
    TextView progressBarDescription;

    // Here's a more permanent home for the callback
    MyCallback callback = new MyCallback() {
        @Override
        public void OnCallback(float [] weeklySpending) {
            chart.clear();
            for(int i = 0; i < 7; i++) {
                entries.add(new BarEntry(i, weeklySpending[6-i]));
            }
            BarDataSet dataSet = new BarDataSet(entries, "");
            BarData barData = new BarData(dataSet);
            barData.setBarWidth(0.85f);
            chart.setData(barData);
            chart.setFitBars(true);
            chart.invalidate();
        }

        @Override
        public void OnCallback(HashMap<String, Object> map) {

        }

        @Override
        public void OnProfileSet() {
            if(ProfileNeedsSetup()) {
                // display profile setup dialog
                Log.i("Profile", "needs set up");
            } else {
                SetExperience(currentUser.getBudgetScore());
                SetSavingsGoal(currentUser.getSavingsGoal());
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        currentUser.setUserInterfaceCallback(callback);
        setupExperienceBar();
        addChart();
        addProgressBar();
    }

    private void setupExperienceBar() {
        experienceBar = findViewById(R.id.experienceBar);
        experienceBar.setMax(2500);
        experienceProgressText = findViewById(R.id.experienceProgessFraction);
        experienceBarAnimation = new ExperienceBarAnimation(experienceBar, experienceProgressText);
        experienceBarAnimation.setProgress(1675);
    }

    public void gotoEntryMethodFor(View view) {
        if(view.getId() == R.id.manualEntry) {
            Intent manualEntryIntent = new Intent(this, ManualEntry.class);
            startActivity(manualEntryIntent);
        } else if (view.getId() == R.id.cameraEntry) {
            Intent cameraEntryIntent = new Intent(this, PhotoEntry.class);
            startActivity(cameraEntryIntent);
        } else if (view.getId() == R.id.profileImageButton) {
            Intent userProfileViewIntent = new Intent(this, UserProfileActivity.class);
            startActivity(userProfileViewIntent);
        } else {
            return;
        }
    }

    private void SetExperience(long experience) {

    }

    private void SetSavingsGoal(long goal) {

    }

    private boolean ProfileNeedsSetup() {
        // check if any one of these is negative, for now rent is required
        if(currentUser.getPrimaryIncome() < 0 || currentUser.getRent() < 0 || currentUser.getSavingsGoal() < 0) {
            return true;
        }
        // might add other checks later

        return false;
    }

    private void addProgressBar() {
        // create description view
        progressBarDescription = new TextView(this);
        progressBarDescription.setId(R.id.progress_bar_description);
        progressBarDescription.setText(this.getString(R.string.goal, 300 - 235));

        GoalProgressBar progressBar = new GoalProgressBar(this);
        progressBar.setId(R.id.progress_bar_view);

        ConstraintLayout cl = findViewById(R.id.dataBreakdownLayout);
        cl.addView(progressBar, 0, 200);
        cl.addView(progressBarDescription, 0, 50);

        ConstraintSet constraintSet = new ConstraintSet();

        constraintSet.clone(cl);
        // constrain bar to bottom and sides
        constraintSet.connect(progressBar.getId(), ConstraintSet.LEFT, cl.getId(),ConstraintSet.LEFT, 8);
        constraintSet.connect(progressBar.getId(), ConstraintSet.RIGHT, cl.getId(),ConstraintSet.RIGHT, 8);
        constraintSet.connect(progressBar.getId(),ConstraintSet.BOTTOM, cl.getId(),ConstraintSet.BOTTOM, 0);
        // constrain description to bar and sides
        constraintSet.connect(progressBarDescription.getId(),ConstraintSet.LEFT, progressBar.getId(), ConstraintSet.LEFT,0);
        constraintSet.connect(progressBarDescription.getId(),ConstraintSet.RIGHT, progressBar.getId(), ConstraintSet.RIGHT,0);
        constraintSet.connect(progressBarDescription.getId(), ConstraintSet.BOTTOM, progressBar.getId(), ConstraintSet.TOP, 0);
        constraintSet.applyTo(cl);

        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, 235));
        BarDataSet barDataSet = new BarDataSet(entries, "");

        // set colors
        barDataSet.setColor(getResources().getColor(R.color.colorPrimary, this.getTheme()));
        barDataSet.setBarBorderColor(getResources().getColor(R.color.colorPrimaryDark, this.getTheme()));
        barDataSet.setBarBorderWidth(2.5f);

        BarData barData = new BarData(barDataSet);
        barData.setDrawValues(false);

        progressBar.setData(barData);
        progressBar.setFitBars(true);

        progressBar.setGoal(300);

        // remove legend
        progressBar.getLegend().setEnabled(false);
        // remove description
        progressBar.getDescription().setEnabled(false);

        progressBar.animateY(getResources().getInteger(R.integer.dashboard_animation_time), Easing.EasingOption.EaseInOutExpo);
    }

    private void addChart() {
        chart = new BarChart(this);
        chart.setId(R.id.bar_graph_view);

        ConstraintLayout cl = (ConstraintLayout) findViewById(R.id.dataGraphLayout);
        cl.addView(chart,0,0);

        ConstraintSet constraintSet = new ConstraintSet();

        constraintSet.clone(cl);
        constraintSet.connect(chart.getId(), ConstraintSet.LEFT, cl.getId(),ConstraintSet.LEFT, 8);
        constraintSet.connect(chart.getId(), ConstraintSet.RIGHT, cl.getId(),ConstraintSet.RIGHT, 8);
        constraintSet.connect(chart.getId(),ConstraintSet.BOTTOM, cl.getId(),ConstraintSet.BOTTOM, 0);
        constraintSet.connect(chart.getId(), ConstraintSet.TOP, cl.getId(), ConstraintSet.TOP, 0);
        constraintSet.applyTo(cl);

        // This is initializing the bars to 0 since we do not have data from Firebase yet.
        for(int i = 0; i < 7; i++) {
            entries.add(new BarEntry(i, 0));
        }

        currentUser.GetWeeklySpending(callback);

        BarDataSet dataSet = new BarDataSet(entries, "");

        // set colors
        dataSet.setColor(getResources().getColor(R.color.colorPrimary, this.getTheme()));
        dataSet.setBarBorderColor(getResources().getColor(R.color.colorPrimaryDark, this.getTheme()));
        dataSet.setBarBorderWidth(2.5f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.85f);

        chart.setData(barData);
        chart.setFitBars(true);
        IValueFormatter valueFormatter = new IValueFormatter() {

            private DecimalFormat mFormat = new DecimalFormat("###,###,##0.00");

            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                return "$" + mFormat.format(value);
            }
        };

        barData.setValueFormatter(valueFormatter);

        // disable touch gestures
        chart.setDragEnabled(false);
        chart.setScaleEnabled(false);
        chart.setTouchEnabled(false);

        // remove label
        chart.getDescription().setEnabled(false);

        // make the daily allowance line
        LimitLine dailyAllowance = new LimitLine(4.0f, "Daily allowance");
        dailyAllowance.setLineColor(getResources().getColor(R.color.colorAccent, this.getTheme()));
        chart.getAxisLeft().addLimitLine(dailyAllowance);

        // don't show the grid
        chart.getXAxis().setDrawGridLines(false);
        chart.getAxisLeft().setDrawGridLines(false);
        chart.getAxisRight().setEnabled(false);

        // draw labels on bottom
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        IAxisValueFormatter axisValueFormatter = new IAxisValueFormatter() {

            private String[] days = getResources().getStringArray(R.array.day_abbreviations);

            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                Calendar calendar = Calendar.getInstance();
                // this graph will only show previous 7 days
                int today = calendar.get(Calendar.DAY_OF_WEEK);
                return days[((int) value + today) % 7];
            }
        };

        chart.getXAxis().setValueFormatter(axisValueFormatter);
        // set the bottom of the window to y=0
        chart.getAxisLeft().setAxisMinimum(0);

        chart.getLegend().setEnabled(false);

        //chart.animateX(2000);
        chart.animateY( 2000, Easing.EasingOption.EaseInOutExpo);
    }
}
