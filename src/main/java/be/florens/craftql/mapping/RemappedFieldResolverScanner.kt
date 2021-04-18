package be.florens.craftql.mapping

import graphql.language.FieldDefinition
import graphql.language.Type
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.util.function.Function
import java.util.function.Predicate

/**
 * Copy of FieldResolverScanner.findResolverMethod, modified to handle remapping.
 * Also checks the method arguments before checking the name to avoid unnecessary mapping lookups.<br>
 *
 * The following arguments are passed
 *
 * @param verifyMethodArgumentsAccess
 * @param isBooleanAccess
 * @param snakeToCammelCaseAccess
 *
 * @see graphql.kickstart.tools.resolver.FieldResolverScanner.findResolverMethod
 */
fun findResolverMethod(field: FieldDefinition, verifyMethodArgumentsAccess: Predicate<Method>,
                       isBooleanAccess: Predicate<Type<*>>, snakeToCammelCaseAccess: Function<String, String>,
                       methods: List<Method>): Method? {
    val name = field.name
    val mappingResolver = YarnMappingResolver.getInstance()

    // Check for the following one by one:
    //   1. Method with exact field name
    //   2. Method that returns a boolean with "is" style getter
    //   3. Method with "get" style getter
    //   4. Method with "getField" style getter
    //   5. Method with "get" style getter with the field name converted from snake_case to camelCased. ex: key_ops -> getKeyOps()
    return methods.find {
        verifyMethodArgumentsAccess.test(it) && mappingResolver.mapMethod(it) == name
    } ?: methods.find {
        verifyMethodArgumentsAccess.test(it) && (isBooleanAccess.test(field.type) && mappingResolver.mapMethod(it) == "is${name.capitalize()}")
    } ?: methods.find {
        verifyMethodArgumentsAccess.test(it) && mappingResolver.mapMethod(it) == "get${name.capitalize()}"
    } ?: methods.find {
        verifyMethodArgumentsAccess.test(it) && mappingResolver.mapMethod(it) == "getField${name.capitalize()}"
    } ?: methods.find {
        verifyMethodArgumentsAccess.test(it) && mappingResolver.mapMethod(it) == "get${snakeToCammelCaseAccess.apply(name)}"
    }
}

/**
 * Copy of FieldResolverScanner.findResolverProperty, modified to handle remapping.
 *
 * @see graphql.kickstart.tools.resolver.FieldResolverScanner.findResolverProperty
 */
fun findResolverProperty(field: FieldDefinition, fields: Array<Field>) =
        fields.find { YarnMappingResolver.getInstance().mapField(it) == field.name }
