package com.cs498MD.SportsRecorder;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class InputActivity extends Activity implements View.OnClickListener {

    // Match Utils
    private String matchId;
    private Period period;
    private final String MATCH = "match";
    private TextView lastAction;
    private TextView tutorial;
    private TextView matchName;
    private TextView myKid;

    // Teams
    private int kidOne;
    private int kidTwo;
    private int kidThree;
    private int kidMiss;

    // Period
    private int currPeriod;

    private TextView myScoreView;
    private TextView opponentScoreView;

    // Active match info
    private Match match;
    private int kidScore;
    private int othersScore;
    private int oppScore;
    private int prevKidScore;
    private int prevOthersScore;
    private int prevOppScore;

    private Stack<Action> history;

    //bottom sheet
    private BottomSheetBehavior bottomSheetBehavior;
    private LinearLayout linearLayoutBSheet;
    private ToggleButton tbUpDown;

    public ArrayList<String> actions = new ArrayList<>();
    private ArrayList<String> actionID = new ArrayList<>();
    private ActionListAdapter adapter;


    RecyclerView recyclerView;
    RecyclerViewAdapter mAdapter;

    ArrayList<String> stringArrayList = new ArrayList<>();
    CoordinatorLayout coordinatorLayout;


    List<Button> actionButtons = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_page);

        initViews();
        initMatchInfo();

        init_bottomsheet();


        tbUpDown.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

                }else{
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

                }
            }
        });

        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(View view, int newState) {
                if(newState == BottomSheetBehavior.STATE_EXPANDED){
                    tbUpDown.setChecked(true);
                    lastAction.setText("Game History");
                    tutorial.setText("(Swipe Left To Delete)");

                }else if(newState == BottomSheetBehavior.STATE_COLLAPSED){
                    tbUpDown.setChecked(false);
                    if(!period.getHistory().empty()){
                        lastAction.setText(period.getHistory().peek().message);
                        tutorial.setText("");
                    }
                    else{
                        lastAction.setText("Slide to view game history");
                        tutorial.setText("");
                    }

                }
            }

            @Override
            public void onSlide(View view, float v) {

            }
        });

//        adapter = new ActionListAdapter(actions,this, InputActivity.this);

//        ListView listView = (ListView) findViewById(R.id.matchList);
//        listView.setEmptyView(findViewById(R.id.noActions));
//        listView.setAdapter(adapter);

        recyclerView = findViewById(R.id.recyclerView);
        coordinatorLayout = findViewById(R.id.coordinatorLayout);

