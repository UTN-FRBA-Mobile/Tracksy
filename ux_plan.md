# Tracksy UX/UI Alignment Plan

## Objetivo

Alinear la UI/UX de toda la aplicacion con el lenguaje visual ya definido en los flujos de autenticacion: bienvenida, login, crear cuenta, recuperar contrasena y verificacion de email.

La fuente de verdad visual inicial son los componentes existentes en:

- `tracksy-mobile/app/src/main/java/com/example/tracksy/ui/auth/AuthComponents.kt`
- `tracksy-mobile/app/src/main/java/com/example/tracksy/ui/auth/AuthScreens.kt`
- `tracksy-mobile/app/src/main/java/com/example/tracksy/ui/theme/Color.kt`

El foco principal es normalizar botones, estados habilitado/deshabilitado, acciones secundarias, acciones destructivas, campos de texto, dialogos y consistencia general de jerarquia visual.

## Fuente de verdad actual

### Boton primario

Referencia: `TracksyPrimaryButton`.

Propiedades:

- Alto: `44.dp`
- Shape: `RoundedCornerShape(25.dp)`
- Sombra: `2.dp`
- Fondo habilitado: `TracksyPrimaryPurple`
- Texto habilitado: `Color.White`
- Fondo deshabilitado: `TracksyDisabledButtonBackground`
- Texto deshabilitado: `TracksyDisabledButtonText`
- Texto: peso `FontWeight.Medium`, tamano `18.sp`

Uso esperado:

- Acciones principales.
- Confirmaciones.
- Guardar cambios.
- Crear cuenta.
- Iniciar sesion.
- Enviar instrucciones.
- Finalizar una accion importante.

### Boton secundario

Referencia: `TracksySecondaryButton`.

Propiedades:

- Alto: `44.dp`
- Shape: `RoundedCornerShape(25.dp)`
- Sombra: `2.dp`
- Fondo: `Color.White.copy(alpha = 0.94f)`
- Borde: `TracksyBorderSoft`
- Texto: `TracksyPrimaryPurple`

Uso esperado:

- Acciones alternativas.
- Cancelar sin ser destructivo.
- Volver a una pantalla anterior cuando aparece junto a una accion primaria.
- Acciones de menor jerarquia.

### Link textual

Referencias:

- `TracksyLinkText`
- `TracksyInlineLink`

Propiedades:

- Texto principal: `TracksyTextSecondary`
- Link: `TracksyPrimaryPurple`
- Peso link: `FontWeight.SemiBold`
- Sin contenedor visual.

Uso esperado:

- Navegacion secundaria.
- Cambiar foto si no se quiere presentar como boton.
- Acciones de baja friccion.

### Campo de texto

Referencia: `TracksyTextField`.

Propiedades:

- Alto: `44.dp`
- Shape: `RoundedCornerShape(10.dp)`
- Fondo: `Color.White`
- Borde normal: `TracksyBorderSoft.copy(alpha = 0.35f)`
- Borde error: `TracksyErrorRed`
- Placeholder: `TracksyPlaceholder`
- Texto: `TracksyTextPrimary`
- Label flotante cuando tiene foco o contenido.
- Sombra: `2.dp`

Uso esperado:

- Formularios de perfil.
- Cambio de contrasena.
- Busquedas si se decide unificar estilos.
- Inputs en dialogos.

## Decisiones de sistema

1. La app debe tener una unica familia de botones compartidos.
2. Los botones principales de la app no deben usar directamente `colors.primary`, `colors.titleText` ni colores locales cuando representan una accion primaria.
3. El estado deshabilitado debe ser visualmente consistente en toda la app.
4. Las acciones destructivas deben tener una variante propia y no reutilizar el boton primario.
5. Los `Button`, `OutlinedButton` y `TextButton` nativos de Material solo deben quedar cuando representen icon buttons, toggles o casos justificados.
6. Los dialogos deben usar los mismos roles visuales: primaria, secundaria y destructiva.
7. La implementacion debe ser incremental por pantalla para controlar regresiones.

## Componentes compartidos a crear

Crear un archivo nuevo sugerido:

`tracksy-mobile/app/src/main/java/com/example/tracksy/ui/components/TracksyButtons.kt`

Componentes:

- `TracksyPrimaryButton`
- `TracksySecondaryButton`
- `TracksyDestructiveButton`
- `TracksyTextAction`

Crear otro archivo sugerido:

`tracksy-mobile/app/src/main/java/com/example/tracksy/ui/components/TracksyFields.kt`

Componentes:

- `TracksyTextField`
- `TracksyPasswordField`, si se decide reutilizar el patron de auth fuera del flujo de autenticacion.

Notas de implementacion:

