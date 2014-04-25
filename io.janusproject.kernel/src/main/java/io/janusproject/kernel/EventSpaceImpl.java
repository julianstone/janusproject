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
package io.janusproject.kernel;

import io.sarl.lang.core.Address;
import io.sarl.lang.core.EventListener;
import io.sarl.lang.core.SpaceID;
import io.sarl.util.OpenEventSpace;

/**
 * Default implementation of an event space.
 * 
 * @author $Author: srodriguez$
 * @author $Author: ngaud$
 * @author $Author: sgalland$
 * @version $Name$ $Revision$ $Date$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public class EventSpaceImpl extends AbstractEventSpace implements OpenEventSpace {

	/**
	 * Constructs an event space.
	 * 
	 * @param id - identifier of the space.
	 */
	public EventSpaceImpl(SpaceID id) {
		super(id);
	}

	@Override
	public Address register(EventListener entity) {
		Address a = new Address(getID(), entity.getID());
		return this.participants.registerParticipant(a, entity);
	}

	@Override
	public Address unregister(EventListener entity) {
		return this.participants.unregisterParticipant(entity);
	}

}
