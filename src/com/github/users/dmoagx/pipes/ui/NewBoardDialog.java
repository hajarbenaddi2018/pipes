package com.github.users.dmoagx.pipes.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class NewBoardDialog extends JDialog {
	private JSpinner rowInput = new JSpinner();
    private JSpinner colInput = new JSpinner();
    private boolean success = false;

    public NewBoardDialog() {
        GridLayout gl = new GridLayout(0,2);
        gl.setHgap(10);
        gl.setVgap(10);
	    JPanel contentPane = new JPanel();
	    contentPane.setLayout(gl);
        contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        setSize(300, 150);
        setContentPane(contentPane);
        setModal(true);
        setResizable(false);
	    JButton buttonOK = new JButton("OK");
	    getRootPane().setDefaultButton(buttonOK);

        JLabel rowLabel = new JLabel("Rows:");
        JLabel colLabel = new JLabel("Columns:");

        contentPane.add(rowLabel);
        contentPane.add(rowInput);
        contentPane.add(colLabel);
        contentPane.add(colInput);
	    JButton buttonCancel = new JButton("Cancel");
	    contentPane.add(buttonCancel);
        contentPane.add(buttonOK);


        buttonOK.setLocation(10, 10);
        buttonOK.setSize(20, 20);
        buttonOK.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
		        onOK();
	        }
        });

        buttonCancel.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
		        onCancel();
	        }
        });

        rowInput.setModel(new SpinnerNumberModel(1,1,1024,1));
        colInput.setModel(new SpinnerNumberModel(1,1,1024,1));


		// call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

		// call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
		        onCancel();
	        }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    public boolean showDialog() {
        setVisible(true);
        return success;
    }

    private void onOK() {
        success = true;
        setVisible(false);
        dispose();
    }

    private void onCancel() {
        success = false;
        setVisible(false);
        dispose();
    }

    public int getNumRows() {
        return (Integer)rowInput.getValue();
    }

    public int getNumCols() {
        return (Integer)colInput.getValue();
    }
}
