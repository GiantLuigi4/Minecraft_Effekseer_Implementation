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
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

public class EffekseerMCAssetLoader extends SinglePreparationResourceReloadListener<Effek> implements IdentifiableResourceReloadListener {
	private static final Gson gson = new GsonBuilder().setLenient().create();
	public static final EffekseerMCAssetLoader INSTANCE = new EffekseerMCAssetLoader();
	private static final Effeks mapHandler = Effeks.getMapHandler();
	
	private static final Logger LOGGER = LogManager.getLogger();
	
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
				if (!object.has("effect")) {
					LOGGER.log(Level.WARN, "Cannot load effek " + location.toString() + " because the \"effect\" entry is missing from the json.");
					continue;
				}
				// load effect
				LoaderIndependentIdentifier location0 = new LoaderIndependentIdentifier(object.get("effect").getAsJsonPrimitive().getAsString());
				String namespace = location0.namespace();
				String path = location0.path();
				Identifier location1 = new Identifier(namespace + ":effeks/" + path);
				stream = getStream(resourceManagerIn, location1);
				if (stream == null) {
					LOGGER.log(Level.WARN, "Effek \"" + location.toString() + "\" failed to load, \"" + location1.toString() + "\" could not be found.");
					continue;
				}
				byte[] bytes1 = readFully(stream);
				if (!effect.load(bytes1, bytes1.length, 1)) {
					LOGGER.log(Level.WARN, "Effek " + location1.toString() + " failed to load, no further info known.");
					continue;
				}
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
							LOGGER.log(Level.WARN, texturePath);
							stream = getStream(resourceManagerIn, new Identifier(texturePath));
							if (stream == null) {
								LOGGER.log(Level.WARN, "Effek \"" + location.toString() + "\" failed to load, texture \"" + texturePath + "\" is missing.");
								continue;
							}
							bytes1 = readFully(stream);
							if (!effect.loadTexture(bytes1, bytes1.length, index, value)) {
								LOGGER.log(Level.WARN, texturePath + " seems to be an invalid png image.");
							}
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
						stream = getStream(resourceManagerIn, new Identifier(texturePath));
						if (stream == null) {
							LOGGER.log(Level.WARN, "Effek \"" + location.toString() + "\" failed to load, model \"" + texturePath + "\" is missing.");
							continue;
						}
						bytes1 = readFully(stream);
						if (!effect.loadModel(bytes1, bytes1.length, index)) {
							LOGGER.log(Level.WARN, texturePath + " seems to be an invalid .efkmodel");
						}
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
						stream = getStream(resourceManagerIn, new Identifier(texturePath));
						if (stream == null) {
							LOGGER.log(Level.WARN, "Effek \"" + location.toString() + "\" failed to load, curve \"" + texturePath + "\" is missing.");
							continue;
						}
						bytes1 = readFully(stream);
						if (!effect.loadCurve(bytes1, bytes1.length, index)) {
							LOGGER.log(Level.WARN, texturePath + " seems to be an invalid [insert curve file type here].");
						}
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
						stream = getStream(resourceManagerIn, new Identifier(texturePath));
						if (stream == null) {
							LOGGER.log(Level.WARN, "Effek \"" + location.toString() + "\" failed to load, material \"" + texturePath + "\" is missing.");
							continue;
						}
						bytes1 = readFully(stream);
						if (!effect.loadMaterial(bytes1, bytes1.length, index)) {
							LOGGER.log(Level.WARN, texturePath + " seems to be an invalid [insert material file type here].");
						}
					}
				}
				location = new Identifier(location.getNamespace() + ":" + location.getPath().substring("effeks/".length(), location.getPath().length() - ".efk.json".length()));
				LOGGER.log(Level.DEBUG, "Successfully loaded effek " + location.toString());
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
	
	private static InputStream getStream(ResourceManager manager, Identifier location) {
		try {
			Resource resource = manager.getResource(location);
			if (resource == null) return null;
			else return resource.getInputStream();
		} catch (Throwable err) {
			return null;
		}
	}
	
	private static byte[] readFully(InputStream stream) throws IOException {
		ByteArrayOutputStream stream1 = new ByteArrayOutputStream();
		int b;
		while (((b = stream.read()) != -1)) stream1.write(b);
		byte[] bytes = stream1.toByteArray();
		stream1.close();
		stream1.flush();
		return bytes;
	}
}
