package com.tfc.minecraft_effekseer_implementation;

import java.util.function.Function;

public class ModDetectionUtil {
	public static final FinalizedReference<Function<String, Boolean>> detector = new FinalizedReference<>();
	
	public static boolean isLoaded(String name) {
		return detector.get().apply(name);
	}
}
