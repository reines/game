package com.game.server.db;

import java.io.File;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.game.common.model.Friend;
import com.game.common.model.Hash;
import com.game.common.model.PlayerProfile;
import com.game.common.model.Stat;
import com.game.server.Server;
import com.game.server.WorldManager;

public class Database {
	private static final Logger log = LoggerFactory.getLogger(Database.class);

	protected final SessionFactory factory;
	protected final Server server;

	public Database(File configFile, Server server) {
		factory = new Configuration().configure(configFile).buildSessionFactory();

		this.server = server;
	}

	public PlayerProfile getPlayerProfile(Hash id, Hash password) {
		Session session = factory.getCurrentSession();
		session.beginTransaction();

		PlayerProfile profile = (PlayerProfile) session.createCriteria(PlayerProfile.class)
												.add(Restrictions.idEq(id))
												.add(Restrictions.eq("password", password))
												.uniqueResult();

		session.getTransaction().commit();

		if (profile == null) {
			if (log.isDebugEnabled())
				log.debug("Failed login attempt for: " + id);

			return null;
		}

		if (log.isDebugEnabled())
			log.debug("Loaded profile: " + profile);

		WorldManager world = server.getWorldManager();

		// Set the online status for all friends
		for (Friend friend : profile.friends)
			friend.setOnline(world.getPlayer(friend.getID()) != null);

		// Set the association between the stat list and the stats, as well as their type
		for (Stat.Type type : Stat.Type.values()) {
			Stat stat = profile.stats.get(type);

			stat.setType(type);
			stat.addObserver(profile.stats);
		}

		// Calculate the combat level and skill total
		profile.stats.update(null, true);

		return profile;
	}

	public Friend getFriend(String username) {
		Session session = factory.getCurrentSession();
		session.beginTransaction();

		Friend friend = (Friend) session.createCriteria(Friend.class)
								.add(Restrictions.eq("username", username))
								.uniqueResult();

		session.getTransaction().commit();

		if (friend == null)
			return null;

		// Set their online status
		friend.setOnline(server.getWorldManager().getPlayer(friend.getID()) != null);

		return friend;
	}

	public void save(PlayerProfile profile) {
		if (profile == null)
			return;

		Session session = factory.getCurrentSession();
		session.beginTransaction();

		session.update(profile);

		session.getTransaction().commit();

		if (log.isDebugEnabled())
			log.debug("Saved profile: " + profile);
	}
}
