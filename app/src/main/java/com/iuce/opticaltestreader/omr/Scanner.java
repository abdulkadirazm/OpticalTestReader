package com.iuce.opticaltestreader.omr;

import  org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.iuce.opticaltestreader.omr.Util.sortLeft2Right;
import static com.iuce.opticaltestreader.omr.Util.sortTopLeft2BottomRight;
import static com.iuce.opticaltestreader.omr.Util.sout;
import static com.iuce.opticaltestreader.omr.Util.write2File;
import static org.opencv.core.CvType.CV_8UC1;
import static org.opencv.imgproc.Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C;
import static org.opencv.imgproc.Imgproc.CHAIN_APPROX_SIMPLE;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;
import static org.opencv.imgproc.Imgproc.Canny;
import static org.opencv.imgproc.Imgproc.MORPH_RECT;
import static org.opencv.imgproc.Imgproc.RETR_EXTERNAL;
import static org.opencv.imgproc.Imgproc.RETR_TREE;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;
import static org.opencv.imgproc.Imgproc.adaptiveThreshold;
import static org.opencv.imgproc.Imgproc.approxPolyDP;
import static org.opencv.imgproc.Imgproc.arcLength;
import static org.opencv.imgproc.Imgproc.blur;
import static org.opencv.imgproc.Imgproc.boundingRect;
import static org.opencv.imgproc.Imgproc.cvtColor;
import static org.opencv.imgproc.Imgproc.dilate;
import static org.opencv.imgproc.Imgproc.drawContours;
import static org.opencv.imgproc.Imgproc.findContours;
import static org.opencv.imgproc.Imgproc.getStructuringElement;
import static org.opencv.imgproc.Imgproc.threshold;

public class Scanner {

    private final Mat source;
    private final double[] ratio = new double[]{ 17, 15.5};
    private final int questionCount;
    private final String[] options = new String[]{"A", "B", "C", "D"};
    private int idx = 0;
    private int correct = 0, incorrect = 0, empty = 0, score = 0;

    private Rect roi;
    private Mat dilated, gray, thresh, blur, canny, adaptiveThresh, hierarchy;
    private List<MatOfPoint> contours, bubbles;
    private List<Integer> answers, answersBy;
    private ArrayList<String> answerList;

    private boolean logging = false;

    public Scanner(Mat source, int questionCount) {
        this.source = source;
        this.questionCount = questionCount;

        hierarchy = new Mat();
        contours = new ArrayList<>();
        bubbles = new ArrayList<>();
        answers = new ArrayList<>();
    }

    public void setLogging(boolean logging) {
        this.logging = logging;
    }

    public StringBuilder scan() throws Exception {

        dilated = new Mat(source.size(), CV_8UC1);
        dilate(source, dilated, getStructuringElement(MORPH_RECT, new Size(3, 3)));
        if(logging) write2File(dilated, "step_1_dilated.png");

        gray = new Mat(dilated.size(), CV_8UC1);
        cvtColor(dilated, gray, COLOR_BGR2GRAY);
        if(logging) write2File(gray, "step_2_gray.png");

        thresh = new Mat(gray.rows(), gray.cols(), gray.type());
        threshold(gray, thresh, 150, 255, THRESH_BINARY);
        if(logging) write2File(thresh, "step_3_thresh.png");

        blur = new Mat(gray.size(), CV_8UC1);
        blur(gray, blur, new Size(5., 5.));
        if(logging) write2File(blur, "step_4_blur.png");

        canny = new Mat(blur.size(), CV_8UC1);
        Canny(blur, canny, 160, 20);
        if(logging) write2File(canny, "step_5_canny.png");

        adaptiveThresh = new Mat(canny.rows(), gray.cols(), gray.type());
        adaptiveThreshold(canny, adaptiveThresh, 255, ADAPTIVE_THRESH_GAUSSIAN_C, THRESH_BINARY, 11, 2);
        if(logging) write2File(adaptiveThresh, "step_6_adaptive_thresh.png");

        findParentRectangle();

        findBubbles();

        recognizeAnswers();

        score = correct * 5;

        Imgproc.putText(source, String.format("Dogru: %d    Yanlis: %d     Bos: %d    Score: %d", correct,incorrect,empty,score) ,
                new Point(10, 30),
                Core.FONT_HERSHEY_SIMPLEX, 0.9, new Scalar(0, 0, 255), 2);


        sout("*************************************");
        sout("*************************************");
        sout("answer is ....");
        sout("*************************************");
        sout("*************************************");

        answerList  = new ArrayList<>();

        for(int index = 0; index < answers.size(); index++){
            Integer optionIndex = answers.get(index);
            answerList.add((index + 1) + ". " + (optionIndex == null ? "EMPTY/INVALID" : options[optionIndex]));
        }


        StringBuilder builder = new StringBuilder();
        for (String details : answerList) {
            builder.append(details + "\n");
        }

        String result = String.format("\n\n\n"+"***" + "Dogru: %d    Yanlis: %d     Bos: %d    Score: %d"+"***", correct,incorrect,empty,score);

        builder.append(result);

        write2File(source, "result.png");

        this.idx = 0;

        return builder;
    }

