package cwgfarplaneview.client;

import static cwgfarplaneview.CWGFarPlaneViewMod.MODID;
import static cwgfarplaneview.util.AddressUtil.*;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Project;

import cwgfarplaneview.world.TerrainSurfaceBuilderWorker;
import cwgfarplaneview.world.storage.WorldSavedDataTerrainSurface;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.client.IRenderHandler;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ClientTerrainRenderer extends IRenderHandler {
	private static final ResourceLocation TERRAIN_TEXTURE = new ResourceLocation(MODID,
			"textures/terrain/white_noise.png");
	public static final int HORIZONT_DISTANCE_CHUNKS = MAX_UPDATE_DISTANCE_CHUNKS - (32 << MESH_SIZE_BIT_CHUNKS);
	public static final int HORIZONT_DISTANCE_BLOCKS = HORIZONT_DISTANCE_CHUNKS << 4;
	private static final float CLOSE_PLANE = 16.0f;
	private static final float FAR_PLANE = HORIZONT_DISTANCE_BLOCKS * MathHelper.SQRT_2;
	
	public ClientTerrainShapeBufferBuilder terrainRenderWorker;
	
	public void initTerrainRenderWorker(WorldClient world) {
		terrainRenderWorker = new ClientTerrainShapeBufferBuilder(world);
		Thread thread = new Thread(terrainRenderWorker, "Client surface builder");
		thread.setDaemon(true);
		thread.setPriority(Thread.MIN_PRIORITY);
		thread.start();
	}

	@SubscribeEvent
	public void onWorldUnloadEvent(WorldEvent.Unload event) {
		World world = event.getWorld();
		if (world.provider.getDimension() != 0 || !(world instanceof WorldClient) || terrainRenderWorker == null)
			return;
		terrainRenderWorker.stop();
	}
	
	private VanillaSkyRenderer vanillaSkyRenderer = new VanillaSkyRenderer();

	private float fov = 70.0f;
	private int seaLevel = 64;
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
			this.compileSeaDisplayList();
		}
		GL11.glTranslatef(0.0f, -renderPosY, 0.0f);
		GL11.glCallList(this.seaDisplayList);
		GL11.glTranslatef(-renderPosX, 0.5f, -renderPosZ);
		if (terrainRenderWorker == null)
			initTerrainRenderWorker(world);
		if (terrainRenderWorker.ready) {
			compileDisplayList(world);
			terrainRenderWorker.ready = false;
		}
		GL11.glCallList(this.terrainDisplayList);
		GL11.glPopMatrix();
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
	}

	private void compileSeaDisplayList() {
		this.seaDisplayList = GLAllocation.generateDisplayLists(1);
		GL11.glNewList(this.seaDisplayList, 4864);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder worldRendererIn = tessellator.getBuffer();
		worldRendererIn.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);
		worldRendererIn.pos(HORIZONT_DISTANCE_BLOCKS, seaLevel, HORIZONT_DISTANCE_BLOCKS).tex(0.0f, 0.0f)
				.lightmap(240, 0).color(0.17f, 0.24f, 0.97f, 1.0f).endVertex();
		worldRendererIn.pos(HORIZONT_DISTANCE_BLOCKS, seaLevel, -HORIZONT_DISTANCE_BLOCKS).tex(1.0f, 0.0f)
				.lightmap(240, 0).color(0.17f, 0.24f, 0.97f, 1.0f).endVertex();
		worldRendererIn.pos(-HORIZONT_DISTANCE_BLOCKS, seaLevel, -HORIZONT_DISTANCE_BLOCKS).tex(1.0f, 1.0f)
				.lightmap(240, 0).color(0.17f, 0.24f, 0.97f, 1.0f).endVertex();
		worldRendererIn.pos(-HORIZONT_DISTANCE_BLOCKS, seaLevel, HORIZONT_DISTANCE_BLOCKS).tex(0.0f, 1.0f)
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
