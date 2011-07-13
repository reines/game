package com.game.client.ui;

import java.awt.Color;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;

import com.game.client.Client;
import com.game.graphics.renderer.Graphics2D;
import com.game.graphics.widget.Widget;

public class ChatMessages extends Widget {
	private static final int CAPACITY = 200;
	private static final int UPDATE_DELAY = 5000;

	public enum Type {
		LOCAL_CHAT(Color.YELLOW),
		PRIVATE_CHAT(Color.CYAN),
		MESSAGE(Color.WHITE);

		public final Color color;

		private Type(Color color) {
			this.color = color;
		}
	};

	private static class ChatMessage {
		public final String message;
		public final Type type;
		public final long time;

		public ChatMessage(String message, Type type, long time) {
			this.message = message;
			this.type = type;
			this.time = time;
		}

		@Override
		public String toString() {
			return message;
		}
	}

	protected long lastUpdate;
	protected int maxToShow;
	protected int amountToShow;
	protected Type filter;
	protected final Deque<ChatMessage> messages;

	public ChatMessages(Client client, int width, int height) {
		super(width, height, true);

		lastUpdate = 0;
		maxToShow = super.height / client.getGraphics().get2D().getFontHeight();
		amountToShow = 0;
		filter = null;

		messages = new LinkedList<ChatMessage>();
	}

	public void setFilterType(Type filter) {
		this.filter = filter;
	}

	public void add(String message, Type type) {
		synchronized (messages) {
			if (messages.size() >= CAPACITY)
				messages.removeLast();

			ChatMessage chatMessage = new ChatMessage(message, type, System.currentTimeMillis());
			lastUpdate = chatMessage.time;

			messages.addFirst(chatMessage);
			if (amountToShow < maxToShow)
				amountToShow++;
		}
	}

	public void update(long now) {
		synchronized (messages) {
			if (now - lastUpdate > UPDATE_DELAY) {
				lastUpdate = now;
				if (amountToShow > 0)
					amountToShow--;
			}
		}
	}

	@Override
	protected void display(Graphics2D g, int x, int y) {
		if (super.backgroundColor != null)
			g.fillRect(x, y, width, height, super.backgroundColor);

		if (super.borderColor != null)
			g.drawRect(x, y, width, height, super.borderColor);

		synchronized (messages) {
			int yOff = y + super.height - 4;
			int limit = (filter == null) ? amountToShow : maxToShow;
			Iterator<ChatMessage> it = messages.iterator();

			for (int i = 0;i < limit && it.hasNext();) {
				ChatMessage message = it.next();

				if (filter != null && filter != message.type)
					continue;

				g.drawString(message.toString(), x + 4, yOff, message.type.color, true);

				yOff -= g.getFontHeight();
				i++;
			}
		}
	}
}
