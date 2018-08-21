import java.io.*;
import java.util.ArrayList;
import java.util.Random;

public class SpacialCalculator {
    private double aParam = 2;
    private double bParam = 1;
    private double sParam = 1.2;
    private double cParam = 1;
    private double alpha = 1;
    private double beta = 4;
    private double gamma = 0.5;
    private double eta = gamma;
    private double epsilon = 1;
    private double tubeSize = 200;
    private double timeSpan = 2500000;
    private double uIntensity = 0.003;
    private double vIntensity = 0.003;
    private double offset;
    private double uDiffusion;
    private double vDiffusion;
    private String path = "C:\\Users\\shuri\\Desktop\\spacial\\E_Brus\\Grid_Data";

    public static double tubeFragment = 0.2;
    public static double timeFragment = 0.00001;


    public SpacialCalculator(double aParam, double bParam, double uDiffusion, double vDiffusion, double offset){
        this.aParam = aParam;
        this.bParam = bParam;
        this.uDiffusion = uDiffusion;
        this.vDiffusion = vDiffusion;
        this.offset = offset;
    }

    private Double[] generateBoxMuller(){
        Random r = new Random();
        double a = r.nextDouble();
        double b = r.nextDouble();
        return new Double[]{
                Math.sqrt(-2*Math.log(a))*Math.cos(2*Math.PI*b),
                Math.sqrt(-2*Math.log(a))*Math.sin(2*Math.PI*b)
        };
    }

    private double f(double u, double v){ return aParam - (bParam + 1)*u + u*u*v; }

    private double g(double u, double v){ return bParam*u - u*u*v; }

    private double f2(double u, double v){ return alpha*u*(1-u-v/(1+beta*u)); }

    private double g2(double u, double v){ return v*(beta*u/(1+beta*u) - gamma*v/(epsilon+eta*v)); }

    private double f11(double u, double v){ return u*(1-u) - v*Math.sqrt(u); }

    private double g11(double u, double v){ return cParam*v*Math.sqrt(u) - sParam*v*v; }


    private ArrayList<Double[]> generateInitialValues(double l){

        ArrayList<Double[]> initialTube = new ArrayList<>();
        //double k = 1/(bParam - 1);
        //double m = bParam*(bParam- cParam -1)/(aParam*(bParam-1)*(bParam-1));
        //double k = (sParam - cParam)/sParam;
        //double m = cParam*Math.sqrt(k)/sParam;
        for (int i = 0; i<=tubeSize; i++) {
            initialTube.add(new Double[]{
                    5 + offset*Math.cos(2*l*Math.PI*tubeFragment*i/(tubeFragment*tubeSize)),
                    6 + offset*Math.cos(2*l*Math.PI*tubeFragment*i/(tubeFragment*tubeSize))
            });
        }

        return initialTube;
    }


    public Double[] calculateCell(int index, ArrayList<Double[]> prevState, Double[] disturbance){
        double cu, cv;

        cu = timeFragment * (f(prevState.get(index)[0], prevState.get(index)[1]) +
                uDiffusion * (
                        prevState.get(index - 1)[0] - 2 * prevState.get(index)[0] + prevState.get(index + 1)[0]) / (tubeFragment * tubeFragment)) +
                prevState.get(index)[0] + disturbance[0] * uIntensity;

        cv = timeFragment * (g(prevState.get(index)[0], prevState.get(index)[1]) +
                vDiffusion * (
                        prevState.get(index - 1)[1] - 2 * prevState.get(index)[1] + prevState.get(index + 1)[1]) / (tubeFragment * tubeFragment)) +
                prevState.get(index)[1] + disturbance[1] * vIntensity;

        return new Double[]{Math.max(cu, 0), Math.max(cv, 0)};
    }

    public ArrayList<Double[]> calculateNextState(ArrayList<Double[]> prevState){
        ArrayList<Double[]> result = new ArrayList<>();
        //calculating left edge
        result.add(calculateCell(1, prevState, generateBoxMuller()));

        for (int r = 1; r < tubeSize; r++)
            result.add(calculateCell(r, prevState, generateBoxMuller()));
        //adding right edge
        result.add(result.get(result.size() - 1));

        return result;
    }

    public ArrayList<Double[]> calculateFinalState(double waveNumber){
        ArrayList<Double[]> prevState = generateInitialValues(waveNumber);
        //ArrayList<Double[]> prevState = loadResult("a="+aParam+"_b="+bParam+"_Du="+uDiffusion+"_Dv="+vDiffusion+"_k="+waveNumber+"_intensity="+uIntensity);
        ArrayList<Double[]> nextState = new ArrayList<>();
        for (int t = 1; t<timeSpan; t++){
            nextState = calculateNextState(prevState);
            prevState = nextState;

            if (t%15000 == 0) System.out.println(Math.floorDiv(t, 15000));
        }

        return nextState;
    }

    public ArrayList<ArrayList<Double[]>> calculateTimeGrid(double waveNumber){
        ArrayList<ArrayList<Double[]>> resultGrid = new ArrayList<>();
        ArrayList<Double[]> prevState = generateInitialValues(waveNumber);
        //ArrayList<Double[]> prevState = calculateFinalState(waveNumber);
        //uIntensity = 0.0003;
        //vIntensity = 0.0003;
        //timeSpan = 5000000;
        //ArrayList<Double[]> prevState = loadResult("a="+aParam+"_b="+bParam+"_Du="+uDiffusion+"_Dv="+vDiffusion+"_k="+waveNumber+"_intensity="+uIntensity);
        ArrayList<Double[]> nextState;

        for (int t = 0; t<timeSpan; t++){
            nextState = calculateNextState(prevState);
            prevState = nextState;
            if (t%1000 == 0){
                System.out.println(Math.floorDiv(t, 1000));
                //System.out.println(prevState.get(120)[0]);
                //System.out.println(prevState.get(120)[1]);
                resultGrid.add(prevState);
            }
        }

        saveResult(resultGrid.get(resultGrid.size()-1),
                "a="+aParam+"_b="+bParam+"_Du="+uDiffusion+"_Dv="+vDiffusion+"_k="+waveNumber+"_intensity="+uIntensity);
        return resultGrid;
    }

    public void saveResult(ArrayList<Double[]> result, String filename){
        try {
            FileOutputStream fileOut =
                    new FileOutputStream(path + "\\" + filename);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(result);
            out.close();
            fileOut.close();
        } catch (IOException i) {
            i.printStackTrace();
        }
    }

    public ArrayList<Double[]> loadResult(String filename){
        ArrayList<Double[]> result;
        try {
            FileInputStream fileIn = new FileInputStream(path + "\\" + filename);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            result = (ArrayList<Double[]>) in.readObject();
            in.close();
            fileIn.close();
            return result;
        } catch (IOException i) {
            i.printStackTrace();
            return null;
        } catch (ClassNotFoundException c) {
            System.out.println("Class not found");
            c.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {

    }

}
