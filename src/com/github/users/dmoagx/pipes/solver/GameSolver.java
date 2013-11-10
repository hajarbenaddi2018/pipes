package com.github.users.dmoagx.pipes.solver;

import com.github.users.dmoagx.pipes.logic.GameChecker;
import com.github.users.dmoagx.pipes.logic.GameplayException;
import com.github.users.dmoagx.pipes.model.Board;
import com.github.users.dmoagx.pipes.model.BoardWalker;
import com.github.users.dmoagx.pipes.model.FieldRef;
import com.github.users.dmoagx.pipes.model.FieldType;
import com.github.users.dmoagx.pipes.solver.backtracking.Alternative;
import com.github.users.dmoagx.pipes.solver.backtracking.BacktrackInfo;
import com.github.users.dmoagx.util.Matrix;

import java.util.ArrayList;
import java.util.Stack;

public class GameSolver {
	private Board board;
	private GameChecker gameChecker;

	private Stack<BacktrackInfo> backStack = new Stack<BacktrackInfo>();

	public GameSolver(Board b) {
		this.board = b;
		this.gameChecker = new GameChecker(b);
	}

	public void solve() {

		//zuerst die absolut sicheren züge machen
		makeSafeChanges();

		while (true) {
			try {
				//ist noch eine lösung möglich?
				checkSolutionViable();

				//wirft bei unmöglichen änderungen fehler. die können durch
				//falsche alternativen aber auftreten.
				makeSafeChanges();

				//durch das anwenden haben wir eine neue basis-situation, für die wir wieder
				//alternativen auflisten müssen
				addAlternatives();

				//ist das hier eine lösung?
				try {
					gameChecker.checkSolution();
					break;
				} catch (SolutionException e) {
					//der detailfehler warum das keine lösung ist, ist egal
				}
			} catch (GameplayException e) {
				//exceptions passieren wenn eine lösung ungültig ist
			}

			//nein, also wählen wir die nächste alternative aus
			backtrackNext();
		}

	}

	private void makeSafeChanges() {
		while (true) {
			int base = board.getChangeCount();
			//zuerst gucken wir mal wo wir MUST einsetzen können
			safeSetMust();
			//jetzt füllen wir die MUSTS wo möglich
			safeFillMust();
			//aus den änderungen ergeben sich evtl. neue verbote
			safeSetForbidden();

			//gucken ob wirklich was passiert ist
			int end = board.getChangeCount();
			if (base == end)
				break;
		}
	}

	private void safeSetForbidden() {
		board.walk(new BoardWalker() {
			@Override
			public void visit(FieldRef fr) {
				//felder die EMPTY, FORBIDDEN oder MUST sind interessieren nicht, die können keine verbote auslösen
				if (!fr.isReal())
					return;
				//alle anderen felder können nur verbote auslösen wenn sie zufrieden sind
				if (!fr.isStatisfied())
					return;
				//gucken wir mal wo wir die hinsetzen
				boolean fTop = true;
				boolean fLeft = true;
				boolean fRight = true;
				boolean fBelow = true;
				//hinter ränder können wir keine setzen
				if (fr.isOnTopBorder())
					fTop = false;
				if (fr.isOnLeftBorder())
					fLeft = false;
				if (fr.isOnRightBorder())
					fRight = false;
				if (fr.isOnBottomBorder())
					fBelow = false;
				//von den verbleibenden müssen wir jetzt gucken ob da was draufsitzt
				if (fTop && fr.top().isReal())
					fTop = false;
				if (fLeft && fr.left().isReal())
					fLeft = false;
				if (fRight && fr.right().isReal())
					fRight = false;
				if (fBelow && fr.below().isReal())
					fBelow = false;
				//und dann können wir forbidden anwenden
				if (fTop)
					setForbiddenPassive(fr.top());
				if (fLeft)
					setForbiddenPassive(fr.left());
				if (fRight)
					setForbiddenPassive(fr.right());
				if (fBelow)
					setForbiddenPassive(fr.below());
			}
		});
	}

	private void setForbiddenPassive(FieldRef fr) {
		//wenn das feld schon forbidden ist gibts nichts zu tun
		if (fr.isForbidden())
			return;
		//wenn es != empty ist darf es nicht forbidden werden
		if (!fr.isEmpty())
			throw new GameplayException("X=" + fr.x + ",Y=" + fr.y + ": Trying to FORBID field which is " + fr.value() + "!");
		//ok, verbieten
		fr.set(FieldType.FORBIDDEN);
	}

