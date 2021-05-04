package com.tfc.minecraft_effekseer_implementation;

import net.minecraft.network.INetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

public class EndEmitterPacket implements IPacket {
	public final ResourceLocation effekName;
	public final ResourceLocation emitterName;
	public final boolean deleteEmitter;
	
	public EndEmitterPacket(ResourceLocation effekName, ResourceLocation emitterName, boolean deleteEmitter) {
		this.effekName = effekName;
		this.emitterName = emitterName;
		this.deleteEmitter = deleteEmitter;
	}
	
	public EndEmitterPacket(PacketBuffer buffer) {
		effekName = buffer.readResourceLocation();
		emitterName = buffer.readResourceLocation();
		deleteEmitter = buffer.readBoolean();
	}
	
	@Override
	public void readPacketData(PacketBuffer buf) {
	}
	
	@Override
	public void writePacketData(PacketBuffer buf) {
		buf.writeResourceLocation(effekName);
		buf.writeResourceLocation(emitterName);
		buf.writeBoolean(deleteEmitter);
	}
	
	@Override
	public void processPacket(INetHandler handler) {
	}
}
