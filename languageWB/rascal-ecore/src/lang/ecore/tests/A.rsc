module lang::ecore::tests::A

import lang::ecore::Ecore;
import lang::ecore::Refs;
import lang::ecore::diff::Diff;

import List;
import IO;

Patch foo(Loader[&T] load) {
  EPackage pkg = load(#EPackage);
  Patch patch = diff(#Machine, pkg, pkg);
  iprintln(patch);
  return patch;
} 