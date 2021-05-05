package com.tfc.minecraft_effekseer_implementation.meifabric.loader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.tfc.effekseer4j.Effekseer;
import com.tfc.effekseer4j.EffekseerEffect;
import com.tfc.effekseer4j.enums.DeviceType;
import com.tfc.effekseer4j.enums.TextureType;
import com.tfc.minecraft_effekseer_implementation.common.Effek;
import com.tfc.minecraft_effekseer_implementation.common.Effeks;
import com.tfc.minecraft_effekseer_implementation.common.LoaderIndependentIdentifier;
import com.tfc.minecraft_effekseer_implementation.common.api.EffekEmitter;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SinglePreparationResourceReloadListener;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.io.InputStream;
import java.util.Collection;

public class EffekseerMCAssetLoader extends SinglePreparationResourceReloadListener<Effek> implements IdentifiableResourceReloadListener {
	private static final Gson gson = new GsonBuilder().setLenient().create();
	public static final EffekseerMCAssetLoader INSTANCE = new EffekseerMCAssetLoader();
	private static final Effeks mapHandler = Effeks.getMapHandler();
	
	/**
	 * @return The unique identifier of this listener.
	 */
	@Override
	public Identifier getFabricId() {
		return new Identifier("mc_effekseer_impl:loader");
	}
	
	@Override
	protected Effek prepare(ResourceManager resourceManagerIn, Profiler profilerIn) {
		return null;
	}
	
	@Override
	protected void apply(Effek objectIn, ResourceManager resourceManagerIn, Profiler profilerIn) {
		if (Effekseer.getDevice() != DeviceType.OPENGL) {
			Effekseer.init();
			Effekseer.setupForOpenGL();
		}
		Effeks.forEach((name, effect)->{
			effect.close();
		});
		mapHandler.clear();
		Collection<Identifier> locations = resourceManagerIn.findResources("effeks", (path) -> path.endsWith(".efk.json"));
		for (Identifier location : locations) {
			try {
				Resource resource = resourceManagerIn.getResource(location);
				InputStream stream = resource.getInputStream();
				byte[] bytes = new byte[stream.available()];
				stream.read(bytes);
				stream.close();
				// read json
				EffekseerEffect effect = new EffekseerEffect();
				JsonObject object = gson.fromJson(new String(bytes), JsonObject.class);
				// load effect
				LoaderIndependentIdentifier location0 = new LoaderIndependentIdentifier(object.get("effect").getAsJsonPrimitive().getAsString());
				String namespace = location0.namespace();
				String path = location0.path();
				Identifier location1 = new Identifier(namespace + ":effeks/" + path);
				stream = resourceManagerIn.getResource(location1).getInputStream();
				effect.load(stream, stream.available(), 1);
				{ // textures
					JsonObject texturesObject = object.has("textures") ? object.getAsJsonObject("textures") : new JsonObject();
					for (TextureType value : TextureType.values()) {
						for (int index = 0; index < effect.textureCount(value); index++) {
							String texturePath = effect.getTexturePath(index, value);
							if (texturesObject.has(texturePath))
								texturePath = texturesObject.get(texturePath).getAsJsonPrimitive().getAsString();
							LoaderIndependentIdentifier textureLocation = new LoaderIndependentIdentifier(texturePath);
							path = textureLocation.path();
							texturePath = namespace + ":effeks/" + path;
							System.out.println(texturePath);
							stream = resourceManagerIn.getResource(new Identifier(texturePath)).getInputStream();
							effect.loadTexture(stream, stream.available(), index, value);
						}
					}
				}
				{ // models
					JsonObject modelsObj = object.has("models") ? object.getAsJsonObject("models") : new JsonObject();
					for (int index = 0; index < effect.modelCount(); index++) {
						String texturePath = effect.getModelPath(index);
						if (modelsObj.has(texturePath)) texturePath = modelsObj.get(texturePath).getAsJsonPrimitive().getAsString();
						LoaderIndependentIdentifier textureLocation = new LoaderIndependentIdentifier(texturePath);
						path = textureLocation.path();
						texturePath = namespace + ":effeks/" + path;
						stream = resourceManagerIn.getResource(new Identifier(texturePath)).getInputStream();
						effect.loadModel(stream, stream.available(), index);
					}
				}
				{ // curves
					JsonObject modelsObj = object.has("curves") ? object.getAsJsonObject("curves") : new JsonObject();
					for (int index = 0; index < effect.curveCount(); index++) {
						String texturePath = effect.getCurvePath(index);
						if (modelsObj.has(texturePath)) texturePath = modelsObj.get(texturePath).getAsJsonPrimitive().getAsString();
						LoaderIndependentIdentifier textureLocation = new LoaderIndependentIdentifier(texturePath);
						path = textureLocation.path();
						texturePath = namespace + ":effeks/" + path;
						stream = resourceManagerIn.getResource(new Identifier(texturePath)).getInputStream();
						effect.loadCurve(stream, stream.available(), index);
					}
				}
				{ // materials
					JsonObject modelsObj = object.has("materials") ? object.getAsJsonObject("materials") : new JsonObject();
					for (int index = 0; index < effect.materialCount(); index++) {
						String texturePath = effect.getMaterialPath(index);
						if (modelsObj.has(texturePath)) texturePath = modelsObj.get(texturePath).getAsJsonPrimitive().getAsString();
						LoaderIndependentIdentifier textureLocation = new LoaderIndependentIdentifier(texturePath);
						path = textureLocation.path();
						texturePath = namespace + ":effeks/" + path;
						stream = resourceManagerIn.getResource(new Identifier(texturePath)).getInputStream();
						effect.loadMaterial(stream, stream.available(), index);
					}
				}
				location = new Identifier(location.getNamespace() + ":" + location.getPath().substring("effeks/".length(), location.getPath().length() - ".efk.json".length()));
//				if (!FMLEnvironment.production) System.out.println(location); //TODO: make it so this happens in fabric only in dev envro
				// register Effek
				Effek effek = new Effek(
						new LoaderIndependentIdentifier(location.toString()),
						(manager) -> new EffekEmitter(manager.createParticle(effect)),
						object.has("maxSpriteCount") ? object.getAsJsonPrimitive("maxSpriteCount").getAsInt() : 100,
						object.has("srgb") && object.getAsJsonPrimitive("srgb").getAsBoolean()
				);
				mapHandler.put(location.toString(), effek);
			} catch (Throwable err) {
				StringBuilder ex = new StringBuilder(err.getLocalizedMessage()).append("\n");
				for (StackTraceElement element : err.getStackTrace()) {
					ex.append(element.toString()).append("\n");
				}
				System.out.print(ex);
			}
		}
	}
}
