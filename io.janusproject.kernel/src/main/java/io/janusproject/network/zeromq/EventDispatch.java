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
package io.janusproject.network.zeromq;

import io.sarl.lang.core.Event;
import io.sarl.lang.core.Scope;
import io.sarl.lang.core.SpaceID;

import java.util.HashMap;
import java.util.Map;

/** Description of the required information for dispatching
 * an event over the network.
 * 
 * @author $Author: srodriguez$
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public class EventDispatch {
	
	private final Scope<?> scope;
	private final Event event;
	private final SpaceID spaceID;
	private Map<String, String> headers;
	
	/** Construct a <code>EventDispatch</code>
	 * 
	 * @param sid - identifier of the space in which the event occurs.
	 * @param event - event to dispatch.
	 * @param scope - scope of the event.
	 * @param headers - custom headers associated to the event.
	 */
	public EventDispatch(SpaceID sid, Event event, Scope<?> scope, Map<String, String> headers){
		assert(sid!=null) : "Parameter 'sid' must not be null"; //$NON-NLS-1$
		assert(event!=null) : "Parameter 'event' must not be null"; //$NON-NLS-1$
		assert(scope!=null) : "Parameter 'scope' must not be null"; //$NON-NLS-1$
		assert(headers!=null) : "Parameter 'headers' must not be null"; //$NON-NLS-1$
		this.spaceID = sid;
		this.event = event;
		this.scope = scope;
		this.headers = headers;
	}

	
	
	/** {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "EventDispatch [scope=" + this.scope + ", event=" + this.event  //$NON-NLS-1$//$NON-NLS-2$
				+ ", spaceID=" + this.spaceID + ", headers=" + this.headers + "]";  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
	}



	/** Construct a <code>EventDispatch</code>
	 * 
	 * @param sid - identifier of the space in which the event occurs.
	 * @param event - event to dispatch.
	 * @param scope - scope of the event.
	 */
	public EventDispatch(SpaceID sid, Event event, Scope<?> scope) {
		this(sid,event,scope,new HashMap<String,String>());
	}
	

	/** Replies the event to dispatch.
	 * 
	 * @return the event.
	 */
	public Event getEvent() {
		return this.event;
	}

	/** Replies the scope of the event.
	 * 
	 * @return the scope.
	 */
	public Scope<?> getScope(){
		return this.scope;
	}
	
	/** Replies the custom headers associated to the event.
	 * 
	 * @return the custom headers.
	 */
	public Map<String, String> getHeaders(){
		return this.headers;
	}
	
	/** Replies the identifier of the space in which the event occurs.
	 * 
	 * @return the space identifier.
	 */
	public SpaceID getSpaceID(){
		return this.spaceID;
	}
	
}
