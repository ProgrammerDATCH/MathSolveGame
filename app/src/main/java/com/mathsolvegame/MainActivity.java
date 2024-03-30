package com.mathsolvegame;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private TextView mathProblemTextView, timerTextView;
    private EditText userAnswerEditText;
    private Button submitButton, helpButton;

    private int score = 0;
    private int warnings = 0;
    private int difficultyLevel = 1;

    private CountDownTimer timer;

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mathProblemTextView = findViewById(R.id.mathProblemTextView);
        userAnswerEditText = findViewById(R.id.userAnswerEditText);
        timerTextView = findViewById(R.id.timerTextView);
        submitButton = findViewById(R.id.submitButton);
        helpButton = findViewById(R.id.helpButton);

        preferences = getPreferences(MODE_PRIVATE);

        loadGame();

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkAnswer();
            }
        });

        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showHelpPopup();
            }
        });

        startNewRound();
    }

    private void startNewRound() {
        generateMathProblem();
        startTimer(10000); // 10 seconds timer
    }

    private void generateMathProblem() {
        Random random = new Random();

        int number1 = random.nextInt(10 * difficultyLevel);
        int number2 = random.nextInt(10 * difficultyLevel);
        int mathSymbol = random.nextInt(4); // 0: +, 1: -, 2: *, 3: /

        String mathProblem = "";

        switch (mathSymbol) {
            case 0:
                mathProblem = number1 + " + " + number2;
                break;
            case 1:
                mathProblem = number1 + " - " + number2;
                break;
            case 2:
                mathProblem = number1 + " * " + number2;
                break;
            case 3:
                number2 = (number2 == 0) ? 1 : number2;
                mathProblem = number1 + " / " + number2;
                break;
        }

        mathProblemTextView.setText(mathProblem);

        submitButton.setTag(calculateAnswer(number1, number2, mathSymbol));
    }

    private int calculateAnswer(int number1, int number2, int mathSymbol) {
        switch (mathSymbol) {
            case 0:
                return number1 + number2;
            case 1:
                return number1 - number2;
            case 2:
                return number1 * number2;
            case 3:
                return (number2 == 0) ? 0 : number1 / number2;
            default:
                return 0;
        }
    }

    private void checkAnswer() {
        String userAnswerString = userAnswerEditText.getText().toString().trim();

        if (!userAnswerString.isEmpty()) {
            int userAnswer = Integer.parseInt(userAnswerString);
            int correctAnswer = (int) submitButton.getTag();

            if (userAnswer == correctAnswer) {
                score++;
                startNewRound();
            } else {
                warnings++;
                if (warnings >= 3) {
                    gameOver();
                } else {
                    startNewRound();
                }
            }

            updateScore();
            userAnswerEditText.getText().clear();
        }
    }

    private void updateScore() {
        TextView scoreTextView = findViewById(R.id.scoreTextView);
        scoreTextView.setText("Score: " + score + " | Warnings: " + warnings);

        if (score == 10 || warnings == 10) {
            showGameResultDialog();
        }
    }

    private void showGameResultDialog() {
        String message = (score == 10) ? "Congratulations! You won!" : "Oops! You lost. Better luck next time!";

        new AlertDialog.Builder(this)
                .setTitle("Game Over")
                .setMessage(message)
                .setPositiveButton("Start Again", (dialog, which) -> {
                    resetGame();
                    startNewRound();
                })
                .setCancelable(false)
                .show();
    }

    private void resetGame() {
        score = 0;
        warnings = 0;
        updateScore();
    }

    private void startTimer(long millisInFuture) {
        if (timer != null) {
            timer.cancel();
        }

        timer = new CountDownTimer(millisInFuture, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long secondsRemaining = millisUntilFinished / 1000;
                String timerText = "Time: " + secondsRemaining + "s";
                timerTextView.setText(timerText);
            }

            @Override
            public void onFinish() {
                warnings++;
                startNewRound();
                updateScore();
            }
        }.start();
    }

    private void gameOver() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("score", score);
        editor.putInt("warnings", warnings);
        editor.apply();
        updateScore();
    }

    private void loadGame() {
        score = preferences.getInt("score", 0);
        warnings = preferences.getInt("warnings", 0);
        updateScore();
    }

    private void showHelpPopup() {
        // Create an AlertDialog to show the help information
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("MathSolve Game Help");
        builder.setMessage("This game is a math-solving game where you need to guess the correct answer to mathematical problems within a time limit. The game was made by BigGamer250.");

        // Add a button to close the dialog
        builder.setPositiveButton("Close", (dialogInterface, i) -> {
            // Dismiss the dialog when the "Close" button is clicked
            dialogInterface.dismiss();
        });

        // Show the AlertDialog
        builder.create().show();
    }
}