- Extraer primero la logica visual desde `ui.auth.AuthComponents`.
- Mantener wrappers temporales en `AuthComponents.kt` para no romper auth durante la migracion.
- Auth debe seguir viendose igual despues de extraer los componentes.
- La app no debe duplicar nombres ambiguos en paquetes distintos a largo plazo. Si temporalmente existen dos `TracksyPrimaryButton`, resolver con imports claros y luego consolidar.

## Paso 1: Actualizar Perfil completo

Este paso es el primero a implementar. Debe dejar toda la seccion de perfil alineada con auth.

### Archivos involucrados

- `tracksy-mobile/app/src/main/java/com/example/tracksy/ui/profile/ProfileScreen.kt`
- `tracksy-mobile/app/src/main/java/com/example/tracksy/ui/profile/ProfileEditScreen.kt`
- `tracksy-mobile/app/src/main/java/com/example/tracksy/ui/profile/ChangePasswordScreen.kt`
- `tracksy-mobile/app/src/main/java/com/example/tracksy/ui/auth/AuthComponents.kt`
- `tracksy-mobile/app/src/main/java/com/example/tracksy/ui/components/TracksyButtons.kt`, nuevo
- `tracksy-mobile/app/src/main/java/com/example/tracksy/ui/components/TracksyFields.kt`, nuevo si aplica

### Perfil principal

Pantalla: `PerfilScreen`.

Cambios:

- Revisar el boton `Cerrar sesion`.
- Mantenerlo como accion destructiva, no primaria.
- Reemplazar `OutlinedButton` manual por `TracksyDestructiveButton` o una variante destructiva secundaria.
- Usar color de error para texto y borde, pero respetando altura, shape y peso del sistema.
- Mantener la jerarquia visual: las filas de navegacion siguen siendo filas, no botones primarios.

Criterio:

- `Cerrar sesion` debe verse distinto de una accion primaria.
- No debe competir visualmente con acciones de guardar o confirmar.
- Debe tener altura y radio consistentes con el sistema.

### Editar perfil

Pantalla: `ProfileEditScreen`.

Cambios:

- Reemplazar el boton manual `Guardar cambios` por `TracksyPrimaryButton`.
- El boton debe usar:
  - habilitado: `TracksyPrimaryPurple`
  - deshabilitado: `TracksyDisabledButtonBackground`
  - texto deshabilitado: `TracksyDisabledButtonText`
- Mantener `canSave = nombre.isNotBlank() && hasChanges`.
- Si no hay cambios, el boton debe verse deshabilitado.
- Si el nombre queda vacio, el boton debe verse deshabilitado.
- Evaluar mostrar feedback de campo requerido si el usuario borra el nombre y sale del campo.
- Reemplazar `TextButton` de `Cambiar foto` por:
  - `TracksyTextAction`, si se quiere mantener como link accion.
  - o `TracksySecondaryButton` compacto, si se quiere mayor affordance.
- Reemplazar `PerfilTextField` por el `TracksyTextField` compartido o ajustar sus tokens para que coincida con auth:
  - alto `44.dp`
  - fondo blanco
  - borde suave
  - shape `10.dp`
  - sombra `2.dp`
  - placeholder `TracksyPlaceholder`
  - texto `TracksyTextPrimary`
- El campo email deshabilitado debe verse no editable sin parecer error.

Criterio:

- La pantalla debe sentirse como una continuacion del formulario de crear cuenta.
- Guardar cambios debe comunicar claramente cuando esta disponible y cuando no.
- Cambiar foto no debe verse como accion primaria.

### Cambiar contrasena

Pantalla: `ChangePasswordScreen`.

Cambios:

- Reemplazar botones manuales por `TracksyPrimaryButton`.
- Usar estado deshabilitado cuando:
  - falte contrasena actual
  - falte nueva contrasena
  - falte confirmacion
  - la nueva contrasena no cumpla validaciones
  - las contrasenas no coincidan
  - exista una operacion en curso
- Alinear campos de contrasena con `TracksyPasswordField` o sus tokens visuales.
- Alinear mensajes de error con `ErrorMessage` de auth.
- Mantener icono de visibilidad con el estilo violeta de auth cuando sea posible.

Criterio:

- Debe reutilizar el mismo lenguaje de validacion de crear cuenta.
- El boton no debe verse habilitado si la accion no puede ejecutarse.
- Los errores deben aparecer cerca del campo que los genera.

### Dialogos de perfil

Si existen o se agregan dialogos en el flujo de perfil:

- Confirmar: accion primaria.
- Cancelar: accion secundaria o textual.
- Destructivo: variante destructiva.

Ejemplos:

