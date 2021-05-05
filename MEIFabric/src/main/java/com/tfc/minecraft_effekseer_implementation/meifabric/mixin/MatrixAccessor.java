package com.tfc.minecraft_effekseer_implementation.meifabric.mixin;

import net.minecraft.util.math.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Matrix4f.class)
public interface MatrixAccessor {
	@Accessor("a00") float m00();
	@Accessor("a01") float m01();
	@Accessor("a02") float m02();
	@Accessor("a03") float m03();
	@Accessor("a10") float m10();
	@Accessor("a11") float m11();
	@Accessor("a12") float m12();
	@Accessor("a13") float m13();
	@Accessor("a20") float m20();
	@Accessor("a21") float m21();
	@Accessor("a22") float m22();
	@Accessor("a23") float m23();
	@Accessor("a30") float m30();
	@Accessor("a31") float m31();
	@Accessor("a32") float m32();
	@Accessor("a33") float m33();
}
