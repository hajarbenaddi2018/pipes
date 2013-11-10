package com.github.users.dmoagx.pipes.persistence;

import com.github.users.dmoagx.pipes.model.Board;
import com.github.users.dmoagx.pipes.model.FieldType;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class BoardReader {

    public static Board ReadFromFile(File f) throws IOException {
        FileReader reader = new FileReader(f);
        if(f.length() > Integer.MAX_VALUE)
            throw new RuntimeException("File too large to read!");
        char cbuf[] = new char[(int) f.length()];
        reader.read(cbuf);
        reader.close();
        ArrayList<String> charMap = new ArrayList<String>();

        boolean lastWasCR = false; //damit wir ein LF ignorieren was nach einem CR kommt
        boolean ignoreLine = false;
        StringBuilder curRow = new StringBuilder();

        for (char c : cbuf) {
            //leerzeichen ignorieren
            if (c == ' ' || c == '\t')
                continue;
            //zeilenumbruch verarbeiten
            if (c == '\r' || c == '\n') {
                //wenn es ein LF ist und vorher ein CR war ist es egal
                if (c == '\n' && lastWasCR) {
                    lastWasCR = false;
                    continue;
                }
                //cr markieren
                if (c == '\r')
                    lastWasCR = true;
                //zeilende behandeln
                if (curRow.length() > 0)
                    charMap.add(curRow.toString());
                curRow = new StringBuilder();
                ignoreLine = false; //neue zeile
                continue;
            }
            lastWasCR = false;
            //kommentare ignorieren
            if (ignoreLine)
                continue;
            //kommentareinführung?
            if (c == ';') {
                ignoreLine = true;
                continue;
            }
            //der rest
            else {
                curRow.append(c);
            }
        }
        //datei ohne abschließendes Zeilenende behandeln
        if(curRow.length() > 0) {
            charMap.add(curRow.toString());
        }

        //sicherstellen, dass es rechteckig ist
        int line0Len = charMap.get(0).length();
        for (int i = 1; i < charMap.size(); i++) {
            if(charMap.get(i).length() != line0Len)
                throw new RuntimeException("Map ist not rectangular!");
        }

        //com.github.users.dmoagx.util.Matrix erzeugen
        Board outBoard = new Board(charMap.get(0).length(),charMap.size());

        for (int x = 0; x < charMap.get(0).length(); x++) {
            for (int y = 0; y < charMap.size(); y++) {
                char c = charMap.get(y).charAt(x);
                FieldType type = FieldType.FromString(String.valueOf(c));
                outBoard.setItemAt(x,y, type);
            }
        }

        return outBoard;
    }
}
