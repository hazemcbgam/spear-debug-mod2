package com.example.speardebug;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Diagnostic client mod.
 *
 * Press the keybind (default: K) while holding an item (e.g. the item
 * ViaBackwards downgraded the 1.21.11 Spear into) to dump its registry id
 * and full NBT (including any CustomModelData tag) to chat and to the log.
 *
 * This lets us figure out exactly what base item + custom_model_data the
 * spear is being mapped to on your specific server, without needing
 * server access.
 */
public class SpearDebugClient implements ClientModInitializer {
	public static final String MOD_ID = "speardebug";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static KeyBinding dumpMainHandKey;
	private static KeyBinding dumpOffHandKey;

	@Override
	public void onInitializeClient() {
		dumpMainHandKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.speardebug.dump_mainhand",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_K,
				"category.speardebug"
		));

		dumpOffHandKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.speardebug.dump_offhand",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_L,
				"category.speardebug"
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (dumpMainHandKey.wasPressed()) {
				dumpHeldItem(client, Hand.MAIN_HAND);
			}
			while (dumpOffHandKey.wasPressed()) {
				dumpHeldItem(client, Hand.OFF_HAND);
			}
		});

		LOGGER.info("[SpearDebug] Loaded. Press K to dump main-hand item, L for off-hand.");
	}

	private void dumpHeldItem(MinecraftClient client, Hand hand) {
		if (client.player == null) {
			return;
		}

		ItemStack stack = client.player.getStackInHand(hand);

		if (stack.isEmpty()) {
			client.player.sendMessage(Text.literal("[SpearDebug] " + hand + " is empty."), false);
			return;
		}

		String itemId = Registries.ITEM.getId(stack.getItem()).toString();
		String displayName = stack.getName().getString();
		int count = stack.getCount();
		int maxDamage = stack.getMaxDamage();
		int damage = stack.isDamageable() ? stack.getDamage() : -1;

		NbtCompound tag = stack.getNbt();
		String nbtString = (tag != null) ? tag.toString() : "(no NBT)";

		// Pull out CustomModelData specifically if present, since that's
		// usually the key ViaBackwards/resource packs use to distinguish
		// downgraded future items from the real vanilla item.
		String customModelData = "(none)";
		if (tag != null && tag.contains("CustomModelData")) {
			customModelData = String.valueOf(tag.getInt("CustomModelData"));
		}

		StringBuilder sb = new StringBuilder();
		sb.append("==== SpearDebug: ").append(hand).append(" ====\n");
		sb.append("Item ID: ").append(itemId).append("\n");
		sb.append("Display Name: ").append(displayName).append("\n");
		sb.append("Count: ").append(count).append("\n");
		if (damage >= 0) {
			sb.append("Damage: ").append(damage).append(" / ").append(maxDamage).append("\n");
		}
		sb.append("CustomModelData: ").append(customModelData).append("\n");
		sb.append("Full NBT: ").append(nbtString);

		String result = sb.toString();

		// Log it (full detail, safe to copy from logs/latest.log)
		LOGGER.info("\n{}", result);

		// Also echo a compact version to chat so you don't have to alt-tab
		client.player.sendMessage(Text.literal("[SpearDebug] " + itemId
				+ " | CustomModelData=" + customModelData
				+ " | name=\"" + displayName + "\""), false);
		client.player.sendMessage(Text.literal("[SpearDebug] Full details printed to log (run/logs/latest.log)"), false);
	}
}