//        populateRecyclerView();
        enableSwipeToDeleteAndUndo();

    }



    /* ================================================================================================================================================= */
    /* ================================================================ Initialization ================================================================= */
    /* ================================================================================================================================================= */

    private void initViews() {
        // Game Util Views
        lastAction = findViewById(R.id.txtCantante);
        tutorial = findViewById(R.id.tutorial);
//        findViewById(R.id.undo).setOnClickListener(this);
        findViewById(R.id.end_game).setOnClickListener(this);
        findViewById(R.id.view_game_stats).setOnClickListener(this);

        matchName = findViewById(R.id.match_name);
        myScoreView = findViewById(R.id.my_score);
        opponentScoreView = findViewById(R.id.opp_score);
        myKid = findViewById(R.id.my_kid);

        // Others Views
        findViewById(R.id.other_free_throw).setOnClickListener(this);
        findViewById(R.id.other_two_ptr).setOnClickListener(this);
        findViewById(R.id.other_three_ptr).setOnClickListener(this);
        findViewById(R.id.other_miss).setOnClickListener(this);

        // Kid Views
        findViewById(R.id.myKid_free_throw).setOnClickListener(this);
        findViewById(R.id.myKid_two_ptr).setOnClickListener(this);
        findViewById(R.id.myKid_three_ptr).setOnClickListener(this);
        findViewById(R.id.myKid_miss).setOnClickListener(this);

        // Opponent Views
        findViewById(R.id.opp_free_throw).setOnClickListener(this);
        findViewById(R.id.opp_two_ptr).setOnClickListener(this);
        findViewById(R.id.opp_three_ptr).setOnClickListener(this);
        findViewById(R.id.opp_miss).setOnClickListener(this);

        // Periods
        findViewById(R.id.period_one).setOnClickListener(this);
        findViewById(R.id.period_two).setOnClickListener(this);
        findViewById(R.id.period_three).setOnClickListener(this);
        findViewById(R.id.period_four).setOnClickListener(this);
        findViewById(R.id.period_fourPlus).setOnClickListener(this);
    }

    private void initMatchInfo() {
        matchId = getIntent().getStringExtra("matchId");
        match = new Match(Integer.parseInt(matchId));
        match.setName(getIntent().getStringExtra("matchName"));
        matchName.setText(getIntent().getStringExtra("matchName"));


        initPeriodInfo(0);

        /* Assume we only enter inputActivity by clicking add button in mainActivity */

//        SharedPreferences sharedPreferences = getSharedPreferences(MATCH, MODE_PRIVATE);
//        Gson gson = new Gson();
//
//        String matchJson = sharedPreferences.getString(matchId, "");
//
//        if (matchJson == null || matchJson.equals("")) {
//            match = new Match(Integer.parseInt(matchId));
//        } else {
//            match = gson.fromJson(matchJson, Match.class);
//        }
    }

    private void savePeriodInfo() {
        Team kid = period.getKid();
        kid.setMiss(kidMiss);
        kid.setScore(kidScore - prevKidScore);
        kid.setOnePoint(kidOne);
        kid.setTwoPoint(kidTwo);
        kid.setThreePoint(kidThree);

        period.getOpponent().setScore(oppScore - prevOppScore);
        period.getOthers().setScore(othersScore - prevOthersScore);
    }

    private void initPeriodInfo(int idx) {
        if (idx > 0) {
            savePeriodInfo();
        }

        // SET BUTTON COLOR
        switch (idx) {
            case 0:
                currPeriod = R.id.period_one;
                break;
            case 1:
                currPeriod = R.id.period_two;
                break;
            case 2:
                currPeriod = R.id.period_three;
                break;
            case 3:
                currPeriod = R.id.period_four;
                break;
            case 4:
                currPeriod = R.id.period_fourPlus;
                break;
        }

        findViewById(currPeriod).setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.selected_period)));
        // END OF SET BUTTON COLOR

        period = match.getPeriods()[idx];
        period.getKid().setName(getIntent().getStringExtra("kidName"));
        myKid.setText(getIntent().getStringExtra("kidName"));

        prevOppScore = oppScore; prevKidScore = kidScore; prevOthersScore = othersScore;
        kidOne = 0; kidTwo = 0; kidThree = 0; kidMiss = 0;

        history = period.getHistory();
        if (history.isEmpty()) {
            lastAction.setText(getString(R.string.action_description));
        } else {
            lastAction.setText(history.peek().getMessage());
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.view_game_stats) {
            Intent intent = new Intent(this, GameStats.class);
            intent.putExtra("matchId", matchId);
            startActivity(intent);
        }
