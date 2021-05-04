package com.tfc.minecraft_effekseer_implementation;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.PacketDistributor;

public class Command {
	public static LiteralArgumentBuilder<CommandSource> construct() {
		return Commands.literal("effek").requires(commandSource ->
				{
					System.out.println(commandSource.hasPermissionLevel(2));
					return commandSource.hasPermissionLevel(2);
				}
		).then(Commands.argument("effek", StringArgumentType.string())
				.then(Commands.argument("emitter", StringArgumentType.string())
						.then(Commands.literal("true").executes((source) -> handle(source, source.getSource(), true)))
								.then(Commands.literal("false").executes((source) -> handle(source, source.getSource(), false)))));
	}
	
	private static int handle(CommandContext<?> context, CommandSource source, boolean delete) {
		if (!delete) {
			Networking.sendEndEffekPacket(
					PacketDistributor.DIMENSION.with(() -> source.getWorld().getDimensionKey()),
					new ResourceLocation(StringArgumentType.getString(context, "effek")),
					new ResourceLocation(StringArgumentType.getString(context, "emitter")),
					true
			);
		} else {
			Networking.sendStartEffekPacket(
					PacketDistributor.DIMENSION.with(() -> source.getWorld().getDimensionKey()),
					new ResourceLocation(StringArgumentType.getString(context, "effek")),
					new ResourceLocation(StringArgumentType.getString(context, "emitter")),
					0, source.getPos()
			);
		}
		return 0;
	}
}
