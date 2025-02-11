import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

public class Main extends JFrame {
    private final XYSeriesCollection dataset;
    private final JFreeChart chart;

    public Main() {
        setTitle("Thread Statistics for Different Array Sizes");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        dataset = new XYSeriesCollection();
        chart = ChartFactory.createXYLineChart(
                "Execution Time vs. Number of Threads",
                "Number of Threads",
                "Execution Time (seconds)",
                dataset
        );

        XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();

        Color[] colors = {Color.RED, Color.BLUE, Color.GREEN, Color.ORANGE, Color.MAGENTA, Color.CYAN};

        for (int i = 0; i < colors.length; i++) {
            renderer.setSeriesPaint(i, colors[i]);
            renderer.setSeriesShapesVisible(i, true);
            renderer.setSeriesShapesFilled(i, true);
        }

        plot.setRenderer(renderer);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new ChartPanel(chart), BorderLayout.CENTER);
        getContentPane().add(panel);
    }

    public void addSeries(XYSeries series) {
        dataset.addSeries(series);
    }

    public static void compute(int[][] array, int startRow, int endRow, int[] minArray, int[] maxArray, int index) {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;

        for (int i = startRow; i < endRow; i++) {
            for (int j = 0; j < array[0].length; j++) {
                int currentValue = array[i][j];
                if (currentValue < min) {
                    min = currentValue;
                }
                if (currentValue > max) {
                    max = currentValue;
                }
            }
        }

        minArray[index] = min;
        maxArray[index] = max;
    }

    public static void main(String[] args) {
        int n = 1000;
        int m = 2000;
        int[] multipliers = {1, 2, 3, 6, 10, 12};
        double runAmount = 20.0;

        int[] numThreadsList = {1, 2, 4, 8, 16, 32, 64};
        Main frame = new Main();
        frame.setVisible(true);

        for (int mult : multipliers) {
            int nSize = n * mult;
            int mSize = m * mult;
            int[][] array = new int[nSize][mSize];

            for (int i = 0; i < nSize; i++) {
                for (int j = 0; j < mSize; j++) {
                    array[i][j] = (int) (Math.random() * 10000);
                }
            }

            XYSeries series = new XYSeries("Mult " + mult);
            int min = -1;
            int max = -1;

            for (int numThreads : numThreadsList) {
                double elapsedTime = 0.0;
                double totalMemoryUsage = 0.0;

                for (int i = 0; i < runAmount; i++) {
                    long startTime = System.nanoTime();
                    int[] minArray = new int[numThreads];
                    int[] maxArray = new int[numThreads];
                    Thread[] threads = new Thread[numThreads];
                    int chunkSize = nSize / numThreads;

                    for (int j = 0; j < numThreads; j++) {
                        int startRow = j * chunkSize;
                        int endRow = (j == numThreads - 1) ? nSize : (j + 1) * chunkSize;
                        final int index = j;
                        threads[j] = new Thread(() -> compute(array, startRow, endRow, minArray, maxArray, index));
                        threads[j].start();
                    }

                    for (Thread thread : threads) {
                        try {
                            thread.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    min = minArray[0];
                    max = maxArray[0];
                    for (int j = 0; j < numThreads; j++) {
                        if (minArray[j] < min) min = minArray[j];
                        if (maxArray[j] > max) max = maxArray[j];
                    }

                    long endTime = System.nanoTime();
                    elapsedTime += (endTime - startTime) / 1e9;
                }

                MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
                totalMemoryUsage += memoryBean.getHeapMemoryUsage().getUsed() / (1024.0 * 1024.0);

                System.out.printf("Number of Threads: %d, Total Execution Time: %.5f seconds, Memory Usage: %.2f MB\n",
                        numThreads, (elapsedTime / runAmount), (totalMemoryUsage / runAmount));

                series.add(numThreads, elapsedTime / runAmount);
            }

            System.out.println("Overall Minimum Value: " + min);
            System.out.println("Overall Maximum Value: " + max);
            frame.addSeries(series);
        }
    }
}