    private void findParentRectangle() throws Exception {

        findContours(adaptiveThresh.clone(), contours, hierarchy, RETR_TREE, CHAIN_APPROX_SIMPLE);

        if(logging) sout("getParentRectangle > hiearchy data:\n" + hierarchy.dump());

        // find rectangles
        HashMap<Double, MatOfPoint> rectangles = new HashMap<>();
        for(int i = 0; i < contours.size(); i++){
            MatOfPoint2f approxCurve = new MatOfPoint2f( contours.get(i).toArray() );
            approxPolyDP(approxCurve, approxCurve, 0.02 * arcLength(approxCurve, true), true);

            if(approxCurve.toArray().length == 4){
                rectangles.put((double) i, contours.get(i));
            }
        }

        if(logging) sout("getParentRectangle > contours.size: " + contours.size());
        if(logging) sout("getParentRectangle > rectangles.size: " + rectangles.size());

        int parentIndex = -1;

        // choose hierarchical rectangle which is our main wrapper rect
        for (Map.Entry<Double, MatOfPoint> rectangle : rectangles.entrySet()) {
            double index = rectangle.getKey();

            double[] ids = hierarchy.get(0, (int) index);
            double nextId = ids[0];
            double previousId = ids[1];
//            double childId = ids[2];

            if(nextId != -1 && previousId != -1) continue;

            int k = (int) index;
            int c = 0;

            while(hierarchy.get(0, k)[2] != -1){
                k = (int) hierarchy.get(0, k)[2];
                c++;
            }

            if(hierarchy.get(0, k)[2] != -1) c++;

            if (c >= 3){
                parentIndex = (int) index;
            }

            if(logging) sout("getParentRectangle > index: " + index + ", c: " + c);
        }

        if(logging) sout("getParentRectangle > parentIndex: " + parentIndex);

        if(parentIndex < 0){
            throw new Exception("Couldn't capture main wrapper");
        }

        roi = boundingRect(contours.get(parentIndex));

        if(logging) sout("getParentRectangle > original roi.x: " + roi.x + ", roi.y: " + roi.y);
        if(logging) sout("getParentRectangle > original roi.width: " + roi.width + ", roi.height: " + roi.height);

        int padding = 30;

        roi.x += padding;
        roi.y += padding;
        roi.width -= 2 * padding;
        roi.height -= 2 * padding;

        if(logging) sout("getParentRectangle > modified roi.x: " + roi.x + ", roi.y: " + roi.y);
        if(logging) sout("getParentRectangle > modified roi.width: " + roi.width + ", roi.height: " + roi.height);

        if(logging) write2File(source.submat(roi), "step_7_roi.png");
    }

    private void findBubbles() throws Exception {

        contours.clear();

        findContours(canny.submat(roi), contours, hierarchy, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);

        double threshold = 0;
        double _w = roi.width / this.ratio[0];
        double _h = roi.height / this.ratio[1];
        double minThreshold = Math.floor(Math.min(_w, _h)) - threshold;
        double maxThreshold = Math.ceil(Math.max(_w, _h)) + threshold;

        if(logging) sout("findBubbles > ideal circle size > minThreshold: " + minThreshold + ", maxThreshold: " + maxThreshold);

        List<MatOfPoint> drafts = new ArrayList<>();
        for(MatOfPoint contour : contours){

            Rect _rect = boundingRect(contour);
            int w = _rect.width;
            int h = _rect.height;
            double ratio = Math.max(w, h) / Math.min(w, h);

            if(logging) sout("findBubbles > founded circle > w: " + w + ", h: " + h);

            if(ratio >= 0.9 && ratio <= 1.1)
                if(Math.max(w, h) < maxThreshold && Math.min(w, h) >= minThreshold){
                    drafts.add(contour);
                }
        }

        if(logging) sout("findBubbles > bubbles.size: " + drafts.size());

        if(drafts.size() != questionCount * options.length){
            throw new Exception("Couldn't capture all bubbles.");
        }

        // order bubbles on coordinate system

        sortTopLeft2BottomRight(drafts);

        bubbles = new ArrayList<>();

        for(int j = 0; j < drafts.size(); j+=options.length*2){

            List<MatOfPoint> row = drafts.subList(j, j + options.length*2);

            sortLeft2Right(row);

            bubbles.addAll(row);
        }
    }

