package week56;

import static org.junit.jupiter.api.Assertions.*;
import static framework.ProcessTests.*;

import java.util.Collection;
import java.util.Map;
import java.util.HashMap;

import org.junit.jupiter.api.Test;

import framework.IllegalReceiveException;
import framework.Message;
import framework.Channel;
import framework.Network;

class TarryProcessTest {
	
	// Initiator should not finish and should send a single token on init
	@Test
	void initTest1() {
		Network n = Network.parse(true, "p:week56.TarryInitiator q,r,s:week56.TarryNonInitiator").makeComplete();

		WaveProcess p = (WaveProcess) n.getProcess("p");
		p.init();

		assertTrue(p.isActive());
		assertFalse(p.isPassive());

		int sum = 0;
		Collection<Message> pout;
		for (Channel d : p.getOutgoing()) {
			pout = d.getContent();
			if (pout.size() > 0) {
				assertTrue(pout.iterator().next() instanceof TokenMessage);
				sum += pout.size();
			}
		}
		assertEquals(1, sum);
	}

	// Non-initiators should not finish and should not send anything on init
	@Test
	void initTest2() {
		Network n = Network.parse(true, "p:week56.TarryInitiator q,r,s:week56.TarryNonInitiator").makeComplete();

		WaveProcess q = (WaveProcess) n.getProcess("q");
		q.init();

		assertTrue(q.isActive());
		assertFalse(q.isPassive());

		int sum = 0;
		for (Channel d : q.getOutgoing()) {
			sum += d.getContent().size();
		}
		assertEquals(0, sum);
	}

	// Initiator illegal message type: throw exception
	@Test
	void receiveTest1() {
		Network n = Network.parse(true, "p:week56.TarryInitiator q,r,s:week56.TarryNonInitiator").makeComplete();

		WaveProcess p = (WaveProcess) n.getProcess("p");
		p.init();

		assertThrows(IllegalReceiveException.class, () -> p.receive(Message.DUMMY, n.getChannel("r", "p")));
	}

	// Initiator receives token, but not from all neighbours: forward token
	@Test
	void receiveTest2() {
		Network n = Network.parse(true, "p:week56.TarryInitiator q,r,s:week56.TarryNonInitiator").makeComplete();

		WaveProcess p = (WaveProcess) n.getProcess("p");
		p.init();

		int sum = 0;
		Collection<Message> pout;
		for (Channel d : p.getOutgoing()) {
			pout = d.getContent();
			if (pout.size() > 0) {
				assertTrue(pout.iterator().next() instanceof TokenMessage);
				sum += pout.size();
			}
		}
		assertEquals(1, sum);

		receiveOrCatch(p, new TokenMessage(), n.getChannel("q", "p"));

		sum = 0;
		for (Channel d : p.getOutgoing()) {
			pout = d.getContent();
			if (pout.size() > 0) {
				assertEquals(1, pout.size());
				assertTrue(pout.iterator().next() instanceof TokenMessage);
				sum += pout.size();
			}
		}
		assertEquals(2, sum);
	}

	// Initiator receives token, but not from all neighbours: do not finish
	@Test
	void receiveTest3() {
		Network n = Network.parse(true, "p:week56.TarryInitiator q,r,s:week56.TarryNonInitiator").makeComplete();

		WaveProcess p = (WaveProcess) n.getProcess("p");
		p.init();

		assertFalse(p.isPassive());

		receiveOrCatch(p, new TokenMessage(), n.getChannel("q", "p"));
		assertFalse(p.isPassive());

		receiveOrCatch(p, new TokenMessage(), n.getChannel("r", "p"));
		assertFalse(p.isPassive());
	}

	// Initiator does not forward through the same channel twice
	@Test
	void receiveTest4() {
		Network n = Network.parse(true, "p:week56.TarryInitiator");
		for (int i = 0; i < 100; i++) {
			n.addProcess("q" + i, "week56.TarryNonInitiator");
		}
		n.makeComplete();

		WaveProcess p = (WaveProcess) n.getProcess("p");
		p.init();

		int sum = 0;
		for (Channel d : p.getOutgoing()) {
			sum += d.getContent().size();
		}
		assertEquals(1, sum);

		// p should forward the token every time it receives it
		for (int i = 1; i < 100; i++) {
			receiveOrCatch(p, new TokenMessage(), n.getChannel("q" + i, "p"));

			sum = 0;
			for (Channel d : p.getOutgoing()) {
				sum += d.getContent().size();
			}
			assertEquals(i + 1, sum);
		}

		// Every outgoing channel from p should contain exactly one token
		Collection<Message> pout;
		for (Channel d : p.getOutgoing()) {
			pout = d.getContent();
			if (pout.size() > 0) {
				assertEquals(1, pout.size());
				assertTrue(pout.iterator().next() instanceof TokenMessage);
			}
		}
	}

