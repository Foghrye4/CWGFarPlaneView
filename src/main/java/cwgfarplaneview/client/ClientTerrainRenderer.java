package cwgfarplaneview.client;

import static cwgfarplaneview.CWGFarPlaneViewMod.MODID;
import static cwgfarplaneview.util.AddressUtil.CLOSE_PLANE;
import static cwgfarplaneview.util.AddressUtil.FAR_PLANE;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Project;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IRenderHandler;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ClientTerrainRenderer extends IRenderHandler {
	private static final ResourceLocation TERRAIN_TEXTURE = new ResourceLocation(MODID,
			"textures/terrain/white_noise.png");

	public ClientTerrainShapeBufferBuilder terrainRenderWorker = new ClientTerrainShapeBufferBuilder();

	public ClientTerrainRenderer() {
		Thread thread = new Thread(terrainRenderWorker, "Client surface builder");
		thread.setDaemon(true);
		thread.setPriority(Thread.MIN_PRIORITY);
		thread.start();
	}

	private VanillaSkyRenderer vanillaSkyRenderer = new VanillaSkyRenderer();

	private float fov = 70.0f;
	private int seaLevel = 64;
	private float prevFarPlane = FAR_PLANE;
	private int terrainDisplayList = -1;
	private int seaDisplayList = -1;

	@Override
	public void render(float partialTicks, WorldClient world, Minecraft mc) {
		vanillaSkyRenderer.renderSky(partialTicks);
		GlStateManager.matrixMode(5889);
		GlStateManager.loadIdentity();
		Project.gluPerspective(fov, (float) mc.displayWidth / (float) mc.displayHeight, CLOSE_PLANE, FAR_PLANE);
		GlStateManager.matrixMode(5888);
		EntityPlayerSP player = mc.player;
		float renderPosX = (float) (player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks);
		float renderPosY = (float) (player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks);
		float renderPosZ = (float) (player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks);
		GL11.glDisable(GL11.GL_FOG);
		GL11.glPushMatrix();
		GL11.glShadeModel(GL11.GL_SMOOTH);
		mc.entityRenderer.enableLightmap();
		mc.getTextureManager().bindTexture(TERRAIN_TEXTURE);
		if (this.seaDisplayList == -1) {
			this.seaDisplayList = GLAllocation.generateDisplayLists(1);
			this.compileSeaDisplayList();
		}
		if (prevFarPlane != FAR_PLANE) {
			this.compileSeaDisplayList();
			prevFarPlane = FAR_PLANE;
		}
		GL11.glTranslatef(0.0f, -renderPosY, 0.0f);
		GL11.glCallList(this.seaDisplayList);
		GL11.glTranslatef(-renderPosX, 0.5f, -renderPosZ);
		if (terrainRenderWorker.ready && terrainRenderWorker.isDrawning) {
			compileDisplayList(world);
			terrainRenderWorker.ready = false;
		}
		GL11.glCallList(this.terrainDisplayList);
		GL11.glPopMatrix();
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
	}

	private void compileSeaDisplayList() {
		GL11.glNewList(this.seaDisplayList, 4864);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder worldRendererIn = tessellator.getBuffer();
		worldRendererIn.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);
		worldRendererIn.pos(FAR_PLANE, seaLevel, FAR_PLANE).tex(0.0f, 0.0f)
				.lightmap(240, 0).color(0.17f, 0.24f, 0.97f, 1.0f).endVertex();
		worldRendererIn.pos(FAR_PLANE, seaLevel, -FAR_PLANE).tex(1.0f, 0.0f)
				.lightmap(240, 0).color(0.17f, 0.24f, 0.97f, 1.0f).endVertex();
		worldRendererIn.pos(-FAR_PLANE, seaLevel, -FAR_PLANE).tex(1.0f, 1.0f)
				.lightmap(240, 0).color(0.17f, 0.24f, 0.97f, 1.0f).endVertex();
		worldRendererIn.pos(-FAR_PLANE, seaLevel, FAR_PLANE).tex(0.0f, 1.0f)
				.lightmap(240, 0).color(0.17f, 0.24f, 0.97f, 1.0f).endVertex();
		tessellator.draw();
		GL11.glEndList();
	}

	private void compileDisplayList(WorldClient world) {
		if (this.terrainDisplayList == -1) {
			this.terrainDisplayList = GLAllocation.generateDisplayLists(1);
		}
		GL11.glNewList(this.terrainDisplayList, 4864);
		terrainRenderWorker.draw();
		GL11.glEndList();
	}

	@SubscribeEvent
	public void fovHook(EntityViewRenderEvent.FOVModifier event) {
		this.fov = event.getFOV();
	}

	public void setSeaLevel(int seaLevelIn) {
		seaLevel = seaLevelIn;
	}
}
