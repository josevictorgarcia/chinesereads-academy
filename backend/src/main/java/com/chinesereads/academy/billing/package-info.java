/**
 * Facturación: suscripción del profesor (TRIAL al alta; Stripe en un plan posterior) y cuota de asientos.
 * No conoce a classroom: expone la cuota y escucha eventos.
 */
@org.springframework.modulith.ApplicationModule(allowedDependencies = {"identity", "shared"})
package com.chinesereads.academy.billing;
