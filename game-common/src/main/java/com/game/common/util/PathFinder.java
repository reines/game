package com.game.common.util;

import java.util.Collections;
import java.util.LinkedList;

import com.game.common.model.Map;
import com.game.common.model.Path;
import com.game.common.model.Point;
import com.game.common.model.Tile;

// Path finder using the A* algorithm
// See: http://www.cokeandcode.com/pathfinding
public class PathFinder {

	protected static class Node implements Comparable<Node> {
		public final Point location;
		public double cost;
		public double heuristic;
		public Node parent;
		public boolean checked;

		public Node(Point location) {
			this.location = location;

			cost = 0.0;
			heuristic = 0.0;

			parent = null;
			checked = false;
		}

		@Override
		public int compareTo(Node n) {
			double totalCost = this.cost + this.heuristic;
			double theirTotalCost = n.cost + n.heuristic;

			if (totalCost > theirTotalCost)
				return 1;
			else if (totalCost < theirTotalCost)
				return -1;

			return 0;
		}

		@Override
		public int hashCode() {
			return location.hashCode();
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof Node))
				return false;

			Node n = (Node) o;
			return location.equals(n.location);
		}

		@Override
		public String toString() {
			return "node[location = " + location + ", cost = " + cost + ", heuristic = " + heuristic + ", parent = " + parent + ", checked = " + checked + "]";
		}
	}

	protected final Map map;
	protected final Point start;
	protected final int maxRadius;

	protected final LinkedList<Node> pending;
	protected Node[][] nodes;

	public PathFinder(Map map, Point start) {
		this (map, start, map.getSectorSize());
	}

	public PathFinder(Map map, Point start, int maxRadius) {
		this.map = map;
		this.start = start;
		this.maxRadius = maxRadius;

		pending = new LinkedList<Node>();
		nodes = null; // initialized in generatePath()
	}

	protected Node getNode(Point location) {
		int x = (location.x - start.x) + maxRadius;
		int y = (location.y - start.y) + maxRadius;

		if (x < 0 || x >= nodes.length || y < 0 || y >= nodes[x].length)
			return null;

		if (nodes[x][y] == null)
			nodes[x][y] = new Node(location);

		return nodes[x][y];
	}

	public Path generatePath(Point target) {
		return this.generatePath(target, 0);
	}

	public Path generatePath(Point target, int close) {
		try {
			Tile tile = map.getTile(target);
			// If there isn't such a tile, or it isn't walkable and we need to go to it then no path can exist
			if (tile == null || (!tile.isWalkable() && close == 0))
				return null;

			nodes = new Node[maxRadius * 2][maxRadius * 2];

			// The target is above map.getSectorSize() tiles away, so we can't find a valid path
			if (this.getNode(target) == null)
				return null;

			// Set our starting position and add it to the open list
			Node startNode = this.getNode(start);
			startNode.cost = 0;
			startNode.heuristic = Double.MAX_VALUE;

			pending.add(startNode);

			// while there are still tiles to be checked
			while (!pending.isEmpty()) {
				Collections.sort(pending);
				Node current = pending.poll(); // Get the first pending node (which heuristics say is closest to the target)
				// if it is actually the target, or close enough, then we're done
				if (current.location.equals(target) || current.location.distanceTo(target) <= close)
					break;

				// Remove from open and add to set this node as checked
				current.checked = true;

				// for each tile around us
				for (int y = -1;y < 2;y++) {
					for (int x = -1;x < 2;x++) {
						if (x == 0 && y == 0)
							continue;

						Point point = new Point(current.location.x + x, current.location.y + y);
						// If it's outside the map, it's an invalid location so skip it
						if (point.x < 0 || point.x >= map.getWidth() || point.y < 0 || point.y >= map.getHeight())
							continue;

						// If it isn't a valid step then skip it
						if (!map.isValidStep(current.location, point))
							continue;

						// If it isn't found then it's greater than a sector away, so the path would be too long
						Node next = this.getNode(point);
						if (next == null)
							continue;

						double cost = current.cost + current.location.distanceTo(point);

						// If we've already visited, and the new cost is less we need to re-evaluate
						if (cost < next.cost) {
							if (pending.contains(next))
								pending.remove(next);

							if (next.checked)
								next.checked = false;
						}

						// If this tile is new or not in either list then we should set its attributes
						if (!pending.contains(next) && !next.checked) {
							next.cost = cost;
							next.heuristic = target.distanceTo(point);

							next.parent = current;

							pending.add(next);
						}
					}
				}
			}

			Node destination = this.getNode(target);

			// The destination wasn't reached
			if (destination == null || destination.parent == null)
				return null;

			// We have found a path
			Path path = new Path();

			for (Node n = destination;n != null && !n.location.equals(start);n = n.parent)
				path.prepend(n.location);

			return path;
		}
		finally {
			pending.clear();
			nodes = null;
		}
	}
}
