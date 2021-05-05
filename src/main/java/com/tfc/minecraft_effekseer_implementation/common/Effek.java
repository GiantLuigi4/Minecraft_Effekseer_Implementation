package com.tfc.minecraft_effekseer_implementation.common;

import com.tfc.effekseer4j.EffekseerManager;
import com.tfc.minecraft_effekseer_implementation.FinalizedReference;
import com.tfc.minecraft_effekseer_implementation.common.api.EffekEmitter;

import java.io.Closeable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

public class Effek implements Closeable {
	private final LoaderIndependentIdentifier id;
	private final Function<EffekseerManager, EffekEmitter> newEffect;
	
	private final EffekseerManager manager = new EffekseerManager();
	
	private final Map<LoaderIndependentIdentifier, EffekEmitter> effectsPresent = new HashMap<>();
	private final Map<LoaderIndependentIdentifier, EffekEmitter> effectsFree = new HashMap<>();
	
	public static final FinalizedReference<Supplier<Integer>> widthGetter = new FinalizedReference<>();
	public static final FinalizedReference<Supplier<Integer>> heightGetter = new FinalizedReference<>();
	
	public Effek(LoaderIndependentIdentifier id, Function<EffekseerManager, EffekEmitter> newEffect, int requestedMaxSprites, boolean srgb) {
		this.id = id;
		this.newEffect = newEffect;
		manager.initialize(requestedMaxSprites, srgb);
		manager.setupWorkerThreads(2);
	}
	
	public void draw(float[][] cameraMatrix, float[][] projectionMatrix, float delta) {
		manager.setViewport(widthGetter.get().get(), heightGetter.get().get());
		manager.setCameraMatrix(cameraMatrix);
		manager.setProjectionMatrix(projectionMatrix);
		manager.update(delta);
		//TODO: canvas shaders
		manager.draw();
	}
	
	public LoaderIndependentIdentifier getId() {
		return id;
	}
	
	@Override
	public void close() {
		for (EffekEmitter value : effectsFree.values()) value.emitter.stop();
		for (EffekEmitter value : effectsPresent.values()) value.emitter.stop();
		manager.delete();
	}
	
	public EffekEmitter getOrCreate(String name) {
		LoaderIndependentIdentifier id = new LoaderIndependentIdentifier(name);
		if (effectsPresent.containsKey(id)) return effectsPresent.get(new LoaderIndependentIdentifier(name));
		else if (effectsFree.containsKey(id)) {
			EffekEmitter emitter = effectsFree.get(id);
			effectsFree.remove(id);
			effectsPresent.put(id, emitter);
			return emitter;
		}
		EffekEmitter emitter = newEffect.apply(manager);
		effectsPresent.put(id, emitter);
		return emitter;
	}
	
	public void markFree(EffekEmitter emitter) {
		AtomicReference<LoaderIndependentIdentifier> idMove = new AtomicReference<>();
		effectsPresent.forEach((id, emitter1)->{
			if (idMove.get() != null) return;
			if (emitter.equals(emitter1)) {
				idMove.set(id);
			}
		});
		if (idMove.get() == null) throw new NullPointerException("Could not find emitter " + emitter.emitter.handle + " for effect type " + id.toString());
		emitter.resetAndHide();
		effectsPresent.remove(idMove.get());
		effectsFree.put(idMove.get(), emitter);
	}
	
	public void delete(EffekEmitter emitter) {
		markFree(emitter);
		emitter.emitter.stop();
		AtomicReference<LoaderIndependentIdentifier> idMove = new AtomicReference<>();
		effectsFree.forEach((id, emitter1)->{
			if (idMove.get() != null) return;
			if (emitter.equals(emitter1)) {
				idMove.set(id);
			}
		});
		emitter.setPaused(true);
		emitter.setVisible(false);
		emitter.setPlayProgress(1f);
		effectsFree.remove(idMove.get());
	}
}