	private void safeFillMust() {
		board.walk(new BoardWalker() {
			@Override
			public void visit(FieldRef fr) {
				//uns interessiert nur ein must
				if (!fr.isMust())
					return;
				//jetzt gucken wir mal ob wir eindeutig bestimmen können was hier hin muss
				boolean p1 = true;
				boolean p2 = true;
				boolean p3 = true;
				boolean p4 = true;

				//zählen wir mal die direkten nachbarn
				int numNeighbours = fr.numMinNeighbours();
				if (numNeighbours > 1)
					p1 = false;
				if (numNeighbours > 2)
					p2 = false;
				if (numNeighbours > 3)
					p3 = false;

				//jetzt müssen wir die schon vorhandenen nachbarn eliminieren
				if (fr.hasNeighbourOfKind(FieldType.PIPE_1))
					p1 = false;
				if (fr.hasNeighbourOfKind(FieldType.PIPE_2))
					p2 = false;
				if (fr.hasNeighbourOfKind(FieldType.PIPE_3))
					p3 = false;
				if (fr.hasNeighbourOfKind(FieldType.PIPE_4))
					p4 = false;

				//und dann eliminieren wir noch die zu großen varianten
				int numMax = fr.numMaxNeighbours();
				if (numMax < 4)
					p4 = false;
				if (numMax < 3)
					p3 = false;
				if (numMax < 2)
					p2 = false;
				if (numMax < 1)
					p1 = false;

				//wenn jetzt nur noch eine möglichkeit übrig ist können wir das feld sauber bestimmen
				if (p1 && !p2 && !p3 && !p4) {
					fr.set(FieldType.PIPE_1);
					return;
				}
				if (!p1 && p2 && !p3 && !p4) {
					fr.set(FieldType.PIPE_2);
					return;
				}
				if (!p1 && !p2 && p3 && !p4) {
					fr.set(FieldType.PIPE_3);
					return;
				}
				if (!p1 && !p2 && !p3 && p4) {
					fr.set(FieldType.PIPE_4);
					return;
				}
				if (!p1 && !p2 && !p3 && !p4) {
					throw new GameplayException("X=" + fr.x + ",Y=" + fr.y + ": MUST field but no value possible!");
				}
				//ansonsten ist keine aussage möglich
			}
		});
	}

	private FieldRef fieldRef(int x, int y) {
		return new FieldRef(x, y, board);
	}

