package com.cs498MD.SportsRecorder;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import info.hoang8f.widget.FButton;

public class InputActivityOrig extends Activity implements View.OnClickListener {

    // Match Util
    private String matchId;
    private Period period;
    private final String MATCH = "match";
    private TextView lastAction;
    private TextView totalScore;

    // Teams
    private TextView myNameView;
    private TextView myScoreView;
    private Button foulBtn;
    private HashMap<Integer, Integer> totalFailAttempts;
    private TextView opponentNameView;
    private TextView opponentScoreView;
    private Button opponentAddBtn;

    // Active match info
    private int totalMyScore = 0;
    private int totalOpponentScore = 0;
    private Match match;
    private int foulCount;
    private int myScore;
    private int opponentScore;
    private Stack<Action> history;
    private ArrayList<Player> players;
    private Player player;

    private Player current_player;

    //ArrayList for dynamically assigning color to player
    private ArrayList<Integer> player_button_colors;
    private int playerCount;


    private ArrayList<Integer> periodBtnIds;

    private String periodUniqueId;
    private int periodNo;

    private String playerUniqueId;
    private int playerNo;

    private FButton periodAddBtn;
    private FButton playerAddBtn;

    private FButton teamBtn;

    private FButton endMatchBtn;
    private int onePoint;
    private int twoPoint;
    private int threePoint;

    private FButton lastClickedBtnInstance;

    @Override
    protected void onStop() {
        super.onStop();
        saveMatchInfo();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveMatchInfo();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_page);

        matchId = getIntent().getStringExtra("matchId");
        periodBtnIds = new ArrayList<>();

        playerCount = 0;
        playerUniqueId = "Clicking Player ";
        totalFailAttempts = new HashMap<>();

        //set team button (the first player head button's on click listener)
//        teamBtn = findViewById(R.id.player_head);
//        teamBtn.setId(playerNo);
//        teamBtn.setOnClickListener(this);
//        teamBtn.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                // TODO Auto-generated method stub
//                showNameChangeDialog(v, teamBtn);
//                return true;
//            }
//        });


        //set end match button onclick listener
        endMatchBtn = findViewById(R.id.end_match);
        endMatchBtn.setOnClickListener(this);

        totalScore = findViewById(R.id.total_score);

        setUpAttemptsMap();

        setMatchUtils();
        setMyTeam();
        setOpponentTeam();

