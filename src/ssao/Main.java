package ssao;

import rafgl.RGL;

public class Main {

	public static void main(String[] args) {
		RGL.setParami(RGL.IParam.WIDTH, 1280);
		RGL.setParami(RGL.IParam.HEIGHT, 720);
		
		RGL.setParami(RGL.IParam.FULLSCREEN, 0);
		RGL.setParami(RGL.IParam.MSAA, 4);
		RGL.setParami(RGL.IParam.VSYNC, 1);
		
		RGL.setParami(RGL.IParam.AUTO_CONFIGURE_PROFILE, 0);
		RGL.setParami(RGL.IParam.CONTEXT_MAJOR, 3);
		RGL.setParami(RGL.IParam.CONTEXT_MINOR, 2);
		
		RGL.init();
		AudioMaster.init();
		AudioMaster.setListenerData();
		int cloakBuffer = AudioMaster.loadSound("ssao/cloakEngaged.wav");
		int armorBuffer = AudioMaster.loadSound("ssao/maximumArmor.wav");
		Source source = new Source();
		
		RGL.setTitle("SSAO");
		RGL.setRunning(true);
		
		MainFrame mainFrame = new MainFrame(source, cloakBuffer, armorBuffer);
		
		while (RGL.isRunning()) {
			RGL.handleEvents();
			mainFrame.update();
			mainFrame.render();
		}
		
		source.delete();
		AudioMaster.cleanUp();
		RGL.deinit();
	}
}