- Confirmar cierre de sesion, si se agrega.
- Descartar cambios al volver, si se agrega.
- Error de guardado, si se agrega.

### Verificacion del Paso 1

Comandos:

```bash
cd tracksy-mobile
./gradlew :app:compileDebugKotlin
```

Busqueda posterior:

```bash
rg -n "ButtonDefaults.buttonColors|OutlinedButton\\(|TextButton\\(|Button\\(" app/src/main/java/com/example/tracksy/ui/profile
```

Criterios de aceptacion:

- Perfil compila.
- `Guardar cambios` usa el boton primario compartido.
- `Guardar cambios` se ve deshabilitado con los colores de auth cuando no hay cambios o el nombre es invalido.
- `Cambiar foto` no usa colores Material por defecto.
- `Cambiar contrasena` respeta los mismos estados habilitado/deshabilitado.
- `Cerrar sesion` usa variante destructiva consistente.
- No quedan botones manuales injustificados en `ui/profile`.

## Paso 2: Consolidar auth con componentes compartidos

Objetivo:

Evitar que auth siga siendo una fuente visual aislada despues de extraer los componentes.

Cambios:

- Hacer que `AuthComponents.kt` delegue en los componentes compartidos.
- Mantener compatibilidad de nombres si hay muchas referencias internas.
- Confirmar que login, crear cuenta, recuperar contrasena y verificar email no cambian visualmente.

Criterios:

- Auth debe verse igual que antes.
- Los componentes compartidos deben representar exactamente el patron de auth.

## Paso 3: Actualizar Listas

Pantallas:

- `MyListsScreen.kt`
- `ListEditScreen.kt`
- `ListDetailScreen.kt`

Cambios:

- Boton crear lista: revisar si debe seguir como FAB circular o adoptar variante primaria segun contexto.
- Confirmar/guardar lista: `TracksyPrimaryButton`.
- Cancelar: `TracksySecondaryButton` o `TracksyTextAction`.
- Editar y comparar: si aparecen juntos, definir jerarquia:
  - accion principal del flujo: primaria
  - accion alternativa: secundaria
- Finalizar compra: primaria.
- Eliminar lista: destructiva.
- Dialogo de eliminar lista:
  - confirmar eliminacion: destructivo
  - cancelar: secundario/textual

Criterios:

- No usar `colors.titleText` como color de boton primario.
- No usar `colors.primary` para botones principales si contradice `TracksyPrimaryPurple`.
- Estados disabled visibles cuando una lista no tiene nombre o no hay cambios validos.

## Paso 4: Actualizar Productos

Pantallas:

- `ProductsScreen.kt`
- `ProductDetailScreen.kt`

Cambios:

- Acciones de sugerencias en home/productos:
  - agregar: primaria compacta o accion de bajo impacto con color primario.
  - descartar: secundaria/textual.
- En detalle de producto:
  - confirmar cambios: `TracksyPrimaryButton`.
  - crear nueva lista: secundaria.
  - dialogos de salir sin guardar y confirmar cambios con roles consistentes.
- Controles de cantidad:
  - mantener formato compacto.
  - revisar color activo/inactivo.

Criterios:

- Confirmar cambios debe deshabilitarse o perder jerarquia cuando no hay cambios.
- Nueva lista no debe competir visualmente con confirmar cambios.

## Paso 5: Actualizar Checkout

Pantalla:

- `FinalizarCompraScreen.kt`

Cambios:

- Finalizar compra: primaria.
- Crear lista con pendientes: secundaria.
- Dialogo para nombre de lista:
  - confirmar: primaria si el nombre es valido.
  - cancelar: secundaria/textual.
- Estados disabled si el nombre esta vacio o no hay accion valida.

Criterios:

- La accion de finalizar debe ser la mas prominente.
- Crear lista de pendientes debe verse alternativa, no equivalente.

## Paso 6: Actualizar Historial

Pantallas:

- `HistoryScreen.kt`
- `HistoryDetailScreen.kt`
- `HistoryComponents.kt`, si queda en uso.

Cambios:

- `Reutilizar lista`: primaria si es accion principal de la pantalla.
- Si la accion no esta implementada, decidir:
  - ocultarla
  - deshabilitarla visualmente con colores disabled
  - implementar la accion
- Remover top bars o componentes viejos no usados si generan estilos alternativos.

Criterios:

- No debe haber botones activos sin comportamiento real.
- Historial debe compartir el mismo sistema de accion primaria.

## Paso 7: Actualizar Scanner

Pantalla:

- `BarcodeScannerScreen.kt`

Cambios:

- Permitir camara/reintentar: primaria.
- Cancelar/cerrar: icon button o secundaria.
- Mensajes de permiso deben usar jerarquia similar a auth.

Criterios:

