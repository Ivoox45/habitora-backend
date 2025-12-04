# WhatsApp Service - Configuración Opcional

## Cambios Realizados

El servicio de WhatsApp ahora es **opcional** y puede ser desactivado en entornos donde no se tienen las credenciales configuradas.

### Archivos Modificados

1. **WhatsAppService.java**
   - Agregado `@ConditionalOnProperty(name = "whatsapp.enabled", havingValue = "true", matchIfMissing = false)`
   - El servicio solo se inicializa si `whatsapp.enabled=true`

2. **RecordatorioService.java**
   - WhatsAppService ahora es opcional (`@Autowired(required = false)`)
   - Todos los métodos verifican si `whatsAppService != null` antes de usarlo
   - Si WhatsApp no está disponible, usa formateo manual de teléfono: `+51` + número

3. **application-prod.properties**
   - `whatsapp.enabled=false` (desactivado por defecto)
   - Las variables de entorno tienen valores por defecto vacíos para evitar errores

4. **application-local.properties**
   - `whatsapp.enabled=true` (activado para desarrollo local)

## Configuración en Railway

### Variables de Entorno Requeridas

Para **desactivar** WhatsApp (recomendado inicialmente):
```
whatsapp.enabled=false
```

Para **activar** WhatsApp cuando tengas las credenciales:
```
whatsapp.enabled=true
WHATSAPP_PHONE_NUMBER_ID=tu_phone_number_id
WHATSAPP_WABA_ID=tu_waba_id
WHATSAPP_ACCESS_TOKEN=tu_access_token
```

## Comportamiento

### WhatsApp Desactivado (`whatsapp.enabled=false`)
- ✅ La aplicación inicia correctamente
- ✅ Los recordatorios se crean y se guardan en la BD
- ✅ Los teléfonos se formatean manualmente (+51 + número)
- ⚠️ Los recordatorios se marcan como FALLIDO al intentar enviarlos
- ⚠️ No se envían mensajes de WhatsApp

### WhatsApp Activado (`whatsapp.enabled=true`)
- ✅ Se inicializa WhatsAppService
- ✅ Se formatean números usando el servicio
- ✅ Se envían mensajes realmente por WhatsApp
- ✅ Los recordatorios se marcan como ENVIADO si es exitoso

## Verificación

Después de desplegar en Railway, verifica los logs:
- ✅ Si ves: "Bean 'whatsAppService' skipped" → Todo bien, WhatsApp desactivado
- ✅ Si la app inicia sin errores → Correcto
- ❌ Si ves error de placeholder → Verifica que `whatsapp.enabled=false`

## Próximos Pasos

1. Despliega con `whatsapp.enabled=false`
2. Verifica que la aplicación funciona correctamente
3. Cuando tengas credenciales de WhatsApp:
   - Agrega las variables de entorno en Railway
   - Cambia `whatsapp.enabled=true`
   - Redespliega
