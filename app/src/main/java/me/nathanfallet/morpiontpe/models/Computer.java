package me.nathanfallet.morpiontpe.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Computer extends Player {

    public Computer(Sign sign) {
        super(sign);
    }

    // Override player play function
    @Override
    public void play(Game game, final GamePlayCallback callback) {
        final Move best = bestMove(game, game.getTable(), sign);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                callback.completion(best.x, best.y);
            }
        }).start();
    }

    // Select the best possibility of game
    public Move bestMove(Game game, Sign[][] table, Sign sign) {
        // Get the sign of ennemy
        Sign other = sign == Sign.O ? Sign.X : Sign.O;

        // Create an array of moves
        ArrayList<Move> moves = new ArrayList<>();

        // Iterate the table to find all moves
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                // If the cell is free
                if (table[x][y] == Sign.empty) {
                    // We copy the table in which we will test
                    Sign[][] copy = copy(table);
                    copy[x][y] = sign;

                    // Adn we get the result
                    Sign win = game.win(copy);
                    int score;

                    // If the table is full
                    if (win == Sign.empty && game.full(copy)) {
                        score = 0;
                    }
                    // If it allow to win, score is 1
                    else if (win == sign) {
                        score = 1;
                    }
                    // Else it is the opposite of the opponent best score (0 or -1)
                    else {
                        score = 0 - bestMove(game, copy, other).score;
                    }

                    Move result = new Move(x, y, score);

                    // If the score is 1, we return the result
                    if (score == 1) {
                        return result;
                    }

                    // Else we add the result in the array and we continue
                    moves.add(result);
                }
            }
        }

        // We shuffle the moves
        Collections.shuffle(moves);

        // If hardcore is enabled, sort moves by score
        if (game.isHardcore()) {
            moves.sort(new Comparator<Move>() {
                @Override
                public int compare(Move o1, Move o2) {
                    return o2.score - o1.score;
                }
            });
        }

        // Return the best move
        return moves.get(0);
    }

    public Sign[][] copy(Sign[][] table) {
        Sign[][] copy = {
                {Sign.empty, Sign.empty, Sign.empty},
                {Sign.empty, Sign.empty, Sign.empty},
                {Sign.empty, Sign.empty, Sign.empty}
        };
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                copy[x][y] = table[x][y];
            }
        }
        return copy;
    }

    // Define an extra struct for a move
    private class Move {

        public int x;
        public int y;
        public int score;

        public Move(int x, int y, int score) {
            this.x = x;
            this.y = y;
            this.score = score;
        }

    }

}
