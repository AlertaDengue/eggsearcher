package dinidiniz.eggsearcher.helper;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import de.bwaldvogel.liblinear.Feature;
import de.bwaldvogel.liblinear.FeatureNode;
import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;
import de.bwaldvogel.liblinear.Parameter;
import de.bwaldvogel.liblinear.Problem;
import de.bwaldvogel.liblinear.SolverType;
import dinidiniz.eggsearcher.SQL.DBHelper;

/**
 * Created by leon on 11/10/16.
 */
public class Logistic {

    private static String TAG = "Logistic";

    public static final String WEIGHT = "weight";
    public static final String PRECISION = "precision";
    /** the learning rate */
    public double rate;

    private static final String data = "0 0 0 0 0 1";

    /** the weight to learn */
    public double[] weights;

    /** the number of iterations */
    private int ITERATIONS = 15000;

    public Logistic(int n) {
        this.rate = 0.0001;
        weights = new double[n];
    }

    private static double sigmoid(double z) {
        return 1.0 / (1.0 + Math.exp(-z));
    }

    public void train(List<Instance> instances) {
        for (int n=0; n<ITERATIONS; n++) {
            double lik = 0.0;
            for (int i=0; i<instances.size(); i++) {
                int[] x = instances.get(i).x;
                double predicted = classify(x);
                int label = instances.get(i).label;
                for (int j=0; j<weights.length; j++) {
                    weights[j] = weights[j] + rate * (label - predicted) * x[j];
                }
                // not necessary for learning
                lik += label * Math.log(classify(x)) + (1-label) * Math.log(1- classify(x));
            }
            System.out.println("iteration: " + n + " " + Arrays.toString(weights) + " mle: " + lik);
        }
    }

    public double classify(int[] x) {
        double logit = .0;
        for (int i=0; i<weights.length;i++)  {
            logit += weights[i] * x[i];
        }

        if (logit > 0) {
            return 1;
        } else {
            return 0;
        }
        //return sigmoid(logit);
    }

    public static class Instance {
        public int label;
        public int[] x;

        public Instance(int label, int[] x) {
            this.label = label;
            this.x = x;
        }
    }

