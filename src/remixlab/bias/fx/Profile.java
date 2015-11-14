/*********************************************************************************
 * bias_tree
 * Copyright (c) 2014 National University of Colombia, https://github.com/remixlab
 * @author Jean Pierre Charalambos, http://otrolado.info/
 *
 * All rights reserved. Library that eases the creation of interactive
 * scenes, released under the terms of the GNU Public License v3.0
 * which is available at http://www.gnu.org/licenses/gpl.html
 *********************************************************************************/

package remixlab.bias.fx;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import remixlab.bias.core.*;
import remixlab.bias.event.*;
import remixlab.util.*;

/**
 * A mapping defining {@link remixlab.bias.core.Shortcut} to {@link java.lang.reflect.Method}
 * bindings.
 * <p>
 * 
 */
public class Profile {
	class ObjectMethodTuple {
		Object object;
		Method method;
		
		ObjectMethodTuple(Object o, Method m) {
			object = o;
			method = m;
		}
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37).
				append(actionMap).
				append(stageMap).toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (obj.getClass() != getClass())
			return false;

		Profile other = (Profile) obj;
		return new EqualsBuilder()
				.append(actionMap, other.actionMap)
				.append(stageMap, other.stageMap).
				isEquals();
	}
	
	protected HashMap<Shortcut, ObjectMethodTuple>	actionMap;
	protected Grabber grabber;
	
	// temporal vars
	String initAction;
	
	protected HashMap<String, ObjectMethodTuple> stageMap;
	
	protected List<String> names = Arrays.asList(
			"init", "initKeyboard", "initClick", "initMotion", "initDOF1", "initDOF2", "initDOF3", "initDOF6",
			"exec", "execKeyboard", "execClick", "execMotion", "execDOF1", "execDOF2", "execDOF3", "execDOF6",
			"flush", "flushKeyboard", "flushClick", "flushMotion", "flushDOF1", "flushDOF2", "flushDOF3", "flushDOF6");

	/**
	 * Constructs the hash-map based profile.
	 */
	public Profile(Grabber g) {
		actionMap = new HashMap<Shortcut, ObjectMethodTuple>();
		stageMap = new HashMap<String, ObjectMethodTuple>();
		grabber = g;
	}
	
	public void from(Profile p) {	
		if( grabber.getClass() != p.grabber.getClass() ) {
			System.err.println("Profile grabbers should be of the same type");
			return;
		}
		actionMap = new HashMap<Shortcut, ObjectMethodTuple>();
		for (Map.Entry<Shortcut, ObjectMethodTuple> entry : p.actionMap().entrySet()) {
			if( entry.getValue().object == p.grabber )
				actionMap.put(entry.getKey(), new ObjectMethodTuple(grabber, entry.getValue().method));
			else
				actionMap.put(entry.getKey(), new ObjectMethodTuple(entry.getValue().object, entry.getValue().method));
		}
		stageMap = new HashMap<String, ObjectMethodTuple>();
		for (Map.Entry<String, ObjectMethodTuple> entry : p.stageMap.entrySet()) {
			if( entry.getValue().object == p.grabber )
				stageMap.put(entry.getKey(), new ObjectMethodTuple(grabber, entry.getValue().method));
			else
				stageMap.put(entry.getKey(), new ObjectMethodTuple(entry.getValue().object, entry.getValue().method));
		}
	}
	
	public Grabber grabber() {
		return grabber;
	}

	/**
	 * Returns the {@code map} (which is simply an instance of {@code HashMap}) encapsulated by this object.
	 */
	protected HashMap<Shortcut, ObjectMethodTuple> actionMap() {
		return actionMap;
	}
	
	protected HashMap<String, ObjectMethodTuple> stageMap() {
		return stageMap;
	}

	/**
	 * Returns the {@link java.lang.reflect.Method} binding for the given {@link remixlab.bias.core.Shortcut}
	 * key.
	 */
	public Method action(Shortcut key) {
		return actionMap.get(key) == null ? null : actionMap.get(key).method;
	}
	
	public String actionName(Shortcut key) {
		Method m = action(key); 
		if(m == null)
			return null;
		return m.getName();		
	}
	
	protected Object object(Shortcut key) {
		return actionMap.get(key) == null ? null : actionMap.get(key).object;
	}
	
	protected Object object(String name) {
		return stageMap.get(name) == null ? null : stageMap.get(name).object;
	}
	
	public void handle(BogusEvent event) {
		if (!processStage(event))
			invokeAction(event);
	}
		
	protected boolean invokeAction(BogusEvent event) {
		Method iHandlerMethod = action(event.shortcut());
		if (iHandlerMethod != null) {
			try {
				if(object(event.shortcut()) == grabber)
					iHandlerMethod.invoke(object(event.shortcut()), new Object[] { event });
				else
					iHandlerMethod.invoke(object(event.shortcut()), new Object[] { grabber, event });
				return true;
			} catch (Exception e) {
				System.out.println("Something went wrong when invoking your " + iHandlerMethod.getName() + " method");
				e.printStackTrace();
				return false;
			}
		}
		return false;
	}
	
	/*
	// TODO keyboard handlers may need a key event
	public boolean handle(BogusEvent event) {
		Method iHandlerMethod = action(event.shortcut());
		if (iHandlerMethod != null) {
			try {
				if(event.shortcut() instanceof KeyboardShortcut)
					if(object(event.shortcut()) == grabber)
						iHandlerMethod.invoke(object(event.shortcut()), new Object[] { });
					else
						iHandlerMethod.invoke(object(event.shortcut()), new Object[] { grabber });
				else
					if(object(event.shortcut()) == grabber)
						iHandlerMethod.invoke(object(event.shortcut()), new Object[] { event });
					else
						iHandlerMethod.invoke(object(event.shortcut()), new Object[] { grabber, event });
				return true;
			} catch (Exception e) {
				System.out.println("Something went wrong when invoking your " + iHandlerMethod.getName() + " method");
				e.printStackTrace();
				return false;
			}
		}
		return false;
	}
	*/
	
	protected boolean printWarning(Shortcut key, String methodName) {
		if(methodName == null) {
			this.removeBinding(key);
			System.out.println(key.description() + " removed");
			return true;
		}			
		if (hasBinding(key)) {
			Method a = action(key);
			if(a.getName().equals(methodName)) {
				System.out.println("Warning: shortcut already bound to " + a.getName());
				return true;
			}
			else {
				System.out.println("Warning: overwritting shortcut which was previously bound to " + a.getName());
				return false;
			}	
		}
		return false;
	}
	
	public void setBinding(Shortcut key, String methodName) {
		if(printWarning(key, methodName)) return;
		try {
			actionMap.put(key, new ObjectMethodTuple(grabber, grabber.getClass().getMethod(methodName, new Class<?>[] { BogusEvent.class })));
		} catch (Exception e) {
			System.out.println("Something went wrong when registering your " + methodName + " method");
			e.printStackTrace();
		}
	}
	
	public void setBinding(Object object, Shortcut key, String methodName) {
		if(printWarning(key, methodName)) return;
		try {
			Method method = object.getClass().getMethod(methodName, new Class<?>[] { grabber.getClass(), BogusEvent.class });
			actionMap.put(key, new ObjectMethodTuple(object, method));
		} catch (Exception e) {
			System.out.println("Something went wrong when registering your " + methodName + " method");
			e.printStackTrace();
		}
	}
	
	/**
	 * Defines the shortcut that triggers the given method.
	 * 
	 * @param key
	 *          {@link remixlab.bias.core.Shortcut}
	 * @param method
	 *          {@link java.lang.reflect.Method}
	 */
	public void setMotionBinding(Shortcut key, String methodName) {
		if(printWarning(key, methodName)) return;
		try {
			Method method = grabber.getClass().getMethod(methodName, new Class<?>[] { MotionEvent.class });
			actionMap.put(key, new ObjectMethodTuple(grabber, method));
			//System.out.println("grabber.getClass().getName() " + grabber.getClass().getName() + ", method.getDeclaringClass().getName(): " + method.getDeclaringClass().getName());
		} catch (Exception e) {
			System.out.println("Something went wrong when registering your " + methodName + " method");
			e.printStackTrace();
		}
	}
	
	public void setMotionBinding(Object object, Shortcut key, String methodName) {
		if(printWarning(key, methodName)) return;
		try {
			Method method = object.getClass().getMethod(methodName, new Class<?>[] { grabber.getClass(), MotionEvent.class });
			actionMap.put(key, new ObjectMethodTuple(object, method));
			//System.out.println("grabber.getClass().getName() " + grabber.getClass().getName() + ", method.getDeclaringClass().getName(): " + method.getDeclaringClass().getName());
		} catch (Exception e) {
			System.out.println("Something went wrong when registering your " + methodName + " method");
			e.printStackTrace();
		}
	}
	
	public void removeMotionBindings() {
		Iterator<Entry<Shortcut, ObjectMethodTuple>> it = actionMap.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry<Shortcut, ObjectMethodTuple> pair = it.next();
	        if( pair.getKey() instanceof MotionShortcut )
	        	it.remove();
	    }
	}
	
	public void removeMotionBindings(int [] ids) {
		if(ids == null)
			return;
		Iterator<Entry<Shortcut, ObjectMethodTuple>> it = actionMap.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry<Shortcut, ObjectMethodTuple> pair = it.next();
	        Shortcut shortcut = pair.getKey();
	        if(shortcut instanceof MotionShortcut) {
	        	int id = shortcut.id();
		        for(int i = 0; i < ids.length; i++ ) {
		        	if( id == ids[i] ) {
		        		it.remove();
		        		break;
		        	}
				}	        
	        }       	
	    }
	}
	
	public String motionBindingsInfo() {
		String result = new String();
		for (Entry<Shortcut, ObjectMethodTuple> entry : actionMap.entrySet())
			if (entry.getKey() != null && entry.getValue() != null)
				if( entry.getKey() instanceof MotionShortcut )
					result += entry.getKey().description() + " -> " + entry.getValue().method.getName() + "\n";
		return result;
	}
	
	public String motionBindingsInfo(int [] ids) {
		String result = new String();
		for (Entry<Shortcut, ObjectMethodTuple> entry : actionMap.entrySet())
			if (entry.getKey() != null && entry.getValue() != null)
				if( entry.getKey() instanceof MotionShortcut ) {
					int id = entry.getKey().id();
			        for(int i = 0; i < ids.length; i++ )
			        	if( id == ids[i] ) {
			        		result += entry.getKey().description() + " -> " + entry.getValue().method.getName() + "\n";
			        		break;
			        	}
				}
		return result;
	}
	
	/*
	// TODO keyboard handlers may need a key event
	public void setKeyboardBinding(Shortcut key, String methodName) {
		if(printWarning(key, methodName)) return;
		try {
			Method method = grabber.getClass().getMethod(methodName, new Class<?>[] { });
			map.put(key, new ObjectMethodTuple(grabber, method));
		} catch (Exception e) {
			System.out.println("Something went wrong when registering your " + methodName + " method");
			e.printStackTrace();
		}
	}
	*/
	
	public void setKeyboardBinding(Shortcut key, String methodName) {
		if(printWarning(key, methodName)) return;
		try {
			Method method = grabber.getClass().getMethod(methodName, new Class<?>[] { KeyboardEvent.class });
			actionMap.put(key, new ObjectMethodTuple(grabber, method));
		} catch (Exception e) {
			System.out.println("Something went wrong when registering your " + methodName + " method");
			e.printStackTrace();
		}
	}
	
	public void setKeyboardBinding(Object object, Shortcut key, String methodName) {
		if(printWarning(key, methodName)) return;
		try {
			Method method = object.getClass().getMethod(methodName, new Class<?>[] { grabber.getClass(), KeyboardEvent.class });
			actionMap.put(key, new ObjectMethodTuple(object, method));
		} catch (Exception e) {
			System.out.println("Something went wrong when registering your " + methodName + " method");
			e.printStackTrace();
		}
	}
	
	public void removeKeyboardBindings() {
		Iterator<Entry<Shortcut, ObjectMethodTuple>> it = actionMap.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry<Shortcut, ObjectMethodTuple> pair = it.next();
	        if( pair.getKey() instanceof KeyboardShortcut )
	        	it.remove();
	    }
	}
	
	public String keyboardBindingsInfo() {
		String result = new String();
		for (Entry<Shortcut, ObjectMethodTuple> entry : actionMap.entrySet())
			if (entry.getKey() != null && entry.getValue() != null)
				if( entry.getKey() instanceof KeyboardShortcut )
					result += entry.getKey().description() + " -> " + entry.getValue().method.getName() + "\n";
		return result;
	}

	public void setClickBinding(Shortcut key, String methodName) {
		if(printWarning(key, methodName)) return;
		try {
			Method method = grabber.getClass().getMethod(methodName, new Class<?>[] { ClickEvent.class });
			actionMap.put(key, new ObjectMethodTuple(grabber, method));
		} catch (Exception e) {
			System.out.println("Something went wrong when registering your " + methodName + " method");
			e.printStackTrace();
		}
	}
	
	public void setClickBinding(Object object, Shortcut key, String methodName) {
		if(printWarning(key, methodName)) return;
		try {
			Method method = object.getClass().getMethod(methodName, new Class<?>[] { grabber.getClass(), ClickEvent.class });
			actionMap.put(key, new ObjectMethodTuple(object, method));
		} catch (Exception e) {
			System.out.println("Something went wrong when registering your " + methodName + " method");
			e.printStackTrace();
		}
	}
	
	public void removeClickBindings() {
		Iterator<Entry<Shortcut, ObjectMethodTuple>> it = actionMap.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry<Shortcut, ObjectMethodTuple> pair = it.next();
	        if( pair.getKey() instanceof ClickShortcut )
	        	it.remove();
	    }
	}
	
	public void removeClickBindings(int [] ids) {
		if(ids == null)
			return;
		Iterator<Entry<Shortcut, ObjectMethodTuple>> it = actionMap.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry<Shortcut, ObjectMethodTuple> pair = it.next();
	        Shortcut shortcut = pair.getKey();
	        if(shortcut instanceof ClickShortcut) {
	        	int id = shortcut.id();
		        for(int i = 0; i < ids.length; i++ ) {
		        	if( id == ids[i] ) {
		        		it.remove();
		        		break;
		        	}
				}	        
	        }       	
	    }
	}
	
	public String clickBindingsInfo() {
		String result = new String();
		for (Entry<Shortcut, ObjectMethodTuple> entry : actionMap.entrySet())
			if (entry.getKey() != null && entry.getValue() != null)
				if( entry.getKey() instanceof ClickShortcut )
					result += entry.getKey().description() + " -> " + entry.getValue().method.getName() + "\n";
		return result;
	}
	
	public String clickBindingsInfo(int [] ids) {
		String result = new String();
		for (Entry<Shortcut, ObjectMethodTuple> entry : actionMap.entrySet())
			if (entry.getKey() != null && entry.getValue() != null)
				if( entry.getKey() instanceof ClickShortcut ) {
					int id = entry.getKey().id();
			        for(int i = 0; i < ids.length; i++ )
			        	if( id == ids[i] ) {
			        		result += entry.getKey().description() + " -> " + entry.getValue().method.getName() + "\n";
			        		break;
			        	}
				}
		return result;
	}

	public void setDOF1Binding(Shortcut key, String methodName) {
		if(printWarning(key, methodName)) return;
		try {
			Method method = grabber.getClass().getMethod(methodName, new Class<?>[] { DOF1Event.class });
			actionMap.put(key, new ObjectMethodTuple(grabber, method));
		} catch (Exception e) {
			System.out.println("Something went wrong when registering your " + methodName + " method");
			e.printStackTrace();
		}
	}
	
	public void setDOF1Binding(Object object, Shortcut key, String methodName) {
		if(printWarning(key, methodName)) return;
		try {
			Method method = object.getClass().getMethod(methodName, new Class<?>[] { grabber.getClass(), DOF1Event.class });
			actionMap.put(key, new ObjectMethodTuple(object, method));
		} catch (Exception e) {
			System.out.println("Something went wrong when registering your " + methodName + " method");
			e.printStackTrace();
		}
	}
	
	public void setDOF2Binding(Shortcut key, String methodName) {
		if(printWarning(key, methodName)) return;
		try {
			Method method = grabber.getClass().getMethod(methodName, new Class<?>[] { DOF2Event.class });
			actionMap.put(key, new ObjectMethodTuple(grabber, method));
		} catch (Exception e) {
			System.out.println("Something went wrong when registering your " + methodName + " method");
			e.printStackTrace();
		}
	}
	
	public void setDOF2Binding(Object object, Shortcut key, String methodName) {
		if(printWarning(key, methodName)) return;
		try {
			Method method = object.getClass().getMethod(methodName, new Class<?>[] { grabber.getClass(), DOF2Event.class });
			actionMap.put(key, new ObjectMethodTuple(object, method));
		} catch (Exception e) {
			System.out.println("Something went wrong when registering your " + methodName + " method");
			e.printStackTrace();
		}
	}
	
	public void setDOF3Binding(Shortcut key, String methodName) {
		if(printWarning(key, methodName)) return;
		try {
			Method method = grabber.getClass().getMethod(methodName, new Class<?>[] { DOF3Event.class });
			actionMap.put(key, new ObjectMethodTuple(grabber, method));
		} catch (Exception e) {
			System.out.println("Something went wrong when registering your " + methodName + " method");
			e.printStackTrace();
		}
	}
	
	public void setDOF3Binding(Object object, Shortcut key, String methodName) {
		if(printWarning(key, methodName)) return;
		try {
			Method method = object.getClass().getMethod(methodName, new Class<?>[] { grabber.getClass(), DOF3Event.class });
			actionMap.put(key, new ObjectMethodTuple(object, method));
		} catch (Exception e) {
			System.out.println("Something went wrong when registering your " + methodName + " method");
			e.printStackTrace();
		}
	}
	
	public void setDOF6Binding(Shortcut key, String methodName) {
		if(printWarning(key, methodName)) return;
		try {
			Method method = grabber.getClass().getMethod(methodName, new Class<?>[] { DOF6Event.class });
			actionMap.put(key, new ObjectMethodTuple(grabber, method));
		} catch (Exception e) {
			System.out.println("Something went wrong when registering your " + methodName + " method");
			e.printStackTrace();
		}
	}
	
	public void setDOF6Binding(Object object, Shortcut key, String methodName) {
		if(printWarning(key, methodName)) return;
		try {
			Method method = object.getClass().getMethod(methodName, new Class<?>[] { grabber.getClass(), DOF6Event.class });
			actionMap.put(key, new ObjectMethodTuple(object, method));
		} catch (Exception e) {
			System.out.println("Something went wrong when registering your " + methodName + " method");
			e.printStackTrace();
		}
	}

	/**
	 * Removes the shortcut binding.
	 * 
	 * @param key
	 *          {@link remixlab.bias.core.Shortcut}
	 */
	public void removeBinding(Shortcut key) {
		actionMap.remove(key);
	}

	/**
	 * Removes all the shortcuts from this object.
	 */
	public void removeBindings() {
		actionMap.clear();
	}
	
	/*
	public void removeBindings(int [] ids) {
		if(ids == null)
			return;
		Iterator<Entry<Shortcut, ObjectMethodTuple>> it = map.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry<Shortcut, ObjectMethodTuple> pair = it.next();
	        int id = pair.getKey().id();
	        for(int i = 0; i < ids.length; i++ ) {
	        	if( id == ids[i] ) {
	        		it.remove();
	        		break;
	        	}
			}	        	
	    }
	}
	*/

	/**
	 * Returns true if this object contains a binding for the specified shortcut.
	 * 
	 * @param key
	 *          {@link remixlab.bias.core.Shortcut}
	 * @return true if this object contains a binding for the specified shortcut.
	 */
	public boolean hasBinding(Shortcut key) {
		return actionMap.containsKey(key);
	}
	
	/**
	 * Returns true if this object maps one or more shortcuts to the specified action.
	 * 
	 * @param method
	 *          {@link java.lang.reflect.Method}
	 * @return true if this object maps one or more shortcuts to the specified action.
	 */
	public boolean isActionBound(String method) {
		for (ObjectMethodTuple tuple : actionMap.values()) {
			if( grabber == tuple.object && tuple.method.getName().equals(method) )
				return true;
		}
		return false;
	}
	
	public boolean isActionBound(Method method) {
		return isActionBound(grabber, method);
	}
	
	public boolean isActionBound(Object object, Method method) {
		return actionMap.containsValue(new ObjectMethodTuple(object, method));
	}

	/**
	 * Returns a description of all the bindings this profile holds.
	 */
	public String info() {
		String result = new String();
		boolean title = false;
		for (Entry<Shortcut, ObjectMethodTuple> entry : actionMap.entrySet())
			if (entry.getKey() != null && entry.getValue() != null) {
				if(!title) {
				  result += entry.getKey().getClass().getSimpleName() + "s:\n";
				  title = true;
				}
				result += entry.getKey().description() + " -> " + entry.getValue().method.getName() + "\n";
			}
		return result;
	}
	
	//
	
	/**
	 * Internal use. Algorithm to split an action flow into a 'three-tempi' {@link remixlab.bias.branch.Action} sequence.
	 * It's called like this (see {@link #performInteraction(BogusEvent)}):
	 * <pre>
     * {@code
	 * public void performInteraction(BogusEvent event) {
	 *	if (processEvent(event))
	 *		return;
	 *	if (event instanceof KeyboardEvent)
	 *		performInteraction((KeyboardEvent) event);
	 *	if (event instanceof ClickEvent)
	 *		performInteraction((ClickEvent) event);
	 *	if (event instanceof MotionEvent)
	 *		performInteraction((MotionEvent) event);
	 * }
     * }
     * </pre>
	 * <p>
	 * The algorithm parses the bogus-event in {@link #performInteraction(BogusEvent)} and then decide what to call:
	 * <ol>
     * <li>{@link #initStage(BogusEvent)} (1st tempi): sets the initAction, called when initAction == null.</li>
     * <li>{@link #execStage(BogusEvent)} (2nd tempi): continues action execution, called when initAction == action()
     * (current action)</li>
     * <li>{@link #flushStage(BogusEvent)} (3rd): ends action, called when {@link remixlab.bias.core.BogusEvent#flushed()}
     * is true or when initAction != action()</li>
     * </ol>
     * <p>
     * Useful to parse multiple-tempi actions, such as a mouse press/move/drag/release flow.
     * <p>
     * The following motion-actions have been implemented using the aforementioned technique:
	 * {@link remixlab.dandelion.branch.Constants.DOF2Action#SCREEN_ROTATE},
	 * {@link remixlab.dandelion.branch.Constants.DOF2Action#ZOOM_ON_REGION},
	 * {@link remixlab.dandelion.branch.Constants.DOF2Action#MOVE_BACKWARD}, and
	 * {@link remixlab.dandelion.branch.Constants.DOF2Action#MOVE_FORWARD}.
	 * <p>
     * Current implementation only supports {@link remixlab.bias.event.MotionEvent}s.
	 */
	protected final boolean processStage(BogusEvent event) {
		if (initAction == null) {
			if (!event.flushed()) {
				return initStage(event);// start action
			}
		}
		else { // initAction != null
			if (!event.flushed()) {
				if (initAction == actionName(event.shortcut()))
					return execStage(event);// continue action
				else { // initAction != action() -> action changes abruptly
					flushStage(event);
					return initStage(event);// start action
				}
			}
			else {// action() == null
				flushStage(event);// stopAction
				initAction = null;
				//setAction(null); // experimental, but sounds logical since: initAction != null && action() == null
				return true;
			}
		}
		return true;// i.e., if initAction == action() == null -> ignore :)
	}

	/**
	 * Internal use.
	 * 
	 * @see #processStage(BogusEvent)
	 */
	protected boolean initStage(BogusEvent event) {
		initAction = actionName(event.shortcut());
		if(initAction == null)
			return false;
		if (event instanceof KeyboardEvent)
			return invokeStageHandler(event, "initKeyboard");
		if (event instanceof ClickEvent)
			return invokeStageHandler(event, "initClick");
		if (event instanceof DOF1Event)
			return invokeStageHandler(event, "initDOF1");
		if (event instanceof DOF2Event) 
			return invokeStageHandler(event, "initDOF2");
		if (event instanceof DOF3Event)
			return invokeStageHandler(event, "initDOF3");
		if (event instanceof DOF6Event)
			return invokeStageHandler(event, "initDOF6");
		if (event instanceof MotionEvent)
			return invokeStageHandler(event, "initMotion");
		return invokeStageHandler(event, "init");
	}
	
	/**
	 * Internal use.
	 * 
	 * @see #processStage(BogusEvent)
	 */
	protected boolean execStage(BogusEvent event) {
		if (event instanceof KeyboardEvent)
			return invokeStageHandler(event, "execKeyboard");
		if (event instanceof ClickEvent)
			return invokeStageHandler(event, "execClick");
		if (event instanceof DOF1Event)
			return invokeStageHandler(event, "execDOF1");
		if (event instanceof DOF2Event)
			return invokeStageHandler(event, "execDOF2");
		if (event instanceof DOF3Event)
			return invokeStageHandler(event, "execDOF3");
		if (event instanceof DOF6Event)
			return invokeStageHandler(event, "execDOF6");
		if (event instanceof MotionEvent)
			return invokeStageHandler(event, "execMotion");
		return invokeStageHandler(event, "exec");
	}
	
	/**
	 * Internal use.
	 * 
	 * @see #processStage(BogusEvent)
	 */
	protected void flushStage(BogusEvent event) {
		if (event instanceof KeyboardEvent) {
			invokeStageHandler(event, "flushKeyboard");
			return;
		}
		if (event instanceof ClickEvent) {
			invokeStageHandler(event, "flushClick");
			return;
		}
		if (event instanceof DOF1Event) {
			invokeStageHandler(event, "flushDOF1");
			return;
		}
		if (event instanceof DOF2Event) {
			invokeStageHandler(event, "flushDOF2");
			return;
		}
		if (event instanceof DOF3Event) {
			invokeStageHandler(event, "flushDOF3");
			return;
		}
		if (event instanceof DOF6Event) {
			invokeStageHandler(event, "flushDOF6");
			return;
		}
		if (event instanceof MotionEvent) {
			invokeStageHandler(event, "flushMotion");
			return;
		}
		invokeStageHandler(event, "flush");
	}
	
	protected boolean invokeStageHandler(BogusEvent event, String methodName) {
		boolean result = false;
		ObjectMethodTuple tuple = stageMap.get(methodName);
		if(tuple == null)
			return result;
		Method iHandlerMethod = tuple.method;
		if (iHandlerMethod != null) {
			try {
				if(methodName.contains("flush")) {
					if(object(methodName) == grabber)
						iHandlerMethod.invoke(object(methodName), new Object[] { event });
					else
						iHandlerMethod.invoke(object(methodName), new Object[] { grabber, event });
					result = true;
				}
				else {//init and exec
					if(object(methodName) == grabber)
						result = (boolean) iHandlerMethod.invoke(object(methodName), new Object[] { event });
					else
						result = (boolean) iHandlerMethod.invoke(object(methodName), new Object[] { grabber, event });										
				}
			} catch (Exception e) {
				System.out.println("Something went wrong when invoking your " + iHandlerMethod.getName() + " method");
				e.printStackTrace();
				result = false;
			}
		}
		return result;
	}
	
	protected Class<?> printWarning(String methodName) {
		if( !names.contains(methodName) ) {
			System.out.println("Warning: your " + methodName + " method hasn't been added, it should be one of: " + names.toString() );
			return null;
		}
		Class<?> cls = null;
		if (stageMap.containsKey(methodName))
			System.out.println("Warning: " + methodName + " re-added");
		if( methodName.contains("Keyboard") )
			cls = KeyboardEvent.class;
		else if (methodName.contains("Click"))
			cls = ClickEvent.class;
		else if (methodName.contains("Motion"))
			cls = MotionEvent.class;
		else if (methodName.contains("DOF1"))
			cls = DOF1Event.class;
		else if (methodName.contains("DOF2"))
			cls = DOF2Event.class;
		else if (methodName.contains("DOF3"))
			cls = DOF3Event.class;
		else if (methodName.contains("DOF6"))
			cls = DOF6Event.class;
		else
			cls = BogusEvent.class;		
		return cls;
	}
	
	public void addStageHandler(String methodName) {
		Class<?> cls = printWarning(methodName);
		if(cls == null) return;
		try {
			stageMap.put(methodName, new ObjectMethodTuple(grabber, grabber.getClass().getMethod(methodName, new Class<?>[] { cls })));
		} catch (Exception e) {
			System.out.println("Something went wrong when registering your " + methodName + " method");
			e.printStackTrace();
		}
	}
	
	public void addStageHandler(Object object, String methodName) {
		Class<?> cls = printWarning(methodName);
		if(cls == null) return;
		try {
			Method method = object.getClass().getMethod(methodName, new Class<?>[] { grabber.getClass(), cls });
			stageMap.put(methodName, new ObjectMethodTuple(object, method));
		} catch (Exception e) {
			System.out.println("Something went wrong when registering your " + methodName + " method");
			e.printStackTrace();
		}
	}
	
	public boolean hasStageHandler(String methodName) {
		return stageMap.containsKey(methodName);
	}
	
	public void removeStageHandler(String methodName) {
		stageMap.remove(methodName);
	}
}