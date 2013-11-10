package com.github.users.dmoagx.pipes;

import com.github.users.dmoagx.pipes.model.Board;
import com.github.users.dmoagx.pipes.ui.GameUI;

import javax.swing.*;

public class Game {

    public static void main(String[] args) {
        Board b = new Board(8,8);
        final GameUI g = new GameUI(b);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                g.showUI();
            }
        });
    }
}
