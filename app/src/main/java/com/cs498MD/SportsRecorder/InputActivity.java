package com.cs498MD.SportsRecorder;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.terry.view.swipeanimationbutton.SwipeAnimationButton;
import com.terry.view.swipeanimationbutton.SwipeAnimationListener;

public class InputActivity extends Activity {

    private TextView opponentScoreView;
    private Button opponentAdd;
    private int opponentScore;

    public static final String OPPONENT = "opponent";
    public static final String OPPONENT_SCORE = "opponentScore";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.input_page);

        SwipeAnimationButton swipeAnimationButton = (SwipeAnimationButton) findViewById(R.id.swipe_btn);
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

        opponentScoreView = (TextView) findViewById(R.id.action);
        opponentAdd = (Button) findViewById(R.id.opponentAdd);

        opponentAdd.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                saveData();
            }
        });

        loadOpponentScore();
        updateViews();

    }

    public void saveData() {
        SharedPreferences sharedPreferences = getSharedPreferences(OPPONENT, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        loadOpponentScore();

        editor.putInt(OPPONENT_SCORE, opponentScore + 1);

        editor.apply();

        updateViews();
    }

    public void loadOpponentScore() {
        SharedPreferences sharedPreferences = getSharedPreferences(OPPONENT, MODE_PRIVATE);
        opponentScore = sharedPreferences.getInt(OPPONENT_SCORE, 0);
    }

    public void updateViews() {
        opponentScoreView.setText(Integer.toString(opponentScore));
    }
}
