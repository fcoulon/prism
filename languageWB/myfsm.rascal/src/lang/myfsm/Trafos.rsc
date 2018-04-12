module lang::myfsm::Trafos

import lang::myfsm::MetaModel;
import lang::ecore::Refs;
import lang::ecore::diff::Diff;

import List;
import IO;

Patch runAddState(Loader[&T] load) {
  Machine m = load(#Machine);
  Patch patch = diff(#Machine, m, addState(m));
  iprintln(patch);
  return patch;
} 


Machine addState(Machine m) {
  r = newRealm();
  newState = r.new(#State, State("NewState_<size(m.states)>", []));
  m.states = [m.states[0]] + [m.states[2]];
  bla = r.new(#State, State("BLA", []));
  tr = r.new(#Trans, Trans("bar", referTo(#State, newState)));
  bla.transitions += [tr];
  m.states += [newState];
  m.states = [bla] + m.states;
  m.initial = referTo(#State, newState);
  m.name = m.name + "_";
  return m;
}
