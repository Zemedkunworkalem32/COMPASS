package com.compass.navigation;

import com.compass.model.CampusLocation;
import com.compass.service.RepositoryFactory;

import java.util.*;

/**
 * Shortest path between campus buildings using Dijkstra's algorithm.
 */
public class RouteFinder {
    private final Map<Integer, List<Edge>> graph = new HashMap<>();

    public RouteFinder() {
        loadGraphFromDatabase();
    }

    public RouteFinder(Map<Integer, List<Edge>> graph) {
        this.graph.putAll(graph);
    }

    private void loadGraphFromDatabase() {
        RepositoryFactory.locations().getEdges().forEach(edge -> {
            int from = edge[0];
            int to = edge[1];
            int weight = edge[2];
            addEdge(from, to, weight);
        });
    }

    public void addEdge(int from, int to, double weight) {
        graph.computeIfAbsent(from, k -> new ArrayList<>()).add(new Edge(to, weight));
    }

    public RouteResult findShortestPath(int startId, int endId) {
        if (startId == endId) {
            return new RouteResult(List.of(startId), 0);
        }
        Map<Integer, Double> distances = new HashMap<>();
        Map<Integer, Integer> previous = new HashMap<>();
        PriorityQueue<Node> queue = new PriorityQueue<>(Comparator.comparingDouble(n -> n.distance));

        for (Integer node : graph.keySet()) {
            distances.put(node, Double.POSITIVE_INFINITY);
        }
        distances.put(startId, 0.0);
        queue.add(new Node(startId, 0));

        while (!queue.isEmpty()) {
            Node current = queue.poll();
            if (current.id == endId) {
                break;
            }
            if (current.distance > distances.getOrDefault(current.id, Double.MAX_VALUE)) {
                continue;
            }
            for (Edge edge : graph.getOrDefault(current.id, List.of())) {
                double alt = distances.get(current.id) + edge.weight;
                if (alt < distances.getOrDefault(edge.to, Double.POSITIVE_INFINITY)) {
                    distances.put(edge.to, alt);
                    previous.put(edge.to, current.id);
                    queue.add(new Node(edge.to, alt));
                }
            }
        }

        if (!previous.containsKey(endId) && startId != endId) {
            return new RouteResult(List.of(), -1);
        }

        List<Integer> path = new ArrayList<>();
        Integer current = endId;
        path.add(current);
        while (previous.containsKey(current)) {
            current = previous.get(current);
            path.add(0, current);
        }
        if (path.get(0) != startId) {
            return new RouteResult(List.of(), -1);
        }
        return new RouteResult(path, distances.getOrDefault(endId, -1.0));
    }

    public String formatRoute(List<Integer> locationIds, List<CampusLocation> allLocations) {
        if (locationIds.isEmpty()) {
            return "No route found";
        }
        Map<Integer, String> names = new HashMap<>();
        for (CampusLocation loc : allLocations) {
            names.put(loc.getLocationId(), loc.getLocationName());
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < locationIds.size(); i++) {
            if (i > 0) {
                sb.append(" → ");
            }
            sb.append(names.getOrDefault(locationIds.get(i), "Building " + locationIds.get(i)));
        }
        return sb.toString();
    }

    public record Edge(int to, double weight) {}
    public record RouteResult(List<Integer> path, double totalDistanceMeters) {}
    private record Node(int id, double distance) {}
}
