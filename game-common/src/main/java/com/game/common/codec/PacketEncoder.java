package com.game.common.codec;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

import com.game.common.util.ISAACAlgorithm;

public class PacketEncoder implements ProtocolEncoder {

	@Override
	public void encode(IoSession session, Object o, ProtocolEncoderOutput out) throws Exception {
		PacketBuilder message = (PacketBuilder) o;

		// Allocate a buffer of the exact size for the packet
		IoBuffer buffer = IoBuffer.allocate(Packet.HEADER_SIZE + message.size(), false);

		// Write the packet header
		int ordinal = message.getType().ordinal();

		// If encryption is enabled, encrypt the packet type
		if (session.containsAttribute("encrypter")) {
			ISAACAlgorithm encrypter = (ISAACAlgorithm) session.getAttribute("encrypter");
			ordinal += encrypter.nextInt();
		}

		// Write the packet headers
		buffer.putInt(ordinal);
		buffer.putInt(message.size());

		// Write the packet payload
		buffer.put(message.getPayload());

		// Flip then output
		out.write(buffer.flip());
	}

	@Override
	public void dispose(IoSession session) throws Exception { }
}
