package main;

import fluent.Fsm;

public class Main {

	public static void main(String[] args) {
		Fsm machine = 
			new Fsm("Doors")
			.initial("opened")
				.target("closed").on("close")
			.state("closed")
				.target("locked").on("lock")
				.target("opened").on("open")
			.state("locked")
				.target("closed").on("unlock")
			.state("b")
				.target("locked")
			.end();
		
		//test
		System.out.println(machine.toString());
	}
}
