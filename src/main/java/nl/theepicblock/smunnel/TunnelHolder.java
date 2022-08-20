package nl.theepicblock.smunnel;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.world.PersistentState;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.qsl.lifecycle.api.client.event.ClientLifecycleEvents;
import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;

import java.util.ArrayList;

public class TunnelHolder extends PersistentState {
	public ArrayList<Tunnel> tunnels = new ArrayList<>();
	private static Identifier SYNC_PACKET = new Identifier("smunnel", "sync_tunnels");

	public static TunnelHolder fromNbt(NbtCompound nbt) {
		var list = nbt.getList("list", NbtElement.COMPOUND_TYPE);
		var holder = new TunnelHolder();
		for (var element : list) {
			var compound = (NbtCompound)element;
			holder.tunnels.add(new Tunnel(
					compound.getInt("zMin"),
					compound.getInt("zMax"),
					compound.getInt("yMin"),
					compound.getInt("yMax"),
					compound.getInt("xMin"),
					compound.getInt("xMax"),
					Direction.Axis.fromName(compound.getString("axis")),
					compound.getFloat("targetLength")
			));
		}
		return holder;
	}

	@Override
	public NbtCompound writeNbt(NbtCompound nbt) {
		var list = new NbtList();
		for (var tunnel : tunnels) {
			var compound = new NbtCompound();
			compound.putInt("zMin", tunnel.zMin());
			compound.putInt("zMax", tunnel.zMax());
			compound.putInt("yMin", tunnel.yMin());
			compound.putInt("yMax", tunnel.yMax());
			compound.putInt("xMin", tunnel.xMin());
			compound.putInt("xMax", tunnel.xMax());
			compound.putString("axis", tunnel.axis().getName());
			compound.putFloat("targetLength", tunnel.targetLength());
			list.add(compound);
		}
		nbt.put("list", list);
		return nbt;
	}

	public void importFromPacket(PacketByteBuf buf) {
		this.tunnels.clear();
		this.tunnels.addAll(buf.readList(Tunnel::fromPacket));
	}

	public void writeToBuf(PacketByteBuf buf) {
		buf.writeCollection(this.tunnels, (buf1, tunnel) -> tunnel.writePacket(buf1));
	}

	public void syncAll(ServerWorld world) {
		var buf = PacketByteBufs.create();
		this.writeToBuf(buf);
		var packet = ServerPlayNetworking.createS2CPacket(Smunnel.SYNC_PACKET, buf);
		world.getPlayers().forEach(player -> player.networkHandler.sendPacket(packet));
	}

	@NotNull
	public static TunnelHolder getFromPersistentState(ServerWorld world) {
		return world.getPersistentStateManager().getOrCreate(TunnelHolder::fromNbt, TunnelHolder::new, "smunnels");
	}
}
