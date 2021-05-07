package com.tfc.minecraft_effekseer_implementation;

import com.tfc.effekseer4j.EffekseerEffect;
import com.tfc.effekseer4j.enums.TextureType;
import com.tfc.minecraft_effekseer_implementation.common.Effek;
import com.tfc.minecraft_effekseer_implementation.common.Effeks;
import com.tfc.minecraft_effekseer_implementation.common.LoaderIndependentIdentifier;
import com.tfc.minecraft_effekseer_implementation.loader.EffekseerMCAssetLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("mc_effekseer_impl")
public class MEI {
	private static final Logger LOGGER = LogManager.getLogger();
	
	private static final Effeks mapHandler = Effeks.getMapHandler();
	
	public MEI() {
		// resource locations are very nice to have for registry type stuff, and I want the api to be 100% loader independent
		if (LoaderIndependentIdentifier.rlConstructor1.get() == null) {
			LoaderIndependentIdentifier.rlConstructor1.set(ResourceLocation::new);
			LoaderIndependentIdentifier.rlConstructor2.set(ResourceLocation::new);
		}
		if (Effek.widthGetter.get() == null) {
			Effek.widthGetter.set(() -> Minecraft.getInstance().getMainWindow().getWidth());
			Effek.heightGetter.set(() -> Minecraft.getInstance().getMainWindow().getHeight());
		}
		
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
		mapHandler.setTimeSinceReload(Effeks.getTimeSinceReload() + 1);
//		Effek effek = Effeks.get("mc_effekseer_impl:example");
//		if (effek != null) {
//			EffekEmitter emitter = effek.getOrCreate("test:test");
//			emitter.setVisible(false);
//			for (Entity allEntity : Minecraft.getInstance().world.getAllEntities()) {
//				if (allEntity instanceof FishingBobberEntity) {
//					emitter.emitter.setVisibility(true);
//					emitter.emitter.move(
//							(float) MathHelper.lerp(Minecraft.getInstance().getRenderPartialTicks(), (float) allEntity.lastTickPosX, allEntity.getPosX()) - 0.5f,
//							(float) MathHelper.lerp(Minecraft.getInstance().getRenderPartialTicks(), (float) allEntity.lastTickPosY, allEntity.getPosY()) - 0.5f,
//							(float) MathHelper.lerp(Minecraft.getInstance().getRenderPartialTicks(), (float) allEntity.lastTickPosZ, allEntity.getPosZ()) - 0.5f
//					);
//				}
//				if (allEntity instanceof ArmorStandEntity) {
//					ResourceLocation location = new ResourceLocation("modid:"+allEntity.getUniqueID().toString());
//					EffekEmitter emitter1 = effek.getOrCreate(location.toString());
//					emitter1.setPosition(allEntity.getPosX(), allEntity.getPosY() + allEntity.getEyeHeight(), allEntity.getPosZ());
//					if (!allEntity.isAlive()) effek.delete(emitter1);
//				}
//			}
//		}
//		effek = Effeks.get("example:aura");
//		if (effek != null)
//			for (int x = 0; x < 16; x++) {
//				for (int y = 0; y < 16; y++) {
//					EffekEmitter emitter = effek.getOrCreate("test:x" + x + "y" + y + "z0");
//					if (emitter != null) emitter.setPosition(x, y + 16, 0);
//					effek.delete(emitter);
//				}
//			}
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
