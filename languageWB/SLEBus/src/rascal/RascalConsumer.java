package rascal;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.emf.ecore.EObject;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.interpreter.TypeReifier;
import org.rascalmpl.interpreter.env.Environment;
import org.rascalmpl.interpreter.utils.RuntimeExceptionFactory;
import org.rascalmpl.uri.URIUtil;

import edit.Create;
import edit.Delete;
import edit.Edit;
import edit.Insert;
import edit.Patch;
import edit.Remove;
import edit.Set;
import edit.UnSet;
import io.usethesource.vallang.IConstructor;
import io.usethesource.vallang.IList;
import io.usethesource.vallang.ISourceLocation;
import io.usethesource.vallang.ITuple;
import io.usethesource.vallang.IValue;
import io.usethesource.vallang.IValueFactory;
import io.usethesource.vallang.exceptions.FactTypeUseException;
import io.usethesource.vallang.type.Type;
import io.usethesource.vallang.type.TypeFactory;
import io.usethesource.vallang.type.TypeStore;
import lang.ecore.bridge.EMFBridge;
import slebus.Consumer;

public class RascalConsumer implements Consumer {
	
//	final static String fileURI = "project://TestIt/src/doors.myfsm";
	final static String scheme = "project";
//	final static String authority = "TestIt";
//	final static String path = "src/doors.mf";
	final static String authority = "example";
	final static String path = "Simple.myfsm";
	

	Evaluator eval;
	private static TypeFactory tf = TypeFactory.getInstance();
	private final IValueFactory vf;
	private final TypeReifier tr;

	public RascalConsumer(IValueFactory vf) {
		this.vf = vf;
		this.tr = new TypeReifier(vf);
	}

	@Override
	public String getId() {
		return "TestIt/src/doors.mf";
	}

	@Override
	public void consume(Patch p) {
		IValue patch = patchToValue(p, eval.getCurrentEnvt());
		eval.call("applyPatch", patch);
	}

	@Override
	public void synchronize(Patch p) {
		this.eval = EMFBridge.getEvaluator("myfsm_rascal", "lang::myfsm::IDE",Optional.of(this.getClass().getClassLoader()));
//		eval.addClassLoader(this.getClass().getClassLoader());
		ClassLoader cl = this.getClass().getClassLoader();
		List<ClassLoader> loaders = eval.getClassLoaders();
		
		eval.call("init");
		IValue patch = patchToValue(p, eval.getCurrentEnvt());
		eval.call("synchronize", patch);
	}
	
	public void init() {
		eval.__getJavaBridge();
		eval.call("init");
	}

