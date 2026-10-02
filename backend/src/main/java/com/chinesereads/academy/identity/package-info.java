/**
 * Identidad: verifica la sesión compartida de ChineseReads (cookie JWT), lee los usuarios del esquema
 * matriz en modo solo lectura, expone el usuario actual y gestiona el perfil de profesor.
 * Otros módulos aportan roles mediante {@link com.chinesereads.academy.identity.RoleContributor}.
 */
@org.springframework.modulith.ApplicationModule(allowedDependencies = {"shared"})
package com.chinesereads.academy.identity;
