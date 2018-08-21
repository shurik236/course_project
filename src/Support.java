import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Created by shuri on 13.03.2017.
 */
public class Support {

    private static final double d = 0.00002;

    private static double evaluateR(double a, double b, double time, double initX, double initY, double delta, double previousTime, double previousResult){
        double result = 0;
        Calculator calc = new Calculator(time, previousTime, delta, a, b);
        XYSeries points = calc.BuildGraphRK(initX, initY, "", false);

        for (int i = 1; i<points.getItemCount(); i++){
            result += 0.5*delta*(processPoint(points.getDataItem(i), a, b)[0] +
                                 processPoint(points.getDataItem(i-1), a, b)[0]);
        }

        return previousResult*Math.exp(result);
    }


    private static double evaluateH(double a, double b, double time, double initX, double initY, double prevTime, double prevH, double prevR){
        double result = 0;
        double delta = d/5;
        double currentTime = prevTime;
        Calculator calc = new Calculator(time, prevTime, delta, a, b);
        double previousR = prevR;
        XYDataItem point;

        XYSeries points = calc.BuildGraphRK(initX, initY, "", false);

        for (int i = 1; i<points.getItemCount(); i++){
            point = points.getDataItem(i-1);
            double r1 = evaluateR(a, b, delta, point.getXValue(), point.getYValue(), delta/5, currentTime, previousR);
            previousR = r1;
            point = points.getDataItem(i);
            double r2 = evaluateR(a, b, delta, point.getXValue(), point.getYValue(), delta/5, currentTime, previousR);
            previousR = r2;
            currentTime += delta;
            result += 0.5*delta*(processPoint(points.getDataItem(i), a, b)[1]/r2 +
                    processPoint(points.getDataItem(i-1), a, b)[1]/r1);
        }

        return prevH + result;
    }

    private static double distance(XYDataItem a, XYDataItem b){
        return Math.sqrt(
                Math.pow((double)a.getX() - (double)b.getX(), 2) +
                        Math.pow((double)a.getY() - (double)b.getY(), 2));
    }

    public static ArrayList<XYDataItem> getCycle(double a, double b){

        Calculator graphBuilder = new Calculator(a, b);

        XYSeries series = graphBuilder.BuildGraphRK(1, 1, "0", false);
        int count = series.getItemCount() - 1;
        XYDataItem lastPoint = series.getDataItem(count);
        count--;
        XYDataItem nextItem = series.getDataItem(count);
        double allowedDist = distance(lastPoint, nextItem);
        count--;

        ArrayList<XYDataItem> cycle = new ArrayList<>();

        while (count > 0){
            nextItem = series.getDataItem(count);
            cycle.add(nextItem);
            if (distance(nextItem, lastPoint) > allowedDist) {
                count--;
            }
            else
                break;
        }

        Collections.reverse(cycle);

        return cycle;

    }

    private static double[] processPoint(XYDataItem point, double a, double b){
        double x = point.getXValue();
        double y = point.getYValue();

        //system
        BiFunction<Double, Double, Double> f = (_x, _y) -> 1 - (b+1)*_x + a*_x*_x*_y;
        BiFunction<Double, Double, Double> g = (_x, _y) -> b*_x - a*_x*_x*_y;

        //jacobian
        BiFunction<Double, Double, Double> dfdx = (_x, _y) -> 2*a*_x*_y - b - 1;
        BiFunction<Double, Double, Double> dfdy = (_x, _y) -> a*_x*_x;
        BiFunction<Double, Double, Double> dgdx = (_x, _y) -> b - 2*a*_x*_y;
        BiFunction<Double, Double, Double> dgdy = (_x, _y) -> -a*_x*_x;

        //sensitivity matrix (there's just one scalar, so....)
        double s1 = 1;

        double px = -g.apply(x, y)/Math.sqrt(Math.pow(f.apply(x, y), 2) + Math.pow(g.apply(x, y), 2));
        double py = f.apply(x,y)/Math.sqrt(Math.pow(f.apply(x, y), 2) + Math.pow(g.apply(x, y), 2));

        double at = px*px*2*(dfdx.apply(x, y)) + 2*px*py*(dfdy.apply(x, y)+dgdx.apply(x, y)) + 2*py*py*(dgdy.apply(x, y));

        double bt = s1*px*px;

        return new double[]{at, bt};
    }


    public static ArrayList<Double> generateSensitivity(ArrayList<XYDataItem> cycle, double a, double b, int skip){
        ArrayList<Double> sensitivity = new ArrayList<>();
        ArrayList<Double> rr = new ArrayList<>();
        ArrayList<Double> hh = new ArrayList<>();
        XYDataItem point;
        double delta = d;
        double t = delta*skip;
        int count = cycle.size();
        double lastR = 1.0;
        double lastH = 0;
        double r, h;

        XYDataItem start = cycle.get(0);
        rr.add(1.0);
        hh.add(0.0);

        for (int i = skip; i < count; i+=skip){
            point = cycle.get(i);
            r = evaluateR(a, b, delta*skip, point.getXValue(), point.getYValue(), delta/10, t, lastR);
            h = evaluateH(a, b, delta*skip, point.getXValue(), point.getYValue(), t, lastH, lastR);
            rr.add(r);
            hh.add(h);
            t += delta*skip;
            lastR = r;
            lastH = h;
            //System.out.println(i);
        }

        double rn = rr.get(rr.size()-1);
        double hn = hh.get(hh.size()-1);

        double c = rn*hn/(1-rn);

        for (int i = 0; i < rr.size(); i++)
            sensitivity.add(rr.get(i)*(c+hh.get(i)));

        return sensitivity;

    }

    public static XYSeries sensitivityAsPerB(){
        XYSeries bGraph = new XYSeries("sensitivity", false);
        for (double b=3.5; b<=7.9; b+=0.01) {
            ArrayList<XYDataItem> cycle = getCycle(2, b);
            ArrayList<Double> sensitivity = generateSensitivity(cycle, 2, b, 20);
            System.out.println(b);
            double maxSensitivity = Collections.max(sensitivity);
            System.out.println(maxSensitivity);
            bGraph.add(b, maxSensitivity);
        }
        System.out.println("All done!");
        return bGraph;
    }

    public static void main(String[] args) {

        LineChart chart = new LineChart("Sensitivity", "Sensitivity as per time");
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        chart.renderer = renderer;
        XYSeriesCollection seriesCollection = new XYSeriesCollection();
        chart.seriesCollection = seriesCollection;
        XYSeries series = new XYSeries("sensitivity", false);
        ArrayList<XYDataItem> cycle = getCycle(2, 3.5);
        ArrayList<Double> sensitivity = generateSensitivity(cycle, 2, 3.5, 50);
        double t = 0;
        for (double m : sensitivity) {
            t += 0.1;
            series.add(t, m);
        }

        seriesCollection.addSeries(series);
        renderer.setSeriesLinesVisible(0, true);
        renderer.setSeriesShapesVisible(0, false);
        renderer.setSeriesPaint(0, Color.blue);
        Stroke stroke = new BasicStroke(0.3f);
        renderer.setSeriesStroke(0, stroke);


        chart.pack();
        chart.setVisible(true);
    }

}
