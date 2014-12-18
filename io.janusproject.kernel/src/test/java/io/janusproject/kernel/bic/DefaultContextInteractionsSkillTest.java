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
package io.janusproject.kernel.bic;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import io.janusproject.testutils.AbstractJanusTest;
import io.sarl.core.Lifecycle;
import io.sarl.lang.core.Address;
import io.sarl.lang.core.Agent;
import io.sarl.lang.core.AgentContext;
import io.sarl.lang.core.Capacity;
import io.sarl.lang.core.Event;
import io.sarl.lang.core.EventSpace;
import io.sarl.lang.core.EventSpaceSpecification;
import io.sarl.lang.core.Scope;
import io.sarl.lang.core.SpaceID;
import io.sarl.util.Scopes;

import java.util.UUID;

import javax.annotation.Nullable;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.internal.verification.Times;

import static org.junit.Assert.*;

/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@SuppressWarnings("all")
public class DefaultContextInteractionsSkillTest extends AbstractJanusTest {

	@Nullable
	private EventSpace defaultSpace;

	@Nullable
	private AgentContext parentContext;

	@Nullable
	private DefaultContextInteractionsSkill skill;

	@Nullable
	private Address address;

	@Nullable
	private Lifecycle lifeCapacity;

	@Before
	public void setUp() throws Exception {
		this.address = new Address(
				new SpaceID(
						UUID.randomUUID(),
						UUID.randomUUID(),
						EventSpaceSpecification.class),
				UUID.randomUUID());
		
		this.defaultSpace = mock(EventSpace.class);
		when(this.defaultSpace.getAddress(Matchers.any(UUID.class))).thenReturn(this.address);
		
		this.parentContext = mock(AgentContext.class);
		when(this.parentContext.getDefaultSpace()).thenReturn(this.defaultSpace);
		
		this.lifeCapacity = mock(Lifecycle.class);
		
		Agent agent = new Agent(UUID.randomUUID()) {
			@Override
			protected <S extends Capacity> S getSkill(Class<S> capacity) {
				return capacity.cast(DefaultContextInteractionsSkillTest.this.lifeCapacity);
			}
		};
		this.skill = new DefaultContextInteractionsSkill(agent, this.parentContext);
	}

	@Test
	public void getDefaultContext() {
		assertSame(this.parentContext, this.skill.getDefaultContext());
	}

	@Test
	public void getDefaultSpace() {
		assertNull(this.skill.getDefaultSpace());
		this.skill.install();
		assertSame(this.defaultSpace, this.skill.getDefaultSpace());
	}

	@Test
	public void getDefaultAddress() {
		this.skill.install();
		assertSame(this.address, this.skill.getDefaultAddress());
	}

	@Test
	public void install() {
		assertNull(this.skill.getDefaultSpace());
		this.skill.install();
		assertSame(this.defaultSpace, this.skill.getDefaultSpace());
	}

	@Test
	public void emitEventScope() {
		this.skill.install();
		Event event = mock(Event.class);
		Scope<Address> scope = Scopes.allParticipants();
		this.skill.emit(event, scope);
		ArgumentCaptor<Event> argument1 = ArgumentCaptor.forClass(Event.class);
		ArgumentCaptor<Scope> argument2 = ArgumentCaptor.forClass(Scope.class);
		verify(this.defaultSpace, new Times(1)).emit(argument1.capture(), argument2.capture());
		assertSame(event, argument1.getValue());
		assertSame(scope, argument2.getValue());
		ArgumentCaptor<Address> argument3 = ArgumentCaptor.forClass(Address.class);
		verify(event).setSource(argument3.capture());
		assertEquals(this.address, argument3.getValue());
	}

	@Test
	public void emitEvent() {
		this.skill.install();
		Event event = mock(Event.class);
		this.skill.emit(event);
		ArgumentCaptor<Event> argument1 = ArgumentCaptor.forClass(Event.class);
		verify(this.defaultSpace, new Times(1)).emit(argument1.capture());
		assertSame(event, argument1.getValue());
		ArgumentCaptor<Address> argument2 = ArgumentCaptor.forClass(Address.class);
		verify(event).setSource(argument2.capture());
		assertEquals(this.address, argument2.getValue());
	}

	@Test
	public void spawn() {
		this.skill.install();
		this.skill.spawn(Agent.class, "a", "b", "c"); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
		ArgumentCaptor<Class> argument1 = ArgumentCaptor.forClass(Class.class);
		ArgumentCaptor<AgentContext> argument2 = ArgumentCaptor.forClass(AgentContext.class);
		ArgumentCaptor<String> argument3 = ArgumentCaptor.forClass(String.class);
		verify(this.lifeCapacity, times(1)).spawnInContext(argument1.capture(),
				argument2.capture(), argument3.capture());
		assertEquals(Agent.class, argument1.getValue());
		assertSame(this.parentContext, argument2.getValue());
		assertArrayEquals(new String[] { "a", "b", "c" }, argument3.getAllValues().toArray()); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
	}
		
}
