package com.game.common.util;

import java.util.SortedMap;
import java.util.TreeMap;

public class EventScheduler implements Runnable {

	protected Thread thread;
	protected boolean running;
	protected SortedMap<Long, Runnable> events;

	public EventScheduler() {
		thread = new Thread(this);
		events = new TreeMap<Long, Runnable>();
	}

	public void start() {
		running = true;
		thread.start();
	}

	public void stop() {
		running = false;
	}

	public void add(Runnable runnable) {
		add (0, runnable);
	}

	public synchronized void add(int delay, Runnable runnable) {
		long t = System.currentTimeMillis() + delay;

		// Loop until we find the next available spot
		while (events.containsKey(t))
			t++;

		events.put(t, runnable);
		notify();
	}

	@Override
	public void run() {
		while (running) {
			int delay;
			Runnable event;

			synchronized (this) {
				while (events.isEmpty()) {
					try { wait(); } catch (InterruptedException e) { }
				}

				long t = events.firstKey();

				event = events.get(t);
				delay = (int) (t - System.currentTimeMillis());

				events.remove(t);
				notify();
			}

			if (delay > 0) {
				try { Thread.sleep(delay); } catch (InterruptedException e) { }
			}

			// Run the event
			event.run();
		}
	}

}
