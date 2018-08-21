import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.NumberTickUnitSource;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * Created by shuri on 02.08.2017.
 */
public class GraphVisualizer extends JFrame {

    public GraphVisualizer() {
        super("Brusselator");

        JPanel chartPanel = createChartPanel();
        add(chartPanel, BorderLayout.CENTER);

        setSize(640, 480);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private JPanel createChartPanel() {
        //String chartTitle = "u value versus x value";
        String xAxisLabel = "x";
        String yAxisLabel = "u";
        float dash[] = {7.5f, 5.0f};
        float dash2[] = {5.0f, 5.0f};
        float dash3[] = {10.0f, 5.0f};

        XYDataset dataset = createDataset(3, 11, 0.1, 1, 5.0);


        JFreeChart chart = ChartFactory.createXYLineChart("",
                xAxisLabel, yAxisLabel, dataset);
        chart.getXYPlot().setBackgroundPaint(Color.white);
        chart.getXYPlot().setDomainGridlinePaint(Color.darkGray);
        chart.getXYPlot().setRangeGridlinePaint(Color.darkGray);
        chart.getXYPlot().getRenderer().setSeriesPaint(0, Color.blue);
        chart.getXYPlot().getRenderer().setSeriesStroke(0, new BasicStroke(2.0f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 5.0f, dash3, 2.0f));
        chart.getXYPlot().getRenderer().setSeriesStroke(0, new BasicStroke(2.0f));
        chart.getXYPlot().getRenderer().setSeriesPaint(1, new Color(0, 120, 0));
        chart.getXYPlot().getRenderer().setSeriesStroke(1, new BasicStroke(2.5f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 5.0f, dash, 2.0f));
        chart.getXYPlot().getRenderer().setSeriesPaint(2, Color.RED);
        chart.getXYPlot().getRenderer().setSeriesStroke(2, new BasicStroke(3.0f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 5.0f, dash2, 2.0f));
        chart.getXYPlot().getRenderer().setSeriesPaint(3, Color.MAGENTA);
        chart.getXYPlot().getRenderer().setSeriesStroke(3, new BasicStroke(2.0f));
        chart.getXYPlot().getRenderer().setSeriesPaint(4, Color.CYAN);
        chart.getXYPlot().getRenderer().setSeriesStroke(4, new BasicStroke(2.0f));
        chart.getXYPlot().getRenderer().setSeriesPaint(5, Color.orange);
        chart.getXYPlot().getRenderer().setSeriesStroke(5, new BasicStroke(2.0f));

        chart.getXYPlot().getRangeAxis().setAutoRange(true);
        chart.getXYPlot().getDomainAxis().setRange(0, 40);
        chart.getXYPlot().addRangeMarker(new ValueMarker(0.2/1.2, Color.BLACK, new BasicStroke(2.0f)));
        chart.removeLegend();

        chart.getXYPlot().setDomainGridlinesVisible(false);
        chart.getXYPlot().setRangeGridlinesVisible(false);

        Font font = new Font("Dialog", Font.BOLD, 18);
        Font labelFont = new Font("Dialog", Font.BOLD, 18);
        NumberAxis xAxis = (NumberAxis)chart.getXYPlot().getDomainAxis();
        xAxis.setTickUnit(new NumberTickUnit(10));
        xAxis.setLabelFont(labelFont);
        xAxis.setTickLabelFont(font);
        xAxis.setAxisLineVisible(false);

        NumberAxis yAxis = (NumberAxis)chart.getXYPlot().getRangeAxis();
        yAxis.setTickUnit(new NumberTickUnit(0.1));
        yAxis.setTickLabelFont(font);
        yAxis.setLabelFont(labelFont);
        yAxis.setAxisLineVisible(false);

        return new ChartPanel(chart);
    }

    private XYDataset createDataset(double a, double b, double du, double dv, double k) {
        XYSeriesCollection dataset = new XYSeriesCollection();
        for (double l=3.0; l<6.1; l+=1.5) {
            XYSeries series = new XYSeries("a=" + a + ", b=" + b + ", D_u=" + du + ", D_v=" + dv + ", k=" + l);

            SpacialCalculator calc = new SpacialCalculator(a, b, du, dv, 0.08);
            ArrayList<Double[]> grid = calc.calculateFinalState(l);

            double maxValue = -100500;
            double minValue = 100500;
            double spaceIndex = 0;
            for (Double[] pair: grid){
                maxValue = Math.max(maxValue, pair[0]);
                minValue = Math.min(minValue, pair[0]);
                series.add(spaceIndex*0.2, pair[0]);
                spaceIndex++;
            }

            System.out.println(k);
            dataset.addSeries(series);
            //System.out.println("Parameter: "+du+" Value range: ["+minValue+", "+maxValue+"]");
        }

        return dataset;

    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new GraphVisualizer().setVisible(true);
            }
        });
    }

}
