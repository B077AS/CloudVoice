package main.model;

import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.animation.Animation.Status;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PingGraphBox extends VBox {

	private XYChart.Series<Number, Number> series;
	private long startTime;
	private LineChart<Number, Number> lineChart;
	private Timeline timeline;
	private Label pingLabel;
	private final ExecutorService executorService = Executors.newSingleThreadExecutor();
	private int counter=0;

	public PingGraphBox(double width, double height) {
		this.setMaxWidth(width);
		this.setMaxHeight(height);
		this.setAlignment(Pos.CENTER);
		
		this.pingLabel=new Label("Ping");
		Platform.runLater(() -> {
			this.pingLabel.setStyle("-fx-text-fill: -primary-color; -fx-font-weight: bold;");
		});

		NumberAxis xAxis = new NumberAxis();
		NumberAxis yAxis = new NumberAxis();    
		xAxis.setTickUnit(1);      
		yAxis.setTickUnit(1);
		xAxis.setTickMarkVisible(false);
		xAxis.setMinorTickVisible(false);
		xAxis.setTickLabelsVisible(false);
		yAxis.setMinorTickVisible(false);
		xAxis.setAutoRanging(false);
		yAxis.setAnimated(false);
		xAxis.setUpperBound(20);

		this.lineChart = new LineChart<>(xAxis, yAxis);
		this.lineChart.setMinHeight(height);
		this.lineChart.setMinWidth(width);
		this.lineChart.setLegendVisible(false);
		this.lineChart.setAlternativeRowFillVisible(false);
		this.lineChart.setAlternativeColumnFillVisible(false);
		this.lineChart.setCreateSymbols(false);
		this.lineChart.setAnimated(false);	
		this.lineChart.setStyle("-fx-background-color: transparent;");
		this.lineChart.lookup(".chart-horizontal-grid-lines").setStyle("-fx-stroke: rgba(110,110, 110, 0.2); -fx-stroke-dash-array: none;");
		this.lineChart.lookup(".chart-vertical-grid-lines").setStyle("-fx-stroke: rgba(110,110, 110, 0.2); -fx-stroke-dash-array: none;");
		this.lineChart.lookup(".chart-plot-background").setStyle("-fx-background-color: transparent;");
		this.series = new XYChart.Series<>();
		this.lineChart.getData().add(series);

		this.getChildren().addAll(pingLabel, lineChart);
	}

	public void startPinging() {
		// Start a separate thread to continuously update the chart
		timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
			if (startTime == 0) {
				startTime = System.currentTimeMillis();
			}
			long currentTime = System.currentTimeMillis();
			double elapsedTime = (currentTime - startTime) / 1000.0; // Convert milliseconds to seconds

			executorService.submit(() -> {
				long pingTime = getPingTime(VPS.SERVER_ADDRESS); // Example IP address
				javafx.application.Platform.runLater(() -> {
					updateChart(elapsedTime, pingTime);
				});
			});
		}));
		timeline.setCycleCount(Timeline.INDEFINITE);
		timeline.play();
	}

	public void updateChart(double elapsedTime, long pingTime) {
		pingLabel.setText("Ping: " + String.valueOf(pingTime) + " ms");
		series.getData().add(new XYChart.Data<>(elapsedTime, pingTime));
		if (series.getData().size() > 21) { // Limit number of data points to keep the chart readable
			series.getData().remove(0);
			// Shift every other element back one place
			for (int i = 0; i < series.getData().size(); i++) {
				series.getData().get(i).setXValue(i); // Update the X value to reflect the new index
			}
		}
	}

	public int getPingTime(String ipAddress) {
		try {
			InetAddress inet = InetAddress.getByName(ipAddress);
			long startTime = System.currentTimeMillis();
			if (inet.isReachable(5000)) { // Timeout set to 5 seconds
				long endTime = System.currentTimeMillis();
				return (int) Math.round(endTime - startTime);
			} else {
				return -1; // Return -1 if not reachable
			}
		} catch (Exception e) {
			e.printStackTrace();
			return -1; // Return -1 in case of exception
		}
	}

	public void stopPinging() {
		// Stop the timeline if it's running
		if (this.timeline != null && this.timeline.getStatus() == Status.RUNNING) {
			this.timeline.stop();
			this.startTime = 0; // Reset the start time
		}
		this.executorService.shutdownNow();
	}

	public int getCounter() {
		return this.counter;
	}

	public void setCounter(int counter) {
		this.counter = counter;
	}
}