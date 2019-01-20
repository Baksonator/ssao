package ssao;

import static org.lwjgl.openal.AL10.*;

public class Source {

	private int sourceId;
	
	public Source() {
		sourceId = alGenSources();
		alSourcef(sourceId, AL_GAIN, 1);
		alSourcef(sourceId, AL_PITCH, 1);
		alSource3f(sourceId, AL_POSITION, 0, 0, 0);
	}
	
	public void play(int buffer) {
		alSourcei(sourceId, AL_BUFFER, buffer);
		alSourcePlay(sourceId);
	}
	
	public void delete() {
		alDeleteSources(sourceId);
	}
}
