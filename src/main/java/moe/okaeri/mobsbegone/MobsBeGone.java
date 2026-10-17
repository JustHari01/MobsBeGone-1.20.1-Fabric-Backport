package moe.okaeri.mobsbegone;

import moe.okaeri.mobsbegone.config.MobsBeGoneConfig;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class MobsBeGone implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("MobsBeGone");
	private static final ObjectArrayList<Entity> ENTITY_BUFFER = new ObjectArrayList<>(64);
	private static long[] blacklist;

	@Override
	public void onInitialize() {
		ServerEntityEvents.ENTITY_LOAD.register(this::onEntityLoad);
		ServerTickEvents.END_SERVER_TICK.register(this::onEndServerTick);
		ServerLifecycleEvents.SERVER_STARTING.register(this::onServerStarting);
	}

	private void onServerStarting(MinecraftServer minecraftServer) {
		ENTITY_BUFFER.clear();
		blacklist = MobsBeGoneConfig.loadBlacklist();

		LOGGER.info("MobsBeGone >> Up and running!");
	}

	private void onEntityLoad(Entity entity, ServerWorld world) {
		if (entity instanceof LivingEntity && isEntityBlacklisted(entity.getType())) {
			ENTITY_BUFFER.add(entity);
		}
	}

	private void onEndServerTick(MinecraftServer minecraftServer) {
		for (int i = ENTITY_BUFFER.size(); i-- > 0;) {
			Entity e = ENTITY_BUFFER.get(i);
			if (!e.isRemoved()) e.discard();
		}

		ENTITY_BUFFER.clear();
	}

	public static boolean isEntityBlacklisted(EntityType<?> entityType) {
		int raw = Registries.ENTITY_TYPE.getRawId(entityType);
		long[] activeBlacklist = blacklist;
		return raw >= 0 && activeBlacklist != null && (raw >>> 6) < activeBlacklist.length
				&& (((activeBlacklist[raw >>> 6] >>> (raw & 63)) & 1L) != 0);
	}
}
