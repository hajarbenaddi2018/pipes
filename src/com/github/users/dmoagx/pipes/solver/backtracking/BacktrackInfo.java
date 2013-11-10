package com.github.users.dmoagx.pipes.solver.backtracking;

import com.github.users.dmoagx.pipes.model.FieldType;
import com.github.users.dmoagx.util.Matrix;

import java.util.ArrayList;

public class BacktrackInfo {
    public final Matrix<FieldType> baseBoard;
    public final ArrayList<Alternative> alts;

    public BacktrackInfo(Matrix<FieldType> baseBoard, ArrayList<Alternative> alts) {
        this.baseBoard = baseBoard;
        this.alts = alts;
    }
}
