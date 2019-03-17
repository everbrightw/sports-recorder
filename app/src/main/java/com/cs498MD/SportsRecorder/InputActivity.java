package com.cs498MD.SportsRecorder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Layout;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.terry.view.swipeanimationbutton.SwipeAnimationButton;
import com.terry.view.swipeanimationbutton.SwipeAnimationListener;

import java.util.ArrayList;

import info.hoang8f.widget.FButton;

public class InputActivity extends Activity implements View.OnClickListener{

    private String matchId;

    private TextView myNameView;
    private TextView myScoreView;
    private SwipeAnimationButton swipeAnimationButton;
    private SwipeAnimationButton swipeAnimationButton2;
    private SwipeAnimationButton swipeAnimationButton3;
    private Button foulBtn;

    private TextView opponentNameView;
    private TextView opponentScoreView;
    private Button opponentAddBtn;

    private TextView lastAction;
    private ImageButton undo;

    private final String MATCH = "match";

    private Button addPeriodBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.input_page);

        matchId = getIntent().getStringExtra("matchId");

        setMatchUtils();
        setMyTeam();
        setOpponentTeam();

        loadMatchInfo();




        SharedPreferences sharedPreferences = getSharedPreferences(MATCH, MODE_PRIVATE);
        String matchJson = sharedPreferences.getString(matchId, "");

        if (matchJson == null || matchJson.equals("")) {
            Match newMatch = new Match();
            Gson gson = new Gson();
            String newMatchJson = gson.toJson(newMatch);
            updateMatchInfo(newMatchJson);
        } else {
            Gson gson = new Gson();
            Match match = gson.fromJson(matchJson, Match.class);
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.period_add){
            //add period event
            FButton myButton = new FButton(this );
            myButton.setButtonColor(getResources().getColor(R.color.fbutton_color_wet_asphalt));

            myButton.setMinHeight(R.dimen.button_min_height);
            myButton.setMinWidth(R.dimen.button_min_width);

            myButton.setShadowEnabled(true);
            myButton.setTextColor(getResources().getColor(R.color.classic_white));
            myButton.setShadowHeight(12);
            myButton.setCornerRadius(20);
            myButton.setText("New");
            
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(convertDipToPixels(48,InputActivity.this), convertDipToPixels(48,InputActivity.this) );
            myButton.setLayoutParams(lp);

            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) myButton.getLayoutParams();
            params.width = convertDipToPixels(48,InputActivity.this);
            params.height = convertDipToPixels(48,InputActivity.this);
            params.setMarginStart(convertDipToPixels(8,InputActivity.this));
            params.setMarginEnd(convertDipToPixels(8,InputActivity.this));
            params.topMargin = convertDipToPixels(8,InputActivity.this);
            params.bottomMargin = convertDipToPixels(8,InputActivity.this);

            LinearLayout ll = (LinearLayout)findViewById(R.id.button_layout);

            ll.addView(myButton, lp);
            ll.removeView(addPeriodBtn);

            ll.addView(addPeriodBtn, lp);

        }

    }


    public static int convertDipToPixels(float dips, Context context)
    {
        return (int) (dips * context.getResources().getDisplayMetrics().density + 0.5f);
    }




    private void setMatchUtils() {
        lastAction = findViewById(R.id.last_action);
        undo = findViewById(R.id.undo);

        undo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

            }
        });
    }

    private void setMyTeam() {
        swipeAnimationButton = (SwipeAnimationButton) findViewById(R.id.swipe_btn);
        swipeAnimationButton.setOnSwipeAnimationListener(new SwipeAnimationListener() {
            @Override
            public void onSwiped(boolean isRight) {
                if (isRight) {
                    Toast.makeText(getApplicationContext(), "right Swipe!!!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "left Swipe!!!", Toast.LENGTH_LONG).show();
                }
            }
        });

        swipeAnimationButton2 = (SwipeAnimationButton) findViewById(R.id.swipe_btn2);
        swipeAnimationButton2.setOnSwipeAnimationListener(new SwipeAnimationListener() {
            @Override
            public void onSwiped(boolean isRight) {
                if (isRight) {
                    Toast.makeText(getApplicationContext(), "2right Swipe!!!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "2left Swipe!!!", Toast.LENGTH_LONG).show();
                }
            }
        });

        swipeAnimationButton3 = (SwipeAnimationButton) findViewById(R.id.swipe_btn3);
        swipeAnimationButton3.setOnSwipeAnimationListener(new SwipeAnimationListener() {
            @Override
            public void onSwiped(boolean isRight) {
                if (isRight) {
                    Toast.makeText(getApplicationContext(), "3right Swipe!!!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "3left Swipe!!!", Toast.LENGTH_LONG).show();
                }
            }
        });

        myNameView = (TextView) findViewById(R.id.my_name);
        myScoreView = (TextView) findViewById(R.id.my_score);
        foulBtn = (Button) findViewById(R.id.foul_btn);

    }

    private void setOpponentTeam() {

        opponentNameView = (TextView) findViewById(R.id.opponent_name);
        opponentScoreView = (TextView) findViewById(R.id.opponent_score);
        opponentAddBtn = (Button) findViewById(R.id.opponent_add);

        opponentAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                saveData();
            }
        });
    }

    public void loadMatchInfo() {
        SharedPreferences sharedPreferences = getSharedPreferences(MATCH, MODE_PRIVATE);
        String matchJson = sharedPreferences.getString(matchId, "");

        Match match;
        Gson gson = new Gson();

        if (matchJson == null || matchJson.equals("")) {
            match = new Match();
        } else {
            match = gson.fromJson(matchJson, Match.class);
        }

        MyTeam myTeam = match.getMyTeam();
        OpponentTeam opponentTeam = match.getOpponentTeam();
        ArrayList<Player> players = match.getPlayers();



    }

    public void updateMatchInfo(String match) {
        SharedPreferences sharedPreferences = getSharedPreferences(MATCH, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(matchId, match);
        editor.apply();

        loadMatchInfo();
    }
}
