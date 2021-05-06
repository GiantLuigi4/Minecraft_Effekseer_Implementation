package com.tfc.minecraft_effekseer_implementation.meifabric;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.impl.networking.ServerSidePacketRegistryImpl;
import net.minecraft.client.util.math.Vector3d;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.nio.ByteBuffer;

public class EffekPacket {
	public final Identifier effekName;
	public final Identifier emmiterName;
	public final float progress;
	public final Vector3d position;
	
	public EffekPacket(Identifier effekName, float progress, Vector3d position, Identifier emmiterName) {
		this.effekName = effekName;
		this.progress = progress;
		this.position = position;
		this.emmiterName = emmiterName;
	}
	
	public void send(ServerPlayerEntity player) {
		PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		buf.writeIdentifier(effekName);
		buf.writeFloat(progress);
		buf.writeDouble(position.x);
		buf.writeDouble(position.y);
		buf.writeDouble(position.z);
		buf.writeIdentifier(emmiterName);
		ServerPlayNetworking.send(player, NetworkingFabric.id1, buf);
	}
	
	public static EffekPacket read(PacketByteBuf buffer) {
		return new EffekPacket(
				buffer.readIdentifier(),
				buffer.readFloat(),
				new Vector3d(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()),
				buffer.readIdentifier()
		);
	}
}
