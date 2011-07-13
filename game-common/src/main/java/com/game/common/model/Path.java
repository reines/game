package com.game.common.model;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;

public class Path implements Iterable<Point> {

	protected final Deque<Point> steps;

	public Path() {
		steps = new LinkedList<Point>();
	}

	public Path(Deque<Point> steps) {
		this.steps = steps;
	}

	public void prepend(Point step) {
		steps.addFirst(step);
	}

	public void append(Point step) {
		steps.addLast(step);
	}

	public boolean hasNext() {
		return !steps.isEmpty();
	}

	public Point getNext() {
		return steps.peekFirst();
	}

	public Point removeNext() {
		return steps.pollFirst();
	}

	public Point getLast() {
		return steps.peekLast();
	}

	public int length() {
		return steps.size();
	}

	@Override
	public String toString() {
		return "path[length = " + steps.size() + ", next = " + this.getNext() + ", target = " + this.getLast() + "]";
	}

	@Override
	public Iterator<Point> iterator() {
		return steps.iterator();
	}
}
