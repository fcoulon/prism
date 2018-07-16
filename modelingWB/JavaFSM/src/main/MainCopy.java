package main;

import fluent.Fsm;

public class MainCopy {

	public static void main(String[] args) {
		Fsm machine = 
			new Fsm("Doors")
				.initial("opened")
					.target("closed").on("close")
				.state("closed")
					.target("opened").on("open")
					.target("locked").on("lock")
				.state("locked")
					.target("closed").on("unlock")
			.end();
		
		System.out.println(machine.toString());
	}
}
