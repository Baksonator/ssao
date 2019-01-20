package ssao;

import rafgl.RGL;
import rafgl.jglm.Mat4;
import rafgl.jglm.Matrices;
import rafgl.jglm.Vec2;
import rafgl.jglm.Vec3;
import rafgl.jglm.Vec4;

import static org.lwjgl.opengl.GL20.*;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;

public class GameCamera
{
	public enum GameCameraMode
	{
	    GAMECAM_TARGETED,
	    GAMECAM_FIRST_PERSON,
	    GAMECAM_ORBIT
	};
	
	// Kljucne komponente za lookUp
	public Vec3 position;
	public Vec3 target;

    // Samo za orijentaciju, nije trenutni "up" vektor
	public Vec3 orientationUp;

    // FoV i zoom se mnoze, zoom je tu samo za laksu upotrebu
	public float vfov;
	public float zoom;

    // Near i far clipping plane
	public float nearZ;
	public float farZ;

    // Trenutni mod kamere
	public GameCameraMode mode;

    // Tri vektora koji se stalno azuriraju i imaju tacne vrijednosti za
    // trenutni polozaj kamere, bez obzira na mod
	public Vec3 forward;
	public Vec3 up;
	public Vec3 right;

    // Uglovi koji se uzimaju u obzir u FIRST_PERSON i ORBIT modovima
    // (roll se trenutno ignorise)
	public float yaw;
	public float pitch;
	public float roll;

    // Udaljenost kamere od njene mete, bitno za ORBIT
	public float distance;

    // View i Projection matrice, automatski azurirane, zajedno sa proizvodom
	public Mat4 matProjection;
	public Mat4 matView;
	public Mat4 matVP;
    
    public FloatBuffer matBuffProjection = BufferUtils.createFloatBuffer(4 * 4);
    public FloatBuffer matBuffView = BufferUtils.createFloatBuffer(4 * 4);
    public FloatBuffer matBuffVP = BufferUtils.createFloatBuffer(4 * 4);
    
    // Pocetne vrijednosti
    public GameCamera(GameCameraMode cmode)
    {
        mode = cmode;

        position = new Vec3(0.0f, 0.0f, -3.0f);
        target = new Vec3(0.0f, 0.0f, 0.0f);
        orientationUp = new Vec3(0.0f, 1.0f, 0.0f);
        vfov = 60.0f;
        zoom = 1.0f;

        forward = new Vec3(0.0f, 0.0f, 1.0f);
        up = new Vec3(0.0f, 1.0f, 0.0f);
        right = new Vec3(1.0f, 0.0f, 0.0f);

        yaw = 0.0f;
        pitch = 0.0f;
        roll = 0.0f;

        nearZ = 0.1f;
        farZ = 100.0f;

        distance = 3.0f;

        update();
    }

    public void update()
    {
        // Zavisno od trenutnog moda kamere, razlicite setove parametara uzimamo
        // kao ulaz i razlicite tretiramo kao izlaz, na kraju formirajuci matrice.
        if(mode == GameCameraMode.GAMECAM_TARGETED)
        {
        	distance = target.subtract(position).getLength();
            yaw = RGL.getAngleRad(position.x, position.z, target.x, target.z);
            pitch = RGL.getAngleRad(0.0f, 0.0f, distance, target.y - position.y);
        }
        else if(mode == GameCameraMode.GAMECAM_FIRST_PERSON)
        {
            forward = new Vec3((float)(Math.cos(yaw) * Math.cos(pitch)), (float)(Math.sin(pitch)), (float)(Math.sin(yaw) * Math.cos(pitch)));

            target = position.add(forward);
        }
        else if(mode == GameCameraMode.GAMECAM_ORBIT)
        {
            position = target.add(new Vec3(distance * (float)Math.cos(yaw) * (float)Math.cos(pitch), distance * (float)Math.sin(pitch), distance * (float)Math.sin(yaw) * (float)Math.cos(pitch)));
        }

        // Modovi kamere zapravo samo govore da li se target postavlja rucno ili se
        // racuna po nekom pravilu, a na kraju uvijek koristimo position/target za
        // glm::lookAt() poziv, kao i glm::perspective() za projection matricu.
        matProjection = Matrices.perspective(vfov * (1.0f / zoom), RGL.getAspectRatio(), nearZ, farZ);
        matView = Matrices.lookAt(position, target, orientationUp);
        matVP = matProjection.multiply(matView);
        
        matProjection.store(matBuffProjection);
        matBuffProjection.flip();
        
        matView.store(matBuffView);
        matBuffView.flip();
        
        matVP.store(matBuffVP);
        matBuffVP.flip();

        // Forward vektor je, logicno, normalizovana razlika mete i pozicije, sto
        // nam daje vektor usmjerenja kamere
        forward = target.subtract(position).getUnitVector();

        // Vektore za gore i desno prosto dobijamo transformisuci jedinicne vektore
        // po Y i X osama koristeci View matricu kamere, sto nam garantuje da ce
        // biti tacni, kakvu god transformaciju napravili
        Vec4 upVector4 = new Vec4(0.0f, 1.0f, 0.0f, 0.0f); // * matView;
        Vec4 rightVector4 = new Vec4(1.0f, 0.0f, 0.0f, 0.0f); // * matView;

        upVector4 = upVector4.multiply(matView); //matView.multiply(upVector4);
        rightVector4 = rightVector4.multiply(matView); //matView.multiply(rightVector4);

        up = new Vec3(upVector4.x, upVector4.y, upVector4.z);
        right = new Vec3(rightVector4.x, rightVector4.y, rightVector4.z);
    }
    
