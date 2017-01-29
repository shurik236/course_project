import cern.jet.random.*;
import cern.jet.random.engine.MersenneTwister;
import org.jfree.data.xy.XYSeries;

import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.Date;
import java.util.Vector;

public class InstabilityModel {

    private static Normal normalDistr = new Normal(0, 0.01, new MersenneTwister(new Date()));
    private static MersenneTwister randomGen = new MersenneTwister(new Date());
    private static double aParam;
    private static double bParam;
    private static double epsilon = 1;

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
        return epsilon*sigma1(x,y)*Math.sqrt(-2*Math.log(aParam))*Math.cos(Math.PI*bParam);
    }

    public static double yInstability(double x, double y){
        return epsilon*sigma2(x,y)*Math.sqrt(-2*Math.log(aParam))*Math.sin(Math.PI*bParam);
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

        System.out.print("heeey");

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

    public static void main(String[] args){
        System.out.println(normalDistr.nextDouble());
        System.out.println(randomGen.nextDouble());
        confidenceEllipse(2, 0.2, 0.99);
    }

}