    public static List<Instance> readDataSet(String file) throws FileNotFoundException {
        List<Instance> dataset = new ArrayList<Instance>();
        Scanner scanner = null;
        try {
            scanner = new Scanner(data);
            while(scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.startsWith("#")) {
                    continue;
                }
                String[] columns = line.split("\\s+");

                // skip first column and last column is the label
                int i = 1;
                int[] data = new int[columns.length-2];
                for (i=1; i<columns.length-1; i++) {
                    data[i-1] = Integer.parseInt(columns[i]);
                }
                int label = Integer.parseInt(columns[i]);
                Instance instance = new Instance(label, data);
                dataset.add(instance);
            }
        } finally {
            if (scanner != null)
                scanner.close();
        }
        return dataset;
    }

    /***
     * Access DataSet in DBHELPER with all the pixels got in trainning
     * @param context
     * @return dataset of pixels in instance form to prepare to use logistic function
     */
    public static List<Instance> readPixelsDataSet(Context context){
        List<Instance> dataset = new ArrayList<Instance>();
        DBHelper db = new DBHelper(context);
        List<List<Integer>> eggs = db.getAllPixeis().get(DBHelper.EGGS_IN_PIXEL_TABLE);
        List<List<Integer>> notEggs = db.getAllPixeis().get(DBHelper.OTHER_IN_PIXEL_TABLE);

        //Get a sample of each part equal to the minimum of each
        Collections.shuffle(eggs);
        Collections.shuffle(notEggs);

        eggs = eggs.subList(0, java.lang.Math.min(eggs.size(), notEggs.size()));
        notEggs = notEggs.subList(0, java.lang.Math.min(eggs.size(), notEggs.size()));

        ArrayList<List<Integer>> listOfPixels = new ArrayList<List<Integer>>(eggs);
        listOfPixels.addAll(notEggs);


        List<Integer> listLabel = new ArrayList<Integer>(Collections.nCopies(eggs.size(), 1));
        listLabel.addAll(Collections.nCopies(notEggs.size(), 0));

        int n = 0;
        for (List<Integer> pixel:listOfPixels){
            int[] data = {pixel.get(0), pixel.get(1), pixel.get(2), pixel.get(3), 100};
            int label = listLabel.get(n);
            Instance instance = new Instance(label, data);
            dataset.add(instance);
            n +=1;
        };

        Log.i(TAG, "n: " + n + "; eggs: " + eggs.size() + "; not eggs: " + notEggs.size());
        return dataset;
    }

    public static HashMap<String, double[]> trainLogisticModel(Context context) {
        List<FeatureNode[]> dataset = new ArrayList<FeatureNode[]>();
        DBHelper db = new DBHelper(context);
        List<List<Integer>> eggs = db.getAllPixeis().get(DBHelper.EGGS_IN_PIXEL_TABLE);
        List<List<Integer>> notEggs = db.getAllPixeis().get(DBHelper.OTHER_IN_PIXEL_TABLE);

        //Get a sample of each part equal to the minimum of each
        Collections.shuffle(eggs);
        Collections.shuffle(notEggs);

        eggs = eggs.subList(0, java.lang.Math.min(eggs.size(), notEggs.size()));
        notEggs = notEggs.subList(0, java.lang.Math.min(eggs.size(), notEggs.size()));

        ArrayList<List<Integer>> listOfPixels = new ArrayList<List<Integer>>(eggs);
        listOfPixels.addAll(notEggs);


        List<Integer> listLabel = new ArrayList<Integer>(Collections.nCopies(eggs.size(), 1));
        listLabel.addAll(Collections.nCopies(notEggs.size(), 0));

        for (List<Integer> pixel:listOfPixels){
            FeatureNode[] data = {new FeatureNode(1, pixel.get(0)), new FeatureNode(2, pixel.get(1)),
                    new FeatureNode(3, pixel.get(2)), new FeatureNode(4, pixel.get(3)),
                    new FeatureNode(5, pixel.get(4))};
            dataset.add(data);
        };

        FeatureNode[][] datasetArray = dataset.toArray(new FeatureNode[dataset.size()][]);
        double[] labelArray = toIntArray(listLabel);

        Problem problem = new Problem();
        problem.l = dataset.size();
        problem.n = datasetArray[0].length;
        problem.x = datasetArray;
        problem.y = labelArray;

        SolverType solver = SolverType.L2R_LR; // -s 0
        double C = 1.0; // cost of constraints violation
        double eps = 0.001; // stopping criteria

        Parameter parameter = new Parameter(solver, C, eps);
        Model model = Linear.train(problem, parameter);

        model.getFeatureWeights();

        int n = 0;
        int diferentEgg = 0;
        int diferentNotEgg = 0;
        int diferent = 0;
        int equal = 0;
        for (FeatureNode[] instance:datasetArray){
            double predict = Linear.predict(model, instance);

            //System.out.println(instance + " = " + predict + " == " + labelArray[n]);
            if (predict != labelArray[n]) {
                diferent += 1;
                if (labelArray[n] == 1) {
                    diferentEgg += 1;
                } else {
                    diferentNotEgg += 1;
                }
            } else {
                equal += 1;
            }
            n += 1;
        }

        System.out.println("n = " + n + "; diferent = " + diferent + "; equal = " + equal + "; diferentEgg = " + diferentEgg + "; diferentNotEgg = " + diferentNotEgg);

        Log.i(TAG, "weights: " + java.util.Arrays.toString(model.getFeatureWeights()));
        double[] precisionLogistic = {n,equal,diferent};

        HashMap answerFinal = new HashMap();
        answerFinal.put(WEIGHT, model.getFeatureWeights());
        answerFinal.put(PRECISION, precisionLogistic);
        return answerFinal;
    }

    private static double[] toIntArray(List<Integer> list)  {
        double[] ret = new double[list.size()];
        int i = 0;
        for (Integer e : list)
            ret[i++] = e.intValue();
        return ret;
    }


    public static double[] main(Context context) {

        //List<Instance> instances = readDataSet("dataset.txt");
        List<Instance> instances = readPixelsDataSet(context);
        Logistic logistic = new Logistic(5);
        logistic.train(instances);
        int[] x = {200,200,200};
        System.out.println("prob(1|x) = " + logistic.classify(x));

        int[] x2 = {10,10,10};
        System.out.println("prob(1|x2) = " + logistic.classify(x2));

        int[] x3 = {30,30,30};
        System.out.println("prob(1|x3) = " + logistic.classify(x3));

        int[] x4 = {50,50,50};
        System.out.println("prob(1|x4) = " + logistic.classify(x4));

        DBHelper db = new DBHelper(context);
        int n = 0;
        int diferentEgg = 0;
        int diferentNotEgg = 0;
        int diferent = 0;
        int equal = 0;
        for (Instance inst: instances){
            boolean algorithm = (logistic.classify(inst.x) > 0);
            boolean label = (inst.label == 1);
            n += 1;
            if (label != algorithm){
                diferent += 1;
                if (inst.label == 1){
                    diferentEgg +=1;
                } else {
                    diferentNotEgg +=1;
                }
            } else {
                equal += 1;
            }
        };
        System.out.println("n = " + n + "; diferente = " + diferent + "; equal = " + equal + "; diferentEgg = " + diferentEgg + "; diferentNotEgg = " + diferentNotEgg );

        return logistic.weights;
    }

}