	// Initiator receives token from all neighbours: finish
	@Test
	void receiveTest5() {
		Network n = Network.parse(true, "p:week56.TarryInitiator q,r,s:week56.TarryNonInitiator").makeComplete();

		WaveProcess p = (WaveProcess) n.getProcess("p");
		p.init();

		assertFalse(p.isPassive());

		receiveOrCatch(p, new TokenMessage(), n.getChannel("q", "p"));
		assertFalse(p.isPassive());

		receiveOrCatch(p, new TokenMessage(), n.getChannel("r", "p"));
		assertFalse(p.isPassive());

		receiveOrCatch(p, new TokenMessage(), n.getChannel("s", "p"));
		assertTrue(p.isPassive());
	}

	// Initiator receives when finished: throw exception
	@Test
	void receiveTest6() {
		Network n = Network.parse(true, "p:week56.TarryInitiator q,r,s:week56.TarryNonInitiator").makeComplete();

		WaveProcess p = (WaveProcess) n.getProcess("p");
		p.init();

		receiveOrCatch(p, new TokenMessage(), n.getChannel("q", "p"));
		receiveOrCatch(p, new TokenMessage(), n.getChannel("r", "p"));
		receiveOrCatch(p, new TokenMessage(), n.getChannel("s", "p"));
		assertTrue(p.isPassive());

		assertThrows(IllegalReceiveException.class, () -> p.receive(new TokenMessage(), n.getChannel("q", "p")));
	}

	// Non-initiator illegal message type: throw exception
	@Test
	void receiveTest7() {
		Network n = Network.parse(true, "p:week56.TarryInitiator q,r,s:week56.TarryNonInitiator").makeComplete();

		WaveProcess q = (WaveProcess) n.getProcess("q");
		q.init();

		assertThrows(IllegalReceiveException.class, () -> q.receive(Message.DUMMY, n.getChannel("r", "q")));
	}

	// Non-initiator receives, but not from all neighbours: forward
	@Test
	void receiveTest8() {
		Network n = Network.parse(true, "p:week56.TarryInitiator q,r,s:week56.TarryNonInitiator").makeComplete();

		WaveProcess q = (WaveProcess) n.getProcess("q");
		q.init();

		int sum = 0;
		for (Channel d : q.getOutgoing()) {
			sum += d.getContent().size();
		}
		assertEquals(0, sum);

		receiveOrCatch(q, new TokenMessage(), n.getChannel("p", "q"));

		sum = 0;
		for (Channel d : q.getOutgoing()) {
			sum += d.getContent().size();
		}
		assertEquals(1, sum);
		assertTrue(q.isActive());

		receiveOrCatch(q, new TokenMessage(), n.getChannel("r", "q"));

		sum = 0;
		for (Channel d : q.getOutgoing()) {
			sum += d.getContent().size();
		}
		assertEquals(2, sum);

		Collection<Message> pout;
		for (Channel d : q.getOutgoing()) {
			pout = d.getContent();
			if (pout.size() > 0) {
				assertEquals(1, pout.size());
				assertTrue(pout.iterator().next() instanceof TokenMessage);
			}
		}
	}

	// Non-initiator receives, but not from all neighbours: do not finish
	@Test
	void receiveTest9() {
		Network n = Network.parse(true, "p:week56.TarryInitiator q,r,s:week56.TarryNonInitiator").makeComplete();

		WaveProcess q = (WaveProcess) n.getProcess("q");
		q.init();

		assertTrue(q.isActive());
		assertFalse(q.isPassive());

		receiveOrCatch(q, new TokenMessage(), n.getChannel("p", "q"));
		assertFalse(q.isPassive());

		receiveOrCatch(q, new TokenMessage(), n.getChannel("r", "q"));
		assertFalse(q.isPassive());
	}

