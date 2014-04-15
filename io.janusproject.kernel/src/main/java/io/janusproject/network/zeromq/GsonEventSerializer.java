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
import io.sarl.lang.core.SpaceSpecification;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import org.arakhne.afc.vmutil.locale.Locale;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**  Serialize the {@link EventDispatch} content using GSON to
 * generate the corresponding {@link EventEnvelope}.
 * 
 * @author $Author: srodriguez$
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public class GsonEventSerializer implements EventSerializer {

	@Inject
	private Gson gson;
	
	@Inject
	private EventEncrypter encrypter;
	
	@Override
	public EventEnvelope serialize(EventDispatch dispatch) throws Exception {
		assert(this.encrypter!=null) : "Invalid injection of the encrypter"; //$NON-NLS-1$
		assert(this.gson!=null) : "Invalid injection of Gson"; //$NON-NLS-1$
		assert(dispatch!=null) : "Parameter 'dispatch' must not be null"; //$NON-NLS-1$

		Event event = dispatch.getEvent();
		assert(event!=null);
		Scope<?> scope = dispatch.getScope();
		assert(scope!=null);
		SpaceID spaceID = dispatch.getSpaceID();
		assert(spaceID!=null);
		assert(spaceID.getSpaceSpecification()!=null);
		Map<String,String> headers = dispatch.getHeaders();
		assert(headers!=null);
		
		headers.put("x-java-event-class", //$NON-NLS-1$
				event.getClass().getName());
		headers.put("x-java-scope-class", //$NON-NLS-1$
				scope.getClass().getName());
		headers.put("x-java-spacespec-class", //$NON-NLS-1$
				spaceID.getSpaceSpecification().getName());
		
		EventPack pack = new EventPack();
		//FIXME: Ensure that the default string encoding does not introduce bugs
		pack.setEvent(this.gson.toJson(event).getBytes());
		//FIXME: Ensure that the default string encoding does not introduce bugs
		pack.setScope(this.gson.toJson(scope).getBytes());
		//FIXME: Ensure that the default string encoding does not introduce bugs
		pack.setHeaders(this.gson.toJson(dispatch.getHeaders()).getBytes());
		//FIXME: Ensure that the default string encoding does not introduce bugs
		pack.setSpaceId(spaceID.getID().toString().getBytes());
		//FIXME: Ensure that the default string encoding does not introduce bugs
		pack.setContextId(spaceID.getContextID().toString().getBytes());

		return this.encrypter.encrypt(pack);

	}

	private static Map<String, String> getHeadersFromString(String src) {
		Gson gson = new Gson();
		Type headersType = new TypeToken<Map<String, String>>() {
			//
		}.getType();
		return gson.fromJson(src, headersType);

	}

	@SuppressWarnings("rawtypes")
	@Override
	public EventDispatch deserialize(EventEnvelope envelope) throws Exception {
		assert(this.encrypter!=null) : "Invalid injection of the encrypter"; //$NON-NLS-1$
		assert(this.gson!=null) : "Invalid injection of Gson"; //$NON-NLS-1$

		EventPack pack = this.encrypter.decrypt(envelope);
		
		//FIXME: Ensure that the default string encoding does not introduce bugs
		UUID contextId = UUID.fromString(new String(pack.getContextId()));
		//FIXME: Ensure that the default string encoding does not introduce bugs
		UUID spaceId = UUID.fromString(new String(pack.getSpaceId()));

		Map<String, String> headers = getHeadersFromString(new String(pack.getHeaders()));

		Class<? extends SpaceSpecification> spaceSpec = extractClass("x-java-spacespec-class", headers, SpaceSpecification.class); //$NON-NLS-1$
		Class<? extends Event> eventClazz = extractClass("x-java-event-class", headers, Event.class); //$NON-NLS-1$
		Class<? extends Scope> scopeClazz = extractClass("x-java-scope-class", headers, Scope.class); //$NON-NLS-1$
		
		SpaceID spaceID = new SpaceID(contextId, spaceId,
				spaceSpec.asSubclass(SpaceSpecification.class));
		
		//FIXME: Ensure that the default string encoding does not introduce bugs
		Event event = this.gson.fromJson(new String(pack.getEvent()), eventClazz);
		assert(event!=null);
		//FIXME: Ensure that the default string encoding does not introduce bugs
		Scope scope = this.gson.fromJson(new String(pack.getScope()), scopeClazz);
		assert(scope!=null);

		return new EventDispatch(spaceID,event, scope,headers);

	}
	
	private static <T> Class<? extends T> extractClass(String key, Map<String,String> headers, Class<T> expectedType) {
		assert(key!=null);
		assert(headers!=null);
		String classname = headers.get(key);
		Class<?> type = null;
		if (classname!=null) {
			try {
				type = Class.forName(classname);
			}
			catch(Throwable _) {
				//
			}
		}
		if (type==null || !expectedType.isAssignableFrom(type)) {
			throw new ClassCastException(Locale.getString("INVALID_TYPE", type)); //$NON-NLS-1$
		}
		assert(type!=null);
		return type.asSubclass(expectedType);
	}


	/** {@inheritDoc}
	 * @throws Exception 
	 */
	@Override
	public byte[] serializeContextID(UUID id) throws Exception {
		assert(this.encrypter!=null) : "Invalid injection of the encrypter"; //$NON-NLS-1$
		byte[] b = this.encrypter.encrytContextID(id);
		assert(b!=null && b.length>0);
		return b;
	}

}
