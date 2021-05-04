package com.tfc.minecraft_effekseer_implementation.loader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.tfc.effekseer4j.Effekseer;
import com.tfc.effekseer4j.EffekseerEffect;
import com.tfc.effekseer4j.enums.DeviceType;
import com.tfc.effekseer4j.enums.TextureType;
import com.tfc.minecraft_effekseer_implementation.MEI;
import com.tfc.minecraft_effekseer_implementation.common.Effek;
import com.tfc.minecraft_effekseer_implementation.common.Effeks;
import com.tfc.minecraft_effekseer_implementation.common.LoaderIndependentIdentifier;
import com.tfc.minecraft_effekseer_implementation.common.api.EffekEmitter;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.client.resources.ReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.loading.FMLEnvironment;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Collection;

public class EffekseerMCAssetLoader extends ReloadListener<Effek> {
	private static final Gson gson = new GsonBuilder().setLenient().create();
	public static final EffekseerMCAssetLoader INSTANCE = new EffekseerMCAssetLoader();
	private static final Effeks mapHandler = Effeks.getMapHandler();
	
	@Override
	protected Effek prepare(IResourceManager resourceManagerIn, IProfiler profilerIn) {
		return null;
	}
	
	@Override
	protected void apply(Effek objectIn, IResourceManager resourceManagerIn, IProfiler profilerIn) {
		if (Effekseer.getDevice() != DeviceType.OPENGL) {
			Effekseer.init();
			Effekseer.setupForOpenGL();
		}
		Effeks.forEach((name, effect)->{
			effect.close();
		});
		mapHandler.clear();
		Collection<ResourceLocation> locations = resourceManagerIn.getAllResourceLocations("effeks", (path) -> path.endsWith(".efk.json"));
		for (ResourceLocation location : locations) {
			try {
				IResource resource = resourceManagerIn.getResource(location);
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
				ResourceLocation location1 = new ResourceLocation(namespace + ":effeks/" + path);
				stream = resourceManagerIn.getResource(location1).getInputStream();
				effect.load(stream, stream.available(), 1);
				{ // textures
					JsonObject texturesObject = object.getAsJsonObject("textures");
					for (TextureType value : TextureType.values()) {
						for (int index = 0; index < effect.textureCount(value); index++) {
							String texturePath = effect.getTexturePath(index, value);
							if (texturesObject.has(texturePath)) texturePath = texturesObject.get(texturePath).getAsJsonPrimitive().getAsString();
							LoaderIndependentIdentifier textureLocation = new LoaderIndependentIdentifier(texturePath);
							path = textureLocation.path();
							texturePath = namespace + ":effeks/" + path;
							stream = resourceManagerIn.getResource(new ResourceLocation(texturePath)).getInputStream();
							effect.loadTexture(stream, stream.available(), index, value);
						}
					}
				}
				{ // models
					JsonObject modelsObj = object.getAsJsonObject("models");
					for (int index = 0; index < effect.modelCount(); index++) {
						String texturePath = effect.getModelPath(index);
						if (modelsObj.has(texturePath)) texturePath = modelsObj.get(texturePath).getAsJsonPrimitive().getAsString();
						LoaderIndependentIdentifier textureLocation = new LoaderIndependentIdentifier(texturePath);
						path = textureLocation.path();
						texturePath = namespace + ":effeks/" + path;
						stream = resourceManagerIn.getResource(new ResourceLocation(texturePath)).getInputStream();
						effect.loadModel(stream, stream.available(), index);
					}
				}
				{ // curves
					JsonObject modelsObj = object.getAsJsonObject("curves");
					for (int index = 0; index < effect.curveCount(); index++) {
						String texturePath = effect.getCurvePath(index);
						if (modelsObj.has(texturePath)) texturePath = modelsObj.get(texturePath).getAsJsonPrimitive().getAsString();
						LoaderIndependentIdentifier textureLocation = new LoaderIndependentIdentifier(texturePath);
						path = textureLocation.path();
						texturePath = namespace + ":effeks/" + path;
						stream = resourceManagerIn.getResource(new ResourceLocation(texturePath)).getInputStream();
						effect.loadCurve(stream, stream.available(), index);
					}
				}
				{ // materials
					JsonObject modelsObj = object.getAsJsonObject("materials");
					for (int index = 0; index < effect.materialCount(); index++) {
						String texturePath = effect.getMaterialPath(index);
						if (modelsObj.has(texturePath)) texturePath = modelsObj.get(texturePath).getAsJsonPrimitive().getAsString();
						LoaderIndependentIdentifier textureLocation = new LoaderIndependentIdentifier(texturePath);
						path = textureLocation.path();
						texturePath = namespace + ":effeks/" + path;
						stream = resourceManagerIn.getResource(new ResourceLocation(texturePath)).getInputStream();
						effect.loadMaterial(stream, stream.available(), index);
					}
				}
				location = new ResourceLocation(location.getNamespace() + ":" + location.getPath().substring("effeks/".length(), location.getPath().length() - ".efk.json".length()));
				if (!FMLEnvironment.production) System.out.println(location);
				// register Effek
				Effek effek = new Effek(
						new LoaderIndependentIdentifier(location.toString()),
						(manager) -> new EffekEmitter(manager.createParticle(effect)),
						object.has("maxSpriteCount") ? object.getAsJsonPrimitive("maxSpriteCount").getAsInt() : 100,
						object.has("srgb") && object.getAsJsonPrimitive("srgb").getAsBoolean()
				);
				mapHandler.put(location.toString(), effek);
			} catch (Throwable err) {
				err.printStackTrace();
			}
		}
	}
}
