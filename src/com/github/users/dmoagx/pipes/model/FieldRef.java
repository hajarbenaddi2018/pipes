package com.github.users.dmoagx.pipes.model;

public class FieldRef {
    public final int x,y;
    private final Board board;

    public FieldRef(int x, int y,Board board) {
        this.x = x;
        this.y = y;
        this.board = board;
    }

    public Board getBoard() {
        return board;
    }

    public FieldRef top() {
        return new FieldRef(x,(y-1),board);
    }

    public FieldRef below() {
        return new FieldRef(x,(y+1),board);
    }

    public FieldRef left() {
        return new FieldRef((x-1),y,board);
    }

    public FieldRef right() {
        return new FieldRef((x+1),y,board);
    }

    public FieldType value() {
        return board.getItemAt(this);
    }

    public boolean is(FieldType ft) {
        return (value().equals(ft));
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof FieldRef) {
            FieldRef fr = (FieldRef)obj;
            //wenn sie sich auf das selbe board beziehen und die gleichen koordinaten haben
            return (fr.board == board && fr.x == x && fr.y == y);
        }
        return super.equals(obj);
    }

    public boolean isEmpty() {
        return is(FieldType.EMPTY);
    }

    public boolean isForbidden() {
        return is(FieldType.FORBIDDEN);
    }

    public boolean isOnLeftBorder() {
        return (x==0);
    }

    public boolean isOnTopBorder() {
        return (y==0);
    }

    public boolean isOnRightBorder() {
        return (x==(board.getWidth() - 1));
    }

    public boolean isOnBottomBorder() {
        return (y == (board.getHeight() - 1));
    }

    public boolean hasNeighbourOfKind(FieldType ft) {
        //oben?
        if(!isOnTopBorder() && top().is(ft))
            return true;
        //links
        if(!isOnLeftBorder() && left().is(ft))
            return true;
        //rechts
        if(!isOnRightBorder() && right().is(ft))
            return true;
        //unten
        if(!isOnBottomBorder() && below().is(ft))
            return true;

        return false;
    }

    public boolean isReal() {
        return value().isReal();
    }


    public boolean isStatisfied() {
        return value().comparePipesTo(numNeighbours());
    }

    public int numNeighbours() {
        int count = 0;

        if(!isOnTopBorder() && top().isReal())
            count++;
        if(!isOnLeftBorder() && left().isReal())
            count++;
        if(!isOnBottomBorder() && below().isReal())
            count++;
        if(!isOnRightBorder() && right().isReal())
            count++;

        return count;
    }

    public void set(FieldType ft) {
        board.setItemAt(this,ft);
    }

    /**
     * Gibt die Anzahl der erfoderlichen Nachbarn zurück
     * @return Die Anzahl der Felder auf denen entweder eine PIPE oder MUST ist
     */
    public int numMinNeighbours() {
        int count = 0;

        if(!isOnTopBorder() && (top().isReal() || top().isMust()))
            count++;
        if(!isOnLeftBorder() && (left().isReal() || left().isMust()))
            count++;
        if(!isOnBottomBorder() && (below().isReal() || below().isMust()))
            count++;
        if(!isOnRightBorder() && (right().isReal() || right().isMust()))
            count++;

        return count;
    }

    /**
     * Gibt die maximale Anzahl an Nachbarn eines Feldes zurück
     * @return Die Anzahl der Felder die nicht FORBIDDEN oder hinter dem Rand sind
     */
    public int numMaxNeighbours() {
        int max = 4;

        if(isOnTopBorder() || top().isForbidden())
            max--;
        if(isOnLeftBorder() || left().isForbidden())
            max--;
        if(isOnBottomBorder() || below().isForbidden())
            max--;
        if(isOnRightBorder() || right().isForbidden())
            max--;

        return max;
    }

    public boolean isMust() {
        return is(FieldType.MUST);
    }

    public String stringVal() {
        return value().getStringValue();
    }

    public int intVal() {
        return value().getIntValue();
    }
}
