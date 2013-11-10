package com.github.users.dmoagx.pipes.logic;

import com.github.users.dmoagx.pipes.model.Board;
import com.github.users.dmoagx.pipes.model.BoardWalker;
import com.github.users.dmoagx.pipes.model.FieldRef;
import com.github.users.dmoagx.pipes.solver.SolutionException;
import com.github.users.dmoagx.util.Matrix;

public class GameChecker {
    private final Board board;

    public GameChecker(Board b) {
        this.board = b;
    }

    public void checkSolution() {
        //prüfen das alle felder die korrekte anzahl und art an nachbarn haben
        checkNeighbours();
        //prüfen, das alle felder ohne "sprünge" erreichbar sind
        checkSingleGroup();
    }

    private void checkNeighbours() {
        board.walk(new BoardWalker() {
            @Override
            public void visit(FieldRef fr) {
                //ein MUST darf nicht mehr auftreten, dass sind nur helfer
                if(fr.isMust())
                    throw new SolutionException("X="+fr.x+",Y="+fr.y+": Is still a MUST!");
                //über ein empty oder forbidden wissen wir nichts, auch nicht wie viele nachbarn es hat
                if(fr.isEmpty() || fr.isForbidden())
                    return;
                int numNeighbours = fr.numNeighbours();
                if(!fr.isStatisfied())
                    throw new SolutionException("X="+fr.x+",Y="+fr.y+": Requires "+fr.stringVal()+" neighbour(s), but has "+numNeighbours+"!");
                //ein nachbar darf auch nicht den selben zahlenwert haben (wenn wir ein zahlenfeld sind)
                if(fr.hasNeighbourOfKind(fr.value()))
                    throw new SolutionException("X="+fr.x+",Y="+fr.y+": Not unique. Has neighbour with same value!");
            }
        });
    }

    private boolean checkSingleGroup() {
        //visited map initialisieren
        final Matrix<Boolean> visitedMap = new Matrix<Boolean>(board.getWidth(),board.getHeight());
	    board.walk(new BoardWalker() {
		    @Override
		    public void visit(FieldRef fr) {
			    visitedMap.set(fr.x,fr.y,false);
		    }
	    });
        //erstes feld auf dem board finden was eine pipe ist und von da rekursiv alles durchgehen
        visitRecursive(firstReal(),visitedMap);
        //prüfen das am ende jedes feld der map mit einer Pipe besucht wurde
        board.walk(new BoardWalker() {
	        @Override
	        public void visit(FieldRef fr) {
		        if(fr.isReal() && !visitedMap.get(fr.x,fr.y))
			        throw new SolutionException("X="+fr.x+",Y="+fr.y+": Is not part of the first pipe group!");
	        }
        });
        //ok
        return true;
    }

    private FieldRef firstReal() {
        for (int x = 0; x < board.getWidth(); x++) {
            for (int y = 0; y < board.getHeight(); y++) {
                FieldRef fr = board.fieldRef(x,y);
                if(fr.isReal()) {
                    return fr;
                }
            }
        }
        return null;
    }

    private void visitRecursive(FieldRef fr, Matrix<Boolean> visitedMap) {
        //sind wir überhaupt ein echtes feld?
        if(!fr.isReal())
            return;
        //vielleicht waren wir hier schon?
        if(visitedMap.get(fr.x,fr.y))
            return;
        //ansonsten sind wir es jetzt gewesen
        visitedMap.set(fr.x,fr.y,true);
        //und jetzt schauen wir uns noch unsere 4 nachbarn an
        if(!fr.isOnLeftBorder())
            visitRecursive(fr.left(),visitedMap);
        if(!fr.isOnRightBorder())
            visitRecursive(fr.right(),visitedMap);
        if(!fr.isOnTopBorder())
            visitRecursive(fr.top(),visitedMap);
        if(!fr.isOnBottomBorder())
            visitRecursive(fr.below(),visitedMap);
        //fertig
    }

}