        initMatchInfo();

//        //set dynamic add button for period
////        periodAddBtn = findViewById(R.id.period_add);
////        periodAddBtn.setOnClickListener(this);
//
//        //set dynamic add button for player
//        playerAddBtn = findViewById(R.id.player_add);
//        playerAddBtn.setOnClickListener(this);
//
//        teamBtn.setButtonColor(getResources().getColor(player_button_colors.get(0)));


    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.player_add){

//            addPlayerButton(playerCount);

        } else if (v.getId() == R.id.opponent_add) {
            totalScore.setText(totalMyScore + " : " + ++totalOpponentScore);

            opponentScoreView.setText(String.valueOf("Score: " + ++opponentScore));
            setLastAction(new Action(period.getOpponentTeam().getName(), Type.Score, 1));

        } else if (v.getId() == R.id.undo && history.size() >= 1) {
            //TODO: change click to warn user undo need to be onclicked
            undoLastAction();
        } else if (v.getId() == R.id.foul_btn) {
            if (player != null)
                player.setFoulCount(player.getFoulCount() + 1);
            foulCount++;
            setLastAction(new Action(player.getName(), Type.Foul, null));
        } else if (v.getId() == R.id.player_head) {
            player = null;
            teamBtn.setButtonColor(getResources().getColor(player_button_colors.get(1)));
            if(lastClickedBtnInstance!=null){

                lastClickedBtnInstance.setButtonColor(getResources().getColor(player_button_colors.get(0)));

            }


        } else if (v.getId() == R.id.end_match){
            showAlertDialog(v);
            Log.e("TEST", "Clicked End Mathc");

        } else if (v.getId() == R.id.next_period) {
            match.getPeriods().push(new Period());
            saveMatchInfo();
            initPeriodInfo();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveMatchInfo();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        IsFinish("Are you sure you want to end this match?");
    }

    private void IsFinish(String alertmessage) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        InputActivityOrig.super.onBackPressed();
                        android.os.Process.killProcess(android.os.Process.myPid());
                        // This above line close correctly
                        //finish();
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
                Toast.makeText(getApplicationContext(), "Match Ended and Saved!", Toast.LENGTH_LONG);
                Log.e("TEST", "Dialog Yes Clicked");

                Intent intent = new Intent(InputActivityOrig.this, GameStats.class);
                intent.putExtra("matchId", matchId);
                startActivity(intent);
            }
        });

        alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.e("TEST", "Dialog No Clicked");

            }
        });

        alert.create().show();
    }




    private void undoLastAction() {
        Action action = history.pop();
        if (action.getType() == Type.Score) {
            if (action.getTeamName().equals(period.getMyTeam().getName())) {
                myScore -= action.getPoint();
                totalMyScore -= action.getPoint();

                totalScore.setText(totalMyScore + " : " + totalOpponentScore);
                myScoreView.setText("Score: " + String.valueOf(myScore));
            } else {
                totalOpponentScore -= action.getPoint();
                opponentScore -= action.getPoint();

                totalScore.setText(totalMyScore + " : " + totalOpponentScore);
                opponentScoreView.setText("Score: " + String.valueOf(opponentScore));
            }


        } else if (action.getType() == Type.Attempt) {
            //TODO: change player count
            int score = action.getPoint();
            totalFailAttempts.put(score, totalFailAttempts.get(score) - 1);
        } else {
            //TODO: change player count
            foulCount--;
        }
        lastAction.setText(history.isEmpty() ? "" : history.peek().getMessage());
    }

    private void setLastAction(Action action) {
        history.push(action);
        lastAction.setText(action.getMessage());
    }

    public static int convertDipToPixels(float dips, Context context)
    {
        return (int) (dips * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    private void setMatchUtils() {
        lastAction = findViewById(R.id.last_action);
        findViewById(R.id.undo).setOnClickListener(this);

        findViewById(R.id.undo).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //TODO: do the undo work here;
                return false;
            }
        });

        findViewById(R.id.next_period).setOnClickListener(this);

    }

    private void setMyTeam() {
//        myOnePointMade = findViewById(R.id.my_made_one_ptr);


        myNameView = (TextView) findViewById(R.id.my_name);
        myScoreView = (TextView) findViewById(R.id.my_score);
        foulBtn = (Button) findViewById(R.id.foul_btn);
        foulBtn.setOnClickListener(this);

    }

    private void setOpponentTeam() {

        opponentNameView = (TextView) findViewById(R.id.opponent_name);
        opponentScoreView = (TextView) findViewById(R.id.opponent_score);
        opponentAddBtn = (Button) findViewById(R.id.opponent_add);
        opponentAddBtn.setOnClickListener(this);
    }

    private void initMatchInfo() {
        SharedPreferences sharedPreferences = getSharedPreferences(MATCH, MODE_PRIVATE);
        String matchJson = sharedPreferences.getString(matchId, "");

        Gson gson = new Gson();

        if (matchJson == null || matchJson.equals("")) {
            match = new Match(Integer.parseInt(matchId));
        } else {
            match = gson.fromJson(matchJson, Match.class);
        }

        TextView matchName = findViewById(R.id.match_name);
        matchName.setText(match.getName() + " : Period" + (match.getPeriods().size() + 1));
        totalScore.setText(totalMyScore + " : " + totalOpponentScore);

        initPeriodInfo();
    }

    private void initPeriodInfo() {
        period = match.getPeriods().peek();

        TextView matchName = findViewById(R.id.match_name);
        matchName.setText(match.getName() + " : Period " + (match.getPeriods().size()));

        MyTeam myTeam = period.getMyTeam();
        OpponentTeam opponentTeam = period.getOpponentTeam();
        if (players == null) {
            players = myTeam.getPlayers();
        }

        onePoint = 0;
        twoPoint = 0;
        threePoint = 0;

        history = period.getHistory();
        myScore = myTeam.getScore();
        opponentScore = opponentTeam.getScore();

        myNameView.setText(myTeam.getName());
        myScoreView.setText("Score: " + String.valueOf(myScore));
        opponentNameView.setText(opponentTeam.getName());
        opponentScoreView.setText("Score: " + String.valueOf(opponentScore));


        if (history.isEmpty()) {
            lastAction.setText("");
        } else {
            lastAction.setText(history.peek().getMessage());
        }
    }

    public void saveMatchInfo() {
        SharedPreferences sharedPreferences = getSharedPreferences(MATCH, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        period.setHistory(history);
        MyTeam myTeam = period.getMyTeam();
        OpponentTeam opponentTeam = period.getOpponentTeam();
        myTeam.setScore(myScore);
        opponentTeam.setScore(opponentScore);

        myTeam.setOnePoint(onePoint);
        myTeam.setTwoPoint(twoPoint);
        myTeam.setThreePoint(threePoint);

        myTeam.setOnePointAttempt(totalFailAttempts.get(1));
        myTeam.setTwoPointAttempt(totalFailAttempts.get(2));
        myTeam.setThreePointAttempt(totalFailAttempts.get(3));
        myTeam.setFoulCount(foulCount);

        Log.d("INPUT DEBUG PLAYERS", players.toString());
        myTeam.setPlayers(players);

        editor.putString(matchId, new Gson().toJson(match, Match.class));

        editor.commit();
    }

    private void setUpAttemptsMap(){
        totalFailAttempts.put(1,0);
        totalFailAttempts.put(2,0);
        totalFailAttempts.put(3,0);
    }

//    public void initPlayerButtonColor(){
//        player_button_colors = new ArrayList<>();
//        player_button_colors.add(R.color.yellow);
//        player_button_colors.add(R.color.fbutton_color_carrot);
//        player_button_colors.add(R.color.fbutton_color_pumpkin);
//        player_button_colors.add(R.color.fbutton_color_pomegranate);
//    }

//    public void showNameChangeDialog (final View v, final FButton myButton){
//        AlertDialog.Builder alert = new AlertDialog.Builder(this);
//        final EditText edittext = new EditText(InputActivity.this);
//        alert.setMessage("Enter Player Initial");
//        alert.setTitle("Sports Recorder");
//
//        alert.setView(edittext);
//
//        alert.setPositiveButton("Change Name", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int whichButton) {
//                //What ever you want to do with the value
////                Editable YouEditTextValue = edittext.getText();
//                //OR
//                String editTextValue = edittext.getText().toString();
//
//                if(editTextValue.length() > 2){
//                    //limit user input to 2
//                    Toast.makeText(InputActivity.this, "Please Enter Name Initial No More Than Two Words", Toast.LENGTH_LONG).show();
//                }
//                else{
//                    myButton.setText(editTextValue);
//                    players.get(myButton.getId()).setName(editTextValue);
//                    Log.e("TEST", editTextValue);
//                }
//
//            }
//        });
//
//        alert.setNegativeButton("Delete Player", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int whichButton) {
//                // what ever you want to do with No option.
//                Toast.makeText(getApplicationContext(), "Player Deleted", Toast.LENGTH_LONG).show();
//                ((ViewGroup) myButton.getParent()).removeView(myButton);
//                playerCount--;
//            }
//        });
//
//        alert.show();
//
//    }

//    private FButton addPeriodButton(){
//        final FButton myButton = new FButton(this );
//        myButton.setButtonColor(getResources().getColor(R.color.fbutton_color_wet_asphalt));
//        myButton.setMinHeight(R.dimen.button_min_height);
//        myButton.setMinWidth(R.dimen.button_min_width);
//        myButton.setShadowEnabled(true);
//        myButton.setTextColor(getResources().getColor(R.color.classic_white));
//        myButton.setShadowHeight(12);
//        myButton.setCornerRadius(20);
//        myButton.setText("New");
//        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(convertDipToPixels(48,InputActivity.this), convertDipToPixels(48,InputActivity.this) );
//        myButton.setLayoutParams(lp);
//        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) myButton.getLayoutParams();
//        params.width = convertDipToPixels(48,InputActivity.this);
//        params.height = convertDipToPixels(48,InputActivity.this);
//        params.setMarginStart(convertDipToPixels(8,InputActivity.this));
//        params.setMarginEnd(convertDipToPixels(8,InputActivity.this));
//        params.topMargin = convertDipToPixels(8,InputActivity.this);
//        params.bottomMargin = convertDipToPixels(8,InputActivity.this);
//        periodNo++;
//        myButton.setTag(periodUniqueId + periodNo);
//        myButton.setText("PP" + periodNo);
//
//        myButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(getApplicationContext(), (String)myButton.getTag(), Toast.LENGTH_LONG).show();
//
//            }
//        });
//        return myButton;
//    }

//    private FButton addPlayerButton(int player_num){
//        if(player_num < 4){
//            final FButton myButton = new FButton(this );
//            myButton.setButtonColor(getResources().getColor(player_button_colors.get(0)));//player_num
//            myButton.setMinHeight(R.dimen.button_min_height);
//            myButton.setMinWidth(R.dimen.button_min_width);
//            myButton.setShadowEnabled(true);
//            myButton.setTextColor(getResources().getColor(R.color.classic_white));
//            myButton.setShadowHeight(12);
//            myButton.setCornerRadius(20);
//            myButton.setText("New");
//            final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(convertDipToPixels(48,InputActivity.this), convertDipToPixels(48,InputActivity.this) );
//            myButton.setLayoutParams(lp);
//            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) myButton.getLayoutParams();
//            params.width = convertDipToPixels(48,InputActivity.this);
//            params.height = convertDipToPixels(48,InputActivity.this);
//            params.setMarginStart(convertDipToPixels(8,InputActivity.this));
//            params.setMarginEnd(convertDipToPixels(8,InputActivity.this));
//            params.topMargin = convertDipToPixels(8,InputActivity.this);
//            params.bottomMargin = convertDipToPixels(8,InputActivity.this);
//            LinearLayout ll = (LinearLayout)findViewById(R.id.button_player_layout);
//            ll.addView(myButton, lp);
//            ll.removeView(playerAddBtn);
//            ll.addView(playerAddBtn, lp);
//            //        myButton.setTag(playerUniqueId + playerNo);
//            myButton.setId(playerCount++);
//            players.add(new Player("P" + playerCount));
//            myButton.setText("P" + playerCount);
//            //        myButton.setBackgroundColor(player_button_colors.get(player_num));
//            myButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    player = players.get(myButton.getId());
//                    if(lastClickedBtnInstance !=null){
//                        //save instance
//                        lastClickedBtnInstance.setButtonColor(getResources().getColor(player_button_colors.get(0)));
//                    }
//                    myButton.setButtonColor(getResources().getColor(player_button_colors.get(1)));
//                    lastClickedBtnInstance = myButton;
//                }
//            });
//            myButton.setOnLongClickListener(new View.OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View v) {
//                    // TODO Auto-generated method stub
//                    showNameChangeDialog(v, myButton);
//                    return true;
//                }
//            });
//
//            return myButton;
//        }
////        Toast.makeText(getApplicationContext(), "You can only add up to 5 players!", Toast.LENGTH_LONG);
//        return null;
//
//    }

//    swipeAnimationButton = (SwipeAnimationButton) findViewById(R.id.swipe_btn);
//    swipeAnimationButton.setPicture(R.drawable.ic_num1);
//    swipeAnimationButton.setOnSwipeAnimationListener(new SwipeAnimationListener() {
//        @Override
//        public void onSwiped(boolean isRight) {
//            if (isRight) {
//                if (player != null) {
//                    player.setScore(player.getScore() + 1);
//                    player.setOnePoint(player.getOnePoint() + 1);
//                }
//                totalScore.setText(++totalMyScore + " : " + totalOpponentScore);
//                myScoreView.setText("Score: " + String.valueOf(++myScore));
//                onePoint++;
//                if(player == null){
//                    setLastAction(new Action(period.getMyTeam().getName(), Type.Score, 1));
//                }
//                else{
//                    setLastAction(new Action(player.getName(), Type.Score, 1));
//                }
//                final Handler handler = new Handler();
//                handler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        swipeAnimationButton.collapseButton();
//                    }
//                }, 400);
//            } else {
//                final Handler handler = new Handler();
//                handler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        swipeAnimationButton.collapseButton();
//                    }
//                }, 400);
//                if(player == null){
//                    setLastAction(new Action(period.getMyTeam().getName(), Type.Attempt, 1));
//                }
//                else{
//                    setLastAction(new Action(player.getName(), Type.Attempt, 1));
//                }
//                totalFailAttempts.put(1, totalFailAttempts.get(1) + 1);
//                if (player != null) {
//                    player.setOnePointAttempt(player.getOnePointAttempt() + 1);
//                }
//            }
//        }
//    });
//    swipeAnimationButton2 = (SwipeAnimationButton) findViewById(R.id.swipe_btn2);
//    swipeAnimationButton2.setPicture(R.drawable.ic_num2);
//    swipeAnimationButton2.setOnSwipeAnimationListener(new SwipeAnimationListener() {
//        @Override
//        public void onSwiped(boolean isRight) {
//            if (isRight) {
//                if (player != null) {
//                    player.setScore(player.getScore() + 2);
//                    player.setTwoPoint(player.getTwoPoint() + 1);
//                }
//
//                totalMyScore += 2;
//                totalScore.setText(totalMyScore + " : " + totalOpponentScore);
//                myScore += 2;
//                twoPoint++;
//                myScoreView.setText("Score: " + String.valueOf(myScore));
//                if(player == null){
//                    setLastAction(new Action(period.getMyTeam().getName(), Type.Score, 2));
//                }
//                else{
//                    setLastAction(new Action(player.getName(), Type.Score, 2));
//                }
//                final Handler handler = new Handler();
//                handler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        swipeAnimationButton2.collapseButton();
//                    }
//                }, 400);
//            } else {
//                final Handler handler = new Handler();
//                handler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        swipeAnimationButton2.collapseButton();
//                    }
//                }, 400);
//                if(player == null){
//                    setLastAction(new Action(period.getMyTeam().getName(), Type.Attempt, 2));
//                }
//                else{
//                    setLastAction(new Action(player.getName(), Type.Attempt, 2));
//                }
//                totalFailAttempts.put(2, totalFailAttempts.get(2) + 1);
//                if (player != null) {
//                    player.setTwoPointAttempt(player.getTwoPointAttempt() + 1);
//                }
//            }
//        }
//    });
//    swipeAnimationButton3 = (SwipeAnimationButton) findViewById(R.id.swipe_btn3);
//    swipeAnimationButton3.setPicture(R.drawable.ic_num3);
//    swipeAnimationButton3.setOnSwipeAnimationListener(new SwipeAnimationListener() {
//        @Override
//        public void onSwiped(boolean isRight) {
//            if (isRight) {
//                if (player != null) {
//                    player.setScore(player.getScore() + 3);
//                    player.setThreePoint(player.getThreePoint() + 1);
//                }
//                totalMyScore += 3;
//                totalScore.setText(totalMyScore + " : " + totalOpponentScore);
//                myScore += 3;
//                threePoint++;
//                myScoreView.setText("Score: " + String.valueOf(myScore));
//                if(player == null){
//                    setLastAction(new Action(period.getMyTeam().getName(), Type.Score, 3));
//                }
//                else{
//                    setLastAction(new Action(player.getName(), Type.Score, 3));
//                }
//                final Handler handler = new Handler();
//                handler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        swipeAnimationButton3.collapseButton();
//                    }
//                }, 400);
//            } else {
//                final Handler handler = new Handler();
//                handler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        swipeAnimationButton3.collapseButton();
//                    }
//                }, 400);
//                if(player == null){
//                    setLastAction(new Action(period.getMyTeam().getName(), Type.Attempt, 3));
//                }
//                else{
//                    setLastAction(new Action(player.getName(), Type.Attempt, 3));
//                }
//                totalFailAttempts.put(3, totalFailAttempts.get(3) + 1);
//                if (player != null) {
//                    player.setThreePointAttempt(player.getThreePointAttempt() + 1);
//                }
//            }
//        }
//    });



}
