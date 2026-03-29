package dev.sbs.serverapi.security;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation requiring API key authentication on a controller class or handler method.
 *
 * <p>When applied to a class, all handler methods in that class require a valid API key.
 * When applied to a method, only that method is protected. Method-level annotations
 * take precedence over class-level annotations.</p>
 *
 * <p>If {@link #requiredPermissions()} is non-empty, the API key must hold at least
 * one of the specified {@link ApiKeyRole} roles (any-match semantics).</p>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface ApiKeyProtected {

    /**
     * Access roles required to use the annotated endpoint.
     *
     * <p>If empty, only a valid API key is required. If multiple values are provided,
     * the API key must hold at least one (any-match semantics).</p>
     *
     * @return an array of required access roles
     */
    @NotNull ApiKeyRole[] requiredPermissions() default {};

}
