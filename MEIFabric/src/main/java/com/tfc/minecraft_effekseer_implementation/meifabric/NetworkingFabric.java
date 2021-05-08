package com.tfc.minecraft_effekseer_implementation.meifabric;

import com.tfc.minecraft_effekseer_implementation.common.Effek;
import com.tfc.minecraft_effekseer_implementation.common.Effeks;
import com.tfc.minecraft_effekseer_implementation.common.api.EffekEmitter;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.Vector3d;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.function.Predicate;

public class NetworkingFabric {
	public static final Identifier id1 = new Identifier("mc_effekseer_impl:effek_spawn");
	public static final Identifier id2 = new Identifier("mc_effekseer_impl:effek_kill");
	private static boolean hasInit = false;
	public static void init() {
		if (hasInit) return;
		hasInit = true;
		ClientPlayNetworking.registerGlobalReceiver(
			id1, (client, handler, buffer, responder)->{
					EffekPacket packet = EffekPacket.read(buffer);
					Effek effek = Effeks.get(packet.effekName.toString());
					if (effek != null) {
						EffekEmitter emitter = effek.getOrCreate(packet.emmiterName.toString());
						emitter.setVisible(true);
						emitter.setPaused(false);
						emitter.setPlayProgress(packet.progress);
						emitter.setPosition(packet.position.x, packet.position.y, packet.position.z);
					}
				}
		);
		ClientPlayNetworking.registerGlobalReceiver(
				id2, (client, handler, buffer, responder)->{
					EndEmitterPacket packet = new EndEmitterPacket(buffer);
					Effek effek = Effeks.get(packet.effekName.toString());
					if (effek != null) effek.delete(effek.getOrCreate(packet.emitterName.toString()));
				}
		);
	}
	
	public static Vector3d blockPosToVector(BlockPos pos) {
		return new Vector3d(pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f);
	}
	
	public static void sendStartEffekPacket(
			Predicate<PlayerEntity> selector, World world,
			Identifier effekName, Identifier emitterName, float progress, Vector3d position
	) {
		EffekPacket packet = new EffekPacket(effekName, progress, position, emitterName);
		for (PlayerEntity player : world.getPlayers()) {
			if (selector.test(player) && player instanceof ServerPlayerEntity) {
				packet.send((ServerPlayerEntity) player);
			}
		}
	}
	
	public static void sendStartEffekPacket(
			World world,
			Identifier effekName, Identifier emitterName, float progress, Vector3d position
	) {
		EffekPacket packet = new EffekPacket(effekName, progress, position, emitterName);
		for (PlayerEntity player : world.getPlayers()) {
			packet.send((ServerPlayerEntity) player);
		}
	}
	
	public static void sendEndEffekPacket(
			Predicate<PlayerEntity> selector, World world,
			Identifier effekName, Identifier emitterName, boolean deleteEmitter
	) {
		EndEmitterPacket packet = new EndEmitterPacket(effekName, emitterName, deleteEmitter);
		for (PlayerEntity player : world.getPlayers()) {
			if (selector.test(player) && player instanceof ServerPlayerEntity) {
				packet.send((ServerPlayerEntity) player);
			}
		}
	}
	
	public static void sendEndEffekPacket(
			World world,
			Identifier effekName, Identifier emitterName, boolean deleteEmitter
	) {
		EndEmitterPacket packet = new EndEmitterPacket(effekName, emitterName, deleteEmitter);
		for (PlayerEntity player : world.getPlayers()) {
			packet.send((ServerPlayerEntity) player);
		}
	}
}
