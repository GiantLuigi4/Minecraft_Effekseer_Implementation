package com.tfc.minecraft_effekseer_implementation.meifabric;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.impl.networking.server.ServerNetworkingImpl;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class EndEmitterPacket {
	public final Identifier effekName;
	public final Identifier emitterName;
	public final boolean deleteEmitter;
	
	public EndEmitterPacket(Identifier effekName, Identifier emitterName, boolean deleteEmitter) {
		this.effekName = effekName;
		this.emitterName = emitterName;
		this.deleteEmitter = deleteEmitter;
	}
	
	public EndEmitterPacket(PacketByteBuf buffer) {
		effekName = buffer.readIdentifier();
		emitterName = buffer.readIdentifier();
		deleteEmitter = buffer.readBoolean();
	}
	
	public void send(ServerPlayerEntity player) {
		PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		writePacketData(buf);
		ServerPlayNetworking.send(player, NetworkingFabric.id2, buf);
	}
	
	public void writePacketData(PacketByteBuf buf) {
		buf.writeIdentifier(effekName);
		buf.writeIdentifier(emitterName);
		buf.writeBoolean(deleteEmitter);
	}
	
}
