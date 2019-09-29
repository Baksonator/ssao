package ssao;

import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL12.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;

import rafgl.RGL;
import rafgl.jglm.Vec3;

public class MainFrame {

	private GameShader[] shaders;
	private GameCamera camera;
	private GameObject nanosuit;
	private GameObject wall;
	private int[] skybox;
	
	private int geometryFramebuffer;
	private int ssaoFramebuffer;
	private int blurFramebuffer;
	private int lightingFramebuffer;
	
	private int geometryNormalTexture;
	private int geometryPositionTexture;
	private int ssaoNoiseTexture;
	private int ssaoColorTexture;
	private int blurColorTexture;
	private int lightingColorTexture;
	
	private Vec3 objectColor = RGL.GRAY75;
	private Vec3 lightColor = RGL.WHITE.multiply(0.2f);
    private Vec3 lightVector = new Vec3(1.0f, -1.0f, 1.0f).getUnitVector();
    private Vec3 ambient = RGL.GRAY75;
    private int kernelSize = 64;
    private float radius = 1.0f;
	private float bias = 0.002f;
	private int flag = 0;
	
	private FloatBuffer kernelSamples;
	
	private Source source;
	
	private int cloakBuffer;
	private int armorBuffer;
	
	public MainFrame(Source source, int cloakBuffer, int armorBuffer) {
	    init();
	    glGenerates();
	    this.cloakBuffer = cloakBuffer;
	    this.armorBuffer = armorBuffer;
	    this.source = source;
	}
	
	/**
	 * Initialize all shaders, set environment, models and camera
	 */
	private void init() {
		shaders = new GameShader[5];
		
		shaders[0] = new GameShader("geometry");
		
		shaders[1] = new GameShader("ssao");
		shaders[1].findSpecialUniform(0, "samples");
		shaders[1].findSpecialUniform(1, "kernelSize");
		shaders[1].findSpecialUniform(2, "radius");
		shaders[1].findSpecialUniform(3, "bias");
		
		shaders[2] = new GameShader("blur");
		
		shaders[3] =  new GameShader("lighting");
		
		shaders[4] = new GameShader("postprocess");
		shaders[4].findSpecialUniform(0, "flag");
		
		camera = new GameCamera(GameCamera.GameCameraMode.GAMECAM_FIRST_PERSON);
		camera.yaw = (RGL.PIf + RGL.HALF_PIf);
		camera.update();
		
		skybox = new int[6];
		RGL.loadSkybox("textures/SnowForest", "jpg", skybox);

		nanosuit = new GameObject();
		nanosuit.model = RGL.loadModelOBJ("models/nanosuit3.obj");
		nanosuit.position = new Vec3(0.0f, -10.0f, -25.0f);
		
		wall = new GameObject();
		wall.model = RGL.loadModelOBJ("models/wall.obj");
		wall.position = new Vec3(0.0f, 0.0f, -27.0f);
		wall.yaw = RGL.HALF_PIf;
	}
	
