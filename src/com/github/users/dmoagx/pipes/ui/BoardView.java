package com.github.users.dmoagx.pipes.ui;

import com.github.users.dmoagx.pipes.model.Board;
import com.github.users.dmoagx.pipes.model.FieldRef;
import com.github.users.dmoagx.pipes.model.FieldType;
import com.github.users.dmoagx.util.Matrix;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class BoardView extends JComponent implements MouseListener {
    private Board board;
    private Matrix<JLabel> labels = null;

    public BoardView(Board board) {

        setBoard(board);
        addMouseListener(this);

    }

    protected void paintComponent(Graphics g) {

        g.setColor(Color.white);
        g.fillRect(0,0,getWidth(),getHeight());

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

        Stroke stroke2 = new BasicStroke(2.0f);
        Stroke stroke1 = new BasicStroke(1.0f);

        g2.setColor(Color.black);

        //grid zeichnen
        double boxWidth  = (getWidth()-7) / board.getWidth();
        double boxHeight = (getHeight()-7) / board.getHeight();

        //wir brauchen eine copy sonst könnte sich die grid verändern während wir sie zeichnen
        Board drawBoard = board.copy();

        double xOffset = 3;
        double yOffset = 3;
        for (int y = 0; y < drawBoard.getHeight(); y++) {
            for (int x = 0; x < drawBoard.getWidth(); x++) {
                g2.setStroke(stroke2);
                g2.drawRect((int)xOffset,(int)yOffset,(int)boxWidth,(int)boxHeight);

                FieldType ft = drawBoard.getItemAt(x, y);

                if(ft == FieldType.FORBIDDEN) {
                    g2.setStroke(stroke2);
                    g2.setColor(Color.red);
                    g2.drawLine((int)(xOffset+10),(int)(yOffset+10),(int)(xOffset+boxWidth-10),(int)(yOffset+boxHeight-10));
                    g2.drawLine((int)(xOffset+10),(int)(yOffset+boxHeight-10),(int)(xOffset+boxWidth-10),(int)(yOffset+10));
                    g2.setColor(Color.black);
                }
                else if(ft == FieldType.MUST) {
                    g2.fillOval((int)(xOffset+10),(int)(yOffset+10),(int)(boxWidth-20),(int)(boxHeight-20));
                }
                else if(ft.isReal()) {
                    //rohrverbindungen zeichnen
                    FieldRef fr = drawBoard.fieldRef(x,y);

                    g2.setStroke(stroke1);

                    if(!fr.isOnTopBorder() && fr.top().isReal()) {
                        //pipe nach oben
                        g2.setColor(Color.yellow);
                        g2.fillRect((int)(xOffset+20),(int)(yOffset-20),(int)(boxWidth-40),(int)(40));
                        g2.setColor(Color.black);
                        g2.drawRect((int)(xOffset+20),(int)(yOffset-20),(int)(boxWidth-40),(int)(40));
                    }

                    if(!fr.isOnLeftBorder() && fr.left().isReal()) {
                        //pipe nach oben
                        g2.setColor(Color.yellow);
                        g2.fillRect((int)(xOffset-20),(int)(yOffset+20),(int)(40),(int)(boxHeight-40));
                        g2.setColor(Color.black);
                        g2.drawRect((int)(xOffset-20),(int)(yOffset+20),(int)(40),(int)(boxHeight-40));
                    }

                    g2.setStroke(stroke2);
                }

                xOffset += boxWidth;
            }
            yOffset += boxHeight;
            xOffset = 3;
        }

        xOffset = 3;
        yOffset = 3;
        for (int y = 0; y < drawBoard.getHeight(); y++) {
            for (int x = 0; x < drawBoard.getWidth(); x++) {

                FieldType ft = drawBoard.getItemAt(x, y);
                FieldRef fr = drawBoard.fieldRef(x,y);

                //labels neu anordnen
                JLabel label = labels.get(x,y);

                label.setLocation((int)(xOffset+15),(int)(yOffset+15));
                label.setSize((int)(boxWidth-30),(int)(boxHeight-30));

                String letter = "";

                if(fr.isReal()){
                    g2.setStroke(stroke2);
                    g2.setColor(Color.gray);
                    if(fr.numNeighbours() > fr.intVal() || fr.hasNeighbourOfKind(ft))
                        g2.setColor(Color.red);
                    else if(fr.isStatisfied())
                        g2.setColor(Color.green);
                    g2.fillRect((int)(xOffset+10),(int)(yOffset+10),(int)(boxWidth-20),(int)(boxHeight-20));
                    g2.setColor(Color.black);
                    g2.drawRect((int) (xOffset + 10), (int) (yOffset + 10), (int) (boxWidth - 20), (int) (boxHeight - 20));

                    switch (ft) {
                        case PIPE_1:
                            letter = "1";
                            break;
                        case PIPE_2:
                            letter = "2";
                            break;
                        case PIPE_3:
                            letter = "3";
                            break;
                        case PIPE_4:
                            letter = "4";
                            break;
                    }

                }

                label.setText(letter);

                xOffset += boxWidth;
            }
            yOffset += boxHeight;
            xOffset = 3;
        }

    }

    @Override
    public void mouseClicked(MouseEvent e) {
        float blockWidth = getWidth() / board.getWidth();
        float blockHeight = getHeight() / board.getHeight();
        //feld bestimmen wo geklickt wurde
        int x = (int)Math.floor(e.getX()/blockWidth);
        int y = (int)Math.floor(e.getY()/blockHeight);

        //System.out.println("Clicked on X="+x+",Y="+y);

        //aktuellen wert holen
        FieldType cur = board.getItemAt(x,y);

        //und weiterzählen
        switch (cur) {

            case EMPTY:
                cur = FieldType.PIPE_1;
                break;
            case PIPE_1:
                cur = FieldType.PIPE_2;
                break;
            case PIPE_2:
                cur = FieldType.PIPE_3;
                break;
            case PIPE_3:
                cur = FieldType.PIPE_4;
                break;
            case PIPE_4:
                cur = FieldType.MUST;
                break;
            case MUST:
                cur = FieldType.FORBIDDEN;
                break;
            case FORBIDDEN:
                cur = FieldType.EMPTY;
                break;
        }

        //zurückschreiben
        board.setItemAt(x,y,cur);

        repaint();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        //ignore
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        //ignore
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        //ignore
    }

    @Override
    public void mouseExited(MouseEvent e) {
        //ignore
    }

    public void setBoard(Board b) {
        this.board = b;

        if(labels != null) {
            for (int x = 0; x < labels.getWidth(); x++) {
                for (int y = 0; y < labels.getHeight(); y++) {
                    remove(labels.get(x,y));
                    labels.set(x,y,null);
                }
            }
        }

        labels = new Matrix<JLabel>(board.getWidth(),board.getHeight());

        for (int x = 0; x < board.getWidth(); x++) {
            for (int y = 0; y < board.getHeight(); y++) {
                JLabel lbl = new JLabel("");
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setVerticalAlignment(SwingConstants.CENTER);
                add(lbl);
                labels.set(x,y,lbl);
            }
        }


        repaint();
    }

}
