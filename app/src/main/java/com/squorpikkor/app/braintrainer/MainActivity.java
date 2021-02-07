package com.squorpikkor.app.braintrainer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String RIGHT_PREF = "right";
    private TextView timer;
    private TextView question;
    private TextView score;
    private int rightCount;
    private int wrongCount;
    private Button startButton;

    ArrayList<TextView> answers;

    int rightAnswerPosition;
    SharedPreferences preferences;
    CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timer = findViewById(R.id.textViewTimer);
        question = findViewById(R.id.textViewQuestion);
        score = findViewById(R.id.textViewCount);

        answers = new ArrayList<>();
        answers.add(findViewById(R.id.textView0));
        answers.add(findViewById(R.id.textView1));
        answers.add(findViewById(R.id.textView2));
        answers.add(findViewById(R.id.textView3));
        answers.add(findViewById(R.id.textView4));
        answers.add(findViewById(R.id.textView5));

        for (int i = 0; i < answers.size(); i++) {
            int a = i;
            answers.get(i).setOnClickListener(view -> onClickAnswer(a));
        }

        startButton = findViewById(R.id.start_button);
        startButton.setOnClickListener(view -> startGame());

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    void onClickAnswer(int position) {
        if (rightAnswerPosition == position) {
            Toast.makeText(this, "Правильно", Toast.LENGTH_SHORT).show();
            rightCount++;
        } else {
            Toast.makeText(this, "Неправильно", Toast.LENGTH_SHORT).show();
            wrongCount++;
        }
        checkWrongCount(wrongCount);
    }

    private void checkWrongCount(int wrongCount) {
        if (wrongCount == 3) {
            stopGame();
            countDownTimer.cancel();
        } else {
            generateQuestion();
            score.setText(""+rightCount+" / "+wrongCount);
        }
    }

    void startGame() {
        rightCount = 0;
        wrongCount = 0;
        timer.setTextColor(getResources().getColor(android.R.color.holo_green_light));
        startButton.setVisibility(View.GONE);
        score.setVisibility(View.VISIBLE);
        timer.setVisibility(View.VISIBLE);

        for (TextView t:answers) {
            t.setEnabled(true);
        }

        score.setText(""+rightCount+" / "+wrongCount);

        generateQuestion();

        countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millis) {
//                int seconds = (int) (millis/1000);
//                seconds++;//так как милисекунды при 1000 не показываются, то "0" будет висеть ещё секунду до onFinish. Поэтому здесь добавляю 1,
//                timer.setText(String.valueOf(seconds));
                timer.setText(getTimeStroke(millis));
                if (millis<10000) timer.setTextColor(Color.RED);
            }

            @Override
            public void onFinish() {
//                Toast.makeText(MainActivity.this, "Таймер завершен", Toast.LENGTH_SHORT).show();
//                timer.setText(String.valueOf(0));//а здесь в конце счета устанавливаю "0"
                stopGame();
            }
        };
        countDownTimer.start();
    }

    private String getTimeStroke(long millis) {
        int seconds = (int) (millis / 1000);
        int minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format(Locale.getDefault(), "%02d : %02d", minutes, seconds);
    }

    void stopGame() {
        question.setText(String.format("Счёт: %s / %s", rightCount, wrongCount));
        checkScore(rightCount);
        startButton.setVisibility(View.VISIBLE);
        for (TextView t:answers) {
            t.setEnabled(false);
        }
        score.setVisibility(View.GONE);
        timer.setVisibility(View.GONE);
    }

    private void checkScore(int rightCount) {
        int saved = 0;
        if (preferences!=null&&preferences.contains(RIGHT_PREF)) {
            saved = preferences.getInt(RIGHT_PREF, 0);
        }
        if (rightCount > saved) {
            preferences.edit().putInt(RIGHT_PREF, rightCount).apply();
            question.setText(String.format("Новый рекорд! Решено правильно: %s", rightCount));
        }
    }

    //todo добавить деление
    void generateQuestion() {
        int a = (int)(Math.random()*8)+2; //от 2 до 9
        int b = (int)(Math.random()*8)+2; //от 2 до 9
        int rightAnswer = a * b;
        String strQuestion = "" + a + " * " + b;

        //Рандомим или-или и если true, переделываем пример в деление
        if (((int)(Math.random()*101))%2==0) {
            int temp = a;
            a = rightAnswer;
            rightAnswer = b;
            b = temp;
            strQuestion = "" + a + " : " + b;
        }

        rightAnswerPosition = (int)(Math.random()*answers.size());
        for (int i = 0; i < answers.size(); i++) {
            if (i==rightAnswerPosition) answers.get(i).setText(String.valueOf(rightAnswer));
            else answers.get(i).setText(String.valueOf(wrongAnswer(rightAnswer)));
        }
        question.setText(strQuestion);
    }

    private int wrongAnswer(int rightAnswer) {
        int wrong;
        do {
            wrong = rightAnswer+((int)(Math.random()*13)-6);
        } while (rightAnswer==wrong || wrong < 4);
        return wrong;
    }

}

