package dinidiniz.eggsearcher.functions;

import android.graphics.Bitmap;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ConnectedComponentsLabelling {
    public static int[][] twoPass(int[][] matrix) {

        int nextLabel = 1;

        int rowLength = matrix.length;
        int columnLength = matrix[0].length;

        List<Set<Integer>> linked = new ArrayList<Set<Integer>>();
        int[][] labels = new int[rowLength][columnLength];

        //Primeiro Passo
        for (int row = 0; row < rowLength; row++) {
            for (int column = 0; column < columnLength; column++) {
                if (matrix[row][column] != 0) {
                    int[] neibours = neibours(row, column, labels);
                    if (neibours.length == 0) {
                        linked.add(new HashSet());
                        linked.get(nextLabel - 1).add(nextLabel);
                        labels[row][column] = nextLabel;
                        nextLabel += 1;
                    } else {
                        Arrays.sort(neibours);
                        labels[row][column] = neibours[0];
                        for (int i = 0; i < neibours.length; i++) {
                            for (int j = 0; j < neibours.length; j++) {
                                linked.get(neibours[i] - 1).add(neibours[j]);
                            }
                        }
                    }

                }
            }
        }

        //Segundo Passo
        int[] vector = new int[nextLabel];
        for (int i= 0; i < nextLabel - 1; i++){
            vector[i] = Collections.min(linked.get(i), null);
        }

        for (int row = 0; row < rowLength; row++) {
            for (int column = 0; column < columnLength; column++) {
                if (matrix[row][column] != 0) {
                    labels[row][column] = vector[labels[row][column] - 1];
                }
            }
        }
        return labels;
    }

    public static int[] neibours(int row, int column, int[][] matrix) {

        int[] neibours = {};
        int rowLength = matrix.length;
        int columnLength = matrix[0].length;


        if (row ==0 && column ==0) { return neibours;
        }
        else if (row == 0) {
            neibours = add_element(matrix[row][column - 1], neibours);
        } else if (column == 0) {
            neibours = add_element(matrix[row - 1][column], neibours);
        } else if ((row > 0) && (column > 0) && (column < columnLength - 1)) {
            neibours = add_element(matrix[row][column - 1], neibours);
            neibours = add_element(matrix[row - 1][column - 1], neibours);
            neibours = add_element(matrix[row - 1][column], neibours);
            neibours = add_element(matrix[row - 1][column + 1], neibours);
        } else if (row > 0 && column > 0) {
            neibours = add_element(matrix[row][column - 1], neibours);
            neibours = add_element(matrix[row - 1][column - 1], neibours);
            neibours = add_element(matrix[row - 1][column], neibours);
        }

        int[] neibours2 = {};
        for (int i = 0; i < neibours.length; i++) {
            if (neibours[i] != 0) {
                neibours2 = add_element(neibours[i], neibours2);
            }
        }
        return neibours2;
    }

    public static int max(int[] vector){
        int max = 0;
        for (int number = 0; number < vector.length; number++) {
            if (number > max){max = number;}
        }
        return max;
    }

    public static int[] areaCount(int[][] matrix){
        int[] vectorLabel = {};
        int[] vectorArea = {};
        int positionNew = 0;
        boolean teste;

        int rowLength = matrix.length;
        int columnLength = matrix[0].length;

        for (int row = 0; row < rowLength; row++) {
            for (int column = 0; column < columnLength; column++) {
                teste = true;

                for (int position = 0; position < vectorLabel.length; position++){
                    if (vectorLabel[position] == matrix[row][column]) {positionNew = position; teste = false;}
                }
                if (teste){
                    vectorLabel = add_element(matrix[row][column], vectorLabel);
                    vectorArea = add_element(1, vectorArea);
                } else {
                    vectorArea[positionNew] = vectorArea[positionNew] + 1;
                }
            }

        }

        return vectorArea;
    }


    public static int[] add_element(int element, int[] neibours) {
        neibours = Arrays.copyOf(neibours, neibours.length + 1);
        neibours[neibours.length - 1] = element;
        return neibours;
    }
}