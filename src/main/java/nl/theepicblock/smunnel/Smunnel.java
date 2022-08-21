package nl.theepicblock.smunnel;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.command.CommandException;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.CoordinateArgument;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.command.api.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;


public class Smunnel implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("smunnel");
	public static final Identifier SYNC_PACKET = new Identifier("smunnel", "sync_smunnels");
	public static final boolean IRIS = QuiltLoader.isModLoaded("iris");

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
											"("+tunnel.xMax()+", "+tunnel.yMax()+", "+tunnel.zMax()+") " +
											"axis: " + tunnel.axis().getName() +
											" length: " + tunnel.targetLength()
									),
									false);
						}
						return Command.SINGLE_SUCCESS;
					}))
					.then(literal("add")
							.then(argument("from", BlockPosArgumentType.blockPos())
							.then(argument("to", BlockPosArgumentType.blockPos())
							.then(argument("direction", StringArgumentType.string())
							.then(argument("targetLength", FloatArgumentType.floatArg(0))
							.executes(ctx -> {
								if (FloatArgumentType.getFloat(ctx, "targetLength") == 0) {
									throw new CommandException(Text.literal("target length can't be 0"));
								}
								var holder = TunnelHolder.getFromPersistentState(ctx.getSource().getWorld());
								var a = BlockPosArgumentType.getBlockPos(ctx, "from");
								var b = BlockPosArgumentType.getBlockPos(ctx, "to");
								holder.tunnels.add(new Tunnel(
										Math.min(a.getZ(), b.getZ()),
										Math.max(a.getZ(), b.getZ())+1,
										Math.min(a.getY(), b.getY()),
										Math.max(a.getY(), b.getY())+1,
										Math.min(a.getX(), b.getX()),
										Math.max(a.getX(), b.getX())+1,
										Direction.Axis.fromName(StringArgumentType.getString(ctx, "direction")),
										FloatArgumentType.getFloat(ctx, "targetLength")
								));
								holder.syncAndMarkDirty(ctx.getSource().getWorld());
								return Command.SINGLE_SUCCESS;
							}))))))
					.then(literal("remove")
							.then(argument("index", IntegerArgumentType.integer())
							.executes(ctx -> {
								var holder = TunnelHolder.getFromPersistentState(ctx.getSource().getWorld());
								holder.tunnels.remove(IntegerArgumentType.getInteger(ctx, "index"));
								holder.syncAndMarkDirty(ctx.getSource().getWorld());
								return Command.SINGLE_SUCCESS;
							}))));
		});
	}
}
