/* This file is part of Find The Mouse.
*
* Find The Mouse is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Find The Mouse is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Find The Mouse. If not, see <http://www.gnu.org/licenses/>. */
package com.dylantaylor.findthemouse;

import java.util.Random;
import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TextView.BufferType;

public class MainActivity extends Activity {
	
	//Global Variables
    private final int cardIDs[] = new int[]{R.id.card1,R.id.card2,R.id.card3,R.id.card4,R.id.card5};
    private final int gameClicks = 3; //number of clicks allowed per game
    private final String MSG_TAG = "FindTheMouse"; //used for debugging logs
    private final int DELAY_TIME = 1000; //delay time in milliseconds between games
    private boolean gameOver = false;
    private boolean gameWon = false;
    private TextView guessCount; //guesses left
    private TextView currScore; //current score
    private TextView currStreak; //current winning streak
    private int mouseCard = -1; //The number of the card the mouse is hidden under
    private int clicksLeft = gameClicks; //clicks left until game is lost
    private int currentStreak = 0; //counter of number of games won in a row
    private int currentScore = 0; //current number of points accumulated
    private Button card[] = new Button[5];
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle(getString(R.string.app_title));
        setContentView(R.layout.main);
        guessCount = (TextView) this.findViewById(R.id.leftCount);
        currScore = (TextView) this.findViewById(R.id.scoreAccumulator);
        currStreak = (TextView) this.findViewById(R.id.winningStreak);
        if (guessCount == null) {
    		Log.i(MSG_TAG, "guessCount is NULL!");
    	}
        OnClickListener cardClick = new OnClickListener() { //This listener is used when a card is clicked.
			
			@Override
			public void onClick(View v) {
				if (gameOver) {
					showMessage(); 
				}
				if (clicksLeft == 0) {
					gameOver = true;
					return;
				}
				clicksLeft--;
				Log.i(MSG_TAG,"The mouse is under card: " + (mouseCard+1));
				int clicked = v.getId(); //the number of the card that is clicked
				Log.i(MSG_TAG,"Card clicked: " + (clicked+1));
				if (clicked == mouseCard) {
					v.setBackgroundDrawable(getBaseContext().getResources().getDrawable(R.drawable.mouse));	
					gameOver = true;
					gameWon = true;
					currentStreak++;
					currentScore += (clicksLeft + 1);
					showMessage();
					resetGame(false);
				} else if (!gameOver) {
					updateStatistics();
					v.setVisibility(View.INVISIBLE); //v.setVisibility(View.GONE); would center the remaining cards...
					if (clicksLeft == 0) {
						gameOver = true;
						showMessage();
					}
				}
			}
		};
        for (int i = 0; i < cardIDs.length; i++) {
        	card[i] = (Button)findViewById(cardIDs[i]);
        	card[i].setOnClickListener(cardClick);
        	card[i].setId(i);
        }        
        startGame();
    }
    
    private void updateStatistics() {
    	guessCount.setText(Integer.toString(clicksLeft), BufferType.NORMAL); //update the guesses left counter
    	currScore.setText(Integer.toString(currentScore), BufferType.NORMAL); //update the current score
    	currStreak.setText(Integer.toString(currentStreak), BufferType.NORMAL); //update the current winning streak
    }
    
    private void delayRefresh() { //waits short amount of time before starting next game
    	Handler handler = new Handler();
        handler.postDelayed(new Runnable() { 
             public void run() { 
            	 MainActivity.this.doReset();
             } 
        }, DELAY_TIME);
    }
    
    private void startGame() { //Called when the game is started
    	Log.i(MSG_TAG,"Starting Game...");
    	gameOver = false; //the game is no longer over
    	gameWon = false; //the game also wasn't won yet since it just started
    	clicksLeft = gameClicks; //reset the number of clicks left    	
    	updateStatistics(); //update the guesses left counter
    	mouseCard = new Random().nextInt(5); //generate a new mouse card
    	Log.i(MSG_TAG,"Shh... the mouse is located under card: " + (mouseCard+1)); //+1 is because the cards actually go from 0-4 not 1-5
    	for (Button b : card) { //make all the buttons visible and change all backgrounds to the card image
    		b.setBackgroundDrawable(getBaseContext().getResources().getDrawable(R.drawable.card));	
    		b.setVisibility(View.VISIBLE);
    	}
    }
    
    @Override
    public boolean onSearchRequested() { 
    	return false; //since the search button resets the game, do nothing
    }
    
    public void resetGame(boolean resetSession) { //handles reset requests
    	if (resetSession) {
    		currentScore = 0; //reset the current score to zero
        	currentStreak = 0; //reset the current winning streak to zero  	
    		doReset();
    	} else {
    		delayRefresh();
    	}
    }
    
    public void doReset() { //actually resets the game, in the easiest way possible
    	this.onDestroy();
    	this.onCreate(null);
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent msg) {
        if ((keyCode == KeyEvent.KEYCODE_MENU) || (keyCode == KeyEvent.KEYCODE_SEARCH)) { //menu or search button pressed
        	if (gameOver) { //this check prevents accidental resetting of scores
        		resetGame(true);
        	}
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
        	finish();
        } else { //otherwise, do the key's original action
            super.onKeyDown(keyCode, msg);
        }
        return true;
    }
        
    public void showMessage() {
    	if (gameWon) { //show game won message
    		new AlertDialog.Builder(this).setTitle(getString(R.string.win_title)).setMessage(getString(R.string.win_message)).setPositiveButton(getString(R.string.ok), null).show();
    	} else {
    		CharSequence lose_message = "You're out of guesses. The mouse was under card " + (mouseCard+1) + '.';
    		new AlertDialog.Builder(this).setTitle(getString(R.string.lose_title)).setMessage(lose_message).setPositiveButton(getString(R.string.ok), null).show();
    	}
    }
}