    private void recognizeAnswers(){

        for(int i = 0; i< bubbles.size(); i+=options.length) {

            List<MatOfPoint> rows = bubbles.subList(i, i+options.length);

            int[][] filled = new int[rows.size()][4];

            for (int j = 0; j < rows.size(); j++) {

                MatOfPoint col = rows.get(j);

                List<MatOfPoint> list = Arrays.asList(col);

                Mat mask = new Mat(thresh.size(), CvType.CV_8UC1);
                drawContours(mask.submat(roi), list, -1, new Scalar(255, 0, 0), -1);

                Mat conjuction = new Mat(thresh.size(), CvType.CV_8UC1);
                Core.bitwise_and(thresh, mask, conjuction);

//                if(logging) write2File(mask, "mask_" + i + "_" + j + ".png");
//                if(logging) write2File(conjuction, "conjuction_" + i + "_" + j + ".png");

                int countNonZero = Core.countNonZero(conjuction);

                while(countNonZero > 9999) {

                    mask = new Mat(thresh.size(), CvType.CV_8UC1);
                    drawContours(mask.submat(roi), list, -1, new Scalar(255, 0, 0), -1);

                    conjuction = new Mat(thresh.size(), CvType.CV_8UC1);
                    Core.bitwise_and(thresh, mask, conjuction);

                    countNonZero = Core.countNonZero(conjuction);
                }

                if(logging) sout("recognizeAnswers > " + i + ":" + j + " > countNonZero: " + countNonZero);

                filled[j] = new int[]{ countNonZero, i, j};
            }

            int[] selection = chooseFilledCircle(filled);

            if(logging) sout("recognizeAnswers > selection is " + (selection == null ? "empty/invalid" : selection[2]));

            answersBy = getAnswerKey();

            int a = this.idx++;

            if(selection != null){

                if (answersBy.get(a) == selection[2]) {
                    drawContours(source.submat(roi), Arrays.asList(rows.get(selection[2])), -1, new Scalar(0, 255, 0), 3);
                    correct++;
                }
                else if (answersBy.get(a) != selection[2]) {
                    drawContours(source.submat(roi), Arrays.asList(rows.get(answersBy.get(a))), -1, new Scalar(255, 0, 0), 3);
                    incorrect++;
                }
                //                putText(source.submat(roi), "(" + i + "_" + selection[2] + ")", new Point(rows.get(selection[2]).get(0, 0)), Core.FONT_HERSHEY_SIMPLEX, 0.3, new Scalar(0, 255, 0));
            } else {
                drawContours(source.submat(roi), Arrays.asList(rows.get(answersBy.get(a))), -1, new Scalar(0, 0, 255), 3);
                empty++;
            }
            answers.add(selection == null ? null : selection[2]);
        }

        List<Integer> odds = new ArrayList<>();
        List<Integer> evens = new ArrayList<>();
        for(int i = 0; i < answers.size(); i++){
            if(i % 2 == 0) {
                odds.add(answers.get(i));
            }
            if(i % 2 == 1) {
                evens.add(answers.get(i));
            }
        }

        answers.clear();
        answers.addAll(odds);
        answers.addAll(evens);
    }

    private static List<Integer> getAnswerKey()
    {
        List<Integer> answers = new ArrayList<>();
        /*
        answers.add(0); // 1- A
        answers.add(3); // 2- D
        answers.add(1); // 3- B
        answers.add(0); // 4- A
        answers.add(3); // 5- D
        answers.add(0); // 6- A
        answers.add(3); // 7- D
        answers.add(2); // 8- C
        answers.add(0); // 9- A
        answers.add(1); // 10-B
        answers.add(1); // 11-B
        answers.add(0); // 12-A
        answers.add(3); // 13-D
        answers.add(2); // 14-C
        answers.add(1); // 15-B
        answers.add(3); // 16-D
        answers.add(1); // 17-B
        answers.add(2); // 18-C
        answers.add(0); // 19-A
        answers.add(3); // 20-D
        */
        answers.add(0); // 1- A
        answers.add(3); // 2- D
        answers.add(1); // 3- B
        answers.add(1); // B
        answers.add(1); // B
        answers.add(0); // 6- A
        answers.add(3); // 7- D
        answers.add(2); // 8- C
        answers.add(1); // B
        answers.add(2); // C
        answers.add(3); // D
        answers.add(0); // A
        answers.add(1); // B
        answers.add(2); // 14-C
        answers.add(1); // 15-B
        answers.add(3); // 16-D
        answers.add(1); // 17-B
        answers.add(2); // 18-C
        answers.add(0); // 19-A
        answers.add(3); // 20-D


        List<Integer> firstHalf = new ArrayList<>();
        List<Integer> secondHalf = new ArrayList<>();

        for (int i = 0; i < answers.size(); i++) {
            if (i < answers.size() / 2) {
                firstHalf.add(answers.get(i));
            }
            if (i > answers.size() / 2 - 1) {
                secondHalf.add(answers.get(i));
            }
        }

        answers.clear();

        for (int i = 0; i < firstHalf.size(); i++) {
            answers.add(firstHalf.get(i));
            answers.add(secondHalf.get(i));
        }

        return answers;
    }

    private int[] chooseFilledCircle(int[][] rows){

        double mean = 0;
        for(int i = 0; i < rows.length; i++){
            mean += rows[i][0];
        }
        mean = 1.0d * mean / options.length;

        int anomalouses = 0;
        for(int i = 0; i < rows.length; i++){
            if(rows[i][0] > mean) anomalouses++;
        }

        if(anomalouses == options.length - 1){

            int[] lower = null;
            for(int i = 0; i < rows.length; i++){
                if(lower == null || lower[0] > rows[i][0]){
                    lower = rows[i];
                }
            }

            return lower;

        } else {
            return null;
        }
    }
}
