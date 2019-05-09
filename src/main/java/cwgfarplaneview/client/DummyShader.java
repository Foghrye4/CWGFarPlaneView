package cwgfarplaneview.client;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import static cwgfarplaneview.CWGFarPlaneViewMod.*;

public class DummyShader {
	private static final String vertShaderSource = "#version 120" + 
			"varying vec2 texcoord;" + 
			"void main(){" + 
			"    gl_Position = ftransform();" + 
			"    texcoord = (gl_TextureMatrix[0] * gl_MultiTexCoord0).xy;" +
			"}";
	private static final String fragShaderSource = "#version 120" + 
			"uniform sampler2D gcolor;" +
			"varying vec2 texcoord;" + 
			"void main(){" + 
			"    vec3 color = texture2D(gcolor, texcoord).rgb;" +
			"    gl_FragData[0] = vec4(color, 1.0);" + 
			"}";
	
	private int vertShader;
	private int fragShader;
	private int program;

	public DummyShader() {
		vertShader = createShader(vertShaderSource, GL20.GL_VERTEX_SHADER);
		fragShader = createShader(fragShaderSource, GL20.GL_FRAGMENT_SHADER);
		if (vertShader != 0 && fragShader != 0)
			program = GL20.glCreateProgram();
		
        if(program == 0)
            return;
         
        GL20.glAttachShader(program, vertShader);
        GL20.glAttachShader(program, fragShader);
        GL20.glLinkProgram(program);
		if (GL20.glGetShaderi(program, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
			logger.error("Error creating shader:" + getLogInfo(program));
			return;
		}
		
		GL20.glValidateProgram(program);
		if (GL20.glGetShaderi(program, GL20.GL_VALIDATE_STATUS) == GL11.GL_FALSE) {
			logger.error("Error creating shader:" + getLogInfo(program));
			return;
		}
	}

	private int createShader(String source, int shaderType) {
		int shader = 0;
		shader = GL20.glCreateShader(shaderType);
		if (shader == 0)
			return 0;
		GL20.glShaderSource(shader, source);
		GL20.glCompileShader(shader);
		if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE)
			logger.error("Error creating shader:" + getLogInfo(shader));
		return shader;
	}

    private static String getLogInfo(int obj) {
        return GL20.glGetProgramInfoLog(obj, GL20.glGetShaderi(obj, GL20.GL_INFO_LOG_LENGTH));
    }

	public void useProgram() {
		if (program != 0)
			GL20.glUseProgram(program);
	}
}
