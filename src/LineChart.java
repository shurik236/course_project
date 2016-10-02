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
    Integer graphCount = 0;

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
        setContentPane(chartPanel);

    }

    private void addGraph(double startX, double startY, Double aParam, Double bParam){
        Calculator graphBuilder = new Calculator(aParam, bParam);
        XYSeries series = graphBuilder.BuildGraphRK(startX, startY, graphCount.toString());
        seriesCollection.addSeries(series);
        XYLineAndShapeRenderer rr = new XYLineAndShapeRenderer();
        rr.setSeriesLinesVisible(0, true);
        rr.setSeriesShapesVisible(0, false);
        chart.getXYPlot().setRenderer(rr);
    }

    public static void main(String[] args){
        LineChart chart = new LineChart("Brusselator", "Phase portrait");
        chart.pack();
        chart.setVisible(true);
        chart.addGraph(0.8, 0.8, 2.0, 7.0);
    }

}
