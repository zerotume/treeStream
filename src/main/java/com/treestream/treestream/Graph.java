package com.treestream.treestream;

import java.util.*;

public class Graph {

    private Map<DraggableNodeController, List<DraggableNodeController>> adjacencyList;

    public Graph() {
        adjacencyList = new HashMap<>();
    }

    // Add a node to the graph
    public void addNode(DraggableNodeController node) {
        adjacencyList.putIfAbsent(node, new ArrayList<>());
    }

    // Remove a node from the graph
    public void removeNode(DraggableNodeController node) {
        adjacencyList.remove(node);
        // Remove references to this node in other adjacency lists
        for (List<DraggableNodeController> neighbors : adjacencyList.values()) {
            neighbors.remove(node);
        }
    }

    // Add a directed edge from source to target
    public void addEdge(DraggableNodeController source, DraggableNodeController target) {
        adjacencyList.putIfAbsent(source, new ArrayList<>());
        adjacencyList.get(source).add(target);
    }

    // Remove an edge from source to target
    public void removeEdge(DraggableNodeController source, DraggableNodeController target) {
        List<DraggableNodeController> neighbors = adjacencyList.get(source);
        if (neighbors != null) {
            neighbors.remove(target);
        }
    }

    // Get neighbors of a node
    public List<DraggableNodeController> getNeighbors(DraggableNodeController node) {
        return adjacencyList.getOrDefault(node, new ArrayList<>());
    }

    // Get all nodes in the graph
    public Set<DraggableNodeController> getNodes() {
        return adjacencyList.keySet();
    }

    // Check if adding an edge creates a cycle
    public boolean createsCycle(DraggableNodeController source, DraggableNodeController target) {
        // Perform DFS to check for cycles
        Set<DraggableNodeController> visited = new HashSet<>();
        return dfsCycleDetection(target, source, visited);
    }

    private boolean dfsCycleDetection(DraggableNodeController current, DraggableNodeController target, Set<DraggableNodeController> visited) {
        if (current == target) {
            return true; // Cycle detected
        }
        visited.add(current);
        for (DraggableNodeController neighbor : getNeighbors(current)) {
            if (!visited.contains(neighbor)) {
                if (dfsCycleDetection(neighbor, target, visited)) {
                    return true;
                }
            }
        }
        return false;
    }

    // Topological sort
    public List<DraggableNodeController> topologicalSort() {
        Set<DraggableNodeController> visited = new HashSet<>();
        Deque<DraggableNodeController> stack = new ArrayDeque<>();
        for (DraggableNodeController node : adjacencyList.keySet()) {
            if (!visited.contains(node)) {
                topologicalSortUtil(node, visited, stack);
            }
        }
        return new ArrayList<>(stack);
    }

    private void topologicalSortUtil(DraggableNodeController node, Set<DraggableNodeController> visited, Deque<DraggableNodeController> stack) {
        visited.add(node);
        for (DraggableNodeController neighbor : getNeighbors(node)) {
            if (!visited.contains(neighbor)) {
                topologicalSortUtil(neighbor, visited, stack);
            }
        }
        stack.push(node);
    }
}
