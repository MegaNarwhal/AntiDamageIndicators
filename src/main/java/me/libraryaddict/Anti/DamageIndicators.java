package me.libraryaddict.Anti;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import org.bukkit.Bukkit;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class DamageIndicators extends JavaPlugin implements Listener{

	public static final Random rand = new Random();

	public void onEnable(){
		ProtocolLibrary.getProtocolManager().addPacketListener(
				new PacketAdapter(this,ListenerPriority.NORMAL,PacketType.Play.Server.ENTITY_METADATA){
					public void onPacketSending(PacketEvent event){
						Player observer = event.getPlayer();
						StructureModifier<Entity> entityModifer = event.getPacket().getEntityModifier(observer.getWorld());
						org.bukkit.entity.Entity entity = entityModifer.read(0);
						if(entity != null && observer != entity && entity instanceof LivingEntity
								&& !(entity instanceof EnderDragon || entity instanceof Wither)
								&& !(entity instanceof Tameable && ((Tameable)entity).getOwner() == observer)
								&& (entity.getPassengers().size() == 0 || entity.getPassengers().contains(observer))){
							event.setPacket(event.getPacket().deepClone());
							StructureModifier<List<WrappedWatchableObject>> watcher = event.getPacket()
									.getWatchableCollectionModifier();
							for(WrappedWatchableObject watch : watcher.read(0)){
								if(watch.getIndex() == 7 && ((Float)watch.getValue() > 0)){
									watch.setValue(rand.nextInt((int)((LivingEntity)entity).getMaxHealth()) + 1F);
/*												+ rand.nextFloat());*/
								}
							}
						}
					}
				});
		Bukkit.getPluginManager().registerEvents(this,this);
	}

	@EventHandler
	public void onMount(final VehicleEnterEvent event){
		if(!(event.getEntered() instanceof Player)) return;
		new BukkitRunnable(){
			public void run(){
				if(event.getVehicle().isValid() && event.getEntered().isValid())
					ProtocolLibrary.getProtocolManager().updateEntity(event.getVehicle(),
							Collections.singletonList((Player)event.getEntered()));
			}
		}.runTask(this);
	}

}
