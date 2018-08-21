import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;

import java.util.ArrayList;
import java.util.Random;
import java.util.function.BiFunction;

public class InstabilityModel {

    private static Random randomGen = new Random();
    private static double aParam;
    private static double bParam;
    private static double epsilon = 0.00;

    private static double sigma1(double x, double y){
        return 1;
    }

    private static double sigma2(double x, double y){
        return 0;
    }

    public static void nextRandomParameters(){
        aParam = randomGen.nextDouble();
        bParam = randomGen.nextDouble();
    }

    public static double xInstability(double x, double y){
        return epsilon*sigma1(x,y)*Math.sqrt(-2*Math.log(aParam))*Math.cos(2*Math.PI*bParam);
    }

    public static double yInstability(double x, double y){
        return epsilon*sigma2(x,y)*Math.sqrt(-2*Math.log(aParam))*Math.sin(2*Math.PI*bParam);
    }

    public static XYSeries confidenceEllipse(double a, double b, double probability) {
        double stableX = 1;
        double stableY = b/a;

        double[] eigenvalues = new double[]{
            (a + a*a + b*b - Math.sqrt(-4*a*b*b + Math.pow(-a-a*a-b*b, 2)))/(4*a*(1+a-b)),
            (a + a*a + b*b + Math.sqrt(-4*a*b*b + Math.pow(-a-a*a-b*b, 2)))/(4*a*(1+a-b))
        };

        double valA = (a + a*a - b*b - Math.sqrt(a*a + 2*a*a*a + a*a*a*a - 2*a*b*b + 2*a*a*b*b + b*b*b*b))/(-2*b*a);
        double valB = (a + a*a - b*b + Math.sqrt(a*a + 2*a*a*a + a*a*a*a - 2*a*b*b + 2*a*a*b*b + b*b*b*b))/(-2*b*a);

        double lenA = Math.sqrt(valA*valA + 1);
        double lenB = Math.sqrt(valB*valB + 1);

        double[][] eigenvectors = new double[][]{
            new double[]{valA/lenA, 1/lenA},
            new double[]{valB/lenB, 1/lenB}
        };


        XYSeries ellipse = new XYSeries("ellipse", false);
        double z1, z2, x, y;
        double d = eigenvectors[0][0]*eigenvectors[1][1] - eigenvectors[0][1]*eigenvectors[1][0];
        double k = Math.sqrt(-Math.log(1 - probability));
        for (double phi=0; phi<2*Math.PI; phi+=0.01) {
            z1 = Math.sqrt(2*eigenvalues[0])*epsilon*k*Math.cos(phi);
            z2 = Math.sqrt(2*eigenvalues[1])*epsilon*k*Math.sin(phi);
            x = stableX + (z1*eigenvectors[1][1] - z2*eigenvectors[0][1])/d;
            y = stableY + (z2*eigenvectors[0][0] - z1*eigenvectors[1][0])/d;
            ellipse.add(x, y);
        }

        return ellipse;
    }


    public static XYSeries outerConfidenceBorder(double a, double b, ArrayList<Double> m, ArrayList<XYDataItem> cycle, double probability, int skip){
        double xNext;
        double yNext;

        XYSeries border = new XYSeries("outer", false);

        BiFunction<Double, Double, Double> f = (_x, _y) -> 1 - (b+1)*_x + a*_x*_x*_y;
        BiFunction<Double, Double, Double> g = (_x, _y) -> b*_x - a*_x*_x*_y;

        for (int i = 0; i<m.size(); i++){
            double x = cycle.get(i*skip).getXValue();
            double y = cycle.get(i*skip).getYValue();

            double px = -g.apply(x, y)/Math.sqrt(Math.pow(f.apply(x, y), 2) + Math.pow(g.apply(x, y), 2));
            double py = f.apply(x, y)/Math.sqrt(Math.pow(f.apply(x, y), 2) + Math.pow(g.apply(x, y), 2));

            xNext = x + epsilon*1.386*Math.sqrt(m.get(i)*2) * px;
            yNext = y + epsilon*1.386*Math.sqrt(m.get(i)*2) * py;

            border.add(xNext, yNext);
        }

        return border;

    }

    public static XYSeries innerConfidenceBorder(double a, double b, ArrayList<Double> m, ArrayList<XYDataItem> cycle, double probability, int skip){
        double xNext;
        double yNext;

        XYSeries border = new XYSeries("inner", false);

        BiFunction<Double, Double, Double> f = (_x, _y) -> 1 - (b+1)*_x + a*_x*_x*_y;
        BiFunction<Double, Double, Double> g = (_x, _y) -> b*_x - a*_x*_x*_y;

        for (int i = 0; i<m.size(); i++){
            double x = cycle.get(i*skip).getXValue();
            double y = cycle.get(i*skip).getYValue();

            double px = -g.apply(x, y)/Math.sqrt(Math.pow(f.apply(x, y), 2) + Math.pow(g.apply(x, y), 2));
            double py = f.apply(x, y)/Math.sqrt(Math.pow(f.apply(x, y), 2) + Math.pow(g.apply(x, y), 2));

            xNext = x - epsilon*1.386*Math.sqrt(m.get(i)*2) * px;
            yNext = y - epsilon*1.386*Math.sqrt(m.get(i)*2) * py;

            border.add(xNext, yNext);
        }

        return border;

    }

    public static void main(String[] args){
        System.out.println(randomGen.nextDouble());
        System.out.println(randomGen.nextDouble());
        System.out.println(randomGen.nextDouble());
        System.out.println(randomGen.nextDouble());
        System.out.println(randomGen.nextDouble());
    }

}
