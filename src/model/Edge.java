package model;

public class Edge implements Comparable<Edge> {

    public int src;     // node id
    public int dest;    // node id
    public int weight;  // integer weight / length

    public Edge(int src, int dest, int weight) {
        this.src = src;
        this.dest = dest;
        this.weight = weight;
    }

    @Override
    public int compareTo(Edge other) {
        return this.weight - other.weight;
    }

    @Override
    public String toString() {
        return "(" + src + " - " + dest + ") : " + weight;
    }
}
