package cwgfarplaneview.client;

import static cwgfarplaneview.CWGFarPlaneViewMod.MODID;
import static cwgfarplaneview.CWGFarPlaneViewMod.logger;
import static cwgfarplaneview.util.TerrainConfig.FLAT;
import static cwgfarplaneview.util.TerrainConfig.VOLUMETRIC_HORIZONTAL;
import static cwgfarplaneview.util.TerrainConfig.VOLUMETRIC_VERTICAL;
import static cwgfarplaneview.util.TerrainConfig.closePlane;
import static cwgfarplaneview.util.TerrainConfig.getFarClippingRange;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
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

	public ClientTerrainSurfaceBufferBuilder terrainSurfaceRenderWorker = new ClientTerrainSurfaceBufferBuilder();
	public ClientTerrain3DShapeBufferBuilder terrain3DShapeRenderWorker = new ClientTerrain3DShapeBufferBuilder();

	public void init() {
		Thread thread = new Thread(terrainSurfaceRenderWorker, "CWGFarPlaneView client surface renderer");
		thread.setDaemon(true);
		thread.setPriority(Thread.MIN_PRIORITY);
		thread.start();
		logger.debug("Client surface renderer initialized.");
		thread = new Thread(terrain3DShapeRenderWorker, "CWGFarPlaneView client volumetric renderer");
		thread.setDaemon(true);
		thread.setPriority(Thread.MIN_PRIORITY);
		thread.start();
		logger.debug("Client volumetric renderer initialized.");
	}

	private float fov = 70.0f;
	private int seaLevel = 64;
	private float prevFarPlane = getFarClippingRange();
	private int terrainSurfaceDisplayList = -1;
	private int terrainVolumetricDisplayList = -1;
	private int seaDisplayList = -1;
	private int backgroundDisplayList = -1;
    @SuppressWarnings("unused")
	private static final FloatBuffer fogColor = BufferUtils.createFloatBuffer(4);

	@Override
	public void render(float partialTicks, WorldClient world, Minecraft mc) {
/*		float red = 0.0f;
		float green = 0.8f;
		float blue = 1.0f;
		GL11.glClearColor(red, green, blue, 1.0f);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
		fogColor.put(red);
		fogColor.put(green);
		fogColor.put(blue);
		fogColor.put(1.0f);
		fogColor.flip();
		GL11.glFog(GL11.GL_FOG_COLOR, fogColor);*/
		world.provider.setSkyRenderer(null);
		mc.renderGlobal.renderSky(partialTicks, 2);
		world.provider.setSkyRenderer(this);
		GlStateManager.matrixMode(5889);
		GlStateManager.loadIdentity();
		Project.gluPerspective(fov, (float) mc.displayWidth / (float) mc.displayHeight, closePlane, getFarClippingRange());
		GlStateManager.matrixMode(5888);
		EntityPlayerSP player = mc.player;
		float renderPosX = (float) (player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks);
		float renderPosY = (float) (player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks);
		float renderPosZ = (float) (player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks);
		GL11.glDisable(GL11.GL_FOG);
		GL11.glShadeModel(GL11.GL_SMOOTH);
		mc.entityRenderer.enableLightmap();
		mc.getTextureManager().bindTexture(TERRAIN_TEXTURE);
		
		if (this.seaDisplayList == -1) {
			this.seaDisplayList = GLAllocation.generateDisplayLists(1);
			this.compileSeaDisplayList();
		}
		if (this.backgroundDisplayList == -1) {
			this.backgroundDisplayList = GLAllocation.generateDisplayLists(1);
			this.compileBackgroundDisplayList();
		}

		
		if (prevFarPlane != getFarClippingRange()) {
			this.compileSeaDisplayList();
			this.compileBackgroundDisplayList();
			prevFarPlane = getFarClippingRange();
		}
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glPushMatrix();
		GL11.glTranslatef(0.0f, -renderPosY, 0.0f);
		GL11.glCallList(this.backgroundDisplayList);
		GL11.glPopMatrix();
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		
		LightHelper.enableLight(world, partialTicks);
		
		// Terrain
		GL11.glPushMatrix();
		GL11.glTranslatef(-renderPosX, 0.5f - renderPosY, -renderPosZ);
		if (terrainSurfaceRenderWorker.ready && terrainSurfaceRenderWorker.isDrawning) {
			compileSurfaceDisplayList(world);
			terrainSurfaceRenderWorker.ready = false;
		}
		if (terrain3DShapeRenderWorker.ready) {
			compileVolumetricDisplayList(world);
		}
		if (FLAT.maxUpdateDistanceCells > 0)
			GL11.glCallList(this.terrainSurfaceDisplayList);
		GL11.glTranslatef(0.0f, 0.2f, 0.0f);
		if (VOLUMETRIC_HORIZONTAL.maxUpdateDistanceCells > 0 && VOLUMETRIC_VERTICAL.maxUpdateDistanceCells > 0)
			GL11.glCallList(this.terrainVolumetricDisplayList);
		GL11.glPopMatrix();

		// Sea
		GL11.glPushMatrix();
		GL11.glTranslatef(0.0f, -renderPosY-0.2f, 0.0f);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glCallList(this.seaDisplayList);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_FOG);
		GL11.glPopMatrix();
		LightHelper.disableLight();
		if(CubeRenderer.DEBUG_MODE)
			GL11.glClearDepth(0.8);
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
	}

	private void compileSeaDisplayList() {
		GL11.glNewList(this.seaDisplayList, 4864);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder worldRendererIn = tessellator.getBuffer();
		worldRendererIn.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);
		float farPlane = getFarClippingRange();
		
		this.addSeaVertex(farPlane * 2,  farPlane * 2, 0.0f, 0.0f, 1.0f);
		this.addSeaVertex(farPlane * 2,  0.0f, 1.0f, 0.0f, 1.0f);
		this.addSeaVertex(0.0f,  0.0f, 1.0f, 1.0f, 0.8f);
		this.addSeaVertex(0.0f,  farPlane * 2, 0.0f, 1.0f, 1.0f);
		
		this.addSeaVertex(farPlane * 2,  -farPlane * 2, 0.0f, 0.0f, 1.0f);
		this.addSeaVertex(0.0f,  -farPlane * 2, 1.0f, 0.0f, 1.0f);
		this.addSeaVertex(0.0f,  0.0f, 1.0f, 1.0f, 0.8f);
		this.addSeaVertex(farPlane * 2,  0.0f, 0.0f, 1.0f, 1.0f);
		
		this.addSeaVertex(-farPlane * 2,  -farPlane * 2, 0.0f, 0.0f, 1.0f);
		this.addSeaVertex(-farPlane * 2,  0.0f, 1.0f, 0.0f, 1.0f);
		this.addSeaVertex(0.0f,  0.0f, 1.0f, 1.0f, 0.8f);
		this.addSeaVertex(0.0f,  -farPlane * 2, 0.0f, 1.0f, 1.0f);

		this.addSeaVertex(-farPlane * 2,  farPlane * 2, 0.0f, 0.0f, 1.0f);
		this.addSeaVertex(0.0f,  farPlane * 2, 1.0f, 0.0f, 1.0f);
		this.addSeaVertex(0.0f,  0.0f, 1.0f, 1.0f, 0.8f);
		this.addSeaVertex(-farPlane * 2,  0.0f, 0.0f, 1.0f, 1.0f);
		
		tessellator.draw();
		GL11.glEndList();
	}
	
	private void compileBackgroundDisplayList() {
		GL11.glNewList(this.backgroundDisplayList, 4864);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder worldRendererIn = tessellator.getBuffer();
		worldRendererIn.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);
		float farPlane = getFarClippingRange();
		
		this.addBackgroundVertex(farPlane * 2,  farPlane * 2, 0.0f, 0.0f);
		this.addBackgroundVertex(farPlane * 2,  -farPlane * 2, 1.0f, 0.0f);
		this.addBackgroundVertex(-farPlane * 2,  -farPlane * 2, 1.0f, 1.0f);
		this.addBackgroundVertex(-farPlane * 2,  farPlane * 2, 0.0f, 1.0f);
		
		tessellator.draw();
		GL11.glEndList();
	}
	
	private void addSeaVertex(float x, float z, float u, float v, float alpha) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder worldRendererIn = tessellator.getBuffer();
		worldRendererIn.pos(x, seaLevel, z).tex(u, v).lightmap(240, 0)
		.color(0.17f, 0.24f, 0.97f, alpha).endVertex();
	}
	
	private void addBackgroundVertex(float x, float z, float u, float v) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder worldRendererIn = tessellator.getBuffer();
		worldRendererIn.pos(x, seaLevel, z).tex(u, v).lightmap(240, 0)
		.color(0.0f, 0.0f, 0.0f, 1.0f).endVertex();
	}


	private void compileSurfaceDisplayList(WorldClient world) {
		if (this.terrainSurfaceDisplayList == -1) {
			this.terrainSurfaceDisplayList = GLAllocation.generateDisplayLists(1);
		}
		terrainSurfaceRenderWorker.draw(this.terrainSurfaceDisplayList);
	}
	
	private void compileVolumetricDisplayList(WorldClient world) {
		if (this.terrainVolumetricDisplayList == -1) {
			this.terrainVolumetricDisplayList = GLAllocation.generateDisplayLists(1);
		}
		terrain3DShapeRenderWorker.draw(this.terrainVolumetricDisplayList);
	}

	@SubscribeEvent
	public void fovHook(EntityViewRenderEvent.FOVModifier event) {
		this.fov = event.getFOV();
	}

	public void setSeaLevel(int seaLevelIn) {
		seaLevel = seaLevelIn;
	}

	public int getSeaLevel() {
		return seaLevel;
	}
}
