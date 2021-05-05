package com.tfc.minecraft_effekseer_implementation.meifabric;

import com.tfc.minecraft_effekseer_implementation.common.LoaderIndependentIdentifier;
import com.tfc.minecraft_effekseer_implementation.meifabric.mixin.MatrixAccessor;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;

public class MEIFabric implements ModInitializer {
	@Override
	public void onInitialize() {
		// resource locations are very nice to have for registry type stuff, and I want the api to be 100% loader independent
		if (LoaderIndependentIdentifier.rlConstructor1.get() == null) {
			LoaderIndependentIdentifier.rlConstructor1.set(Identifier::new);
			LoaderIndependentIdentifier.rlConstructor2.set(Identifier::new);
		}
	}
	
	public static float[][] matrixToArray(Matrix4f matrix4) {
		MatrixAccessor matrix = (MatrixAccessor) (Object) matrix4;
		return new float[][]{
				{matrix.m00(), matrix.m01(), matrix.m02(), matrix.m03()},
				{matrix.m10(), matrix.m11(), matrix.m12(), matrix.m13()},
				{matrix.m20(), matrix.m21(), matrix.m22(), matrix.m23()},
				{matrix.m30(), matrix.m31(), matrix.m32(), matrix.m33()}
		};
	}
}
