import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.PaintScale;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.chart.title.PaintScaleLegend;
import org.jfree.data.Range;
import org.jfree.data.xy.*;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleEdge;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * Created by shuri on 22.10.2017.
 */
public class GridVisualizer extends JFrame {
    private double a = 3;
    private double b = 11;
    private double uDiffusion = 2.0;
    private double vDiffusion = 10;
    private double waveNumber = 2.0;
    private double offset = 0.25;
    private ArrayList<Double[]> result = new ArrayList<>();

    public GridVisualizer() {
        super("Brusselator time grid");

        JPanel chartPanel = createChartPanel();
        JPanel fourierPanel = createFourierPanel();
        chartPanel.setPreferredSize(new Dimension(640, 480));
        //fourierPanel.setPreferredSize(new Dimension(640, 480));
        add(chartPanel, BorderLayout.CENTER);
        //add(fourierPanel, BorderLayout.EAST);

        setSize(640, 480);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private JPanel createChartPanel(){
        JPanel content = new JPanel(new BorderLayout());
        XYZDataset dataset = createDataSet();
        Font font = new Font("Dialog", Font.BOLD, 18);

        NumberAxis xAxis = new NumberAxis("x");
        xAxis.setLowerMargin(0.0);
        xAxis.setUpperMargin(0.0);
        xAxis.setTickUnit(new NumberTickUnit(10));
        xAxis.setTickLabelFont(font);
        xAxis.setLabelFont(font);
        xAxis.setRange(0, 40);

        NumberAxis yAxis = new NumberAxis("t");
        yAxis.setTickUnit(new NumberTickUnit(5));
        yAxis.setTickLabelFont(font);
        yAxis.setLabelFont(font);
        yAxis.setUpperMargin(0.0);
        yAxis.setLowerMargin(0.0);


        NumberAxis cAxis = new NumberAxis("");
        cAxis.setTickLabelFont(font);
        cAxis.setTickUnit(new NumberTickUnit(1.0));
        cAxis.setTickLabelFont(font);
        PaintScale paintScale = new SpectrumPaintScale(0.0, 8.0);
        PaintScaleLegend psLegend = new PaintScaleLegend(paintScale, cAxis);
        psLegend.getAxis().setRange(0.0, 8.0);
        psLegend.setMargin(0, 100, 0, 8);
        psLegend.setBackgroundPaint(Color.WHITE);

        XYBlockRenderer renderer = new XYBlockRenderer();
        renderer.setPaintScale(paintScale);
        renderer.setBlockHeight(0.1);
        renderer.setBlockWidth(0.2);
        renderer.setBlockAnchor(RectangleAnchor.BOTTOM_LEFT);

        XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);
        plot.setRangeGridlinePaint(Color.white);
        plot.setDomainGridlineStroke(new BasicStroke(0.1f));
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlineStroke(new BasicStroke(0.1f));

        JFreeChart chart = new JFreeChart("", plot);
        chart.removeLegend();
        chart.addSubtitle(psLegend);
        psLegend.setPosition(RectangleEdge.BOTTOM);
        chart.setBackgroundPaint(Color.WHITE);

        content.add(new ChartPanel(chart));

        return content;
    }

    private JPanel createFourierPanel(){
        JPanel content = new JPanel(new BorderLayout());
        XYDataset dataset = createFourierDataset(result);
        JFreeChart fchart = ChartFactory.createXYLineChart("FourierTransform",
                "WaveNumber", "Intensity", dataset);
        fchart.getXYPlot().setBackgroundPaint(Color.white);
        fchart.getXYPlot().setDomainGridlinePaint(Color.darkGray);
        fchart.getXYPlot().setRangeGridlinePaint(Color.darkGray);
        fchart.getXYPlot().getDomainAxis(0).setRange(new Range(0, 100));
        fchart.getXYPlot().getRenderer().setSeriesPaint(0, Color.blue);
        fchart.getXYPlot().getRenderer().setSeriesPaint(1, Color.red);
        fchart.getXYPlot().getRenderer().setSeriesStroke(0, new BasicStroke(2.0f));
        fchart.getXYPlot().getRenderer().setSeriesStroke(1, new BasicStroke(2.0f));

        content.add(new ChartPanel(fchart));
        return new ChartPanel(fchart);
    }

    private XYZDataset createDataSet(){
        DefaultXYZDataset dataset = new DefaultXYZDataset();
        SpacialCalculator calculator = new SpacialCalculator(a, b, uDiffusion, vDiffusion, offset);
        ArrayList<ArrayList<Double[]>> data = calculator.calculateTimeGrid(waveNumber);

        result = data.get(data.size()-1);

        int timeCount = data.size();
        int tubeCount = data.get(0).size();
        double[] xValues = new double[timeCount*tubeCount];
        double[] yValues = new double[timeCount*tubeCount];
        double[] zValues = new double[timeCount*tubeCount];

        for(int t=0; t < timeCount; t++){
            for (int r = 0; r < tubeCount; r++) {
                xValues[tubeCount * t + r] = SpacialCalculator.tubeFragment * r;
                yValues[tubeCount * t + r] = SpacialCalculator.timeFragment * t*1000;
                zValues[tubeCount * t + r] = data.get(t).get(r)[0];
            }

            if (t%100 == 0){
                double[][] transformed = applyTransform(data.get(t));
                for (int i=0; i<50; i++){
                    System.out.println("[Time: "+t+" Index: "+i+" Real: "+transformed[0][i]+" Imag: "+transformed[1][i]+"]");
                }
            }

        }

        dataset.addSeries("Concentration time grid", new double[][]{xValues, yValues, zValues});

        return dataset;
    }

    private double[][] applyTransform(ArrayList<Double[]> data){
        double[] realfunc = new double[data.size()];
        double[] imagfunc = new double[data.size()];
        for (int i=0; i<data.size(); i++){
            realfunc[i] = data.get(i)[0];
            imagfunc[i] = 0;
        }
        double[][] result = new double[][]{realfunc, imagfunc};
        FFT.transform(realfunc, imagfunc);
        return result;
    }

    public XYDataset createFourierDataset(ArrayList<Double[]> data){
        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeries realSeries = new XYSeries("RealParts");
        XYSeries imagSeries = new XYSeries("ImaginaryParts");

        double[][] transformed = applyTransform(data);

        for (int i = 0; i<data.size(); i++) {
            imagSeries.add(i, transformed[1][i]);
            realSeries.add(i, transformed[0][i]);
        }

        dataset.addSeries(realSeries);
        dataset.addSeries(imagSeries);
        return dataset;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new GridVisualizer().setVisible(true);
            }
        });
    }

}
