package ssao;

import static org.lwjgl.openal.AL10.*;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.LWJGLException;
import org.lwjgl.openal.AL;
import org.lwjgl.util.WaveData;

/**
 * Class for handling the audio effects
 * @author Bogdan
 *
 */
public class AudioMaster {

	private static List<Integer> buffers = new ArrayList<>();
	
	public static void init() {
		try {
			AL.create();
		} catch (LWJGLException e) {
			e.printStackTrace();
		}
	}
	
	public static void setListenerData() {
		alListener3f(AL_POSITION, 0, 0, 0);
		alListener3f(AL_VELOCITY, 0, 0, 0);
	}
	
	public static int loadSound(String file) {
		int buffer = alGenBuffers();
		buffers.add(buffer);
		WaveData waveFile = WaveData.create(file);
		alBufferData(buffer, waveFile.format, waveFile.data, waveFile.samplerate);
		waveFile.dispose();
		return buffer;
	}
	
	public static void cleanUp() {
		for (int buffer : buffers) {
			alDeleteBuffers(buffer);
		}
		AL.destroy();
	}
}
