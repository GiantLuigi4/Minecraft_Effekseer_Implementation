package com.tfc.minecraft_effekseer_implementation;

import com.google.gson.internal.$Gson$Types;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.tfc.effekseer4j.Effekseer;
import com.tfc.effekseer4j.EffekseerEffect;
import com.tfc.effekseer4j.EffekseerManager;
import com.tfc.effekseer4j.EffekseerParticleEmitter;
import com.tfc.effekseer4j.enums.DeviceType;
import com.tfc.effekseer4j.enums.TextureType;
import com.tfc.minecraft_effekseer_implementation.common.Effek;
import com.tfc.minecraft_effekseer_implementation.common.Effeks;
import com.tfc.minecraft_effekseer_implementation.common.LoaderIndependentIdentifier;
import com.tfc.minecraft_effekseer_implementation.common.api.EffekEmitter;
import com.tfc.minecraft_effekseer_implementation.loader.EffekseerMCAssetLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.settings.GraphicsFanciness;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import java.io.InputStream;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("mc_effekseer_impl")
public class MEI {
	private static final Logger LOGGER = LogManager.getLogger();
	
	// resource locations are very nice to have for registry type stuff, and I want the api to be 100% loader independent
	static {
		LoaderIndependentIdentifier.rlConstructor1.set(ResourceLocation::new);
		LoaderIndependentIdentifier.rlConstructor2.set(ResourceLocation::new);
	}
	
	public MEI() {
		Networking.init();
		MinecraftForge.EVENT_BUS.addListener(this::onServerStartup);
		if (!FMLEnvironment.dist.isClient()) return;
		MinecraftForge.EVENT_BUS.addListener(this::renderWorldLast);
		IReloadableResourceManager manager = (IReloadableResourceManager) Minecraft.getInstance().getResourceManager();
		manager.addReloadListener(EffekseerMCAssetLoader.INSTANCE);
	}
	
	private void onServerStartup(FMLServerAboutToStartEvent event) {
		event.getServer().getCommandManager().getDispatcher().register(Command.construct());
	}
	
	private static long lastFrame = -1;
	
	private void renderWorldLast(RenderWorldLastEvent event) {
		Effek effek = Effeks.get("mc_effekseer_impl:example");
		EffekEmitter emitter = effek.getOrCreate("test:test");
		emitter.setVisible(false);
		for (Entity allEntity : Minecraft.getInstance().world.getAllEntities()) {
			if (allEntity instanceof FishingBobberEntity) {
				emitter.emitter.setVisibility(true);
				emitter.emitter.move(
						(float) MathHelper.lerp(Minecraft.getInstance().getRenderPartialTicks(), (float) allEntity.lastTickPosX, allEntity.getPosX()) - 0.5f,
						(float) MathHelper.lerp(Minecraft.getInstance().getRenderPartialTicks(), (float) allEntity.lastTickPosY, allEntity.getPosY()) - 0.5f,
						(float) MathHelper.lerp(Minecraft.getInstance().getRenderPartialTicks(), (float) allEntity.lastTickPosZ, allEntity.getPosZ()) - 0.5f
				);
			}
		}
		float diff = 1;
		if (lastFrame != -1) {
			long currentTime = System.currentTimeMillis();
			diff = (Math.abs(currentTime - lastFrame) / 1000f) * 60;
		}
		lastFrame = System.currentTimeMillis();
		Matrix4f matrix;
		event.getMatrixStack().push();
		event.getMatrixStack().translate(
				-Minecraft.getInstance().getRenderManager().info.getProjectedView().getX(),
				-Minecraft.getInstance().getRenderManager().info.getProjectedView().getY(),
				-Minecraft.getInstance().getRenderManager().info.getProjectedView().getZ()
		);
		event.getMatrixStack().translate(0.5f, 0.5f, 0.5f);
		matrix = event.getMatrixStack().getLast().getMatrix();
		float[][] cameraMatrix = matrixToArray(matrix);
		event.getMatrixStack().pop();
		matrix = Minecraft.getInstance().gameRenderer.getProjectionMatrix(
				Minecraft.getInstance().getRenderManager().info,
				event.getPartialTicks(), true
		);
		float[][] projectionMatrix = matrixToArray(matrix);
		final float finalDiff = diff;
		if (Minecraft.getInstance().worldRenderer.getParticleFrameBuffer() != null)
			Minecraft.getInstance().worldRenderer.getParticleFrameBuffer().func_237506_a_(Minecraft.getInstance().getFramebuffer());
		RenderState.PARTICLES_TARGET.setupRenderState();
		Effeks.forEach((name, effect) -> effect.draw(cameraMatrix, projectionMatrix, finalDiff));
		RenderState.PARTICLES_TARGET.clearRenderState();
	}
	
	public static void printEffectInfo(EffekseerEffect effect) {
		System.out.println("Effect info:");
		System.out.println(" curveCount: " + effect.curveCount());
		for (int index = 0; index < effect.curveCount(); index++) System.out.println("  curve"+index+": " + effect.getCurvePath(index));
		System.out.println(" materialCount: " + effect.materialCount());
		for (int index = 0; index < effect.materialCount(); index++) System.out.println("  material"+index+": " + effect.getMaterialPath(index));
		System.out.println(" modelCount: " + effect.modelCount());
		for (int index = 0; index < effect.modelCount(); index++) System.out.println("  model"+index+": " + effect.getModelPath(index));
		System.out.println(" textureCount: " + effect.textureCount());
		for (TextureType value : TextureType.values()) {
			System.out.println("  textureCount"+value.toString()+":"+effect.textureCount(value));
			for (int index = 0; index < effect.textureCount(value); index++) System.out.println("   model"+index+": " + effect.getTexturePath(index, value));
		}
		System.out.println(" isLoaded: " + effect.isLoaded());
		System.out.println(" minTerm: " + effect.minTerm());
		System.out.println(" maxTerm: " + effect.maxTerm());
	}
	
	public static float[][] matrixToArray(Matrix4f matrix) {
		return new float[][]{
				{matrix.m00, matrix.m01, matrix.m02, matrix.m03},
				{matrix.m10, matrix.m11, matrix.m12, matrix.m13},
				{matrix.m20, matrix.m21, matrix.m22, matrix.m23},
				{matrix.m30, matrix.m31, matrix.m32, matrix.m33}
		};
	}
}
