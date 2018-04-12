package rascal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

import edit.Create;
import edit.Delete;
import edit.Edit;
import edit.Insert;
import edit.Patch;
import edit.Remove;
import edit.Set;
import edit.UnSet;
import ide.Activator;
import io.usethesource.vallang.IBool;
import io.usethesource.vallang.IConstructor;
import io.usethesource.vallang.IInteger;
import io.usethesource.vallang.IList;
import io.usethesource.vallang.IReal;
import io.usethesource.vallang.ISourceLocation;
import io.usethesource.vallang.IString;
import io.usethesource.vallang.ITuple;
import io.usethesource.vallang.IValue;
import io.usethesource.vallang.IValueFactory;
import io.usethesource.vallang.type.Type;
import slebus.Producer;

public class RascalProducer extends RascalConsumer implements Producer {
	
	public RascalProducer(IValueFactory vf) {
		super(vf);
	}

	public void publish(IString sourceId, ITuple patch) {
		
//		ClassLoader parentCl = this.getClass().getClassLoader().getParent();
		ClassLoader c =  this.getClass().getClassLoader();
//		try {
//			Class activatorCls = parentCl.loadClass("");
//			Method getWorkspaceListener = activatorCls.getMethod("getWorkspaceListener");
//			Object bus = getWorkspaceListener.invoke(null, null);
//		} catch (ClassNotFoundException e) {
//			e.printStackTrace();
//		} catch (NoSuchMethodException e) {
//			e.printStackTrace();
//		} catch (SecurityException e) {
//			e.printStackTrace();
//		} catch (IllegalAccessException e) {
//			e.printStackTrace();
//		} catch (IllegalArgumentException e) {
//			e.printStackTrace();
//		} catch (InvocationTargetException e) {
//			e.printStackTrace();
//		}
		
		System.out.println("Foobar!");
		System.out.println(patch);
		
		Patch res = valueToPatch(sourceId.getValue(), patch);
		Activator.getWorkspaceListener().getBus().publish(res, "FSM");
		
		System.out.println(res);
	}
	
	private Patch valueToPatch(String sourceId, ITuple value) {
		Patch res = new Patch(sourceId);
		
		IValue rootID = value.get(0);
		IList edits = (IList)value.get(1);
		
		for(IValue valueEdit : edits) {
			Optional<Edit> edit = value2Edit(valueEdit);
			if(edit.isPresent()) {
				res.getEdits().add(edit.get());
			}
		}
		
		return res;
	}
	
	private Optional<Edit> value2Edit(IValue value) {
		
		Edit res = null;
		
		IConstructor id = (IConstructor) ((ITuple)value).get(0);
		IConstructor edit = (IConstructor) ((ITuple)value).get(1);
		ISourceLocation loc = (ISourceLocation)id.get(0);
		
		if (edit.getName().equals("create")) {
			String clsName = ((IString)edit.get("class")).getValue();
			res = new Create(valueToString(loc),clsName);
		}
		else if (edit.getName().equals("destroy")) {
			res = new Delete(valueToString(loc));
		}
		else {
			String fieldName = ((IString)edit.get("field")).getValue();
			IValue v = edit.get("val");
			if (edit.getName().equals("put")) {
				res = new Set(valueToString(loc),fieldName,valueToString(v));
			}
			else if (edit.getName().equals("unset")) {
				res = new UnSet(valueToString(loc),fieldName);
			}
			else if (edit.getName().equals("ins")) {
				int pos = ((IInteger)edit.get("pos")).intValue();
				res = new Insert(valueToString(loc),fieldName,valueToString(v),pos);
			}
			else if (edit.getName().equals("del")) {
				int pos = ((IInteger)edit.get("pos")).intValue();
				res = new Remove(valueToString(loc),fieldName,pos);
			}
		}
		
		return Optional.ofNullable(res);
	}
	
	private String valueToString(IValue v) {
		Type type = v.getType();
		if (type.isInteger()) {
			return ((IInteger)v).intValue()+"";
		}
		if (type.isString()) {
			return ((IString)v).getValue();
		}
		if (type.isReal()) {
			return ((IReal)v).floatValue()+"";
		}
		if (type.isBool()) {
			return ((IBool)v).getValue()+"";
		}
		if(v instanceof IConstructor) {
			IConstructor constr = (IConstructor) v;
			if(constr.getName().equals("id")) {
				if(constr.getChildren().iterator().hasNext()) {
					return valueToString(constr.getChildren().iterator().next());
				}
			}
		}
		if(type.isSourceLocation()) {
			ISourceLocation loc = (ISourceLocation) v;
//			String locStr = loc.toString();
//			return locStr.substring(1, locStr.length()-1); //remove starting & ending '|'
			return loc.getFragment();
		}
		
		return "";
	}
	
	

	@Override
	public Patch produce() {
		// Can't produce a Patch, we don't know the last state
		// Patch are published by the Rascal builder
		return new Patch(getId());
	}

	@Override
	public Patch synchronize() {
		// TODO: diff with empty Machine
		return new Patch(getId());
	}
}
