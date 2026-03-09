# Network Cable Optimizer

A **Java Swing desktop application** that helps design and optimize computer networks by calculating the **Minimum Spanning Tree (MST)** of devices using **Kruskal's Algorithm** or **Prim's Algorithm**.

The application allows users to visually place devices on a floor map, connect them with cables, and compute the **minimum total cable length and cost** required to connect all devices.

---

## Academic Context

This project was developed as part of the **Data Structures and Algorithms (DSA) Laboratory course** during my **4th semester of undergraduate studies**.

The objective of the project was to apply graph algorithms in a practical scenario by designing a system that optimizes network cabling using **Minimum Spanning Tree algorithms**.

---

## Features

- Interactive **network layout designer**
- Add and manage network devices:
  - PC
  - Router
  - Switch
  - Server
  - Custom
- Connect devices with customizable cable lengths
- Compute **Minimum Spanning Tree (MST)**
- Choose between two algorithms:
  - **Kruskal's Algorithm**
  - **Prim's Algorithm**
- Real-time MST calculation option
- Drag and reposition devices on the canvas
- Rename devices and change device types
- Edit cable lengths
- Import a **floor plan image** as background
- Save and load projects in **JSON format**
- Export MST report to **text file**
- Automatic **cost calculation based on cable length**

---

## Algorithms Used

### Kruskal's Algorithm
- Greedy algorithm
- Sorts all edges by weight
- Uses **Union-Find (Disjoint Set)** to prevent cycles
- Selects the smallest edges until all nodes are connected

### Prim's Algorithm
- Builds the MST incrementally
- Starts from a single node
- Always selects the minimum weight edge connecting a new node

Both algorithms produce a **Minimum Spanning Tree**, which represents the **lowest-cost network connection**.

---

## Project Structure

```
NetworkCableOptimizer
│
├── src
│   ├── model
│   │   ├── Edge.java
│   │   ├── Graph.java
│   │   ├── KruskalMST.java
│   │   ├── PrimMST.java
│   │   └── Node.java
│   │
│   ├── util
│   │   └── FileManager.java
│   │
│   └── view
│       └── FloorMapUI.java
│
└── README.md
```

### Package Description

#### model
Contains graph data structures and algorithm implementations.

- **Edge.java** – Represents a connection between two nodes
- **Graph.java** – Graph data structure storing vertices and edges
- **KruskalMST.java** – Implementation of Kruskal's MST algorithm
- **PrimMST.java** – Implementation of Prim's MST algorithm
- **Node.java** – Represents a network device in the system

#### util
Utility classes used by the application.

- **FileManager.java** – Handles saving and loading project data in JSON format

#### view
Graphical user interface.

- **FloorMapUI.java** – Main Swing application that allows users to design and visualize networks

---

## How the Application Works

1. Place devices on the canvas.
2. Connect devices with cables.
3. Assign cable lengths.
4. Select the MST algorithm.
5. Compute the optimized network.
6. View total cable length and cost.

---

## Installation

### Requirements

- **Java JDK 17 or higher**
- Java IDE such as:
  - NetBeans
  - IntelliJ IDEA
  - Eclipse

---

### Running the Application

Open the project in your IDE and run:

```
FloorMapUI.java
```

Or compile manually:

```
javac view/FloorMapUI.java
java view.FloorMapUI
```

---

## Usage Guide

### Adding Devices
Select a device type from the sidebar and click on the canvas to place it.

### Connecting Devices
Hold **Ctrl + Drag** from one device to another to create a connection.

### Computing MST
Click **Compute MST** and choose the algorithm (Kruskal or Prim).

### Real-Time Mode
Enable **Real-time Mode** to recompute MST automatically when the network changes.

### Export Report
Generate a text report containing the MST edges and total cost.

---

## Example Output

```
MST Report
==========
0 - 1 : 12 m  cost=24.00
1 - 3 : 10 m  cost=20.00
3 - 4 : 8 m   cost=16.00
----------
Total cost: 60.00
```

---

## License

This project is intended for **educational purposes** and may be freely used for learning and academic reference.