	/**
	 * Generate all needed textures and framebuffers
	 */
	private void glGenerates() {
		geometryFramebuffer = glGenFramebuffers();
		glBindFramebuffer(GL_FRAMEBUFFER, geometryFramebuffer);
		
		geometryPositionTexture = glGenFramebuffers();
		glBindTexture(GL_TEXTURE_2D, geometryPositionTexture);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB16F, RGL.getWidth(), RGL.getHeight(), 0, GL_RGB, GL_FLOAT,
				(ByteBuffer)null);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, geometryPositionTexture, 0);
		
		geometryNormalTexture = glGenFramebuffers();
		glBindTexture(GL_TEXTURE_2D, geometryNormalTexture);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB16F, RGL.getWidth(), RGL.getHeight(), 0, GL_RGB, GL_FLOAT,
				(ByteBuffer)null);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT1, GL_TEXTURE_2D, geometryNormalTexture, 0);
		
		int[] attachments = {GL_COLOR_ATTACHMENT0, GL_COLOR_ATTACHMENT1};
		IntBuffer buffer = BufferUtils.createIntBuffer(attachments.length);
		buffer.put(attachments);
		buffer.flip();
		glDrawBuffers(buffer);
		
		int depthBuffer = glGenRenderbuffers();
	    glBindRenderbuffer(GL_RENDERBUFFER, depthBuffer);
	    glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT24, RGL.getWidth(), RGL.getHeight());
	    
	    glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, depthBuffer);
	    
	    int status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
	    
	    switch(status)
	    {
	       case GL_FRAMEBUFFER_COMPLETE:
	    	   RGL.log("Framebuffer is OK");
	    	   break;
	       default:
	    	   RGL.log("Framebuffer has not been successfully created");
	    	   return;
	    }

		FloatBuffer noiseBuffer = Util.noise();
		ssaoNoiseTexture = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, ssaoNoiseTexture);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB16F, 4, 4, 0, GL_RGB, GL_FLOAT, noiseBuffer);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
		
		kernelSamples = Util.kernel();
		
		ssaoFramebuffer = glGenFramebuffers();
		glBindFramebuffer(GL_FRAMEBUFFER, ssaoFramebuffer);
		
		ssaoColorTexture = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, ssaoColorTexture);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RED, RGL.getWidth(), RGL.getHeight(), 0, GL_RGB, GL_FLOAT,
				(ByteBuffer)null);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, ssaoColorTexture, 0);

		blurFramebuffer = glGenFramebuffers();
		glBindFramebuffer(GL_FRAMEBUFFER, blurFramebuffer);
		
		blurColorTexture = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, blurColorTexture);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RED, RGL.getWidth(), RGL.getHeight(), 0, GL_RGB, GL_FLOAT,
				(ByteBuffer)null);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, blurColorTexture, 0);
		
		lightingFramebuffer = glGenFramebuffers();
		glBindFramebuffer(GL_FRAMEBUFFER, lightingFramebuffer);
		
		lightingColorTexture = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, lightingColorTexture);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB16F, RGL.getWidth(), RGL.getHeight(), 0, GL_RGB, GL_FLOAT,
				(ByteBuffer)null);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_LINEAR);
		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, lightingColorTexture, 0);
		
		int depthBuffer1 = glGenRenderbuffers();
	    glBindRenderbuffer(GL_RENDERBUFFER, depthBuffer1);
	    glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT24, RGL.getWidth(), RGL.getHeight());
	    
	    glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, depthBuffer1);
	    
	    int status1 = glCheckFramebufferStatus(GL_FRAMEBUFFER);
	    
	    switch(status1)
	    {
	       case GL_FRAMEBUFFER_COMPLETE:
	    	   RGL.log("Framebuffer is OK");
	    	   break;
	       default:
	    	   RGL.log("Framebuffer has not been successfully created");
	    	   return;
	    }
	}
	
	public void update() {
		if (RGL.isKeyDown(Keyboard.KEY_W)) {
			camera.position = camera.position.add(camera.forward.multiply(0.5f));
		}
		if (RGL.isKeyDown(Keyboard.KEY_S)) {
			camera.position = camera.position.subtract(camera.forward.multiply(0.5f));
		}
		if (RGL.isKeyDown(Keyboard.KEY_D)) {
			camera.position = (camera.position.add(camera.right.multiply(0.5f)));
		}
		if (RGL.isKeyDown(Keyboard.KEY_A)) {
			camera.position = (camera.position.subtract(camera.right.multiply(0.5f)));
		}
		if (RGL.isKeyDown(Keyboard.KEY_UP)) {
			camera.position = camera.position.add(camera.up.multiply(0.5f));
		}
		if (RGL.isKeyDown(Keyboard.KEY_DOWN)) {
			camera.position = camera.position.subtract(camera.up.multiply(0.5f));
		}
		if (RGL.isKeyDown(Keyboard.KEY_RIGHT)) {
			camera.yaw += 0.05f;
		}
		if (RGL.isKeyDown(Keyboard.KEY_LEFT)) {
			camera.yaw -= 0.05f;
		}
		if (RGL.wasKeyJustPressed(Keyboard.KEY_SPACE)) {
			flag++;
			flag %= 2;
			if (flag == 1) {
				source.play(cloakBuffer);
			} else {
				source.play(armorBuffer);
			}
		}
		if (RGL.isKeyDown(Keyboard.KEY_Q)) {
			radius += 0.1f;
		}
		if (RGL.isKeyDown(Keyboard.KEY_E)) {
			radius -= 0.1f;
		}
		if (RGL.isKeyDown(Keyboard.KEY_R)) {
			bias += 0.01f;
		}
		if (RGL.isKeyDown(Keyboard.KEY_T)) {
			bias -= 0.01f;
		}
		if (RGL.isKeyDown(Keyboard.KEY_Z)) {
			wall.position = wall.position.add(wall.right.multiply(0.5f));
		}
		if (RGL.isKeyDown(Keyboard.KEY_X)) {
			wall.position = wall.position.subtract(wall.right.multiply(0.5f));
		}
		
		camera.update();
		
		nanosuit.updateMatrix();
		
		wall.updateMatrix();
	}
	
	public void render() {
		RGL.beginFrame();
		
		geometryPass();
		ssaoPass();
		blurPass();
		lightingPass();
		postprocessPass();
		
		RGL.endFrame();
	}
	
	private void geometryPass() {
		glBindFramebuffer(GL_FRAMEBUFFER, geometryFramebuffer);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		glEnable(GL_DEPTH_TEST);

		nanosuit.shader = shaders[0];
		glUseProgram(nanosuit.shader.shaderID);
		camera.uploadUniforms(nanosuit.shader);
		
		wall.shader = shaders[0];
		
		nanosuit.draw(nanosuit.shader, camera);
		wall.draw(wall.shader, camera);
	}

	private void ssaoPass() {
		glBindFramebuffer(GL_FRAMEBUFFER, ssaoFramebuffer);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		glUseProgram(shaders[1].shaderID);
		camera.uploadUniforms(shaders[1]);

		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, geometryPositionTexture);
		glUniform1i(shaders[1].uni_texture[0], 0);
		
		glActiveTexture(GL_TEXTURE1);
		glBindTexture(GL_TEXTURE_2D, geometryNormalTexture);
		glUniform1i(shaders[1].uni_texture[1], 1);

		glActiveTexture(GL_TEXTURE2);
		glBindTexture(GL_TEXTURE_2D, ssaoNoiseTexture);
		glUniform1i(shaders[1].uni_texture[2], 2);

		glUniform1(shaders[1].uni_special[0], kernelSamples);
		glUniform1i(shaders[1].uni_special[1], kernelSize);
		glUniform1f(shaders[1].uni_special[2], radius);
		glUniform1f(shaders[1].uni_special[3], bias);
		
		RGL.drawFullscreenQuad(shaders[1].attr_position, shaders[1].attr_uv);
	}

	private void blurPass() {
		glBindFramebuffer(GL_FRAMEBUFFER, blurFramebuffer);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		glUseProgram(shaders[2].shaderID);
		camera.uploadUniforms(shaders[2]);

		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, ssaoColorTexture);
		glUniform1i(shaders[2].uni_texture[0], 0);
		
		RGL.drawFullscreenQuad(shaders[2].attr_position, shaders[2].attr_uv);
	}

	private void lightingPass() {
		glBindFramebuffer(GL_FRAMEBUFFER, lightingFramebuffer);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		glEnable(GL_DEPTH_TEST);
		
		RGL.renderSkybox(skybox, camera.position, camera.matBuffVP);
		nanosuit.shader = shaders[3];
		glUseProgram(shaders[3].shaderID);
		camera.uploadUniforms(shaders[3]);
		
		wall.shader = shaders[3];
		
		glUniform3f(nanosuit.shader.uni_ambient, ambient.x, ambient.y, ambient.z);
		glUniform3f(nanosuit.shader.uni_lightVector1, lightVector.x, lightVector.y, lightVector.z);
		glUniform3f(nanosuit.shader.uni_lightColor1, lightColor.x, lightColor.y, lightColor.z);
		glUniform3f(nanosuit.shader.uni_objectColor, objectColor.x, objectColor.y, objectColor.z);
		
		nanosuit.draw(nanosuit.shader, camera);
		wall.draw(wall.shader, camera);
	}

	private void postprocessPass() {
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		glUseProgram(shaders[4].shaderID);
		camera.uploadUniforms(shaders[4]);

		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, blurColorTexture);
		glUniform1i(shaders[4].uni_texture[0], 0);

		glActiveTexture(GL_TEXTURE1);
		glBindTexture(GL_TEXTURE_2D, lightingColorTexture);
		glUniform1i(shaders[4].uni_texture[1], 1);

		glUniform1i(shaders[4].uni_special[0], flag);
		
		RGL.drawFullscreenQuad(shaders[4].attr_position, shaders[4].attr_uv);
	}
	
}
