package week1;

import framework.Channel;
import framework.IllegalReceiveException;
import framework.Message;
import framework.Process;

public class RockPaperScissorsCheatingProcess extends Process {

	@Override
	public void init() {
		// TODO
	}

	@Override
	public void receive(Message m, Channel c) throws IllegalReceiveException {
		throw new IllegalReceiveException();
		// TODO
	}
}
