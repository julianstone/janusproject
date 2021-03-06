/*
 * $Id$
 * 
 * Janus platform is an open-source multiagent platform.
 * More details on http://www.janusproject.io
 * 
 * Copyright (C) 2014 Sebastian RODRIGUEZ, Nicolas GAUD, Stéphane GALLAND.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.janusproject.kernel.services.jdk.distributeddata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import io.janusproject.kernel.services.hazelcast.HazelcastDMultiMapView;
import io.janusproject.services.distributeddata.DMapListener;
import io.janusproject.testutils.AbstractJanusTest;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.annotation.Nullable;

import junit.framework.TestSuite;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.mockito.ArgumentCaptor;

import com.google.common.collect.Maps;
import com.google.common.collect.testing.MapTestSuiteBuilder;
import com.google.common.collect.testing.TestStringMapGenerator;
import com.google.common.collect.testing.features.CollectionFeature;
import com.google.common.collect.testing.features.CollectionSize;
import com.google.common.collect.testing.features.MapFeature;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MultiMap;

/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@RunWith(Suite.class)
@SuiteClasses({
	DMapViewTest.BackedCollectionTests.class,
	DMapViewTest.GuavaMapOperationTests.class,
	DMapViewTest.ViewTests.class,
	DMapViewTest.SpecificDMapFunctionTests.class,
	DMapViewTest.ListeningFeatureTests.class,
})
@SuppressWarnings("all")
public class DMapViewTest {

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	public static class BackedCollectionTests extends AbstractJanusTest {

		@Nullable
		private String name;

		@Nullable
		private Map<String, String> map;

		@Nullable
		private DMapView<String, String> view;

		@Before
		public void setUp() {
			this.name = UUID.randomUUID().toString();
			this.map = Maps.newHashMap();
			this.view = new DMapView<>(this.name, this.map);
			this.view.put("a", "va");
			this.view.put("b", "vb");
		}

		@Test
		public void isBackedCollection() {
			assertTrue(this.view.isBackedCollection());
		}

		@Test
		public void changesPropagation() {
			Assume.assumeTrue(
					"The collection is not backing the changes to the underlying collection",
					this.view.isBackedCollection());
			assertTrue(this.view.keySet().remove("b"));
			assertEquals(1, this.map.size());
			assertTrue(this.map.containsKey("a"));
			assertTrue(this.map.containsValue("va"));
		}

		@Test
		public void noChangesPropagation() {
			Assume.assumeFalse(
					"The collection is backing the changes to the underlying collection",
					this.view.isBackedCollection());
			assertTrue(this.view.keySet().remove("b"));
			assertEquals(2, this.map.size());
			assertTrue(this.map.containsKey("a"));
			assertTrue(this.map.containsValue("va"));
			assertTrue(this.map.containsKey("b"));
			assertTrue(this.map.containsValue("vb"));
		}

	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	public static class GuavaMapOperationTests {

		public static TestSuite suite() {
			return MapTestSuiteBuilder
					// The create method is called with an array of elements
					// that should populate the collection.
					.using(new TestStringMapGenerator() {
						@Override
						protected Map<String, String> create(Entry<String, String>[] arg0) {
							Map<String, String> map = Maps.newHashMap();
							for(Entry<String, String> entry : arg0) {
								map.put(entry.getKey(), entry.getValue());
							}
							return new DMapView<>(
									UUID.randomUUID().toString(),
									map);
						}
					})
					.named("Guava-based DMap tests")
					.withFeatures(
							MapFeature.ALLOWS_NULL_KEYS,
				            MapFeature.ALLOWS_NULL_VALUES,
				            MapFeature.ALLOWS_ANY_NULL_QUERIES,
				            MapFeature.GENERAL_PURPOSE,
				            MapFeature.FAILS_FAST_ON_CONCURRENT_MODIFICATION,
				            CollectionFeature.SUPPORTS_ITERATOR_REMOVE,
				            CollectionFeature.SERIALIZABLE,
				            CollectionSize.ANY
							).createTestSuite();
		}

	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	public static class ViewTests extends AbstractJanusTest {

		@Nullable
		private Map<String, String> map;

		@Nullable
		private DMapView<String, String> view;

		@Before
		public void setUp() {
			this.map = Maps.newHashMap();
			this.view = new DMapView<>(
					UUID.randomUUID().toString(),
					this.map);
		}

		@Test
		public void put() {
			String newKey = UUID.randomUUID().toString();
			String newValue = UUID.randomUUID().toString();
			this.map.put(newKey, newValue);
			//
			assertEquals(1, this.view.size());
			assertSame(newValue, this.view.get(newKey));
		}

	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	public static class SpecificDMapFunctionTests extends AbstractJanusTest {

		@Nullable
		private Map<String, String> map;
		
		@Nullable
		private DMapView<String, String> view;

		@Before
		public void setUp() {
			this.map = Maps.newHashMap();
			this.view = new DMapView<>(
					UUID.randomUUID().toString(),
					this.map);
		}

		@Test
		public void isBackedCollection() {
			assertTrue(this.view.isBackedCollection());
		}

		@Test
		public void putIfAbsent_withoutElement() {
			this.view.putIfAbsent("abc", "vABC");
			assertEquals(1, this.map.size());
			assertTrue(this.map.containsKey("abc"));
			assertEquals("vABC", this.map.get("abc"));
		}

		@Test
		public void putIfAbsent_withElement() {
			this.view.putIfAbsent("abc", "vABC");
			this.view.putIfAbsent("def", "vDEF");
			this.view.putIfAbsent("abc", "vXYZ");
			assertEquals(2, this.map.size());
			assertTrue(this.map.containsKey("abc"));
			assertTrue(this.map.containsKey("def"));
			assertEquals("vABC", this.map.get("abc"));
			assertEquals("vDEF", this.map.get("def"));
		}

	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	public static class ListeningFeatureTests extends AbstractJanusTest {

		@Nullable
		private DMapListener<String, String> listener;
		
		@Nullable
		private Map<String, String> map;
		
		@Nullable
		private DMapView<String, String> view;

		@Before
		public void setUp() {
			this.listener = mock(DMapListener.class);
			this.map = Maps.newHashMap();
			this.view = new DMapView<>(
					UUID.randomUUID().toString(),
					this.map);
		}

		@Test
		public void entryAdded_withoutListener() {
			this.view.put("abc", "vABC");
			verifyZeroInteractions(this.listener);
		}

		@Test
		public void entryAdded_removeListener() {
			this.view.addDMapListener(this.listener);
			this.view.removeDMapListener(this.listener);
			this.view.put("abc", "vABC");
			verifyZeroInteractions(this.listener);
		}

		@Test
		public void entryAdded_withListener() {
			this.view.addDMapListener(this.listener);
			this.view.put("abc", "vABC");
			ArgumentCaptor<String> arg0 = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<String> arg1 = ArgumentCaptor.forClass(String.class);
			verify(this.listener, times(1)).entryAdded(arg0.capture(), arg1.capture());
			assertEquals("abc", arg0.getValue());
			assertEquals("vABC", arg1.getValue());
		}

		@Test
		public void entryUpdated_withoutListener() {
			this.view.put("abc", "vABC");
			this.view.put("abc", "vABC2");
			verifyZeroInteractions(this.listener);
		}

		@Test
		public void entryUpdated_removeListener() {
			this.view.put("abc", "vABC");
			this.view.addDMapListener(this.listener);
			this.view.removeDMapListener(this.listener);
			this.view.put("abc", "vABC2");
			verifyZeroInteractions(this.listener);
		}

		@Test
		public void entryUpdated_withListener() {
			this.view.put("abc", "vABC");
			this.view.addDMapListener(this.listener);
			this.view.put("abc", "vABC2");
			ArgumentCaptor<String> arg0 = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<String> arg1 = ArgumentCaptor.forClass(String.class);
			verify(this.listener, times(1)).entryUpdated(arg0.capture(), arg1.capture());
			assertEquals("abc", arg0.getValue());
			assertEquals("vABC2", arg1.getValue());
		}

		@Test
		public void entryRemoved_withListener() {
			this.view.put("abc", "vABC");
			this.view.remove("abc");
			verifyZeroInteractions(this.listener);
		}

		@Test
		public void entryRemoved_removeListener() {
			this.view.put("abc", "vABC");
			this.view.addDMapListener(this.listener);
			this.view.removeDMapListener(this.listener);
			this.view.remove("abc");
			verifyZeroInteractions(this.listener);
		}

		@Test
		public void entryRemoved_withoutListener() {
			this.view.put("abc", "vABC");
			this.view.addDMapListener(this.listener);
			this.view.remove("abc");
			ArgumentCaptor<String> arg0 = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<String> arg1 = ArgumentCaptor.forClass(String.class);
			verify(this.listener, times(1)).entryRemoved(arg0.capture(), arg1.capture());
			assertEquals("abc", arg0.getValue());
			assertEquals("vABC", arg1.getValue());
		}

		@Test
		public void mapCleared_withListener() {
			this.view.put("abc", "vABC");
			this.view.clear();
			verifyZeroInteractions(this.listener);
		}

		@Test
		public void mapCleared_removeListener() {
			this.view.put("abc", "vABC");
			this.view.addDMapListener(this.listener);
			this.view.removeDMapListener(this.listener);
			this.view.clear();
			verifyZeroInteractions(this.listener);
		}

		@Test
		public void mapCleared_withoutListener() {
			this.view.put("abc", "vABC");
			this.view.addDMapListener(this.listener);
			this.view.clear();
			ArgumentCaptor<Boolean> arg0 = ArgumentCaptor.forClass(Boolean.class);
			verify(this.listener, times(1)).mapCleared(arg0.capture());
			assertFalse(arg0.getValue());
		}

	}

}
