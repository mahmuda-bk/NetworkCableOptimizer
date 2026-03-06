package model;

import java.util.*;

public class PrimMST {

    public List<Edge> computeMST(Graph graph) {
        List<Edge> result = new ArrayList<>();
        if (graph.vertices == 0) {
            return result;
        }

        Map<Integer, List<Edge>> adj = new HashMap<>();
        for (Edge e : graph.edges) {
            adj.computeIfAbsent(e.src, k -> new ArrayList<>()).add(new Edge(e.src, e.dest, e.weight));
            adj.computeIfAbsent(e.dest, k -> new ArrayList<>()).add(new Edge(e.dest, e.src, e.weight));
        }

        boolean[] visited = new boolean[graph.vertices];
        PriorityQueue<Edge> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a.weight));

        // start from 0
        visited[0] = true;
        if (adj.get(0) != null) {
            pq.addAll(adj.get(0));
        }

        while (!pq.isEmpty()) {
            Edge e = pq.poll();
            if (visited[e.dest]) {
                continue;
            }
            result.add(e);
            visited[e.dest] = true;
            List<Edge> next = adj.get(e.dest);
            if (next != null) {
                for (Edge ne : next) {
                    if (!visited[ne.dest]) {
                        pq.add(ne);
                    }
                }
            }
        }

        return result;
    }
}
