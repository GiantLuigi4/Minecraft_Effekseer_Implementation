package com.tfc.minecraft_effekseer_implementation.meifabric.client;

import com.tfc.minecraft_effekseer_implementation.common.Effeks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.tfc.minecraft_effekseer_implementation.meifabric.MEIFabric.matrixToArray;

public class WorldRendererMixinHandler {
	private static long lastFrame = -1;
	
	private static final Effeks mapHandler = Effeks.getMapHandler();
	
	public static void onRenderParticles(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo ci) {
		mapHandler.setTimeSinceReload(Effeks.getTimeSinceReload() + 1);
//		Effek efk = Effeks.get("example:aura");
//		EffekEmitter emitter = efk.getOrCreate("test:test");
//		emitter.setPosition(0, 10, 0);
		float diff = 1;
		if (lastFrame != -1) {
			long currentTime = System.currentTimeMillis();
			diff = (Math.abs(currentTime - lastFrame) / 1000f) * 60;
		}
		lastFrame = System.currentTimeMillis();
		Matrix4f matrix;
		matrices.push();
		matrices.translate(
				-MinecraftClient.getInstance().getEntityRenderDispatcher().camera.getPos().getX(),
				-MinecraftClient.getInstance().getEntityRenderDispatcher().camera.getPos().getY(),
				-MinecraftClient.getInstance().getEntityRenderDispatcher().camera.getPos().getZ()
		);
		matrices.translate(0.5f, 0.5f, 0.5f);
		matrix = matrices.peek().getModel();
		float[][] cameraMatrix = matrixToArray(matrix);
		matrices.pop();
		matrix = (MinecraftClient.getInstance().gameRenderer).getBasicProjectionMatrix(
				MinecraftClient.getInstance().getEntityRenderDispatcher().camera,
				tickDelta, true
		);
		float[][] projectionMatrix = matrixToArray(matrix);
		final float finalDiff = diff;
		Effeks.forEach((name,effek)->{
			effek.draw(cameraMatrix, projectionMatrix, finalDiff);
		});
	}
}
