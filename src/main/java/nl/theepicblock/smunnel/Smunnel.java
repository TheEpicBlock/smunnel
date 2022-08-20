package nl.theepicblock.smunnel;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import nl.theepicblock.smunnel.rendering.MainRenderManager;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.command.api.CommandRegistrationCallback;
import org.quiltmc.qsl.command.api.QuiltCommandRegistrationEnvironment;
import org.quiltmc.qsl.lifecycle.api.event.ServerLifecycleEvents;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;
import org.quiltmc.qsl.resource.loader.api.client.ClientResourceLoaderEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;


public class Smunnel implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("smunnel");
	public static final Identifier SYNC_PACKET = new Identifier("smunnel", "sync_smunnels");

	@Override
	public void onInitialize(ModContainer mod) {
		// Commands
		CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, environment) -> {
			dispatcher.register(literal("smunnel")
					.then(literal("list").executes(context -> {
						var holder = TunnelHolder.getFromPersistentState(context.getSource().getWorld());
						for (var tunnel : holder.tunnels) {
							context.getSource().sendFeedback(
									Text.literal(
											"("+tunnel.xMin()+", "+tunnel.yMin()+", "+tunnel.zMin()+")" +
											" -> " +
											"("+tunnel.xMax()+", "+tunnel.yMax()+", "+tunnel.zMax()+")" +
											"axis: " + tunnel.axis().getName() +
											" length: " + tunnel.targetLength()
									),
									false);
						}
						return Command.SINGLE_SUCCESS;
					}))
					.then(literal("add")
							.then(argument("xmin", IntegerArgumentType.integer())
							.then(argument("ymin", IntegerArgumentType.integer())
							.then(argument("zmin", IntegerArgumentType.integer())
							.then(argument("xmax", IntegerArgumentType.integer())
							.then(argument("ymax", IntegerArgumentType.integer())
							.then(argument("zmax", IntegerArgumentType.integer())
							.then(argument("direction", StringArgumentType.string())
							.then(argument("targetLength", FloatArgumentType.floatArg(0))
							.executes(ctx -> {
								var holder = TunnelHolder.getFromPersistentState(ctx.getSource().getWorld());
								holder.tunnels.add(new Tunnel(
										IntegerArgumentType.getInteger(ctx, "zmin"),
										IntegerArgumentType.getInteger(ctx, "zmax"),
										IntegerArgumentType.getInteger(ctx, "ymin"),
										IntegerArgumentType.getInteger(ctx, "ymax"),
										IntegerArgumentType.getInteger(ctx, "xmin"),
										IntegerArgumentType.getInteger(ctx, "xmax"),
										Direction.Axis.fromName(StringArgumentType.getString(ctx, "direction")),
										FloatArgumentType.getFloat(ctx, "targetLength")
								));
								holder.syncAll(ctx.getSource().getWorld());
								return Command.SINGLE_SUCCESS;
							}))))))))))
					.then(literal("remove")
							.then(argument("index", IntegerArgumentType.integer())
							.executes(ctx -> {
								var holder = TunnelHolder.getFromPersistentState(ctx.getSource().getWorld());
								holder.tunnels.remove(IntegerArgumentType.getInteger(ctx, "index"));
								holder.syncAll(ctx.getSource().getWorld());
								return Command.SINGLE_SUCCESS;
							}))));
		});
	}
}
