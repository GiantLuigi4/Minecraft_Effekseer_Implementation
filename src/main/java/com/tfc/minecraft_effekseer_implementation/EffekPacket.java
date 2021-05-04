package com.tfc.minecraft_effekseer_implementation;

import net.minecraft.network.INetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;

import java.io.IOException;

//I will implement IPacket, and you cannot stop me.
public class EffekPacket implements IPacket {
	public final ResourceLocation effekName;
	public final ResourceLocation emmiterName;
	public final float progress;
	public final Vector3d position;
	
	public EffekPacket(ResourceLocation effekName, float progress, Vector3d position, ResourceLocation emmiterName) {
		this.effekName = effekName;
		this.progress = progress;
		this.position = position;
		this.emmiterName = emmiterName;
	}
	
	public static EffekPacket read(PacketBuffer buffer) {
		return new EffekPacket(
				buffer.readResourceLocation(),
				buffer.readFloat(),
				new Vector3d(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()),
				buffer.readResourceLocation()
		);
	}
	
	@Override
	public void readPacketData(PacketBuffer buf) {
		// nothing to do, as this method goes unused
	}
	
	@Override
	public void writePacketData(PacketBuffer buf) {
		buf.writeResourceLocation(effekName);
		buf.writeFloat(progress);
		buf.writeDouble(position.x);
		buf.writeDouble(position.y);
		buf.writeDouble(position.z);
		buf.writeResourceLocation(emmiterName);
	}
	
	@Override
	public void processPacket(INetHandler handler) {
		// nothing to do
	}
}
