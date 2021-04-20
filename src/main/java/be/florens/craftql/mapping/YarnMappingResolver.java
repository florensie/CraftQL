package be.florens.craftql.mapping;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import net.fabricmc.mapping.tree.TinyMappingFactory;
import net.fabricmc.mapping.tree.TinyTree;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.MethodUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.function.Supplier;

public class YarnMappingResolver {
    private static final String CURRENT_NAMESPACE = FabricLoader.getInstance().getMappingResolver()
            .getCurrentRuntimeNamespace();
    private static YarnMappingResolver instance;
    private final MappingResolver mappingResolver;

    private YarnMappingResolver() {
        // Parse the mappings
        Path mappingsPath = FabricLoader.getInstance().getModContainer("net_fabricmc_yarn")
                .orElseThrow(() -> new RuntimeException("Yarn Jar-in-Jar was not loaded"))
                .getPath("mappings/mappings.tiny");

        TinyTree tinyTree;
        try {
            BufferedReader mappingsReader = Files.newBufferedReader(mappingsPath);
            tinyTree = TinyMappingFactory.loadWithDetection(mappingsReader);
        } catch (IOException e) {
            throw new RuntimeException("Yarn mappings could not be read", e);
        }

        // Create MappingResolver
        try {
            // Get FabricMappingResolver constructor
            Class<?> clazz = Class.forName("net.fabricmc.loader.FabricMappingResolver");
            Constructor<?> constructor = clazz.getDeclaredConstructor(Supplier.class, String.class);
            constructor.setAccessible(true);

            // Create a new instance
            mappingResolver = (MappingResolver) constructor.newInstance((Supplier<TinyTree>) () -> tinyTree, "named");
        } catch (Exception e) {
            throw new RuntimeException("Failed to create FabricMappingResolver", e);
        }
    }

    public static YarnMappingResolver getInstance() {
        if (instance == null) {
            instance = new YarnMappingResolver();
        }
        return instance;
    }

    public String mapField(Field field) {
        // TODO: do we need to check the hierarchy here?

        // Get method field meta as strings
        String owner = field.getDeclaringClass().getName();
        String name = field.getName();
        String descriptor = org.objectweb.asm.Type.getDescriptor(field.getType());

        // Remap
        return this.mappingResolver.mapFieldName(CURRENT_NAMESPACE, owner, name, descriptor);
    }

    public String mapMethod(Method method) {
        // Make sure we have the root method first
        LinkedHashSet<Method> overrideHierarchy = (LinkedHashSet<Method>) MethodUtils.getOverrideHierarchy(method, ClassUtils.Interfaces.INCLUDE);
        method = (Method) overrideHierarchy.toArray()[overrideHierarchy.size() - 1];

        // Get method meta as strings
        String owner = method.getDeclaringClass().getName();
        String name = method.getName();
        String descriptor = org.objectweb.asm.Type.getMethodDescriptor(method);

        // Remap
        return this.mappingResolver.mapMethodName(CURRENT_NAMESPACE, owner, name, descriptor);
    }
}
