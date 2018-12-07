package lib.basicFrm.rtmp.message;

import java.util.ArrayList;
import java.util.List;

import lib.basicFrm.amf.Amf0Object;
import lib.basicFrm.amf.Amf0Value;
import lib.basicFrm.rtmp.RTMPHeader;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

public class CommandAmf0 extends Command {    

    public CommandAmf0(RTMPHeader header, ChannelBuffer in) {
        super(header, in);        
    }

    public CommandAmf0(int transactionId, String name, Amf0Object object, Object ... args) {
        super(transactionId, name, object, args);
    }

    public CommandAmf0(String name, Amf0Object object, Object ... args) {
        super(name, object, args);
    }

    @Override
    MessageType getMessageType() {
        return MessageType.COMMAND_AMF0;
    }

    @Override
    public ChannelBuffer encode() {
        ChannelBuffer out = ChannelBuffers.dynamicBuffer();
        Amf0Value.encode(out, name, transactionId, object);
        if(args != null) {
            for(Object o : args) {
                Amf0Value.encode(out, o);
            }
        }
        return out;
    }

    @Override
    public void decode(ChannelBuffer in) {                
        name = (String) Amf0Value.decode(in);
        transactionId = ((Double) Amf0Value.decode(in)).intValue();
        object = (Amf0Object) Amf0Value.decode(in);
        List<Object> list = new ArrayList<Object>();
        while(in.readable()) {
            list.add(Amf0Value.decode(in));
        }
        args = list.toArray();
    }

}
