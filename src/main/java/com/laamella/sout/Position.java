package com.laamella.sout;

/**
 * A position in a text file.
 */
public class Position {
    final int column, row;

    Position(int column, int row) {
        this.column = column;
        this.row = row;
    }

    @Override
    public String toString() {
        return String.format("%d:%d", row, column);
    }
}
