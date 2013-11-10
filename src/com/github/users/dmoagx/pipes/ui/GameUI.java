package com.github.users.dmoagx.pipes.ui;

import com.github.users.dmoagx.pipes.model.Board;
import com.github.users.dmoagx.pipes.persistence.BoardReader;
import com.github.users.dmoagx.pipes.persistence.BoardWriter;
import com.github.users.dmoagx.pipes.solver.GameSolver;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class GameUI implements ActionListener, Thread.UncaughtExceptionHandler {
    JFrame window;
    BoardView boardView;
    JMenuItem openItem;
    JMenuItem saveItem;
    JMenuItem solveItem;
    JMenuItem newItem;
    Board board;
    Timer timer;

    public GameUI(Board b) {
        boardView = new BoardView(b);
        board = b;

        timer = new Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boardView.repaint();
            }
        });
        timer.setRepeats(true);
    }

    public void showUI() {
        window = new JFrame("com.github.users.dmoagx.pipes.Game UI");
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        window.setSize(720,520);

        Border border = BorderFactory.createLoweredBevelBorder();
        boardView.setBorder(border);
        window.getContentPane().add(boardView);

        JMenuBar mainMenu = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        openItem = new JMenuItem("Open...");
        openItem.addActionListener(this);
        fileMenu.add(openItem);

        saveItem = new JMenuItem("Save...");
        saveItem.addActionListener(this);
        fileMenu.add(saveItem);

        mainMenu.add(fileMenu);

        JMenu gameMenu = new JMenu("Game");

        newItem = new JMenuItem("New...");
        newItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                newGame();
            }
        });
        gameMenu.add(newItem);

        solveItem = new JMenuItem("Solve");
        solveItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                solveGame();
            }
        });
        gameMenu.add(solveItem);

        mainMenu.add(gameMenu);

        window.setJMenuBar(mainMenu);

        setWinSize();
        window.setVisible(true);
    }

    private void setWinSize() {
        window.setMinimumSize(new Dimension(board.getWidth()*48,board.getHeight()*48+window.getJMenuBar().getHeight()));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == openItem) {
            try {
                openBoard();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        else if(e.getSource() == saveItem) {
            try {
                saveBoard();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    private void newGame() {
        NewBoardDialog dlg = new NewBoardDialog();
        if(dlg.showDialog()) {
            board = new Board(dlg.getNumCols(),dlg.getNumRows());
            boardView.setBoard(board);
            setWinSize();
        }
    }

    private void solveGame() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                GameSolver gs = new GameSolver(board);
                gs.solve();
                solutionFound();
            }
        });
        t.setUncaughtExceptionHandler(this);
        t.start();

        timer.start();
    }

    private void solutionFound() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                timer.stop();
                boardView.repaint();
            }
        });
    }

    private void openBoard() throws IOException {
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(false);
        if(chooser.showOpenDialog(window) == JFileChooser.APPROVE_OPTION) {
            board = BoardReader.ReadFromFile(chooser.getSelectedFile());
            boardView.setBoard(board);
            setWinSize();
        }
    }

    private void saveBoard() throws IOException {
        JFileChooser chooser = new JFileChooser();
        if(chooser.showSaveDialog(window) == JFileChooser.APPROVE_OPTION) {
            BoardWriter.WriteBoard(board,chooser.getSelectedFile());
        }
    }

    @Override
    public void uncaughtException(Thread t, final Throwable e) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                timer.stop();
                boardView.repaint();
                //dialog anzeigen
                JOptionPane.showMessageDialog(window,"Error:\n"+e.getLocalizedMessage(),"No solution found!",JOptionPane.ERROR_MESSAGE);
            }
        });

    }
}

