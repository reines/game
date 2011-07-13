package com.game.common.codec;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

import com.game.common.util.ISAACAlgorithm;

public class PacketDecoder extends CumulativeProtocolDecoder {

	@Override
	public boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
		if (in.remaining() < Packet.HEADER_SIZE)
			return false;

		// Mark the position in-case after reading the header we realise we don't have enough data yet
		in.mark();

		int ordinal = in.getInt();
		int size = in.getInt();
		// If we don't have enough data for the payload yet, reset back to before the header
		if (in.remaining() < size) {
			in.reset();
			return false;
		}

		// If encryption is enabled, decrypt the packet type
		if (session.containsAttribute("decrypter")) {
			ISAACAlgorithm decrypter = (ISAACAlgorithm) session.getAttribute("decrypter");
			ordinal -= decrypter.nextInt();
		}

		Packet.Type type = Packet.Type.values()[ordinal];

		IoBuffer payload = in.getSlice(size).asReadOnlyBuffer();
		Packet message = new Packet(type, payload, session);

		// Output the decoded packet
		out.write(message);
		return true;
	}

	@Override
	public void dispose(IoSession session) throws Exception { }

}
