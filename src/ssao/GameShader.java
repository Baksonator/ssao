package ssao;

import rafgl.RGL;
import static org.lwjgl.opengl.GL20.*;

public class GameShader
{
	// Shader program ID, koristimo ga za glUseProgram, itd.
	public int shaderID;

    // Potencijalne ID vrijednosti za uniforme koje ocekujemo da bismo
    // mogli koristiti u svojim shaderima u ovom projektu
	public int uni_MVP;
	public int uni_MV;
	public int uni_VP;
	public int uni_M;
	public int uni_V;
	public int uni_P;

	public int uni_camPosition;
	public int uni_camVector;

	public int uni_screenSize;

	public int uni_lightVector1;
	public int uni_lightColor1;
	public int uni_lightRange1;

	public int uni_lightVector2;
	public int uni_lightColor2;
	public int uni_lightRange2;

	public int uni_lightVector3;
	public int uni_lightColor3;
	public int uni_lightRange3;

	public int uni_lightCount;
	public int uni_ambient;
	public int uni_objectColor;

    // Za laksi rad cemo ostaviti mjesta i za neke specijalne uniform
    // vrijednosti specificne za pojedinacne shadere (animacije, recimo)
	public int[] uni_special = new int[16];

    // Za multi-texturing, ako koristimo vise tekstura (odnosno vise samplera),
    // tada je bitno postaviti i odgovarajuce texture jedinice
	public int[] uni_texture = new int[16];

    //////

    // Slicno kao i za uniform, lista atributa za koje ocekujemo da bi mogli
    // biti od koristi (naravno, ne moraju svi biti prisutni, kao i uniformi)
	public int attr_position;
	public int attr_color;
	public int attr_alpha;
	public int attr_uv;
	public int attr_normal;

    // Ponovo, mjesto za eventualne posebne atribute
	public int[] attr_special = new int[16];

    //////

    // Naziv shadera
	public String name;

    
	// Inicijalne vrijednosti
	public GameShader()
	{
	    shaderID = 0;
	    name = "";

	    for(int i = 0; i < 16; i++)
	    {
	        uni_special[i] = -1;
	        attr_special[i] = -1;
	    }

	    for(int i = 0; i < 8; i++)
	        uni_texture[i] = -1;
	}

	public GameShader(String shaderName)
	{
	    name = shaderName;

	    for(int i = 0; i < 16; i++)
	    {
	        uni_special[i] = -1;
	        attr_special[i] = -1;
	    }

	    for(int i = 0; i < 8; i++)
	        uni_texture[i] = -1;

	    loadShader(shaderName);
	}

	public void loadShader(String shaderName)
	{
	    String vertPath;
	    String fragPath;

	    // Sablon po kom od naziva trazimo stvarne putanje fajlova
	    vertPath = "shaders/" + shaderName + "-vert.glsl";
	    fragPath = "shaders/" + shaderName + "-frag.glsl";

	    // Stvarno ucitavanje GLSL shadera radimo postojecim pozivom
	    shaderID = RGL.loadShader(vertPath, fragPath);

	    if(shaderID > 0)
	    {
	        // Ako je shader uspjesno ucitan, idemo traziti uniforme i atribute
	        queryAttributes();
	        queryUniforms();
	        RGL.log("[RGL] Game shader '" + shaderName + "' loaded");
	    }
	    else
	    {
	    	RGL.log("[RGL] Failed to load game shader '" + shaderName + "' ('" + vertPath + "' / '" + fragPath + "')");
	    }
	}

