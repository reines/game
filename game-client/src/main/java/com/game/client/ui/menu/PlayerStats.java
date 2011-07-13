package com.game.client.ui.menu;

import java.awt.Color;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import com.game.client.WorldManager;
import com.game.client.ui.Menu;
import com.game.common.model.Stat;
import com.game.common.util.StatList;
import com.game.graphics.input.Mouse;
import com.game.graphics.renderer.Graphics;
import com.game.graphics.renderer.Graphics2D;

public class PlayerStats extends Menu {
	public static final int STAT_X_OFFSET = 60;

	protected final WorldManager world;
	protected final Mouse mouse;
	protected final NumberFormat formatter;
	protected Stat.Type activeStat;

	public PlayerStats(WorldManager world, Graphics g, Mouse mouse) {
		super("Player Stats", STAT_X_OFFSET + 52, 36 + ((StatList.NUM_STATS + 4) * g.get2D().getFontHeight()));

		this.world = world;
		this.mouse = mouse;

		formatter = new DecimalFormat("#,###,###");
		activeStat = null;
	}

	@Override
	public void onUpdate(long now) { }

	@Override
	public void onMouseClicked(int x, int y, boolean left) { }

	@Override
	public void onDisplay(Graphics2D g, int x, int y) {
		y += 4;

		activeStat = null;

		// Display the stats
		for (Stat.Type type : Stat.Type.values()) {
			Stat stat = world.getStats().get(type);
			Color color = Color.WHITE;

			if (mouse.getX() > x && mouse.getX() < (x + super.width) && mouse.getY() >= y && mouse.getY() < (y + g.getFontHeight())) {
				activeStat = type;
				color = Color.RED;
			}

			y += g.getFontHeight();

			g.drawString(type.name, x + 12, y, color, true);
			g.drawString(stat.toString(), x + 12 + STAT_X_OFFSET, y, color, true);

		}

		y += 8;
		g.drawLine(x, y, x + super.width, y, super.borderColor);
		y += 4;

		// Combat level
		y += g.getFontHeight();
		g.drawString("Combat: " + world.getStats().getCombatLevel(), x + 12, y, Color.WHITE, true);

		// Skill total
		y += g.getFontHeight();
		g.drawString("Skill total: " + world.getStats().getSkillTotal(), x + 12, y, Color.WHITE, true);

		y += 8;
		g.drawLine(x, y, x + super.width, y, super.borderColor);
		y += 4;

		if (activeStat == null) {
			y += g.getFontHeight();
			g.drawString("Hover for details.", x + 12, y, Color.WHITE, true);
		}
		else {
			Stat stat = world.getStats().get(activeStat);

			// Experience
			y += g.getFontHeight();
			g.drawString("Exp: " + formatter.format(stat.getExp()), x + 6, y, Color.WHITE, true);

			// Next level
			y += g.getFontHeight();
			g.drawString("Next at: " + formatter.format(Stat.levelToExp(stat.getLevel() + 1)), x + 6, y, Color.WHITE, true);
		}
	}
}
