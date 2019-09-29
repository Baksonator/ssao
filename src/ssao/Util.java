package ssao;

import java.nio.FloatBuffer;
import java.util.Random;

import org.lwjgl.BufferUtils;

import rafgl.jglm.Vec3;

public class Util {

	/**
	 * Kernel sampling
	 * @return Kernel buffer
	 */
	public static FloatBuffer kernel() {
		FloatBuffer kernelBuffer = BufferUtils.createFloatBuffer(8 * 8 * 3);
		Random r = new Random(Double.doubleToLongBits(Math.random()));
		for (int i = 0; i < 64; i++) {
			Vec3 sample = new Vec3((float)(r.nextDouble() * 2.0 - 1.0), (float)(r.nextDouble() * 2.0 - 1.0),
					(float)(r.nextDouble()));
			sample = sample.getUnitVector();
			sample = sample.multiply((float)r.nextDouble());
			float scale = (float)i / 64.0f;
			scale = lerp(0.1f, 1.0f, scale * scale);
			sample = sample.multiply(scale);
			kernelBuffer.put(sample.x);
			kernelBuffer.put(sample.y);
			kernelBuffer.put(sample.z);
		}
		kernelBuffer.flip();
		return kernelBuffer;
	}
	
	/**
	 * Linear interpolation
	 * @param a
	 * @param b
	 * @param f
	 * @return
	 */
	public static float lerp(float a, float b, float f) {
		return a + f * (b - a);
	}
	
	/**
	 * Noise for kernel rotation
	 * @return Noise buffer
	 */
	public static FloatBuffer noise() {
		FloatBuffer noiseBuffer = BufferUtils.createFloatBuffer(4 * 4 * 3);
		Random r = new Random(Double.doubleToLongBits(Math.random()));
		for (int i = 0; i < 16; i++) {
			Vec3 sample = new Vec3((float)(r.nextDouble() * 2.0 - 1.0), (float)(r.nextDouble() * 2.0 - 1.0), 0);
			noiseBuffer.put(sample.x);
			noiseBuffer.put(sample.y);
			noiseBuffer.put(sample.z);
		}
		noiseBuffer.flip();
		return noiseBuffer;
	}
	
}