	private void safeSetMust() {
		board.walk(new BoardWalker() {
			@Override
			public void visit(FieldRef fr) {
				int maxNeighbours = fr.numMaxNeighbours();
				//sonderfall: ein MUST mit max. 1 nachbarn MUSS den auch haben, sonst ist es isoliert
				if (fr.isMust() && maxNeighbours == 1) {
					//darüber?
					if (!fr.isOnTopBorder() && !fr.top().isForbidden())
						setMustPassive(fr.top());
					//links
					if (!fr.isOnLeftBorder() && !fr.left().isForbidden())
						setMustPassive(fr.left());
					//unten
					if (!fr.isOnBottomBorder() && !fr.below().isForbidden())
						setMustPassive(fr.below());
					//rechts
					if (!fr.isOnRightBorder() && !fr.right().isForbidden())
						setMustPassive(fr.right());
					return;
				}
				//MUST, EMPTY und FORBIDDEN sind egal, damit können wir nix machen
				if (!fr.isReal())
					return;
				if (maxNeighbours < fr.intVal())
					throw new GameplayException("X=" + fr.x + ",Y=" + fr.y + ": Trying to set " + fr.intVal() + " pipe(s) in a field with " + maxNeighbours + " possible neighbour(s)!");
				//ein 4er braucht immer vier nachbarn
				if (fr.is(FieldType.PIPE_4)) {
					setMustPassive(fr.top());
					setMustPassive(fr.left());
					setMustPassive(fr.below());
					setMustPassive(fr.right());
					return;
				}
				//ein 3er feld...
				if (fr.is(FieldType.PIPE_3)) {
					//... am rand braucht auch 3 nachbarn ODER mit einem verbot auf einer seite
					if (fr.isOnLeftBorder() || fr.left().isForbidden()) {
						//links: oben, unten, rechts
						setMustPassive(fr.top());
						setMustPassive(fr.below());
						setMustPassive(fr.right());
						return;
					} else if (fr.isOnTopBorder() || fr.top().isForbidden()) {
						//oben: links, unten, rechts
						setMustPassive(fr.left());
						setMustPassive(fr.below());
						setMustPassive(fr.right());
						return;
					} else if (fr.isOnRightBorder() || fr.right().isForbidden()) {
						//rechts: oben, links, unten
						setMustPassive(fr.top());
						setMustPassive(fr.left());
						setMustPassive(fr.below());
						return;
					} else if (fr.isOnBottomBorder() || fr.below().isForbidden()) {
						//unten: links, oben, rechts
						setMustPassive(fr.left());
						setMustPassive(fr.top());
						setMustPassive(fr.right());
						return;
					}
				}
				//ein 2er feld
				if (fr.is(FieldType.PIPE_2)) {
					//ist eindeutig wenn:
					//  _X_ _X_ _X_ ___ ___ ___
					//  X2_ _2X _2_ X2X X2_ _2X
					//  ___ ___ _X_ ___ _X_ _X_

					//wir oben links in der ecke sind oder oben und links verbote haben
					if ((fr.isOnTopBorder() || fr.top().isForbidden()) && (fr.isOnLeftBorder() || fr.left().isForbidden())) {
						setMustPassive(fr.right());
						setMustPassive(fr.below());
						return;
					}
					//wir oben rechts in der ecke sind oder oben und rechts verbote haben
					if ((fr.isOnTopBorder() || fr.top().isForbidden()) && (fr.isOnRightBorder() || fr.right().isForbidden())) {
						setMustPassive(fr.left());
						setMustPassive(fr.below());
						return;
					}
					//wir unten links in der ecke sind oder unten und links verbote haben
					if ((fr.isOnBottomBorder() || fr.below().isForbidden()) && (fr.isOnLeftBorder() || fr.left().isForbidden())) {
						setMustPassive(fr.top());
						setMustPassive(fr.right());
						return;
					}
					//unten rechts in der ecke
					if ((fr.isOnBottomBorder() || fr.below().isForbidden()) && (fr.isOnRightBorder() || fr.right().isForbidden())) {
						setMustPassive(fr.top());
						setMustPassive(fr.left());
						return;
					}
					//wir oben an der kante sind oder ein verbot haben und unten an der ecke sind oder ein verbot haben
					if ((fr.isOnTopBorder() || fr.top().isForbidden()) && (fr.isOnBottomBorder() || fr.below().isForbidden())) {
						setMustPassive(fr.left());
						setMustPassive(fr.right());
						return;
					}
					//wir links an der kante sind...
					if ((fr.isOnLeftBorder() || fr.left().isForbidden()) && (fr.isOnRightBorder() || fr.right().isForbidden())) {
						setMustPassive(fr.top());
						setMustPassive(fr.below());
						return;
					}
				}
				//ein 1er feld
				if (fr.is(FieldType.PIPE_1)) {
					//ist eindeutig wenn:
					// _X_ _X_ _X_ ___
					// X1X X1_ _1X X1X
					// ___ _X_ _X_ _X_
					if ((fr.isOnTopBorder() || fr.top().isForbidden()) &&
							(fr.isOnLeftBorder() || fr.left().isForbidden()) &&
							(fr.isOnRightBorder() || fr.right().isForbidden())) {
						setMustPassive(fr.below());
						return;
					}

					if ((fr.isOnTopBorder() || fr.top().isForbidden()) &&
							(fr.isOnLeftBorder() || fr.left().isForbidden()) &&
							(fr.isOnBottomBorder() || fr.below().isForbidden())) {
						setMustPassive(fr.right());
						return;
					}

					if ((fr.isOnTopBorder() || fr.top().isForbidden()) &&
							(fr.isOnBottomBorder() || fr.below().isForbidden()) &&
							(fr.isOnRightBorder() || fr.right().isForbidden())) {
						setMustPassive(fr.left());
						return;
					}

					if ((fr.isOnBottomBorder() || fr.below().isForbidden()) &&
							(fr.isOnLeftBorder() || fr.left().isForbidden()) &&
							(fr.isOnRightBorder() || fr.right().isForbidden())) {
						setMustPassive(fr.top());
						return;
					}

				}
			}
		});
	}

	private void setMustPassive(FieldRef fr) {
		//ein feld was forbidden ist darf nicht auch MUST sein
		if (fr.isForbidden())
			throw new GameplayException("X=" + fr.x + ",Y=" + fr.y + ": MUST conflicts with FORBIDDEN!");
		//wenn es schon MUST oder ein echter wert ist überschreiben wir das nicht
		if (fr.isEmpty())
			fr.set(FieldType.MUST);
	}

	private void applyAlternative(Alternative a) {
		board.setItemAt(a.x, a.y, a.value);
	}