	public IValue patchToValue(Patch patch, Environment env) {

		TypeStore ts = new TypeStore();
		Type stringType = tf.stringType();
		Type valueType = tf.valueType();
		Type intType = tf.integerType();

		Type editType = tf.abstractDataType(ts, "Edit");
		Type idType = tf.abstractDataType(ts, "Id");

		Object[] typesAndLabels = new Object[] { stringType, "field", valueType, "val" };
		Type put = tf.constructor(ts, editType, "put", typesAndLabels); // put(str,value)

		typesAndLabels = new Object[] { stringType, "field" };
		Type unset = tf.constructor(ts, editType, "unset", typesAndLabels); // unset(str)

		typesAndLabels = new Object[] { stringType, "field", intType, "pos", valueType, "val" };
		Type ins = tf.constructor(ts, editType, "ins", typesAndLabels); // ins(str,int,value)

		typesAndLabels = new Object[] { stringType, "field", intType, "pos" };
		Type del = tf.constructor(ts, editType, "del", typesAndLabels); // del(str,int)

		typesAndLabels = new Object[] { stringType, "class" };
		Type create = tf.constructor(ts, editType, "create", typesAndLabels); // create(str)

		Type destroy = tf.constructor(ts, editType, "destroy"); // destroy()

		Type idOfInt = tf.constructor(ts, idType, "id", intType); // id(int)
		Type idOfLoc = tf.constructor(ts, idType, "id", tf.sourceLocationType()); // id(loc)

		List<ITuple> edits = new ArrayList<>();

		for (Edit edit : patch.getEdits()) {

			IConstructor value = null;
			IConstructor insValueForCreate = null;
			String insIdForCreate = null;

			if (edit instanceof Create) {
				String className = ((Create) edit).getType();
				value = vf.constructor(create, vf.string(className));
				
				String fragment = edit.getId();
				
				int lastSlashPos = fragment.lastIndexOf("/");
				int lastAtPos = fragment.lastIndexOf("@");
				int lastDotPos = fragment.lastIndexOf(".");
				if(lastSlashPos > 0 && lastAtPos != -1 && lastDotPos != -1) {
					String posStr = fragment.substring(lastDotPos+1);
					int position = Integer.decode(posStr);
					String field = fragment.substring(lastAtPos+1, lastDotPos);
					insIdForCreate = fragment.substring(0, lastSlashPos);
					insValueForCreate = vf.constructor(ins, vf.string(field), vf.integer(position), makeId(fragment,vf,ts)); //FIXME: can be a put()
				}
				
			} else if (edit instanceof Delete) {
				value = vf.constructor(destroy);
			} else if (edit instanceof Set) {
				String field = ((Set) edit).getField();
				String setVal = ((Set) edit).getValue();
				if(setVal.startsWith("/")) { //FIXME: check if field's type
					value = vf.constructor(put, vf.string(field), makeId(setVal,vf,ts));
				}
				else {
					value = vf.constructor(put, vf.string(field), vf.string(setVal));
				}
			} else if (edit instanceof UnSet) {
				String field = ((UnSet) edit).getField();
				value = vf.constructor(unset, vf.string(field));
			} else if (edit instanceof Insert) {
				String field = ((Insert) edit).getField();
				String setVal = ((Insert) edit).getValue();
				int position = ((Insert) edit).getIndex();
				if(setVal.startsWith("/")) { //FIXME: check if field's type 
					value = vf.constructor(ins, vf.string(field), vf.integer(position), makeId(setVal,vf,ts));
				}
				else {
					value = vf.constructor(ins, vf.string(field), vf.integer(position), vf.string(setVal));
				}
			} else if (edit instanceof Remove) {
				String field = ((Remove) edit).getField();
				int position = ((Remove) edit).getIndex();
				value = vf.constructor(del, vf.string(field), vf.integer(position));
			}

			if (value != null) {
				try {
//					URI uri = URI.create(edit.getId());
//					String authority = uri.getAuthority();
					String fragment = edit.getId();
//					String path = uri.getPath();
//					String scheme = uri.getScheme();
					IConstructor id = vf.constructor(idOfLoc,
							vf.sourceLocation(scheme, authority, path, null, fragment));
					ITuple tuple = vf.tuple(id, value);
					edits.add(tuple);
					
					if(insIdForCreate != null) { // in case of create, we add a Set(container,field,value)
						IConstructor setId = vf.constructor(idOfLoc,
								vf.sourceLocation(scheme, authority, path, null, insIdForCreate));
						ITuple setTuple = vf.tuple(setId, insValueForCreate);
						edits.add(setTuple);
					}
					
				} catch (FactTypeUseException e) {
					e.printStackTrace();
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
			}
		}

		ITuple[] tuples = new ITuple[edits.size()];
		IList editLists = vf.list(edits.toArray(tuples)); // Edits

		try {
//			IConstructor idRoot = vf.constructor(idOfLoc,
//					vf.sourceLocation("project", "TestIt", "src/doors.myfsm", null, "/"));
			IConstructor idRoot = vf.constructor(idOfLoc,
					vf.sourceLocation("project", "example", "Simple.myfsm", null, "/"));
			return vf.tuple(idRoot, editLists); // Patch
		} catch (FactTypeUseException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		return vf.tuple();
	}
	
	private static IValue makeId(String refValue, IValueFactory vf, TypeStore ts) {
//		Type genRefType = ts.lookupAbstractDataType("Ref");
//		Type genRefType = tf.abstractDataType(ts, "Ref");
		
		Type idType = ts.lookupAbstractDataType("Id");
		
//		Type idOfLoc = tf.constructor(ts, idType, "id", tf.sourceLocationType()); // id(loc)
		Type idCons = ts.lookupConstructor(idType, "id", tf.tupleType(tf.sourceLocationType()));
		
//		Type refCons = tf.constructor(ts, genRefType, "ref", tf.tupleType(idType));
//		Type refCons = ts.lookupConstructor(genRefType,  "ref", tf.tupleType(idType));
//		IValue id = getIdFor(eObj, vf, ts, src);
		
		try {
			return vf.constructor(idCons, vf.sourceLocation(scheme, authority, path, null, refValue));
//			return vf.constructor(refCons, id);
		} catch (FactTypeUseException | URISyntaxException e) {
			throw RuntimeExceptionFactory.malformedURI(scheme + "/" + authority + "/" + path + "#" + refValue, null, null);
		}
	}

}
