package util;

import model.Node;
import model.Edge;

import java.io.*;
import java.util.*;
import java.nio.charset.StandardCharsets;

public class FileManager {

    public static void saveAsJson(File file, List<Node> nodes, List<Edge> edges, String backgroundImagePath, double costPerMeter) throws IOException {
        try (Writer w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            sb.append("{\n");
            sb.append("  \"background\": ").append(backgroundImagePath == null ? "null" : "\"" + escape(backgroundImagePath) + "\"").append(",\n");
            sb.append("  \"costPerMeter\": ").append(costPerMeter).append(",\n");

            sb.append("  \"nodes\": [\n");
            for (int i = 0; i < nodes.size(); i++) {
                Node n = nodes.get(i);
                sb.append("    {\"id\":").append(n.id)
                        .append(",\"x\":").append(n.x)
                        .append(",\"y\":").append(n.y)
                        .append(",\"deviceType\":\"").append(escape(n.deviceType)).append("\"")
                        .append(",\"label\":\"").append(escape(n.label)).append("\"")
                        .append("}");
                if (i < nodes.size() - 1) {
                    sb.append(",");
                }
                sb.append("\n");
            }
            sb.append("  ],\n");

            sb.append("  \"edges\": [\n");
            for (int i = 0; i < edges.size(); i++) {
                Edge e = edges.get(i);
                sb.append("    {\"src\":").append(e.src).append(",\"dest\":").append(e.dest).append(",\"weight\":").append(e.weight).append("}");
                if (i < edges.size() - 1) {
                    sb.append(",");
                }
                sb.append("\n");
            }
            sb.append("  ]\n");
            sb.append("}\n");
            w.write(sb.toString());
        }
    }

    public static LoadedData loadFromJson(File file) throws IOException {
        String content;
        try (Reader r = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            char[] buf = new char[4096];
            int n;
            while ((n = r.read(buf)) != -1) {
                sb.append(buf, 0, n);
            }
            content = sb.toString();
        }

        String background = null;
        double costPerMeter = 1.0;
        List<Node> nodes = new ArrayList<>();
        List<Edge> edges = new ArrayList<>();

        String bgKey = "\"background\":";
        int bgPos = content.indexOf(bgKey);
        if (bgPos >= 0) {
            int lineEnd = content.indexOf(',', bgPos);
            if (lineEnd < 0) {
                lineEnd = content.indexOf('\n', bgPos);
            }
            String token = content.substring(bgPos + bgKey.length(), lineEnd).trim();
            if (!token.equals("null")) {
                if (token.startsWith("\"") && token.endsWith("\"")) {
                    token = token.substring(1, token.length() - 1);
                }
                background = unescape(token);
            }
        }

        String costKey = "\"costPerMeter\":";
        int cPos = content.indexOf(costKey);
        if (cPos >= 0) {
            int lineEnd = content.indexOf(',', cPos);
            if (lineEnd < 0) {
                lineEnd = content.indexOf('\n', cPos);
            }
            String token = content.substring(cPos + costKey.length(), lineEnd).trim();
            try {
                costPerMeter = Double.parseDouble(token);
            } catch (Exception ignored) {
            }
        }

        String nodesKey = "\"nodes\":";
        int npos = content.indexOf(nodesKey);
        if (npos >= 0) {
            int start = content.indexOf('[', npos);
            int end = content.indexOf(']', start);
            if (start >= 0 && end >= 0) {
                String arr = content.substring(start + 1, end);
                String[] items = arr.split("\\},");
                for (String it : items) {
                    it = it.replace("{", "").replace("}", "").trim();
                    if (it.isEmpty()) {
                        continue;
                    }
                    Map<String, String> map = parseSimpleFields(it);
                    int id = Integer.parseInt(map.get("id"));
                    int x = Integer.parseInt(map.get("x"));
                    int y = Integer.parseInt(map.get("y"));
                    String deviceType = map.get("deviceType");
                    if (deviceType == null) {
                        deviceType = "Custom";
                    }
                    String label = map.get("label");
                    if (label == null) {
                        label = deviceType + "-" + id;
                    }
                    nodes.add(new Node(id, x, y, deviceType, label));
                }
            }
        }

        String edgesKey = "\"edges\":";
        int epos = content.indexOf(edgesKey);
        if (epos >= 0) {
            int start = content.indexOf('[', epos);
            int end = content.indexOf(']', start);
            if (start >= 0 && end >= 0) {
                String arr = content.substring(start + 1, end);
                String[] items = arr.split("\\},");
                for (String it : items) {
                    it = it.replace("{", "").replace("}", "").trim();
                    if (it.isEmpty()) {
                        continue;
                    }
                    Map<String, String> map = parseSimpleFields(it);
                    int src = Integer.parseInt(map.get("src"));
                    int dest = Integer.parseInt(map.get("dest"));
                    int weight = Integer.parseInt(map.get("weight"));
                    edges.add(new Edge(src, dest, weight));
                }
            }
        }

        return new LoadedData(nodes, edges, background, costPerMeter);
    }

    private static Map<String, String> parseSimpleFields(String s) {
        Map<String, String> map = new HashMap<>();
        String[] parts = s.split(",");
        for (String p : parts) {
            String[] kv = p.trim().split(":", 2);
            if (kv.length < 2) {
                continue;
            }
            String k = kv[0].trim().replace("\"", "");
            String v = kv[1].trim();
            if (v.startsWith("\"") && v.endsWith("\"")) {
                v = v.substring(1, v.length() - 1);
            }
            v = v.replace("\\\"", "\"").replace("\\\\", "\\");
            map.put(k, v);
        }
        return map;
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String unescape(String s) {
        return s.replace("\\\"", "\"").replace("\\\\", "\\");
    }

    public static class LoadedData {

        public List<Node> nodes;
        public List<Edge> edges;
        public String backgroundPath;
        public double costPerMeter;

        public LoadedData(List<Node> nodes, List<Edge> edges, String backgroundPath, double costPerMeter) {
            this.nodes = nodes;
            this.edges = edges;
            this.backgroundPath = backgroundPath;
            this.costPerMeter = costPerMeter;
        }
    }
}
