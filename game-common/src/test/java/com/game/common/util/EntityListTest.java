package com.game.common.util;

import junit.framework.TestCase;

import com.game.common.model.Entity;
import com.game.common.model.Hash;
import com.game.common.model.Point;

public class EntityListTest extends TestCase {

	private static class TestEntity extends Entity {

		protected final Hash id;

		public TestEntity(String str) {
			id = new Hash(str);
		}

		@Override
		public Hash getID() {
			return id;
		}

		@Override
		public Point getLocation() {
			return null;
		}

		@Override
		public void setLocation(Point p) { }

		@Override
		public int hashCode() {
			return id.hashCode();
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof TestEntity))
				return false;

			TestEntity te = (TestEntity) o;
			return id.equals(te.id);
		}
	}

	public void testSize() {
		EntityList<TestEntity> list = new EntityList<TestEntity>();
		TestEntity te1 = new TestEntity("hello");

		// A new list should have size 0
		assertEquals(list.size(), 0);

		list.add(te1);

		// We've added one entity so the size should be 1
		assertEquals(list.size(), 1);

		list.remove(te1.getID());

		// Now it's removed again the size should be back to 0
		assertEquals(list.size(), 0);
	}

	public void testGet() {
		EntityList<TestEntity> list = new EntityList<TestEntity>();
		TestEntity te1 = new TestEntity("hello");

		list.add(te1);

		// get() on a key which doesn't exist should return null
		assertNull(list.get(new Hash("good bye")));

		//get() on the key for te1 should find te1
		assertTrue(te1.equals(list.get(te1.getID())));
	}

	public void testRemove() {
		EntityList<TestEntity> list = new EntityList<TestEntity>();
		TestEntity te1 = new TestEntity("hello");
		TestEntity te2 = new TestEntity("good bye");

		// Add 2 entities
		list.add(te1);
		list.add(te2);

		// The size should be 2
		assertEquals(list.size(), 2);

		// Remove te2 only
		list.remove(te2.getID());

		// The size should now be 1
		assertEquals(list.size(), 1);
	}

	public void testHasNewEntities() {
		EntityList<TestEntity> list = new EntityList<TestEntity>();
		TestEntity te1 = new TestEntity("hello");

		// A new list shouldn't have any new entities
		assertFalse(list.hasNewEntities());

		list.add(te1);

		// We've added 1 new entity, so we should have new entities now
		assertTrue(list.hasNewEntities());
	}

	public void testHasRemovedEntities() {
		EntityList<TestEntity> list = new EntityList<TestEntity>();
		TestEntity te1 = new TestEntity("hello");

		// A new list shouldn't have any removed entities
		assertFalse(list.hasRemovedEntities());

		list.add(te1);

		// We've added 1 new entity, but it isn't removed yet
		assertFalse(list.hasRemovedEntities());

		list.remove(te1.getID());

		// Now we should have a removed entity
		assertTrue(list.hasRemovedEntities());
	}

}
