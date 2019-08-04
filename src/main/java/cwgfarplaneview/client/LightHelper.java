package cwgfarplaneview.client;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.RenderHelper;

public class LightHelper {
	
    public static void enableLight(WorldClient world, float partialTicks)
    {
		double celestialAngle = (world.getWorldTime() % 24000L / 12000f) * Math.PI;
    	
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_LIGHT0);
        GL11.glDisable(GL11.GL_LIGHT1);
        GL11.glDisable(GL11.GL_LIGHT2);
        GL11.glEnable(GL11.GL_LIGHT3);
        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
        GL11.glColorMaterial(GL11.GL_FRONT_AND_BACK, GL11.GL_AMBIENT_AND_DIFFUSE);
        
        GL11.glLight(GL11.GL_LIGHT3, GL11.GL_POSITION, RenderHelper.setColorBuffer((float)Math.cos(celestialAngle), (float)Math.sin(celestialAngle), 0.0f, 0.0f));
        GL11.glLight(GL11.GL_LIGHT3, GL11.GL_DIFFUSE, RenderHelper.setColorBuffer(0.3F, 0.3F, 0.3F, 1.0F));
        GL11.glLight(GL11.GL_LIGHT3, GL11.GL_AMBIENT, RenderHelper.setColorBuffer(0.6F, 0.6F, 0.6F, 1.0F));
    }
    
    public static void disableLight()
    {
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_LIGHT3);
        GL11.glDisable(GL11.GL_COLOR_MATERIAL);
    }

}
