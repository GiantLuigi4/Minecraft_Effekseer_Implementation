package com.tfc.minecraft_effekseer_implementation.meifabric.client;

import com.tfc.minecraft_effekseer_implementation.common.Effek;
import com.tfc.minecraft_effekseer_implementation.common.LoaderIndependentIdentifier;
import com.tfc.minecraft_effekseer_implementation.meifabric.loader.EffekseerMCAssetLoader;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class MEIFabricClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// resource locations are very nice to have for registry type stuff, and I want the api to be 100% loader independent
		if (LoaderIndependentIdentifier.rlConstructor1.get() == null) {
			LoaderIndependentIdentifier.rlConstructor1.set(Identifier::new);
			LoaderIndependentIdentifier.rlConstructor2.set(Identifier::new);
		}
		if (Effek.widthGetter.get() == null) {
			Effek.widthGetter.set(()-> MinecraftClient.getInstance().getWindow().getWidth());
			Effek.heightGetter.set(()-> MinecraftClient.getInstance().getWindow().getHeight());
		}
		
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(EffekseerMCAssetLoader.INSTANCE);
	}
}
