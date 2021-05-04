package com.tfc.minecraft_effekseer_implementation.common.api;

import com.tfc.effekseer4j.EffekseerParticleEmitter;
import com.tfc.minecraft_effekseer_implementation.FinalizedReference;

import java.util.HashMap;
import java.util.Objects;

/**
 * A more mc implementation friendly wrapper for EffekseerParticleEmitter
 */
public class EffekEmitter {
	// this is intentionally not private/protected, so you can access it if need be
	public final EffekseerParticleEmitter emitter;
	
	public EffekEmitter(EffekseerParticleEmitter emitter) {
		this.emitter = emitter;
	}
	
	public void resetAndHide() {
		emitter.pause();
		emitter.setVisibility(false);
		emitter.setProgress(0);
	}
	
	public void setPaused(boolean paused) {
		if (paused) emitter.pause();
		else emitter.resume();
	}
	
	public void setVisible(boolean visible) {
		emitter.setVisibility(visible);
	}
	
	public void setPlayProgress(float progress) {
		emitter.setProgress(progress);
	}
	
	public void setDynamicInput(int index, float value) {
		emitter.setDynamicInput(index, value);
	}
	
	public float getDynamicInput(int index) {
		return emitter.getDynamicInput(index);
	}
	
	@Override
	public boolean equals(Object object) {
		if (this == object) return true;
		if (object == null || getClass() != object.getClass()) return false;
		EffekEmitter emitter1 = (EffekEmitter) object;
		return Objects.equals(emitter, emitter1.emitter);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(emitter);
	}
	
	public void setPosition(int x, int y, int z) {
		emitter.move(x, y, z);
	}
	
	public void setPosition(double x, double y, double z) {
		emitter.move((float) x - 0.5f, (float) y - 0.5f, (float) z - 0.5f);
	}
	
	public boolean exists() {
		return emitter.exists();
	}
}
