package com.google.engedu.puzzle8;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import java.util.ArrayList;


public class PuzzleBoard implements Comparable<PuzzleBoard>{
    private int steps;
    private PuzzleBoard previous;
    private static final int NUM_TILES = 3;
    private static final int[][] NEIGHBOUR_COORDS = {
            { -1, 0 },
            { 1, 0 },
            { 0, -1 },
            { 0, 1 }
    };
    private ArrayList<PuzzleTile> tiles = new ArrayList<>();

    PuzzleBoard(Bitmap bitmap, int parentWidth) {
        bitmap = Bitmap.createScaledBitmap(bitmap, parentWidth, parentWidth, true);
        int width = bitmap.getWidth() / NUM_TILES;
        int height = bitmap.getHeight() / NUM_TILES;
        PuzzleTile tile;
        int pieceCount = 0;
        for(int i = 0; i < NUM_TILES; i++){
            for(int j = 0; j < NUM_TILES; j++){
                tile = new PuzzleTile(Bitmap.createBitmap(bitmap, i * width, j * height, width, height), pieceCount);
                pieceCount++;
                tiles.add(tile);
            }
        }
        tiles.remove(tiles.size() - 1);
        tiles.add(null);
    }

    PuzzleBoard(PuzzleBoard otherBoard) {
        tiles = (ArrayList<PuzzleTile>) otherBoard.tiles.clone();
        this.steps = otherBoard.steps + 1;
        previous = otherBoard;
    }

    public void reset() {
        // Nothing for now but you may have things to reset once you implement the solver.
    }

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        return tiles.equals(((PuzzleBoard) o).tiles);
    }

    public void draw(Canvas canvas) {
        if (tiles == null) {
            return;
        }
        for (int i = 0; i < NUM_TILES * NUM_TILES; i++) {
            PuzzleTile tile = tiles.get(i);
            if (tile != null) {
                tile.draw(canvas, i % NUM_TILES, i / NUM_TILES);
            }
        }
    }

    public boolean click(float x, float y) {
        for (int i = 0; i < NUM_TILES * NUM_TILES; i++) {
            PuzzleTile tile = tiles.get(i);
            if (tile != null) {
                if (tile.isClicked(x, y, i % NUM_TILES, i / NUM_TILES)) {
                    return tryMoving(i % NUM_TILES, i / NUM_TILES);
                }
            }
        }
        return false;
    }

    public boolean resolved() {
        for (int i = 0; i < NUM_TILES * NUM_TILES - 1; i++) {
            PuzzleTile tile = tiles.get(i);
            if (tile == null || tile.getNumber() != i)
                return false;
        }
        return true;
    }

    private int XYtoIndex(int x, int y) {
        return x + y * NUM_TILES;
    }

    protected void swapTiles(int i, int j) {
        PuzzleTile temp = tiles.get(i);
        tiles.set(i, tiles.get(j));
        tiles.set(j, temp);
    }

    private boolean tryMoving(int tileX, int tileY) {
        for (int[] delta : NEIGHBOUR_COORDS) {
            int nullX = tileX + delta[0];
            int nullY = tileY + delta[1];
            if (nullX >= 0 && nullX < NUM_TILES && nullY >= 0 && nullY < NUM_TILES &&
                    tiles.get(XYtoIndex(nullX, nullY)) == null) {
                swapTiles(XYtoIndex(nullX, nullY), XYtoIndex(tileX, tileY));
                return true;
            }
        }
        return false;
    }

    public ArrayList<PuzzleBoard> neighbours() {
        ArrayList<PuzzleBoard> boards = new ArrayList<>();
        int nullIndex = 0;
        for(int i = 0; i < NUM_TILES * NUM_TILES; i++){
            if(tiles.get(i) == null){
                nullIndex = i;
            }
        }

        for (int[] delta : NEIGHBOUR_COORDS) {
            int neighborX = nullIndex % NUM_TILES + delta[0];
            int neighborY = nullIndex / NUM_TILES  + delta[1];
            if (neighborX >= 0 && neighborX < NUM_TILES && neighborY >= 0 && neighborY < NUM_TILES) {
                PuzzleBoard copy = new PuzzleBoard(this);
                copy.swapTiles(XYtoIndex(neighborX, neighborY), XYtoIndex(nullIndex % NUM_TILES, nullIndex / NUM_TILES));
                boards.add(copy);
            }
        }
        return boards;
    }

    public int priority() {
        int totalSteps = 0;
        for(int i = 0; i < NUM_TILES * NUM_TILES; i++){
            if(tiles.get(i) != null){
                totalSteps += Math.abs(i/NUM_TILES - tiles.get(i).getNumber()/ NUM_TILES)
                        + Math.abs(i%NUM_TILES - tiles.get(i).getNumber()% NUM_TILES);
            }
        }
        return totalSteps + steps;
    }

    public PuzzleBoard getPrevious(){
        return previous;
    }
    @Override
    public int compareTo(PuzzleBoard another) {
        return this.priority() - another.priority();
    }
}
