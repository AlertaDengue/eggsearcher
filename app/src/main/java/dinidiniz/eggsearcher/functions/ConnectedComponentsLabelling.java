package dinidiniz.eggsearcher.functions;

import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ConnectedComponentsLabelling {
    public static Mat twoPass(Mat matrix) {

        int nextLabel = 1;

        int rowLength = matrix.rows();
        int columnLength = matrix.cols();

        List<Set<Integer>> linked = new ArrayList<Set<Integer>>();
        Mat labels = new Mat(matrix.rows(),matrix.cols(), matrix.type());

        //Primeiro Passo
        for (int row = 0; row < rowLength; row++) {
            for (int column = 0; column < columnLength; column++) {
                if (matrix.get(row, column)[0] != 0) {
                    double[] neibours = neibours(row, column, labels);
                    if (neibours.length == 0) {
                        linked.add(new HashSet());
                        linked.get(nextLabel - 1).add(nextLabel);
                        labels.put(row, column, nextLabel);
                        nextLabel += 1;
                    } else {
                        Arrays.sort(neibours);
                        labels.put(row, column, neibours[0]);
                        for (int i = 0; i < neibours.length; i++) {
                            for (int j = 0; j < neibours.length; j++) {
                                linked.get((int) neibours[i] - 1).add((int) neibours[j]);
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
                if (!matrix.get(row, column).equals(0)) {
                    labels.put(row, column, vector[(int) labels.get(row,column)[0] - 1]);
                }
            }
        }
        return labels;
    }

    public static double[] neibours(int row, int column, Mat matrix) {

        double[] neibours = {};
        int rowLength = matrix.rows();
        int columnLength = matrix.cols();


        if (row ==0 && column ==0) { return neibours;
        }
        else if (row == 0) {
            neibours = add_element(matrix.get(row, column - 1)[0], neibours);
        } else if (column == 0) {
            neibours = add_element(matrix.get(row - 1,column)[0], neibours);
        } else if ((row > 0) && (column > 0) && (column < columnLength - 1)) {
            neibours = add_element(matrix.get(row, column - 1)[0], neibours);
            neibours = add_element(matrix.get(row - 1,column - 1)[0], neibours);
            neibours = add_element(matrix.get(row - 1,column)[0], neibours);
            neibours = add_element(matrix.get(row - 1,column + 1)[0], neibours);
        } else if (row > 0 && column > 0) {
            neibours = add_element(matrix.get(row,column - 1)[0], neibours);
            neibours = add_element(matrix.get(row - 1,column - 1)[0], neibours);
            neibours = add_element(matrix.get(row - 1,column)[0], neibours);
        }

        double[] neibours2 = {};
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

    public static List<Double> areaCount(Mat matrix){
        double[] vectorLabel = {};
        double[] vectorArea = {};
        int positionNew = 0;
        boolean teste;
        List<Double> finalList = new ArrayList<Double>();

        int rowLength = matrix.rows();
        int columnLength = matrix.cols();

        for (int row = 0; row < rowLength; row++) {
            for (int column = 0; column < columnLength; column++) {
                teste = true;

                for (int position = 0; position < vectorLabel.length; position++){
                    if (vectorLabel[position] == matrix.get(row, column)[0]) {positionNew = position; teste = false;}
                }
                if (teste){
                    vectorLabel = add_element(matrix.get(row, column)[0], vectorLabel);
                    vectorArea = add_element(1, vectorArea);
                } else {
                    vectorArea[positionNew] = vectorArea[positionNew] + 1;
                }
            }

        }

        for (int i = 0; i < vectorArea.length; i++){
            finalList.add(vectorArea[i]);
        }

        return finalList;
    }


    public static double[] add_element(double element, double[] neibours) {
        neibours = Arrays.copyOf(neibours, neibours.length + 1);
        neibours[neibours.length - 1] = element;
        return neibours;
    }
}