    public Vec3 projectToScreen(Vec3 worldPoint)
    {
    	Vec4 tmp = new Vec4(worldPoint.x, worldPoint.y, worldPoint.z, 1.0f);
    	tmp = tmp.multiplyTP(matVP);
    	tmp = tmp.multiply((1.0f / tmp.w) * 0.5f);
    	return new Vec3((tmp.x + 0.5f) * RGL.getWidth(), (1.0f - (tmp.y + 0.5f)) * RGL.getHeight(), (tmp.z + 0.5f));
    }

    public Vec3 getPixelViewVector(int x, int y)
    {
        // Tangensom racunamo world-space visinu pogleda na nearZ daljini,
        // uracunavajuci vertikalni field-of-view, zatim racunajuci i sirinu
        // jednostavnim mnozenjem sa proporcijom ekrana
        float Hnear = 2.0f * (float)Math.tan(((vfov * (1.0f / zoom)) * RGL.DEG_TO_RADf) * 0.5f) * nearZ;
        float Wnear = Hnear * RGL.getAspectRatio();

        // Normalizujemo 2D koordinate u pixelima u -0.5 do 0.5 opseg
        Vec2 screenSize = new Vec2((float)RGL.getWidth(), (float)RGL.getHeight());
        Vec2 scrPos = new Vec2((float)x - screenSize.x * 0.5f, (float)y - screenSize.y * 0.5f);
        scrPos = new Vec2(scrPos.x / screenSize.x, scrPos.y / screenSize.y);

        // Na poziciju kamere dodajemo forward vektor, pomnozen sa nearZ, sto
        // daje poziciju centra ekrana na nearZ daljini
        Vec3 nc = position.add(forward.multiply(nearZ));

        // Odave se pomijeramo desno i gore, po izracunatim proporcijama, kako
        // bismo dosli na polozaj koji odgovara trazenom pikeslu
        Vec3 res = nc.add(right.multiply(scrPos.x * Wnear)).add(up.multiply(-scrPos.y * Hnear)).subtract(position);
        
        // Na kraju vracamo normalizovanu razliku dobijene tacke i pozicije kamere
        // kao trazeni vektor pravca
        return res.getUnitVector();
    }

    public Vec3 getMouseViewVector()
    {
        return getPixelViewVector(RGL.getMouseX(), RGL.getMouseY());
    }

    public void uploadUniforms(GameShader shader)
    {
    	matView.store(matBuffView); matBuffView.flip();
    	matProjection.store(matBuffProjection); matBuffProjection.flip();
    	matVP.store(matBuffVP); matBuffVP.flip();
    	
        // Ukoliko shader ima neke od poznatih uniform vrijednosti koje kamera
        // moze da ponudi, radimo upload glUniform pozivima
        
    	
        if(shader.uni_V >= 0)
            glUniformMatrix4(shader.uni_V, false, matBuffView);

        if(shader.uni_P >= 0)
            glUniformMatrix4(shader.uni_P, false, matBuffProjection);
        
        if(shader.uni_VP >= 0)
            glUniformMatrix4(shader.uni_VP, false, matBuffVP);

        if(shader.uni_camPosition >= 0)
            glUniform3f(shader.uni_camPosition, position.x, position.y, position.z);

        if(shader.uni_camVector >= 0)
            glUniform3f(shader.uni_camVector, forward.x, forward.y, forward.z);
    }
}
