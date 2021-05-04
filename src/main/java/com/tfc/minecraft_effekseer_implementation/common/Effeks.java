package com.tfc.minecraft_effekseer_implementation.common;

import java.util.HashMap;
import java.util.function.BiConsumer;

/**
 * Fully loader independent registry style class
 * everything is registered using asset loaders, you have no reason to be registering stuff through code
 * instead, just use the "get" method with a string which represents the identifier/resource location of your effect's name
 * for example: Effeks.get("mc_effekseer_impl:example")
 */
public class Effeks {
	protected static Effeks MapHandler = new Effeks();
	
	private static boolean hasConstructedMapHandler = false;
	
	protected Effeks() {
		if (!hasConstructedMapHandler) {
			hasConstructedMapHandler = true;
		} else {
			throw new RuntimeException(new IllegalAccessException("what the heck are you doing?"));
		}
	}
	
	private static final HashMap<LoaderIndependentIdentifier, Effek> effeks = new HashMap<>();
	
	public static void forEach(BiConsumer<String, Effek> effekConsumer) {
		effeks.forEach((name, effect)->{
			effekConsumer.accept(name.toString(), effect);
		});
	}
	
	public void put(String name, Effek effect) {
		effeks.put(new LoaderIndependentIdentifier(name), effect);
	}
	
	public static int count() {
		return effeks.size();
	}
	
	public Effek get(int index) {
		return effeks.values().toArray(new Effek[0])[index];
	}
	
	public static Effek get(String s) {
		return effeks.get(new LoaderIndependentIdentifier(s));
	}
	
	public void clear() {
		effeks.clear();
	}
	
	public static Effeks getMapHandler() {
		Effeks handler = MapHandler;
		MapHandler = null;
		return handler;
	}
}
