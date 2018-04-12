module lang::myfsm::IDE

import lang::myfsm::Syntax;
import lang::ecore::text::Tree2Model;
import lang::ecore::text::PatchTree;
import lang::ecore::text::PTDiff;
import lang::ecore::IO;
import lang::ecore::diff::Diff;
import lang::myfsm::MetaModel;
import lang::myfsm::Slebus;
import lang::ecore::Refs;

import util::IDE;
import Message;
import IO;
import ParseTree;

  map[loc, void(lang::myfsm::MetaModel::Machine)] observers = ();
  
  map[loc, void(Patch)] modelEditors = ();
  map[loc, void(lrel[loc, str])] termEditors = ();
  
  map[loc, lang::myfsm::MetaModel::Machine] models = ();
  map[loc, lang::myfsm::Syntax::Machine] terms = ();
  
  loc myModel = |project://example/Simple.myfsm|;
  loc myTerm = |project://TestIt/src/doors.mf|;

  void applyPatch(Patch p) {
  	iprintln(p);
  	
  	// build the model conforming to the current source code        
    <model, orgs> = tree2modelWithOrigins(#lang::myfsm::MetaModel::Machine, terms[myTerm], uri=myModel);
  	
  	// patch the original source code according to patch p    
    println("[Rascal] Patching tree according to patch");
    println(model);
    println("[Rascal] patch:");
    println(p);
    pt2 = patchTree(#lang::myfsm::Syntax::Machine, terms[myTerm], p, orgs, Tree(type[&U<:Tree] tt, str src) {
       return parse(tt, src);
    });
    
    // parse again to get locs right.
    println("[Rascal] Reparse");
    pt2 = parse(#lang::myfsm::Syntax::Machine, "<pt2>", myTerm);
    println(pt2);
    
    // compute the textual diff between the old parse tree and the patched one
    println("[Rascal] Computing text diff");
    lrel[loc, str] d = ptDiff(terms[myTerm], pt2);
    
    println("TEXT DIFF");
    iprintln(d); 
    
    // the patched tree is now the current one.
    println("[Rascal] Saving new parse tree");
    terms[myTerm] = pt2;
    
    // update the editor
    println("[Rascal] Updating the editor");
    termEd = termEditors[myTerm];
    termEd(d);
  }


void init() {
  termEditors[myTerm] = termEditor(myTerm);
  
  terms[myTerm] = parse(#lang::myfsm::Syntax::Machine, myTerm);
    
  registerLanguage("MyFSM", "mf", lang::myfsm::Syntax::Machine(str src, loc org) {
    return parse(#lang::myfsm::Syntax::Machine, src, org);
  });
  
  registerContributions("MyFSM", {
    builder(set[Message] (lang::myfsm::Syntax::Machine input) {
     
        // save the term
        println("[Rascal] Saving the term <myTerm>");
        terms[myTerm] = input;
        
        
	    // construct the model corresponding to the source code
	    println("[Rascal] Tree 2 model");
	    <model, orgs> = tree2modelWithOrigins(#lang::myfsm::MetaModel::Machine, input, uri=myModel);
	    
	    // if no change in terms of the model, just return the parse tree    
	    if (myModel in models, models[myModel] == model) {
	      return {};
	    }
	    
		println("[Rascal] Saving the model");
	    old = 
		    if(myModel in models) {
				models[myModel];
		    }
		    else {
		    	model;
		    }
	    models[myModel] = model;
	
	    Patch p = diff(#lang::myfsm::MetaModel::Machine, old, models[myModel]);
	    println("[Rascal] Publish PATCH: ");
	    iprintln(p);
	    publish("TestIt/src/doors.mf",p);
	      
	    println("Returning OK");
	    return {};
	 })
  });
}

void synchronize(Patch p) {

	println("[Rascal] DEBUG:syncho patch");
	println(p);

	patchTreeVar = patch2tree(#lang::myfsm::Syntax::Machine, p, Tree(type[&U<:Tree] tt, str src) {
       return parse(tt, src);
    });
    println("[DEBUG] patchTreeVar");
	println(patchTreeVar);
	
	//------------------
	<patchModel, patchOrgs> = tree2modelWithOrigins(#lang::myfsm::MetaModel::Machine, patchTreeVar, uri=myModel);
	<model, orgs> = tree2modelWithOrigins(#lang::myfsm::MetaModel::Machine, terms[myTerm], uri=myModel);
	diffPatch = diff(#lang::myfsm::MetaModel::Machine,model,patchModel);
	
	patchedTree = patchTree(#lang::myfsm::Syntax::Machine, terms[myTerm], diffPatch, orgs, Tree(type[&U<:Tree] tt, str src) {
       return parse(tt, src);
    });
    
    patchedTree = parse(#lang::myfsm::Syntax::Machine, "<patchedTree>", myTerm);
    println("[DEBUG] patchedTree");
	println(patchedTree);
	
	println("[DEBUG] loc");
	println(patchedTree@\loc);
	//------------------
		
	lrel[loc, str] d = ptDiff(terms[myTerm], patchedTree);
    println("TEXT DIFF");
    iprintln(d);
    
    println("[Rascal] Updating the editor");
    termEd = termEditors[myTerm];
    
    println("[DEBUG] replacement");
    println("<patchedTree>");
    
    replace = [<terms[myTerm]@\loc, "<patchedTree>">];
    termEd(replace);
	
}

@javaClass{rascal.RascalProducer}
java void publish(str sourceId, Patch p);
