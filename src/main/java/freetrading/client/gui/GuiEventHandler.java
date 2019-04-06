package freetrading.client.gui;

import org.lwjgl.opengl.GL11;

import freetrading.ClientNetworkHandler;
import freetrading.ClientProxy;
import freetrading.FreeTradingMod;
import freetrading.client.Icon;
import freetrading.tileentity.TileEntityVillageMarket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiMerchant;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class GuiEventHandler {

	public float moneyAlpha = 1.0f;
	public String moneyString = "";
	public String balanceString = "";
	private final int moneyColor = 0x00FFFFFF;
	public int balanceColor = 0x00FF0000;
	private BlockPos prevPos;
	private TileEntity tile;
	private ItemStack itemStackOverlayTooltip = ItemStack.EMPTY;

	@SubscribeEvent
	public void onGuiOpenEvent(GuiScreenEvent.InitGuiEvent.Post event) {
		if (!(event.getGui() instanceof GuiMerchant))
			return;
		Minecraft mc = Minecraft.getMinecraft();
		Entity entity = mc.objectMouseOver.entityHit;
		if (!(entity instanceof EntityVillager))
			return;
		EntityVillager villager = (EntityVillager) entity;
		if (villager != null) {
			mc.currentScreen = null;
			ClientNetworkHandler cn = (ClientNetworkHandler) FreeTradingMod.network;
			cn.openMerchantGui(villager);
		}
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public void onOverlayRender(RenderGameOverlayEvent.Post action) {
		if (action.getType() != RenderGameOverlayEvent.ElementType.ALL)
			return;
		Minecraft mc = Minecraft.getMinecraft();
		if (mc.gameSettings.showDebugInfo || mc.gameSettings.hideGUI)
			return;
		if (moneyAlpha < 0.1f) {
			balanceString = "";
			return;
		}
		GL11.glColor4f(1.0f, 1.0f, 1.0f, moneyAlpha);
		boolean unicode = mc.ingameGUI.getFontRenderer().getUnicodeFlag();
		mc.ingameGUI.getFontRenderer().setUnicodeFlag(false);
		mc.ingameGUI.getFontRenderer().drawString(moneyString, 20, 5, moneyColor | ((int) (moneyAlpha * 255.0f) << 24));
		mc.ingameGUI.getFontRenderer().drawString(balanceString, 60, 5,
				balanceColor | ((int) (moneyAlpha * 255.0f) << 24));
		mc.ingameGUI.getFontRenderer().setUnicodeFlag(unicode);
		mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		GL11.glColor4f(1.0f, 1.0f, 1.0f, moneyAlpha);
		drawEmeraldSign();
		drawToolTip();
		moneyAlpha -= 0.01f;
	}

	public void drawToolTip() {
		if (itemStackOverlayTooltip.isEmpty())
			return;
		GuiUtils.preItemToolTip(itemStackOverlayTooltip);
		Minecraft mc = Minecraft.getMinecraft();
		GuiUtils.drawHoveringText(itemStackOverlayTooltip.getTooltip(mc.player, ITooltipFlag.TooltipFlags.NORMAL), 0,
				30, 100, 100, -1, mc.fontRenderer);
		GuiUtils.postItemToolTip();
	}

	private void drawEmeraldSign() {
		Icon emeraldIcon = ClientProxy.EMERALD_SHARD;
		float minu = emeraldIcon.getMinU();
		float minv = emeraldIcon.getMinV();
		float maxu = emeraldIcon.getMaxU();
		float maxv = emeraldIcon.getMaxV();
		double scale = 1.0d;
		double x1 = 0.0D;
		double y1 = 0.0D;
		double x2 = 16D;
		double y2 = 16D;
		double z = 0 / 16D;
		BufferBuilder vb = Tessellator.getInstance().getBuffer();
		vb.begin(7, DefaultVertexFormats.POSITION_TEX);
		vb.pos(x1 * scale, y2 * scale, z * scale).tex(minu, maxv).endVertex();
		vb.pos(x2 * scale, y2 * scale, z * scale).tex(maxu, maxv).endVertex();
		vb.pos(x2 * scale, y1 * scale, z * scale).tex(maxu, minv).endVertex();
		vb.pos(x1 * scale, y1 * scale, z * scale).tex(minu, minv).endVertex();
		Tessellator.getInstance().draw();
	}

	@SubscribeEvent
	public void drawBlockSelectionBox(DrawBlockHighlightEvent event) {
		BlockPos pos = event.getTarget().getBlockPos();
		EntityPlayer playerIn = event.getPlayer();
		if (pos == null)
			return;
		if (prevPos != pos) {
			prevPos = pos;
			tile = playerIn.world.getTileEntity(pos);
		} else {
			return;
		}
		if (!(tile instanceof TileEntityVillageMarket)) {
			itemStackOverlayTooltip = ItemStack.EMPTY;
		} else {
			ClientNetworkHandler cnh = (ClientNetworkHandler) FreeTradingMod.network;
			cnh.requestPlayerMoneyInfo();
			itemStackOverlayTooltip = ((TileEntityVillageMarket) tile).displayedItem;
			moneyAlpha = 1.0f;
		}
	}

	public void updateBalance(long money, long balance) {
		moneyString = String.valueOf(money);
		moneyAlpha = 1.0f;
		if (balance == 0)
			return;
		if (balance < 0) {
			balanceString = String.valueOf(balance);
			balanceColor = 0x00FF0000;
		} else {
			balanceString = "+" + String.valueOf(balance);
			balanceColor = 0x0000FF00;
		}
	}
}
