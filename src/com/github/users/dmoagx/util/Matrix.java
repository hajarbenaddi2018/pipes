package com.github.users.dmoagx.util;

public class Matrix<T extends Object> {
    private Object[][] matrix;
    private int width;
    private int height;

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public synchronized Matrix<T> copy() {
        Matrix<T> out = new Matrix<T>(width,height);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                out.set(x,y,get(x,y));
            }
        }
        return out;
    }

    public Matrix(int w, int h) {
        width = w;
        height = h;
        matrix = new Object[h][w];
    }

    public T get(int x,int y) {
        return (T) matrix[y][x];
    }

    public synchronized void set(int x,int y,T o) {
        matrix[y][x] = o;
    }
}
