package be.florens.craftql.mixin;

import be.florens.craftql.mapping.RemappedFieldResolverScannerKt;
import graphql.kickstart.tools.resolver.FieldResolverScanner;
import graphql.kickstart.tools.util.UtilsKt;
import graphql.language.FieldDefinition;
import graphql.language.Type;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/**
 * This mixin into graphql-java-tools makes it so fields and methods mapped as intermediary names can be resolved.<br>
 *
 * Conveniently, this mixin won't apply in a dev environment (read: where we don't need it) because it isn't passed
 * through the mixin transformer.
 */
@SuppressWarnings("KotlinInternalInJava")
@Mixin(value = FieldResolverScanner.class, remap = false)
public abstract class FieldResolverScannerMixin {

    @Shadow protected abstract boolean verifyMethodArguments(Method method, int requiredCount, FieldResolverScanner.Search search);
    @Shadow protected abstract boolean isBoolean(Type<?> type);

    @Inject(method = "findResolverMethod", locals = LocalCapture.CAPTURE_FAILHARD, at = @At("RETURN"), cancellable = true)
    private void resolveRemappedMethods(FieldDefinition field, FieldResolverScanner.Search search, CallbackInfoReturnable<Method> cir, List<Method> methods, int argCount) {
        if (cir.getReturnValue() == null) {
            Method resolvedMethod = RemappedFieldResolverScannerKt.findResolverMethod(
                    field,
                    method -> verifyMethodArguments(method, argCount, search),
                    this::isBoolean,
                    UtilsKt::snakeToCamelCase,
                    methods
            );

            if (resolvedMethod != null) {
                cir.setReturnValue(resolvedMethod);
            }
        }
    }

    @Inject(method = "findResolverProperty", at = @At("RETURN"), cancellable = true)
    private void resolveRemappedFields(FieldDefinition field, FieldResolverScanner.Search search, CallbackInfoReturnable<Field> cir) {
        if (cir.getReturnValue() == null && search.getType() instanceof Class) {
            Field[] allFields = FieldUtils.getAllFields(UtilsKt.unwrap(search.getType()));
            Field resolvedField = RemappedFieldResolverScannerKt.findResolverProperty(field, allFields);

            if (resolvedField != null) {
                cir.setReturnValue(resolvedField);
            }
        }
    }
}
