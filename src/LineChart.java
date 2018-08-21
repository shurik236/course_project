import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;

import java.awt.*;
import java.util.ArrayList;

public class LineChart extends ApplicationFrame{

    XYSeriesCollection seriesCollection;
    JFreeChart chart;
    int graphCount = 0;
    XYLineAndShapeRenderer renderer;

    public LineChart(String appTitle, String chartTitle){

        super(appTitle);
        this.seriesCollection = new XYSeriesCollection();
        this.chart = ChartFactory.createXYLineChart(
                chartTitle,
                "",
                "",
                seriesCollection,
                PlotOrientation.VERTICAL,
                true, true, false
        );
        this.chart.removeLegend();
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(600, 480));
        renderer = new XYLineAndShapeRenderer();
        chart.getXYPlot().setRenderer(renderer);
        chart.getXYPlot().setBackgroundPaint(Color.white);
        chart.getXYPlot().setRangeGridlinePaint(Color.darkGray);
        chart.getXYPlot().setDomainGridlinePaint(Color.darkGray);
        setContentPane(chartPanel);

    }

    private void addGraph(double startX, double startY, Double aParam, Double bParam, boolean noise){
        Calculator graphBuilder = new Calculator(aParam, bParam);
        XYSeries series = graphBuilder.BuildGraphRK(startX, startY, Integer.toString(graphCount), noise);
        seriesCollection.addSeries(series);
        renderer.setSeriesLinesVisible(graphCount, true);
        renderer.setSeriesShapesVisible(graphCount, false);
        renderer.setSeriesPaint(graphCount, Color.blue);
        Stroke stroke = new BasicStroke(0.8f);
        renderer.setSeriesStroke(graphCount, stroke);
        graphCount ++;
    }

    private void addEllipse(double a, double b, double p){
        XYSeries series = InstabilityModel.confidenceEllipse(a, b, p);
        seriesCollection.addSeries(series);
        renderer.setSeriesLinesVisible(graphCount, true);
        renderer.setSeriesShapesVisible(graphCount, false);
        renderer.setSeriesPaint(graphCount, Color.red);
        Stroke stroke = new BasicStroke(2.0f);
        renderer.setSeriesStroke(graphCount, stroke);
        graphCount ++;
    }

    private void addBorders(double a, double b, double p){
        ArrayList<XYDataItem> cycle = Support.getCycle(a, b);
        ArrayList<Double> m = Support.generateSensitivity(cycle, a, b, 1);
        XYSeries outer = InstabilityModel.outerConfidenceBorder(a, b, m, cycle, p, 1);
        XYSeries inner = InstabilityModel.innerConfidenceBorder(a, b, m, cycle, p, 1);

        seriesCollection.addSeries(outer);
        renderer.setSeriesLinesVisible(graphCount, true);
        renderer.setSeriesShapesVisible(graphCount, false);
        renderer.setSeriesPaint(graphCount, Color.red);
        Stroke ostroke = new BasicStroke(1.5f);
        renderer.setSeriesStroke(graphCount, ostroke);
        graphCount ++;

        seriesCollection.addSeries(inner);
        renderer.setSeriesLinesVisible(graphCount, true);
        renderer.setSeriesShapesVisible(graphCount, false);
        renderer.setSeriesPaint(graphCount, Color.red);
        Stroke istroke = new BasicStroke(1.5f);
        renderer.setSeriesStroke(graphCount, istroke);
        graphCount ++;

    }

    private void addSensitivity(double a, double b){
        ArrayList<XYDataItem> cycle = Support.getCycle(a, b);
        ArrayList<Double> m = Support.generateSensitivity(cycle, a, b, 2);

        XYSeries series = new XYSeries("sensitivity", false);
        double t = 0;
        for (double mm : m) {
            t += 0.001;
            series.add(t, mm);
        }

        seriesCollection.addSeries(series);
        renderer.setSeriesLinesVisible(graphCount, true);
        renderer.setSeriesShapesVisible(graphCount, false);
        renderer.setSeriesPaint(graphCount, Color.blue);
        Stroke stroke = new BasicStroke(2.5f);
        renderer.setSeriesStroke(graphCount, stroke);
        graphCount++;
    }

    public void addSense(){
        seriesCollection.addSeries(Support.sensitivityAsPerB());
        renderer.setSeriesLinesVisible(graphCount, true);
        renderer.setSeriesShapesVisible(graphCount, false);
        renderer.setSeriesPaint(graphCount, Color.blue);
        Stroke stroke = new BasicStroke(1.3f);
        renderer.setSeriesStroke(graphCount, stroke);
        graphCount++;
    }


    public static void main(String[] args){
        LineChart chart = new LineChart("", "");
        chart.pack();
        chart.setVisible(true);
        chart.addGraph(0.6, 0.6, 2.0, 2.0, true);
        chart.addGraph(1.0, 0.01, 2.0, 7.0, false);
        chart.addGraph(0.9, 0.02, 2.0, 7.0, false);
        //chart.addGraph(0.8, 0.06, 2.0, 7.0, false);
        chart.addGraph(0.7, 0.4, 2.0, 7.0, false);
        //chart.addEllipse(2.0, 2.0, 0.99);
        //chart.addBorders(2.0, 6.4, 0.95);
        //chart.addSensitivity(2.0, 7.2);
        //chart.addSense();

        //LineChart chart2 = new LineChart("", "");
        //chart2.pack();
        //chart2.setVisible(true);
        //chart2.addSensitivity(2.0, 7.0);


    }

}
