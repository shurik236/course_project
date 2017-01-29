import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;

import java.awt.*;

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
                "Reagent concentration",
                "Product concentration",
                seriesCollection,
                PlotOrientation.VERTICAL,
                true, true, false
        );
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(600, 480));
        renderer = new XYLineAndShapeRenderer();
        chart.getXYPlot().setRenderer(renderer);
        setContentPane(chartPanel);

    }

    private void addGraph(double startX, double startY, Double aParam, Double bParam){
        Calculator graphBuilder = new Calculator(aParam, bParam);
        XYSeries series = graphBuilder.BuildGraphRK(startX, startY, Integer.toString(graphCount));
        seriesCollection.addSeries(series);
        renderer.setSeriesLinesVisible(graphCount, true);
        renderer.setSeriesShapesVisible(graphCount, false);
        renderer.setSeriesPaint(graphCount, Color.blue);
        Stroke stroke = new BasicStroke(0.3f);
        renderer.setSeriesStroke(graphCount, stroke);
        graphCount ++;
    }

    private void addEllipse(double a, double b, double p){
        XYSeries series = InstabilityModel.confidenceEllipse(a, b, p);
        seriesCollection.addSeries(series);
        renderer.setSeriesLinesVisible(graphCount, true);
        renderer.setSeriesShapesVisible(graphCount, false);
        renderer.setSeriesPaint(graphCount, Color.red);
        Stroke stroke = new BasicStroke(0.5f);
        renderer.setSeriesStroke(graphCount, stroke);
        graphCount ++;
    }


    public static void main(String[] args){
        LineChart chart = new LineChart("Brusselator", "Phase portrait");
        chart.pack();
        chart.setVisible(true);
        chart.addGraph(1.5, 1.8, 2.0, 2.0);
        chart.addEllipse(2.0, 2.0, 0.99);

    }

}
