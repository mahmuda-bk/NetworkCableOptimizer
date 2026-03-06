package model;

import java.awt.*;

public class Node {

    public int id;
    public int x, y;
    public String deviceType;
    public String label;

    public Node(int id, int x, int y, String deviceType, String label) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.deviceType = deviceType;
        this.label = label;
    }

    public Point getPoint() {
        return new Point(x, y);
    }

    public String getEmoji() {
        switch ((deviceType == null) ? "" : deviceType.toLowerCase()) {
            case "pc":
                return "\uD83D\uDCBB";
            case "router":
                return "\uD83D\uDCE1";
            case "switch":
                return "\uD83D\uDD00";
            case "server":
                return "\uD83D\uDDA5";
            default:
                return "\u25EF";
        }
    }
}

/**
 Node: stores id, position, device type (PC, Router, Switch, Server, Custom),
 label (display name) and derived emoji icon (rendered as text).
 */