- El usuario debe identificar rapidamente la accion recomendada.
- No mezclar botones grandes de distinto color para acciones equivalentes.

## Paso 8: Actualizar Home y acciones compactas

Pantalla:

- `HomeScreen.kt`

Cambios:

- Sugerencias:
  - `Agregar`: boton compacto primario.
  - `No`: accion secundaria compacta.
- Evaluar crear versiones compactas:
  - `TracksyPrimaryButtonSmall`
  - `TracksySecondaryButtonSmall`

Criterios:

- Las acciones compactas deben usar los mismos colores aunque tengan menor alto.
- No deben romper el layout de cards.

## Paso 9: Unificar dialogos

Buscar:

```bash
rg -n "AlertDialog|TextButton\\(" tracksy-mobile/app/src/main/java/com/example/tracksy
```

Cambios:

- Crear helpers si conviene:
  - `TracksyDialogPrimaryAction`
  - `TracksyDialogSecondaryAction`
  - `TracksyDialogDestructiveAction`
- Reemplazar textos genericos por roles consistentes.

Criterios:

- En todo dialogo debe quedar claro cual es la accion principal.
- Cancelar no debe verse igual que confirmar.
- Eliminar/cerrar sesion debe verse destructivo.

## Paso 10: Auditoria final de botones

Busqueda:

```bash
rg -n "Button\\(|OutlinedButton\\(|TextButton\\(|ButtonDefaults" tracksy-mobile/app/src/main/java/com/example/tracksy
```

Clasificar cada resultado:

- Migrado a componente compartido.
- Justificado como icon button o control especifico.
- Pendiente.

Criterios:

- No quedan botones principales con colores locales.
- No quedan estados disabled sin colores de auth.
- No quedan acciones destructivas presentadas como primarias.

## Paso 11: Auditoria final de formularios

Pantallas con formularios:

- Login
- Crear cuenta
- Recuperar contrasena
- Editar perfil
- Cambiar contrasena
- Crear/editar lista
- Crear lista con pendientes
- Busquedas principales

Cambios:

- Unificar campos donde tenga sentido.
- Mantener excepciones si una busqueda necesita otro patron, pero documentarlas.
- Validar error, foco, placeholder y disabled.

Criterios:

- Los formularios importantes comparten altura, shape, borde y comportamiento de error.
- Los campos deshabilitados no parecen rotos ni errores.

## Paso 12: Verificacion tecnica final

Comandos:

```bash
cd tracksy-mobile
./gradlew :app:compileDebugKotlin
```

Si se agregan tests o previews, ejecutar tambien:

```bash
./gradlew :app:testDebugUnitTest
```

Criterios:

- Build exitoso.
- Sin imports muertos.
- Sin duplicacion innecesaria de componentes visuales.

## Paso 13: Verificacion visual final

Recorrer manualmente:

- Welcome
- Login
- Crear cuenta
- Recuperar contrasena
- Verificar email
- Perfil
- Editar perfil
- Cambiar contrasena
- Inicio
- Mis listas
- Crear lista
- Editar lista
- Detalle lista
- Productos
- Detalle producto
- Scanner
- Finalizar compra
- Historial
- Detalle historial

Checklist:

- El boton principal se identifica igual en toda la app.
- El estado disabled es consistente.
- Las acciones secundarias no compiten con la primaria.
- Las acciones destructivas estan diferenciadas.
- No hay botones con texto cortado.
- No hay cambios de altura inesperados entre pantallas similares.
- La app se percibe como un sistema unico.

## Orden recomendado de implementacion

1. Perfil completo.
2. Extraccion/consolidacion de componentes compartidos con auth.
3. Listas.
4. Productos.
5. Checkout.
6. Historial.
7. Scanner.
8. Home y acciones compactas.
9. Dialogos globales.
10. Auditoria final.

## Notas de riesgo

- Auth ya tiene un estilo fuerte. Extraer componentes sin cambiar su apariencia requiere cuidado.
- La app usa `LocalTracksyColors` para tema claro/oscuro, mientras auth usa tokens directos. Hay que decidir si los botones compartidos mantienen tokens directos de auth o si se agregan esos valores a `TracksyColors`.
- El modo oscuro puede necesitar variantes propias para botones compartidos. Si se mantiene `TracksyPrimaryPurple` directo, validar contraste sobre fondos oscuros.
- Cambiar altura de botones de `52.dp` a `44.dp` puede alterar la densidad de algunas pantallas. Si se decide preservar `52.dp` en pantallas internas, documentar una variante `large`, pero mantener colores y estados de auth.
- Algunos botones actuales pueden estar activos aunque su accion no este implementada. Esos casos deben corregirse, no solo restilizarse.
