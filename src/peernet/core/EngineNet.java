/*
 * Created on Apr 28, 2012 by Spyros Voulgaris
 * Change log: (Jleitao May 2021) Changed the Event class to do not point to the internal classe in Heap
 */
package peernet.core;

import peernet.dynamics.BootstrapClient;
import peernet.dynamics.BootstrapServer.BootstrapMessage;
import peernet.transport.Address;
import peernet.transport.Packet;
import peernet.transport.TransportNet;
import peernet.util.CountLatch;


public class EngineNet extends Engine {
    Heap controlHeap = null;

    private CountLatch blockingInitializers = new CountLatch(0);


    @Override
    protected void createHeaps() {
        // one heap per node
        for (int n = 0; n < Network.size(); n++)
            Network.get(n).setHeap(new Heap());

        // and one heap for all controls together
        controlHeap = new Heap();
    }


    @Override
    public void startExperiment() {
        super.startExperiment();

        // If we are in NET mode, start a network listener per node
        // (or more network listeners, if nodes define multiple Transports)
        if (getType() == Type.NET) {
            for (int n = 0; n < Network.size(); n++) {
                Node node = Network.get(n);
                for (int j = 0; j < node.getTransports(); j++) {
                    new ListeningThread(node, node.getHeap(), (TransportNet) node.getTransport(j)).start();
                }
            }
        }

        // Wait for initializers to complete
        blockingInitializers.await();

        // Now let time start rolling!
        CommonState.timeStartsNow();

        // Either in NET or EMU mode, start a thread per node.
        for (int n = 0; n < Network.size(); n++) {
            Node node = Network.get(n);
            new ExecutionThread(node.getHeap()).start();
        }

        // And create a single thread for control messages
        new ExecutionThread(controlHeap).start();

//
//    // analysis after the simulation
//    CommonState.setPhase(CommonState.POST_SIMULATION);
//    for (int j = 0; j<controls.length; ++j)
//    {
//      if (controlSchedules[j].fin)
//        controls[j].execute();
//    }
    }


    public void addEventAt(long time, Address src, Node node, int pid, Object event) {
        if (time >= endtime)
            return;

        time = (time << rbits) | CommonState.r.nextInt(1 << rbits);

        Heap heap = null;
        if (node == null)  // control event
            heap = controlHeap;
        else
            heap = node.getHeap();

        synchronized (heap) {
            heap.add(time, src, node, (byte) pid, event);
            heap.notify();
        }
    }


    public long pendingEvents() {
        int events = 0;
        for (int n = 0; n < Network.size(); n++)
            events += Network.get(n).getHeap().size();
        return events;
    }


    /**
     * Execute and remove the next event from the ordered event list.
     * This is called by each node's own execution thread individually.
     *
     * @return true if the execution should be stopped.
     */
    protected boolean executeNext(Event ev) {
        long time = ev.time >> rbits;
        if (time >= endtime) // XXX Should we also check here, or only when scheduling an event?
            return true;

        int pid = ev.pid;
        if (ev.node == null)  // XXX ugly way to identify control events
        {
            for (int n = 0; n < Network.size(); n++) //XXX The network size might change in the meantime
                Network.get(n).acquireLock();

            boolean ret = controls[pid].execute();

            for (int n = 0; n < Network.size(); n++)
                Network.get(n).releaseLock();

            long delay = controlSchedules[pid].nextDelay(time);
            if (delay >= 0)
                addEventAt(time + delay, null, null, pid, null);
            return ret;
        } else if (ev.node.isUp()) {
//        CommonState.setPid(pid);  // XXX try to entirely avoid CommonState
//        CommonState.setNode(ev.node);

            Protocol prot = ev.node.getProtocol(pid);

            if (ev.event instanceof Schedule) {
                ev.node.acquireLock();
                prot.nextCycle(((Schedule) ev.event).schedId);
                ev.node.releaseLock();

                long delay = prot.nextDelay();
                if (delay == 0)
                    delay = ((Schedule) ev.event).nextDelay(time);

                if (delay > 0)
                    addEventAt(time + delay, null, ev.node, pid, ev.event);
            } else // call Protocol.processEvent()
            {
                ev.node.acquireLock();
                prot.processEvent(ev.src, ev.event);
                ev.node.releaseLock();
            }
        }
        return false;
    }


    @Override
    public void blockingInitializerStart() {
        blockingInitializers.countUp();
    }


    @Override
    public void blockingInitializerDone() {
        blockingInitializers.countDown();
    }


    public class ExecutionThread extends Thread {
        private Heap heap = null;

        public ExecutionThread(Heap heap) {
            this.heap = heap;
        }


        public void run() {
            boolean exit = false;

            long remainingTime;
            while (!exit) {
                Event event = null;
                synchronized (heap) {
                    while ((remainingTime = (heap.getNextTime() >> rbits) - CommonState.getTime()) > 0) {
                        try {
                            heap.wait(remainingTime);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    event = heap.removeFirst();
                }
                exit = executeNext(event);
            }
        }

        public Object clone() {
            return new Heap();
        }
    }


    public class ListeningThread extends Thread {
        Node node = null;
        Heap heap = null;
        TransportNet transport = null;

        public ListeningThread(Node node, Heap heap, TransportNet transport) {
            this.node = node;
            this.heap = heap;
            this.transport = transport;
        }

        public void run() {
            while (true) {
                Packet packet = transport.receive();

                assert packet != null : "packet is null!";
                assert packet.src != null : "packet.src is null!";
                assert packet.event != null : "packet.event is null!";

                if (packet.event instanceof BootstrapMessage)
                    BootstrapClient.report(node, (BootstrapMessage) packet.event);
                else {
                    synchronized (heap) {
                        heap.add(0, packet.src, node, (byte) packet.pid, packet.event);
                        heap.notify();
                    }
                }
            }
        }
    }
}