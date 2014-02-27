/*********************************************************************************
 * TerseHandling (version 1.0.0)
 * Copyright (c) 2014 National University of Colombia, https://github.com/remixlab
 * @author Jean Pierre Charalambos, http://otrolado.info/
 *     
 * All rights reserved. Library that eases the creation of interactive
 * scenes, released under the terms of the GNU Public License v3.0
 * which is available at http://www.gnu.org/licenses/gpl.html
 *********************************************************************************/
package remixlab.tersehandling.generic.event;

import remixlab.tersehandling.core.Action;
import remixlab.tersehandling.core.GenericEvent;
import remixlab.tersehandling.event.ClickEvent;

/**
 * Generic version of {@link remixlab.tersehandling.event.ClickEvent}.
 * <p>
 * Generic events attach an {@link #action()} to the non-generic version of the event. The idea being that an
 * {@link remixlab.tersehandling.core.Agent} will at some point automatically attach the action to the event (
 * {@link remixlab.tersehandling.core.Agent#handle(remixlab.tersehandling.event.TerseEvent)}).
 * 
 * @param <A>
 *          user defined action
 */
public class GenericClickEvent<A extends Action<?>> extends ClickEvent implements GenericEvent<A> {
	Action<?> action;

	/**
	 * Convenience constructor that calls the equivalent {@link remixlab.tersehandling.event.ClickEvent} one.
	 */
	public GenericClickEvent(float x, float y, int b) {
		super(x, y, b);
	}

	/**
	 * Convenience constructor that calls the equivalent {@link remixlab.tersehandling.event.ClickEvent} one.
	 */
	public GenericClickEvent(float x, float y, int b, int clicks) {
		super(x, y, b, clicks);
	}

	/**
	 * Convenience constructor that calls the equivalent {@link remixlab.tersehandling.event.ClickEvent} one.
	 */
	public GenericClickEvent(float x, float y, Integer modifiers, int b, int clicks) {
		super(x, y, modifiers, b, clicks);
	}

	/**
	 * Convenience constructor that calls the equivalent {@link remixlab.tersehandling.event.ClickEvent} one and then
	 * attaches to it the given user-defined action.
	 */
	public GenericClickEvent(float x, float y, int b, Action<?> a) {
		super(x, y, b);
		action = a;
	}

	/**
	 * Convenience constructor that calls the equivalent {@link remixlab.tersehandling.event.ClickEvent} one and then
	 * attaches to it the given user-defined action.
	 */
	public GenericClickEvent(float x, float y, int b, int clicks, Action<?> a) {
		super(x, y, b, clicks);
		action = a;
	}

	/**
	 * Convenience constructor that calls the equivalent {@link remixlab.tersehandling.event.ClickEvent} one and then
	 * attaches to it the given user-defined action.
	 */
	public GenericClickEvent(float x, float y, Integer modifiers, int b, int clicks, Action<?> a) {
		super(x, y, modifiers, b, clicks);
		action = a;
	}

	protected GenericClickEvent(GenericClickEvent<A> other) {
		super(other);
		action = other.action;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see remixlab.tersehandling.core.GenericEvent#action()
	 */
	@Override
	public Action<?> action() {
		return action;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see remixlab.tersehandling.core.GenericEvent#setAction(remixlab.tersehandling.core.Action)
	 */
	@Override
	public void setAction(Action<?> a) {
		if (a instanceof Action<?>)
			action = a;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see remixlab.tersehandling.event.ClickEvent#get()
	 */
	@Override
	public GenericClickEvent<A> get() {
		return new GenericClickEvent<A>(this);
	}
}
