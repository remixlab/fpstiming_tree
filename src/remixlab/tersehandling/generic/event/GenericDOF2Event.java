/*********************************************************************************
 * TerseHandling
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
import remixlab.tersehandling.event.DOF2Event;

/**
 * Generic version of {@link remixlab.tersehandling.event.DOF2Event}.
 * <p>
 * Generic events attach an {@link #action()} to the non-generic version of the event. The idea being that an
 * {@link remixlab.tersehandling.core.Agent} will at some point automatically attach the action to the event (
 * {@link remixlab.tersehandling.core.Agent#handle(remixlab.tersehandling.event.TerseEvent)}).
 * 
 * @param <A>
 *          user-defined action
 */
public class GenericDOF2Event<A extends Action<?>> extends DOF2Event implements GenericEvent<A> {
	Action<?> action;

	/**
	 * Convenience constructor that calls the equivalent {@link remixlab.tersehandling.event.DOF2Event} one.
	 */
	public GenericDOF2Event(float x, float y) {
		super(x, y);
	}

	/**
	 * Convenience constructor that calls the equivalent {@link remixlab.tersehandling.event.DOF2Event} one.
	 */
	public GenericDOF2Event(GenericDOF2Event<A> prevEvent, float x, float y) {
		super(prevEvent, x, y);
	}

	/**
	 * Convenience constructor that calls the equivalent {@link remixlab.tersehandling.event.DOF2Event} one.
	 */
	public GenericDOF2Event(GenericDOF2Event<A> prevEvent, float x, float y, int modifiers, int button) {
		super(prevEvent, x, y, modifiers, button);
	}

	/**
	 * Convenience constructor that calls the equivalent {@link remixlab.tersehandling.event.DOF2Event} one.
	 */
	public GenericDOF2Event(float x, float y, int modifiers, int button) {
		super(x, y, modifiers, button);
	}

	/**
	 * Convenience constructor that calls the equivalent {@link remixlab.tersehandling.event.DOF2Event} one and then
	 * attaches to it the given user-defined action.
	 */
	public GenericDOF2Event(float x, float y, Action<?> a) {
		super(x, y);
		action = a;
	}

	/**
	 * Convenience constructor that calls the equivalent {@link remixlab.tersehandling.event.DOF2Event} one and then
	 * attaches to it the given user-defined action.
	 */
	public GenericDOF2Event(GenericDOF2Event<A> prevEvent, float x, float y, Action<?> a) {
		super(prevEvent, x, y);
		action = a;
	}

	/**
	 * Convenience constructor that calls the equivalent {@link remixlab.tersehandling.event.DOF2Event} one and then
	 * attaches to it the given user-defined action.
	 */
	public GenericDOF2Event(GenericDOF2Event<A> prevEvent, float x, float y, int modifiers, int button, Action<?> a) {
		super(prevEvent, x, y, modifiers, button);
		action = a;
	}

	/**
	 * Convenience constructor that calls the equivalent {@link remixlab.tersehandling.event.DOF2Event} one and then
	 * attaches to it the given user-defined action.
	 */
	public GenericDOF2Event(float x, float y, int modifiers, int button, Action<?> a) {
		super(x, y, modifiers, button);
		action = a;
	}

	protected GenericDOF2Event(GenericDOF2Event<A> other) {
		super(other);
		action = other.action;
	}

	@Override
	public Action<?> action() {
		return action;
	}

	@Override
	public void setAction(Action<?> a) {
		if (a instanceof Action<?>)
			action = a;
	}

	@Override
	public GenericDOF2Event<A> get() {
		return new GenericDOF2Event<A>(this);
	}
}
