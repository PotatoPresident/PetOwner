package us.potatoboy.petowner.mixin;

import net.minecraft.entity.LazyEntityReference;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.FoxEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.stream.Stream;

@Mixin(FoxEntity.class)
public interface FoxTrustedInvoker {
	@Invoker("getTrustedEntities")
	Stream<LazyEntityReference<LivingEntity>> invokeGetTrustedEntities();
}