	public void queryUniforms()
	{
	    // Redom ispitujemo shader trazeci uniforme po nazivima koje planiramo
	    // koristiti. Naravno, svi ovi nazivi su potpuno proizvoljni, ali korisno
	    // je odluciti se za neku konvenciju bas zbog ovakvih nacina automatizacije
	    // kroz objedinjene funkcije za ucitavanje, kako ne bismo svaki shader na
	    // drugaciji nacin ucitavali i koristili. Ako zelite koristiti drugacije
	    // nazive u svojim GLSL fajlovima ili dodati ili oduzeti neke naziva, ovo
	    // je mjesto za to.

	    uni_MVP = glGetUniformLocation(shaderID, "uni_MVP");
	    uni_MV = glGetUniformLocation(shaderID, "uni_MV");
	    uni_VP = glGetUniformLocation(shaderID, "uni_VP");
	    uni_M = glGetUniformLocation(shaderID, "uni_M");
	    uni_V = glGetUniformLocation(shaderID, "uni_V");
	    uni_P = glGetUniformLocation(shaderID, "uni_P");

	    uni_camPosition = glGetUniformLocation(shaderID, "uni_camPosition");
	    uni_camVector = glGetUniformLocation(shaderID, "uni_camVector");

	    uni_screenSize = glGetUniformLocation(shaderID, "uni_screenSize");

	    uni_lightVector1 = glGetUniformLocation(shaderID, "uni_lightVector1");
	    uni_lightColor1 = glGetUniformLocation(shaderID, "uni_lightColor1");
	    uni_lightRange1 = glGetUniformLocation(shaderID, "uni_lightRange1");

	    uni_lightVector2 = glGetUniformLocation(shaderID, "uni_lightVector2");
	    uni_lightColor2 = glGetUniformLocation(shaderID, "uni_lightColor2");
	    uni_lightRange2 = glGetUniformLocation(shaderID, "uni_lightRange2");

	    uni_lightVector3 = glGetUniformLocation(shaderID, "uni_lightVector3");
	    uni_lightColor3 = glGetUniformLocation(shaderID, "uni_lightColor3");
	    uni_lightRange3 = glGetUniformLocation(shaderID, "uni_lightRange3");

	    uni_lightCount = glGetUniformLocation(shaderID, "uni_lightCount");
	    uni_ambient = glGetUniformLocation(shaderID, "uni_ambient");
	    uni_objectColor = glGetUniformLocation(shaderID, "uni_objectColor");

	    // Ocekujemo da ce sampleri tekstura biti nazvani uni_texture0, uni_texture1, ...
	    for(int i = 0; i < 8; ++i)
	    {
	        String uniname = "uni_texture" + i;
	        uni_texture[i] = glGetUniformLocation(shaderID, uniname);
	    }
	}

	public void queryAttributes()
	{
	    // Slicno kao i za uniforme, trazimo atribute po unaprijed odlucenim
	    // nazivima. Ovdje ih mozemo promijeniti po potrebi.

	    attr_position = glGetAttribLocation(shaderID, "in_Position");
	    attr_color = glGetAttribLocation(shaderID, "in_Color");
	    attr_alpha = glGetAttribLocation(shaderID, "in_Alpha");
	    attr_uv = glGetAttribLocation(shaderID, "in_UV");
	    attr_normal = glGetAttribLocation(shaderID, "in_Normal");
	}

	// Trazenje posebnih uniform vrijednosti
	public void findSpecialUniform(int slot, String uniformName)
	{
	    if(slot >= 0 && slot < 16)
	    {
	        uni_special[slot] = glGetUniformLocation(shaderID, uniformName);
	        if(uni_special[slot] < 0)
	            RGL.log("[RGL] Can't find uniform '" + uniformName + "' for shader '" + name + "'");
	    }
	    else
	    {
	    	RGL.log("[RGL] Special uniform '" + uniformName + "' slot out of range (0 - 15, given: " + slot + ")");
	    }
	}

	// Trazenje posebnih atributa
	public void findSpecialAttribute(int slot, String attributeName)
	{
	    if(slot >= 0 && slot < 16)
	    {
	        attr_special[slot] = glGetAttribLocation(shaderID, attributeName);
	        if(attr_special[slot] < 0)
	        	RGL.log("[RGL] Can't find attribute '" + attributeName + "' for shader '" + name + "'");
	    }
	    else
	    {
	    	RGL.log("[RGL] Special attribute '" + attributeName + "' slot out of range (0 - 15, given: " + slot + ")");
	    }
	}

}
