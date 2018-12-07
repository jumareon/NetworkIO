package lib.basicFrm.rtmp.message;

import lib.basicFrm.rtmp.RTMPHeader;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

public class ChunkSize extends AbstractMessage {

    private int chunkSize;

    public ChunkSize(RTMPHeader header, ChannelBuffer in) {
        super(header, in);
    }

    public ChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    @Override
    MessageType getMessageType() {
        return MessageType.CHUNK_SIZE;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    @Override
    public ChannelBuffer encode() {
        ChannelBuffer out = ChannelBuffers.buffer(4);
        out.writeInt(chunkSize);
        return out;
    }

    @Override
    public void decode(ChannelBuffer in) {
        chunkSize = in.readInt();
    }

    @Override
    public String toString() {
        return super.toString() + chunkSize;
    }

}