//        else if (v.getId() == R.id.undo && history.size() >= 1) {
//            undoLastAction();
//        }
        else if (v.getId() == R.id.end_game) {
            showAlertDialog(v);
        } else if (v.getId() == R.id.period_one) {
            findViewById(currPeriod).setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.periods)));
            initPeriodInfo(0);
        } else if (v.getId() == R.id.period_two) {
            findViewById(currPeriod).setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.periods)));
            initPeriodInfo(1);
        } else if (v.getId() == R.id.period_three) {
            findViewById(currPeriod).setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.periods)));
            initPeriodInfo(2);
        } else if (v.getId() == R.id.period_four) {
            findViewById(currPeriod).setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.periods)));
            initPeriodInfo(3);
        } else if (v.getId() == R.id.period_fourPlus) {
            findViewById(currPeriod).setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.periods)));
            initPeriodInfo(4);
        }

        else {
            switch (v.getId()) {
                case R.id.opp_free_throw:
                    oppScore += 1;
                    setLastAction(new Action(period.getOpponent().getName(), Type.Score, 1));
                    break;
                case R.id.opp_two_ptr:
                    oppScore += 2;
                    setLastAction(new Action(period.getOpponent().getName(), Type.Score, 2));
                    break;
                case R.id.opp_three_ptr:
                    oppScore += 3;
                    setLastAction(new Action(period.getOpponent().getName(), Type.Score, 3));
                    break;
                case R.id.opp_miss:
                    setLastAction(new Action(period.getOpponent().getName(), Type.Foul, 1));
                    break;
                case R.id.other_free_throw:
                    othersScore += 1;
                    setLastAction(new Action(period.getOthers().getName(), Type.Score, 1));
                    break;
                case R.id.other_two_ptr:
                    othersScore += 2;
                    setLastAction(new Action(period.getOthers().getName(), Type.Score, 2));
                    break;
                case R.id.other_three_ptr:
                    othersScore += 3;
                    setLastAction(new Action(period.getOthers().getName(), Type.Score, 3));
                    break;
                case R.id.other_miss:
                    setLastAction(new Action(period.getOthers().getName(), Type.Foul, 1));
                    break;
                case R.id.myKid_free_throw:
                    kidOne += 1;
                    kidScore += 1;
                    setLastAction(new Action(period.getKid().getName(), Type.Score, 1));
                    break;
                case R.id.myKid_two_ptr:
                    kidTwo += 2;
                    kidScore += 2;
                    setLastAction(new Action(period.getKid().getName(), Type.Score, 2));
                    break;
                case R.id.myKid_three_ptr:
                    kidThree += 3;
                    kidScore += 3;
                    setLastAction(new Action(period.getKid().getName(), Type.Score, 3));
                    break;
                case R.id.myKid_miss:
                    kidMiss += 1;
                    setLastAction(new Action(period.getKid().getName(), Type.Foul, 1));
                    break;
            }

            myScoreView.setText(String.valueOf(kidScore + othersScore));
            opponentScoreView.setText(String.valueOf(oppScore));
        }

        actions.clear();

        for(Action action : period.getHistory())
        {
            actions.add(action.message);
        }

        Collections.reverse(actions);
        populateRecyclerView();



        //delete button on click
//        Button deleteActionHistoryBtn = findViewById(R.id.action_delete_btn);
//        deleteActionHistoryBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                RecyclerView.ViewHolder viewHolder = mAdapter.onCreateViewHolder(recyclerView, 0);
//                final int position = viewHolder.getAdapterPosition();
//                final String item = mAdapter.getData().get(position);
//
//                mAdapter.removeItem(position);
//                final Action temp_action = history.get(position);
//                undoSpecificAction(history.get(position));
//
//
//
//
//
//                Snackbar snackbar = Snackbar
//                        .make(coordinatorLayout, "Action was deleted.", Snackbar.LENGTH_LONG);
//                snackbar.setAction("UNDO", new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//
//                        mAdapter.restoreItem(item, position);
//                        recyclerView.scrollToPosition(position);
//                        setLastAction(temp_action);
//                        if(temp_action.getTeamName().equals(period.getOthers().getName())){
//                            //opponent
//
//                            oppScore+= temp_action.point;
//                            opponentScoreView.setText(String.valueOf(oppScore));
//                        }
//                        else{
//
//                            kidScore += temp_action.point;
//
//                            myScoreView.setText(String.valueOf(kidScore + othersScore));
//                        }
//
//                    }
//                });
//
//                snackbar.setActionTextColor(Color.YELLOW);
//                snackbar.show();
//
//            }
//
//
//        });

