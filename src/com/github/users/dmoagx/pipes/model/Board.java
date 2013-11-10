package com.github.users.dmoagx.pipes.model;

import com.github.users.dmoagx.util.Matrix;

public class Board {
    private final int w;
    private final int h;

    private final Matrix<FieldType> board;
    private int changeCount = 0;

    public Board(int w,int h) {
        this.w = w;
        this.h = h;
        this.board = new Matrix<FieldType>(w,h);

        fillMatrix();
    }

    public Board copy() {
        final Board out = new Board(w,h);
	    walk(new BoardWalker() {
		    @Override
		    public void visit(FieldRef fr) {
			    out.board.set(fr.x,fr.y,getItemAt(fr));
		    }
	    });
        out.changeCount = 0;
        return out;
    }

    private void fillMatrix() {
        walk(new BoardWalker() {
            @Override
            public void visit(FieldRef fr) {
                setItemAt(fr.x,fr.y,FieldType.EMPTY);
            }
        });
    }

    private void assertRange(int x,int y) {
        if(!isOnBoard(x,y))
            throw new RuntimeException("Coordinate ("+x+"|"+y+") out of range. (0 <= x < "+w+" | 0 <= y < "+h+")");
    }

    public boolean isOnBoard(int x,int y) {
        return !(x < 0 || y < 0 || x >= w || y >= h);
    }

    public FieldType getItemAt(int x,int y) {
        assertRange(x,y);
        return board.get(x,y);
    }

    public FieldType getItemAt(FieldRef fr) {
        if(fr.getBoard() != this)
            throw new RuntimeException("getItemAt() called with FieldRef to different board!");
        return board.get(fr.x,fr.y);
    }

    public void setItemAt(int x,int y, FieldType item) {
        assertRange(x,y);
        changeCount++;
        board.set(x,y,item);
    }

    public void setItemAt(FieldRef fr, FieldType ft) {
        if(fr.getBoard() != this)
            throw new RuntimeException("setItemAt() called with FieldRef to different board!");
        setItemAt(fr.x,fr.y,ft);
    }

    public int getWidth() {
        return w;
    }

    public int getHeight() {
        return h;
    }

    public int getChangeCount() {
        return changeCount;
    }

    public FieldRef fieldRef(int x,int y) {
        return new FieldRef(x,y,this);
    }

    public void walk(BoardWalker w) {
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                w.visit(fieldRef(x,y));
            }
        }
    }

    public Matrix<FieldType> rawCopy() {
        return board.copy();
    }
}