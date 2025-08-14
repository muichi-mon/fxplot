package io.github.rajveer.fxplot;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.stage.Stage;

import java.util.*;

/**
 * A simple JavaFX-based plotting utility inspired by Python's Matplotlib.
 * <p>
 * Supports:
 * <ul>
 *     <li>Line plots ("l")</li>
 *     <li>Scatter plots ("s")</li>
 *     <li>Histograms ("h")</li>
 * </ul>
 * <p>
 * Data can be added via {@link #addNumericSeries(String, List)} for
 * numerical XY data or {@link #addCategorySeries(String, List)} for
 * categorical histogram data.
 * <p>
 * Example:
 * <pre>
 * Figure fig = new Figure("Sample Plot", "l");
 * fig.setXLabel("X Axis");
 * fig.setYLabel("Y Axis");
 * fig.addNumericSeries("Series 1", Arrays.asList(new double[]{0, 1}, new double[]{1, 2}));
 * fig.show();
 * </pre>
 */
public class Figure {
    private String title;
    private String type; // "l" = line, "s" = scatter, "h" = histogram
    private String xLabel = "";
    private String yLabel = "Frequency"; // default for histograms
    private final List<SeriesWrapper> seriesList = new ArrayList<>();

    /**
     * Wrapper for holding either numeric or categorical data.
     */
    private static class SeriesWrapper {
        String name;
        List<double[]> numericData;
        List<String> categoryData;
    }

    /**
     * Creates a new figure.
     *
     * @param title the title of the figure
     * @param type  the type of chart ("l" = line, "s" = scatter, "h" = histogram)
     */
    public Figure(String title, String type) {
        this.title = title;
        this.type = type.toLowerCase();
    }

    /**
     * Sets the label for the X-axis.
     *
     * @param label axis label
     */
    public void setXLabel(String label) {
        this.xLabel = label;
    }

    /**
     * Sets the label for the Y-axis.
     *
     * @param label axis label
     */
    public void setYLabel(String label) {
        this.yLabel = label;
    }

    /**
     * Adds a numeric data series for line or scatter plots.
     *
     * @param name the series name
     * @param data a list of double arrays where each array is {x, y}
     */
    public void addNumericSeries(String name, List<double[]> data) {
        SeriesWrapper sw = new SeriesWrapper();
        sw.name = name;
        sw.numericData = data;
        seriesList.add(sw);
    }

    /**
     * Adds a categorical data series for histograms.
     *
     * @param name       the series name
     * @param categories list of category labels
     */
    public void addCategorySeries(String name, List<String> categories) {
        SeriesWrapper sw = new SeriesWrapper();
        sw.name = name;
        sw.categoryData = categories;
        seriesList.add(sw);
    }

    /**
     * Displays the figure in a JavaFX window.
     * <p>
     * Automatically starts the JavaFX runtime if not already running.
     */
    public void show() {
        startJavaFXIfNeeded();

        Platform.runLater(() -> {
            Stage stage = new Stage();
            stage.setTitle(title);

            Scene scene;
            switch (type) {
                case "l":
                    scene = new Scene(createLineChart());
                    break;
                case "s":
                    scene = new Scene(createScatterChart());
                    break;
                case "h":
                    scene = new Scene(createHistogram());
                    break;
                default:
                    throw new IllegalArgumentException("Unknown chart type: " + type);
            }

            stage.setScene(scene);
            stage.show();
        });
    }

    /**
     * Creates a JavaFX LineChart with all added numeric series.
     *
     * @return configured {@link LineChart}
     */
    private LineChart<Number, Number> createLineChart() {
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel(xLabel);
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel(yLabel);

        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle(title);
        chart.setCreateSymbols(false);

        for (SeriesWrapper sw : seriesList) {
            XYChart.Series<Number, Number> s = new XYChart.Series<>();
            s.setName(sw.name);
            for (double[] point : sw.numericData) {
                s.getData().add(new XYChart.Data<>(point[0], point[1]));
            }
            chart.getData().add(s);
        }
        return chart;
    }

    /**
     * Creates a JavaFX ScatterChart with all added numeric series.
     *
     * @return configured {@link ScatterChart}
     */
    private ScatterChart<Number, Number> createScatterChart() {
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel(xLabel);
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel(yLabel);

        ScatterChart<Number, Number> chart = new ScatterChart<>(xAxis, yAxis);
        chart.setTitle(title);

        for (SeriesWrapper sw : seriesList) {
            XYChart.Series<Number, Number> s = new XYChart.Series<>();
            s.setName(sw.name);
            for (double[] point : sw.numericData) {
                s.getData().add(new XYChart.Data<>(point[0], point[1]));
            }
            chart.getData().add(s);
        }
        return chart;
    }

    /**
     * Creates a JavaFX BarChart representing a histogram from categorical data.
     *
     * @return configured {@link BarChart}
     */
    private BarChart<String, Number> createHistogram() {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel(xLabel);
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel(yLabel);

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle(title);

        for (SeriesWrapper sw : seriesList) {
            if (sw.categoryData != null) {
                Map<String, Integer> freqMap = new LinkedHashMap<>();
                for (String cat : sw.categoryData) {
                    freqMap.put(cat, freqMap.getOrDefault(cat, 0) + 1);
                }

                XYChart.Series<String, Number> s = new XYChart.Series<>();
                s.setName(sw.name != null ? sw.name : "Series");
                for (Map.Entry<String, Integer> entry : freqMap.entrySet()) {
                    s.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
                }
                chart.getData().add(s);
            }
        }
        return chart;
    }

    // -------- JavaFX runtime handling --------

    private static boolean javafxStarted = false;

    /**
     * Ensures that the JavaFX runtime is started before attempting to display any charts.
     */
    private static void startJavaFXIfNeeded() {
        if (!javafxStarted) {
            new Thread(() -> Application.launch(EmptyApp.class)).start();
            javafxStarted = true;
            try {
                Thread.sleep(500); // Allow FX thread to initialize
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Minimal JavaFX application to initialize the JavaFX runtime without showing a window.
     */
    public static class EmptyApp extends Application {
        @Override
        public void start(Stage primaryStage) {
        }
    }
}