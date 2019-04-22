package me.nathanfallet.morpiontpe.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import me.nathanfallet.morpiontpe.R;
import me.nathanfallet.morpiontpe.models.Computer;
import me.nathanfallet.morpiontpe.models.Game;
import me.nathanfallet.morpiontpe.models.Human;
import me.nathanfallet.morpiontpe.models.Player;
import me.nathanfallet.morpiontpe.models.Sign;
import me.nathanfallet.morpiontpe.models.UIUpdater;

public class GameActivity extends AppCompatActivity {

    private Game game;
    private ImageButton box1;
    private ImageButton box2;
    private ImageButton box3;
    private ImageButton box4;
    private ImageButton box5;
    private ImageButton box6;
    private ImageButton box7;
    private ImageButton box8;
    private ImageButton box9;
    private TextView infos;
    private Button back;

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check for dark mode
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.AppThemeDark);
        } else {
            setTheme(R.style.AppThemeLight);
        }

        // Remove the title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();

        // Set content view
        setContentView(R.layout.activity_game);

        // Get views
        box1 = findViewById(R.id.box1);
        box2 = findViewById(R.id.box2);
        box3 = findViewById(R.id.box3);
        box4 = findViewById(R.id.box4);
        box5 = findViewById(R.id.box5);
        box6 = findViewById(R.id.box6);
        box7 = findViewById(R.id.box7);
        box8 = findViewById(R.id.box8);
        box9 = findViewById(R.id.box9);
        infos = findViewById(R.id.infos);
        back = findViewById(R.id.back);

        // Handle click
        box1.setOnClickListener(new BoxClickListener(0, 0));
        box2.setOnClickListener(new BoxClickListener(1, 0));
        box3.setOnClickListener(new BoxClickListener(2, 0));
        box4.setOnClickListener(new BoxClickListener(0, 1));
        box5.setOnClickListener(new BoxClickListener(1, 1));
        box6.setOnClickListener(new BoxClickListener(2, 1));
        box7.setOnClickListener(new BoxClickListener(0, 2));
        box8.setOnClickListener(new BoxClickListener(1, 2));
        box9.setOnClickListener(new BoxClickListener(2, 2));
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Init the game object from the Intent
        Intent intent = getIntent();

        // We determine who play the game
        if (intent.getExtras().getInt("id") == R.id.button1) {
            game = new Game(new Human(Sign.X), new Human(Sign.O));
        } else if (intent.getExtras().getInt("id") == R.id.button2) {
            Player[] players_brut = {new Computer(Sign.X), new Human(Sign.O)};
            List<Player> players = Arrays.asList(players_brut);
            Collections.shuffle(players);
            game = new Game(players.get(0), players.get(1));
        } else {
            game = new Game(new Computer(Sign.X), new Computer(Sign.O));
        }

        // Load the empty grid
        updateUI();

        // Everything is up, start the game
        new Thread(new Runnable() {
            @Override
            public void run() {
                game.nextMove();
            }
        }).start();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUIUpdater(UIUpdater updater) {
        updateUI();
    }

    public void updateUI() {
        // Update infos label
        if (game.getCurrent() != Sign.empty) {
            // Get the player object
            Player current = null;
            Player[] players = {game.getPlayer1(), game.getPlayer2()};
            for (Player player : players) {
                if (player.sign == game.getCurrent()) {
                    current = player;
                }
            }

            // Differentiate human and computer in text
            if (current instanceof Computer) {
                infos.setText(getString(R.string.playing_computer, game.getCurrent().toString()));
            } else {
                infos.setText(getString(R.string.playing_human, game.getCurrent().toString()));
            }
            back.setVisibility(View.INVISIBLE);
        } else {
            // Game has ended
            Sign win = game.win(game.getTable());
            Player current = null;
            Player[] players = {game.getPlayer1(), game.getPlayer2()};
            for (Player player : players) {
                if (player.sign == win) {
                    current = player;
                }
            }

            // Differentiate human and computer in text
            if (current instanceof Computer) {
                infos.setText(getString(R.string.ended_computer, win.toString()));
            } else if (current instanceof Human) {
                infos.setText(getString(R.string.ended_human, win.toString()));
            } else {
                infos.setText(getString(R.string.ended_empty));
            }
            back.setVisibility(View.VISIBLE);
        }

        // Update images
        ImageButton[][] boxes = {{box1, box4, box7}, {box2, box5, box8}, {box3, box6, box9}};

        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                ImageButton box = boxes[x][y];
                Sign sign = game.getTable()[x][y];

                if (sign != Sign.empty) {
                    box.setImageDrawable(getDrawable(sign == Sign.X ? R.drawable.x : R.drawable.o));
                } else {
                    box.setImageDrawable(null);
                }
            }
        }
    }

    private class BoxClickListener implements View.OnClickListener {

        private int x;
        private int y;

        BoxClickListener(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public void onClick(View v) {
            // Iterate the players
            Player[] players = {game.getPlayer1(), game.getPlayer2()};
            for (Player player : players) {
                // Check if it's a human, and the current player
                if (player instanceof Human && game.getCurrent() == player.sign) {
                    final Human human = (Human) player;

                    // Play!
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            human.getCallback().completion(x, y);
                        }
                    }).start();
                }
            }
        }

    }

}
