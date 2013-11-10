package com.github.users.dmoagx.pipes.solver.backtracking;

import com.github.users.dmoagx.pipes.model.FieldType;

public class Alternative {
    public final int x;
    public final int y;
    public final FieldType value;

    public Alternative(int x, int y, FieldType value) {
        this.x = x;
        this.y = y;
        this.value = value;
    }
}
