package com.tfc.minecraft_effekseer_implementation;

import com.tfc.minecraft_effekseer_implementation.common.Effek;
import com.tfc.minecraft_effekseer_implementation.common.Effeks;
import com.tfc.minecraft_effekseer_implementation.common.api.EffekEmitter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.function.Predicate;

public class Networking {
	private static final String version = "1";
	private static final SimpleChannel channel = NetworkRegistry.ChannelBuilder
			.named(new ResourceLocation("mc_effekseer_impl:main"))
			.serverAcceptedVersions((v) -> version.equals(v) || NetworkRegistry.ABSENT.equals(v) || NetworkRegistry.ACCEPTVANILLA.equals(v))
			.clientAcceptedVersions((v) -> version.equals(v) || NetworkRegistry.ABSENT.equals(v) || NetworkRegistry.ACCEPTVANILLA.equals(v))
			.networkProtocolVersion(() -> "1")
			.simpleChannel();
	
	protected static void init() {
	}
	
	static {
		channel.registerMessage(
				0, EffekPacket.class,
				EffekPacket::writePacketData, EffekPacket::read,
				(packet, context) -> {
					Effek effek = Effeks.get(packet.effekName.toString());
					if (effek != null) {
						EffekEmitter emitter = effek.getOrCreate(packet.emmiterName.toString());
						emitter.setVisible(true);
						emitter.setPaused(false);
						emitter.setPlayProgress(packet.progress);
						emitter.setPosition(packet.position.getX(), packet.position.getY(), packet.position.getZ());
					}
					context.get().setPacketHandled(true);
				}
		);
		channel.registerMessage(
				1, EndEmitterPacket.class,
				EndEmitterPacket::writePacketData, EndEmitterPacket::new,
				(packet, context) -> {
					Effek effek = Effeks.get(packet.effekName.toString());
					if (effek != null) effek.delete(effek.getOrCreate(packet.emitterName.toString()));
					context.get().setPacketHandled(true);
				}
		);
	}
	
	public static Vector3d blockPosToVector(BlockPos pos) {
		return new Vector3d(pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f);
	}
	
	public static void sendStartEffekPacket(
			Predicate<PlayerEntity> selector, World world,
			ResourceLocation effekName, ResourceLocation emitterName, float progress, Vector3d position
	) {
		EffekPacket packet = new EffekPacket(effekName, progress, position, emitterName);
		for (PlayerEntity player : world.getPlayers()) {
			if (selector.test(player) && player instanceof ServerPlayerEntity) {
				channel.send(
						PacketDistributor.PLAYER.with(() -> ((ServerPlayerEntity) player)),
						packet
				);
			}
		}
	}
	
	public static void sendStartEffekPacket(
			PacketDistributor.PacketTarget target,
			ResourceLocation effekName, ResourceLocation emitterName, float progress, Vector3d position
	) {
		EffekPacket packet = new EffekPacket(effekName, progress, position, emitterName);
		channel.send(target, packet);
	}
	
	public static void sendEndEffekPacket(
			PacketDistributor.PacketTarget target,
			ResourceLocation effekName, ResourceLocation emitterName, boolean deleteEmitter
	) {
		EndEmitterPacket packet = new EndEmitterPacket(effekName, emitterName, deleteEmitter);
		channel.send(target, packet);
	}
}
