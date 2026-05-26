package com.campus.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import com.campus.repository.ComplaintRepository;
import com.campus.service.PerformanceAnalyticsService;
import com.campus.model.Complaint;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class AdminDashboardController {

    @FXML
    private VBox rootPane;

    @FXML
    private GridPane analyticsCardsGrid;

    @FXML
    private Label totalComplaintsLabel;
    @FXML
    private Label pendingComplaintsLabel;
    @FXML
    private Label resolvedComplaintsLabel;
    @FXML
    private Label avgResponseTimeLabel;

    @FXML
    private BarChart<String, Number> complaintsByStatusChart;
    @FXML
    private CategoryAxis complaintStatusAxis;
    @FXML
    private NumberAxis complaintCountAxis;

    @FXML
    private LineChart<String, Number> responseTimeChart;
    @FXML
    private CategoryAxis responseTimeDayAxis;
    @FXML
    private NumberAxis responseTimeHoursAxis;

    @FXML
    private PieChart complaintPriorityChart;

    @FXML
    private DatePicker dateFilterPicker;
    @FXML
    private Button refreshButton;

    private ComplaintRepository complaintRepository;
    private PerformanceAnalyticsService analyticsService;
    private ScheduledExecutorService autoRefreshExecutor;
    private boolean isVisible = false;

    @FXML
    public void initialize() throws SQLException {
        complaintRepository = new ComplaintRepository();
        analyticsService = new PerformanceAnalyticsService();

        setupCharts();

        dateFilterPicker.setValue(LocalDate.now());
        dateFilterPicker.setOnAction(e -> loadDashboardData());

        refreshButton.setOnAction(e -> loadDashboardData());

        loadDashboardData();
    }
    private void setupCharts() {
        complaintsByStatusChart.setTitle("Complaints by Status");
        complaintStatusAxis.setLabel("Status");
        complaintCountAxis.setLabel("Count");

        responseTimeChart.setTitle("Average Response Time (Last 7 Days)");
        responseTimeDayAxis.setLabel("Date");
        responseTimeHoursAxis.setLabel("Hours");

        complaintPriorityChart.setTitle("Complaints by Priority");
        complaintPriorityChart.setLabelsVisible(true);
    }
    private void loadDashboardData() {
        new Thread(() -> {
            try {
                List<Complaint> allComplaints = complaintRepository.findAll();
                LocalDate filterDate = dateFilterPicker.getValue();

                List<Complaint> filteredComplaints = allComplaints.stream()
                        .filter(c -> c.getCreatedAt().toLocalDate().isEqual(filterDate))
                        .toList();

                updateAnalyticsCards(allComplaints, filteredComplaints);

                Platform.runLater(() -> {
                    updateComplaintsByStatusChart(allComplaints);
                    updateResponseTimeChart(allComplaints);
                    updatePriorityChart(allComplaints);
                });
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Error", "Failed to load dashboard data: " + e.getMessage(), Alert.AlertType.ERROR));
            }
        }).start();
    }

    private void updateAnalyticsCards(List<Complaint> allComplaints, List<Complaint> todayComplaints) {
        int total = allComplaints.size();
        int pending = (int) allComplaints.stream().filter(c -> "NEW".equals(c.getStatus()) || "IN_PROGRESS".equals(c.getStatus())).count();
        int resolved = (int) allComplaints.stream().filter(c -> "RESOLVED".equals(c.getStatus())).count();

        double avgResponseTime = analyticsService.calculateAverageResponseTime(allComplaints);

        Platform.runLater(() -> {
            totalComplaintsLabel.setText(String.valueOf(total));
            pendingComplaintsLabel.setText(String.valueOf(pending));
            resolvedComplaintsLabel.setText(String.valueOf(resolved));
            avgResponseTimeLabel.setText(String.format("%.1f hrs", avgResponseTime));
        });
    }
    private void updateComplaintsByStatusChart(List<Complaint> complaints) {
        Map<String, Integer> statusCounts = new HashMap<>();
        complaints.forEach(c -> statusCounts.merge(c.getStatus(), 1, Integer::sum));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Count");

        statusCounts.forEach((status, count) -> {
            series.getData().add(new XYChart.Data<>(status, count));
        });

        complaintsByStatusChart.getData().clear();
        complaintsByStatusChart.getData().add(series);
    }
    private void updateResponseTimeChart(List<Complaint> complaints) {
        Map<LocalDate, List<Complaint>> complaintsByDate = new HashMap<>();

        complaints.forEach(c -> {
            LocalDate date = c.getCreatedAt().toLocalDate();
            complaintsByDate.computeIfAbsent(date, k -> new ArrayList<>()).add(c);
        });

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Avg Response Time (hours)");
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            List<Complaint> dayComplaints = complaintsByDate.getOrDefault(date, new ArrayList<>());
            double avgTime = analyticsService.calculateAverageResponseTime(dayComplaints);
            series.getData().add(new XYChart.Data<>(date.toString(), avgTime));
        }

        responseTimeChart.getData().clear();
        responseTimeChart.getData().add(series);
    }
    private void updatePriorityChart(List<Complaint> complaints) {
        Map<String, Integer> priorityCounts = new HashMap<>();
        complaints.forEach(c -> priorityCounts.merge(c.getPriority(), 1, Integer::sum));

        ObservableList<PieChart.Data> pieData = javafx.collections.FXCollections.observableArrayList();
        priorityCounts.forEach((priority, count) -> {
            pieData.add(new PieChart.Data(priority + " (" + count + ")", count));
        });

        complaintPriorityChart.setData(pieData);
    }

    public void startAutoRefresh() {
        if (isVisible) {
            return; // Already running
        }
        isVisible = true;

        autoRefreshExecutor = Executors.newScheduledThreadPool(1);
        autoRefreshExecutor.scheduleAtFixedRate(
                this::loadDashboardData,
                30,
                30,
                TimeUnit.SECONDS
        );
    }
    public void stopAutoRefresh() {
        isVisible = false;
        if (autoRefreshExecutor != null && !autoRefreshExecutor.isShutdown()) {
            autoRefreshExecutor.shutdown();
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public VBox getRootPane() {
        return rootPane;
    }

    @Override
    protected void finalize() throws Throwable {
        stopAutoRefresh();
        super.finalize();
    }
}
