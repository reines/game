package com.game.common.codec;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

public class PacketCodecFactory implements ProtocolCodecFactory {

	private final ProtocolDecoder decoder;
	private final ProtocolEncoder encoder;

	public PacketCodecFactory() {
		decoder = new PacketDecoder();
		encoder = new PacketEncoder();
	}

	public ProtocolDecoder getDecoder(IoSession session) throws Exception {
		return decoder;
	}

	public ProtocolEncoder getEncoder(IoSession session) throws Exception {
		return encoder;
	}

}