	// Non-initiator does not forward through the same channel twice
	@Test
	void receiveTest10() {
		Network n = Network.parse(true, "p:week56.TarryInitiator");
		for (int i = 0; i < 100; i++) {
			n.addProcess("q" + i, "week56.TarryNonInitiator");
		}
		n.makeComplete();

		WaveProcess q = (WaveProcess) n.getProcess("q0");
		q.init();

		int sum = 0;
		for (Channel d : q.getOutgoing()) {
			sum += d.getContent().size();
		}
		assertEquals(0, sum);

		Collection<Message> pout;
		for (int i = 1; i < 100; i++) {
			receiveOrCatch(q, new TokenMessage(), n.getChannel("q" + i, "q0"));

			sum = 0;
			for (Channel d : q.getOutgoing()) {
				pout = d.getContent();
				if (pout.size() > 0) {
					assertEquals(1, pout.size());
					assertTrue(pout.iterator().next() instanceof TokenMessage);
					sum += pout.size();
				}
			}
			assertEquals(i, sum);
		}
	}

	// Non-initiator only forwards to parent if only option
	@Test
	void receiveTest11() {
		Network n = Network.parse(true, "p:week56.TarryInitiator");
		for (int i = 0; i < 100; i++) {
			n.addProcess("q" + i, "week56.TarryNonInitiator");
		}
		n.makeComplete();

		WaveProcess q = (WaveProcess) n.getProcess("q0");
		q.init();

		receiveOrCatch(q, new TokenMessage(), n.getChannel("p", "q0"));

		for (int i = 2; i < 100; i++) {
			receiveOrCatch(q, new TokenMessage(), n.getChannel("q" + i, "q0"));
		}
		assertEquals(0, n.getChannel("q0", "p").getContent().size());

		receiveOrCatch(q, new TokenMessage(), n.getChannel("q1", "q0"));
		assertEquals(1, n.getChannel("q0", "p").getContent().size());
	}

	// Non-initiator receives from all neighbours: finish
	@Test
	void receiveTest12() {
		Network n = Network.parse(true, "p:week56.TarryInitiator");
		for (int i = 0; i < 100; i++) {
			n.addProcess("q" + i, "week56.TarryNonInitiator");
		}
		n.makeComplete();

		WaveProcess q = (WaveProcess) n.getProcess("q0");
		q.init();

		assertFalse(q.isPassive());

		for (int i = 1; i < 100; i++) {
			receiveOrCatch(q, new TokenMessage(), n.getChannel("q" + i, "q0"));
			assertFalse(q.isPassive());
		}

		receiveOrCatch(q, new TokenMessage(), n.getChannel("p", "q0"));
		assertTrue(q.isPassive());
	}

	// Non-initiator receives when finished: throw exception
	@Test
	void receiveTest13() {
		Network n = Network.parse(true, "p:week56.TarryInitiator q,r,s:week56.TarryNonInitiator").makeComplete();

		WaveProcess q = (WaveProcess) n.getProcess("q");
		q.init();

		receiveOrCatch(q, new TokenMessage(), n.getChannel("p", "q"));
		receiveOrCatch(q, new TokenMessage(), n.getChannel("r", "q"));
		receiveOrCatch(q, new TokenMessage(), n.getChannel("s", "q"));
		assertTrue(q.isPassive());

		assertThrows(IllegalReceiveException.class, () -> q.receive(new TokenMessage(), n.getChannel("r", "q")));
	}

	// Simulate full run
	@Test
	void simulationTest1() {
		Network n = Network.parse(true, "p:week56.TarryInitiator");
		for (int i = 0; i < 10; i++) {
			n.addProcess("q" + i, "week56.TarryNonInitiator");
		}
		n.makeComplete();
		Map<String, Collection<String>> output = new HashMap<String, Collection<String>>();

		try {
			assertTrue(n.simulate(output));
		} catch (IllegalReceiveException e) {
			assertTrue(false);
		}

		// No output, check internal state:
		// All processes should have finished
		assertTrue(((WaveProcess) n.getProcess("p")).isPassive());
		for (int i = 0; i < 10; i++) {
			assertTrue(((WaveProcess) n.getProcess("q" + i)).isPassive());
		}
	}
}
