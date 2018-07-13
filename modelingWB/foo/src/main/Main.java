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
				.state("locked")
			.end();

		//test
		System.out.println(machine.toString());
	}
}
