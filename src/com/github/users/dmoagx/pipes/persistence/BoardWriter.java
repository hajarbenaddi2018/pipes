package com.github.users.dmoagx.pipes.persistence;

import com.github.users.dmoagx.pipes.model.Board;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class BoardWriter {

    public static void WriteBoard(Board board,File f) throws IOException {
        FileWriter fw = new FileWriter(f);
        for (int y = 0; y < board.getHeight(); y++) {
            for (int x = 0; x < board.getWidth(); x++) {
                fw.append(board.getItemAt(x,y).toString());
            }
            fw.append("\r\n");
        }
        fw.close();
    }
}