	/**
	 * Prüft ob es mit der aktuellen zusammensetzung noch möglich ist eine lösung zu bekommen
	 */
	private void checkSolutionViable() {
		//auf verletzungen der nachbarszahlen prüfen (kann vorkommen)
		checkPossibleNeighbours();

	}

	private void checkPossibleNeighbours() {
		board.walk(new BoardWalker() {
			@Override
			public void visit(FieldRef fr) {
				//empty und forbidden haben keine nachbarzahlen
				if (fr.isEmpty() || fr.isForbidden())
					return;
				//alle anderen felder haben verloren wenn sie garkeine Nachbarn haben dürfen
				int numPossibleNeighbours = fr.numMaxNeighbours();
				if (numPossibleNeighbours < 1)
					throw new GameplayException("X=" + fr.x + ",Y=" + fr.y + ": Isolated MUST field!");
				if (fr.isMust())
					return;
				//andernfalls hat verloren wer mehr nachbarn hat als er darf oder weniger nachbarn möglich sind als er will
				if (numPossibleNeighbours < fr.intVal() || fr.numNeighbours() > fr.intVal())
					throw new GameplayException("X=" + fr.x + ",Y=" + fr.y + ": Field can never be statisfied!");

			}
		});
	}

	private void restoreBoard(final Matrix<FieldType> res) {
		board.walk(new BoardWalker() {
			@Override
			public void visit(FieldRef fr) {
				fr.set(res.get(fr.x, fr.y));
			}
		});
	}

	private void addAlternatives() {
		ArrayList<Alternative> alts = new ArrayList<Alternative>();
		//der erste block den wir finden, der leer und nicht geblockt ist, ist eine alternative
		for (int x = 0; x < board.getWidth(); x++) {
			for (int y = 0; y < board.getHeight(); y++) {
				FieldRef fr = fieldRef(x, y);

				if (fr.isEmpty() || fr.isMust()) {
					//jetzt müssen wir noch beurteilen welche alternativen konkret hier rein dürfen

					int maxNeighbours = fr.numMaxNeighbours();
					//theoretisch ist es möglich, dass wir ein eingeschlossenes feld gefunden haben, was noch nicht verboten ist
					if (maxNeighbours == 0) {
						fr.set(FieldType.FORBIDDEN);
						continue;
					}

					//wenn das feld kein MUST ist, ist forbidden eine alternative
					if (!fr.isMust())
						alts.add(new Alternative(x, y, FieldType.FORBIDDEN));

					//weitere alternativen einfügen
					int minNeighbours = fr.numMinNeighbours();

					if (maxNeighbours >= 1 && !fr.hasNeighbourOfKind(FieldType.PIPE_1) && 1 >= minNeighbours)
						alts.add(new Alternative(x, y, FieldType.PIPE_1));
					if (maxNeighbours >= 2 && !fr.hasNeighbourOfKind(FieldType.PIPE_2) && 2 >= minNeighbours)
						alts.add(new Alternative(x, y, FieldType.PIPE_2));
					if (maxNeighbours >= 3 && !fr.hasNeighbourOfKind(FieldType.PIPE_3) && 3 >= minNeighbours)
						alts.add(new Alternative(x, y, FieldType.PIPE_3));
					if (maxNeighbours >= 4 && !fr.hasNeighbourOfKind(FieldType.PIPE_4) && 4 >= minNeighbours)
						alts.add(new Alternative(x, y, FieldType.PIPE_4));

					//das reicht uns an alternativen. bt info hinzufügen und fertig
					if (!alts.isEmpty()) {
						backtrackPush(alts);
					}
					return;
				}

			}
		}
	}

	private void backtrackPush(ArrayList<Alternative> alts) {
		backStack.push(new BacktrackInfo(board.rawCopy(), alts));
	}

	private boolean backtrackNext() {
		BacktrackInfo tos = backStack.peek();

		//gucken ob wir alternativen auf diesem level haben
		if (!tos.alts.isEmpty()) {
			//board restoren
			restoreBoard(tos.baseBoard);
			//alternative anwenden
			applyAlternative(tos.alts.get(0));
			//und entfernen
			tos.alts.remove(0);
			return true;
		}
		//nein, dann werfen wir das level weg und versuchen es erneut
		else {
			backStack.pop();
			//wenn wir das letzte element gepopt haben sind wir am ende ohne lösung
			if (backStack.isEmpty())
				throw new GameplayException("Backtrack Stack is empty! Nothing more to try!");
			return backtrackNext();
		}
	}

}
