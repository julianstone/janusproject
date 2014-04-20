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
package io.janusproject2.services;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.arakhne.afc.vmutil.locale.Locale;

import com.google.common.util.concurrent.AbstractService;
import com.google.inject.Inject;

/** This class enables to log information by ensuring
 * that the values of the parameters are not evaluated
 * until the information should be really log, according
 * to the log level.
 * This implementation is based on {@link Locale}, and the logger is injected.
 * <p>
 * The LogService considers the parameters of the functions as:<ul>
 * <li>the <var>messageKey</var> is the name of the message in the property file;</li>
 * <li>the <var>message</var> parameters are the values that will replace the
 * strings {0}, {1}, {2}... in the text extracted from the ressource property;</li>
 * <li>the parameter <var>propertyType</var> is the class from which the filename of
 * the property file will be built.</li>
 * </ul>
 * <p>
 * If a <code>Throwable</code> is passed as parameter, the text of the
 * exception is retreived.
 * <p>
 * If a <code>Callable</code> is passed as parameter, the object is automatically
 * called.
 * <p>
 * If a <code>LogParam</code> is passed as parameter, the <code>toString</code>
 * function will be invoked.
 * <p>
 * For all the other objects, the {@link #toString()} function is invoked.
 * 
 * 
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public class ArakhneLocaleLogService extends AbstractService implements LogService {

	@Inject
	private Logger logger;
	
	/**
	 */
	public ArakhneLocaleLogService() {
		//
	}
	
	private void log(Level level, boolean exception, String messageKey, Object... message) {
		if (isRunning() && this.logger.isLoggable(level)) {
			String text = Locale.getString(messageKey, message);
			Throwable e = null;
			if (exception) {
				for(Object m : message) {
					if (m instanceof Throwable) {
						e = (Throwable)m;
						break;
					}
				}
			}
			if (e!=null) {
				this.logger.log(level, text, e);
			}
			else {
				this.logger.log(level, text);
			}
		}
	}
	
	private void log(Level level, boolean exception, Class<?> propertyType, String messageKey, Object... message) {
		if (isRunning() && this.logger.isLoggable(level)) {
			String text = Locale.getString(propertyType, messageKey, message);
			Throwable e = null;
			if (exception) {
				for(Object m : message) {
					if (m instanceof Throwable) {
						e = (Throwable)m;
						break;
					}
				}
			}
			if (e!=null) {
				this.logger.log(level, text, e);
			}
			else {
				this.logger.log(level, text);
			}
		}
	}

	/** {@inheritDoc}
	 */
	@Override
	public void info(String messageKey, Object... message) {
		log(Level.INFO, false, messageKey, message);
	}

	/** {@inheritDoc}
	 */
	@Override
	public void info(Class<?> propertyType, String messageKey,
			Object... message) {
		log(Level.INFO, false, propertyType, messageKey, message);
	}

	/** {@inheritDoc}
	 */
	@Override
	public void debug(String messageKey, Object... message) {
		log(Level.FINE, true, messageKey, message);
	}

	/** {@inheritDoc}
	 */
	@Override
	public void debug(Class<?> propertyType, String messageKey,
			Object... message) {
		log(Level.FINE, true, propertyType, messageKey, message);
	}

	/** {@inheritDoc}
	 */
	@Override
	public void warning(Class<?> propertyType, String messageKey,
			Object... message) {
		log(Level.WARNING, true, propertyType, messageKey, message);
	}

	/** {@inheritDoc}
	 */
	@Override
	public void warning(String messageKey, Object... message) {
		log(Level.WARNING, true, messageKey, message);
	}

	/** {@inheritDoc}
	 */
	@Override
	public void error(String messageKey, Object... message) {
		log(Level.SEVERE, true, messageKey, message);
	}

	/** {@inheritDoc}
	 */
	@Override
	public void error(Class<?> propertyType, String messageKey,
			Object... message) {
		log(Level.SEVERE, true, propertyType, messageKey, message);
	}

	/** {@inheritDoc}
	 */
	@Override
	public Logger getLogger() {
		return this.logger;
	}

	/** {@inheritDoc}
	 */
	@Override
	public void setLogger(Logger logger) {
		if (logger!=null) this.logger = logger;
	}

	/** {@inheritDoc}
	 */
	@Override
	protected void doStart() {
		notifyStarted();
	}

	/** {@inheritDoc}
	 */
	@Override
	protected void doStop() {
		notifyStopped();
	}
	
}
