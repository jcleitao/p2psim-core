package peernet.transport;

/**
 * This is an abstract class to simplify the representation of a network message.
 *
 * @author João Leitão (jc.leitao@fct.unl.pt)
 */

public abstract class NetworkMessage implements Cloneable {

    private int protoId;
    private short msgType;
    private Address sender;
    private Address destination;

    public NetworkMessage(int protoId, short type, Address sender, Address destination) {
        this.protoId = protoId;
        this.msgType = type;
        this.sender = sender;
        this.destination = destination;
    }

    public int getProtoId() {
        return protoId;
    }

    public void setProtoId(int protoId) {
        this.protoId = protoId;
    }

    public short getType() {
        return msgType;
    }

    public void setType(short type) {
        this.msgType = type;
    }

    public Address getSender() {
        return sender;
    }

    public void setSender(Address sender) {
        this.sender = sender;
    }

    public Address getDestination() {
        return destination;
    }

    public void setDestination(Address destination) {
        this.destination = destination;
    }

    public NetworkMessage clone() {
        NetworkMessage s = null;
        try {
            s = (NetworkMessage) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            System.exit(1);
        }
        s.sender = (Address) this.sender.clone();
        s.destination = (Address) this.destination.clone();
        return s;
    }
}
