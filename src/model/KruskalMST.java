package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KruskalMST {

    class Subset {

        int parent, rank;
    }

    int find(Subset[] subsets, int i) {
        if (subsets[i].parent != i) {
            subsets[i].parent = find(subsets, subsets[i].parent);
        }
        return subsets[i].parent;
    }

    void union(Subset[] subsets, int x, int y) {
        int xroot = find(subsets, x);
        int yroot = find(subsets, y);

        if (subsets[xroot].rank < subsets[yroot].rank) {
            subsets[xroot].parent = yroot;
        } else if (subsets[xroot].rank > subsets[yroot].rank) {
            subsets[yroot].parent = xroot;
        } else {
            subsets[yroot].parent = xroot;
            subsets[xroot].rank++;
        }
    }

    public List<Edge> computeMST(Graph graph) {
        List<Edge> result = new ArrayList<>();
        Collections.sort(graph.edges);

        Subset[] subsets = new Subset[graph.vertices];
        for (int v = 0; v < graph.vertices; ++v) {
            subsets[v] = new Subset();
            subsets[v].parent = v;
            subsets[v].rank = 0;
        }

        int e = 0, i = 0;
        while (e < graph.vertices - 1 && i < graph.edges.size()) {
            Edge next_edge = graph.edges.get(i++);
            int x = find(subsets, next_edge.src);
            int y = find(subsets, next_edge.dest);

            if (x != y) {
                result.add(next_edge);
                union(subsets, x, y);
                e++;
            }
        }
        return result;
    }
}
