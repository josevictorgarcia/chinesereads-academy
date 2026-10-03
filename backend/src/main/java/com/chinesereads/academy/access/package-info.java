/**
 * Frontera saliente con ChineseReads: concede o retira el premium de un alumno por asiento llamando al endpoint
 * interno del proyecto matriz (contrato en docs/integracion-chinesereads/CONTRACT.md §3). Escucha los eventos de
 * classroom, registra cada intento en premium_grant y reintenta los fallidos.
 */
@org.springframework.modulith.ApplicationModule(allowedDependencies = {"classroom", "billing", "identity", "shared"})
package com.chinesereads.academy.access;
