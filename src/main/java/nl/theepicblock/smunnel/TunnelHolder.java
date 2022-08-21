package nl.theepicblock.smunnel;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.PersistentState;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class TunnelHolder extends PersistentState {
	public List<Tunnel> tunnels;

	public TunnelHolder(boolean concurrent) {
		if (concurrent) {
			tunnels = new CopyOnWriteArrayList<>();
		} else {
			tunnels = new ArrayList<>();
		}
	}

	// Serialization code

	public static TunnelHolder fromNbt(NbtCompound nbt) {
		var list = nbt.getList("list", NbtElement.COMPOUND_TYPE);
		var holder = new TunnelHolder(false);
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

	public static List<Tunnel> readPacket(PacketByteBuf buf) {
		return buf.readList(Tunnel::fromPacket);
	}

	public void importFromPacket(List<Tunnel> packet) {
		this.tunnels.clear();
		this.tunnels.addAll(packet);
	}

	public void writeToBuf(PacketByteBuf buf) {
		buf.writeCollection(this.tunnels, (buf1, tunnel) -> tunnel.writePacket(buf1));
	}

	// End serialization

	public void syncAndMarkDirty(ServerWorld world) {
		this.setDirty(true);
		var buf = PacketByteBufs.create();
		this.writeToBuf(buf);
		ServerPlayNetworking.send(world.getPlayers(), Smunnel.SYNC_PACKET, buf);
	}

	public Vec3d rayToWorldSpace(Vec3d source, Vec3d ray) {
		for (var tunnel : tunnels) {
			ray = tunnel.rayToWorldSpace(source, ray);
		}
		return ray;
	}

	public Vec3d rayToIllusionSpace(Vec3d source, Vec3d ray) {
		for (var tunnel : tunnels) {
			ray = tunnel.rayToIllusionSpace(source, ray);
		}
		return ray;
	}

	@NotNull
	public static TunnelHolder getFromPersistentState(ServerWorld world) {
		return world.getPersistentStateManager().getOrCreate(TunnelHolder::fromNbt, () -> new TunnelHolder(false), "smunnels");
	}
}