//        adapter.notifyDataSetChanged();
    }

    /* ================================================================================================================================================= */
    /* ==================================================================== Actions ==================================================================== */
    /* ================================================================================================================================================= */
    private void undoLastAction() {
        Action action = history.pop();
        if (action.getType() == Type.Score) {
            if (action.getTeamName().equals(period.getOthers().getName())) {
                othersScore -= action.getPoint();
                myScoreView.setText(String.valueOf(othersScore + kidScore));
            } else if (action.getTeamName().equals(period.getOpponent().getName())) {
                oppScore -= action.getPoint();
                opponentScoreView.setText(String.valueOf(oppScore));
            } else {
                kidScore -= action.getPoint();
                myScoreView.setText(String.valueOf(othersScore + kidScore));

                switch (action.getPoint()) {
                    case 1:
                        kidOne--;
                    case 2:
                        kidTwo--;
                    case 3:
                        kidThree--;
                }
            }
        } else if (action.getType() == Type.Attempt && action.getTeamName().equals(period.getKid().getName())) {
            kidMiss--;
        }
        lastAction.setText(history.isEmpty() ? getString(R.string.action_description) : history.peek().getMessage());
    }


    private void undoSpecificAction(Action action, int position) {

        history.remove(position);
        if (action.getType() == Type.Score) {
            if (action.getTeamName().equals(period.getOthers().getName())) {
                othersScore -= action.getPoint();
                myScoreView.setText(String.valueOf(othersScore + kidScore));
            } else if (action.getTeamName().equals(period.getOpponent().getName())) {
                oppScore -= action.getPoint();
                opponentScoreView.setText(String.valueOf(oppScore));
            } else {
                kidScore -= action.getPoint();
                myScoreView.setText(String.valueOf(othersScore + kidScore));

                switch (action.getPoint()) {
                    case 1:
                        kidOne--;
                    case 2:
                        kidTwo--;
                    case 3:
                        kidThree--;
                }
            }
        } else if (action.getType() == Type.Attempt && action.getTeamName().equals(period.getKid().getName())) {
            kidMiss--;
        }
        lastAction.setText(history.isEmpty() ? getString(R.string.action_description) : history.peek().getMessage());
    }



    private void setLastAction(Action action) {
        history.push(action);
        lastAction.setText(action.getMessage());
    }

    /* ================================================================================================================================================= */
    /* ================================================================== END MATCH ==================================================================== */
    /* ================================================================================================================================================= */

    private void IsFinish(String alertmessage) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        InputActivity.super.onBackPressed();
                        android.os.Process.killProcess(android.os.Process.myPid());
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(alertmessage)
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    public void showAlertDialog (final View v) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage("Are you sure you want to end this match?");
        alert.setTitle("Sports Recorder");
        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                match.setDone(true);
                Intent intent = new Intent(InputActivity.this, GameStats.class);
                intent.putExtra("matchId", matchId);
                startActivity(intent);
            }
        });

        alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        alert.create().show();
    }

    /* ================================================================================================================================================= */
    /* ================================================================== SAVE MATCH =================================================================== */
    /* ================================================================================================================================================= */


    public void saveMatchInfo() {
        SharedPreferences sharedPreferences = getSharedPreferences(MATCH, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        savePeriodInfo();
        editor.putString(matchId, new Gson().toJson(match, Match.class));

        editor.commit();
//        editor.apply();
    }

    /* ================================================================================================================================================= */
    /* ============================================================== ANDROID LIFE CYCLE =============================================================== */
    /* ================================================================================================================================================= */

    @Override
    protected void onPause() {
        super.onPause();
        saveMatchInfo();
    }

    @Override
    public void onBackPressed() {
        IsFinish("Are you sure you want to end this match? \nYour game will not be saved.");
    }
    private void init_bottomsheet() {
        this.linearLayoutBSheet = findViewById(R.id.periodSheet);
        this.bottomSheetBehavior = BottomSheetBehavior.from(linearLayoutBSheet);
        this.tbUpDown = findViewById(R.id.toggleButton_black);

    }


    private void populateRecyclerView() {
        stringArrayList.clear();
        for (int i = 0; i < actions.size(); i++){
            stringArrayList.add(actions.get(i));

        }

        mAdapter = new RecyclerViewAdapter(stringArrayList);
        recyclerView.setAdapter(mAdapter);

    }

    private void enableSwipeToDeleteAndUndo() {

        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(this) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {



                final int position = viewHolder.getAdapterPosition();
                final String item = mAdapter.getData().get(position);
                mAdapter.removeItem(position);

                final Action temp_action = history.get(position);
                Log.e("TEST", "" + position);
                undoSpecificAction(history.get(position), position);



                Snackbar snackbar = Snackbar
                        .make(coordinatorLayout, "Action was deleted.", Snackbar.LENGTH_LONG);
                snackbar.setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        mAdapter.restoreItem(item, position);
                        recyclerView.scrollToPosition(position);
                        setLastAction(temp_action);
                        if(temp_action.getTeamName().equals(period.getOpponent().getName())){
                            //opponent

                            oppScore+= temp_action.point;
                            Log.e("TEST", "" + temp_action.point);
                            history.insertElementAt(temp_action, position);
                            opponentScoreView.setText(String.valueOf(oppScore));
                        }

                        else{

                            kidScore += temp_action.point;

                            myScoreView.setText(String.valueOf(kidScore + othersScore));
                        }

                    }
                });

                snackbar.setActionTextColor(Color.YELLOW);
                snackbar.show();

            }
        };

        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchhelper.attachToRecyclerView(recyclerView);
    }


}
