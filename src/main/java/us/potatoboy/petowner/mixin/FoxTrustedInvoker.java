package us.potatoboy.petowner.mixin;

import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.fox.Fox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.stream.Stream;

@Mixin(Fox.class)
public interface FoxTrustedInvoker {
	@Invoker("getTrustedEntities")
	Stream<EntityReference<LivingEntity>> invokeGetTrustedEntities();
}
