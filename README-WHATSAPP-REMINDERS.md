# Sistema de Recordatorios Automáticos de WhatsApp

## 📋 Descripción

Sistema automatizado de recordatorios de pago por WhatsApp que se ejecuta diariamente para notificar a los inquilinos sobre sus pagos de renta pendientes.

## 🚀 Características

### Recordatorios Progresivos

El sistema envía **6 tipos de mensajes** según la proximidad a la fecha de vencimiento:

#### Antes del vencimiento:
- **-3 días**: "Te recordamos que en 3 días vence tu pago..."
- **-2 días**: "Faltan 2 días para el vencimiento..."
- **-1 día**: "Mañana vence tu pago..."

#### Día del vencimiento:
- **Día 0**: "Hoy es el último día para pagar..."

#### Después del vencimiento:
- **+1 día**: "Tu pago está vencido..."
- **+2 días**: "Tu pago lleva 2 días de retraso. Riesgo de desalojo..."

## 📁 Estructura del Código

```
habitora-backend/
├── integration/
│   └── whatsapp/
│       └── WhatsAppService.java          # Integración con WhatsApp Business API
├── persistence/
│   └── repository/
│       ├── FacturaRepository.java        # Query para facturas abiertas
│       └── RecordatorioRepository.java   # Gestión de recordatorios
├── scheduler/
│   └── RecordatorioScheduler.java        # Tarea programada diaria
└── service/
    └── implementation/
        ├── RecordatorioService.java      # Lógica de negocio
        └── MensajeRecordatorioTemplate.java  # Templates de mensajes
```

## ⚙️ Configuración

### Variables de Entorno (Producción)

Agrega estas variables en Railway:

```properties
WHATSAPP_PHONE_NUMBER_ID=829058643633682
WHATSAPP_WABA_ID=1911620962461679
WHATSAPP_ACCESS_TOKEN=tu_token_de_meta
```

### application-local.properties (Desarrollo)

```properties
whatsapp.phone-number-id=829058643633682
whatsapp.waba-id=1911620962461679
whatsapp.access-token=EAAbEDxLbqxMBQ...
whatsapp.api-version=v19.0
whatsapp.api-base-url=https://graph.facebook.com
```

## 🕐 Programación

El sistema se ejecuta **automáticamente todos los días a las 8:00 AM** (hora de Lima).

```java
@Scheduled(cron = "0 0 8 * * ?", zone = "America/Lima")
```

## 🔄 Flujo de Funcionamiento

1. **8:00 AM** - Se ejecuta el scheduler
2. Busca todas las facturas con estado `ABIERTA`
3. Para cada factura:
   - Calcula días restantes hasta vencimiento
   - Verifica si corresponde enviar recordatorio (-3, -2, -1, 0, +1, +2)
   - Verifica que no exista recordatorio duplicado
   - Verifica que el inquilino tenga teléfono
   - Crea el recordatorio con mensaje personalizado
4. Envía todos los recordatorios programados por WhatsApp
5. Actualiza el estado de cada recordatorio (ENVIADO/FALLIDO)

## 📱 Formato de Mensajes

Ejemplo de mensaje 3 días antes:

```
Hola Juan, te recordamos que en *3 días* vence tu pago de renta 🏠

📍 Habitación: A-101
💰 Monto: S/ 800.00

Por favor, realiza tu pago a tiempo para evitar inconvenientes.

¡Gracias por tu puntualidad! 😊
```

## 🔐 Seguridad

- Los tokens de WhatsApp están protegidos en variables de entorno
- Solo se procesan facturas con estado `ABIERTA`
- Se valida que el inquilino tenga teléfono registrado
- Se previenen recordatorios duplicados
- Todos los números se formatean a formato internacional (+51)

## 📊 Logs

El sistema genera logs detallados:

```
🔔 Iniciando procesamiento diario de recordatorios de pago
📅 Fecha: 2025-11-30
📋 Facturas ABIERTAS encontradas: 15
✅ Recordatorios creados: 8
📤 Enviando recordatorios pendientes...
📨 Recordatorios pendientes de envío: 8
✅ Recordatorios enviados exitosamente: 8
✅ Procesamiento de recordatorios completado
```

## 🛠️ Mantenimiento

### Cambiar hora de ejecución

Modifica el cron expression en `RecordatorioScheduler.java`:

```java
// Cambiar de 8:00 AM a 9:00 AM
@Scheduled(cron = "0 0 9 * * ?", zone = "America/Lima")
```

### Agregar nuevo tipo de recordatorio

1. Agrega el caso en `MensajeRecordatorioTemplate.generarMensaje()`
2. Actualiza `RecordatorioService.correspondeEnviarRecordatorio()`

### Personalizar mensajes

Edita los métodos privados en `MensajeRecordatorioTemplate.java`:
- `mensajeTresDiasAntes()`
- `mensajeDosDiasAntes()`
- etc.

## 🧪 Testing Manual

Para probar el sistema sin esperar a las 8:00 AM:

1. Cambia el cron expression temporalmente:
   ```java
   @Scheduled(cron = "0 * * * * ?") // Cada minuto
   ```

2. O crea un endpoint de prueba:
   ```java
   @GetMapping("/api/test/recordatorios")
   public String testRecordatorios() {
       recordatorioScheduler.procesarRecordatoriosDiarios();
       return "Recordatorios procesados";
   }
   ```

## 📝 Notas Importantes

- ✅ Ya está activado `@EnableScheduling` en `BackendApplication`
- ✅ Solo funciona con números de Perú (+51)
- ✅ Los recordatorios se crean pero se envían inmediatamente
- ✅ Si falla el envío, el estado queda como `FALLIDO`
- ✅ No se envían recordatorios duplicados el mismo día
- ✅ El inquilino debe tener `telefonoWhatsapp` registrado

## 🎯 Próximas Mejoras

- [ ] Dashboard para ver historial de recordatorios
- [ ] Configuración personalizada por propiedad
- [ ] Soporte para otros países
- [ ] Reintentos automáticos para mensajes fallidos
- [ ] Estadísticas de efectividad de recordatorios
- [ ] Webhooks para recibir confirmación de lectura

## 📞 Soporte

Para cualquier duda sobre la integración de WhatsApp Business API:
- [Documentación oficial de Meta](https://developers.facebook.com/docs/whatsapp/cloud-api)
- [Consola de Meta para desarrolladores](https://developers.facebook.com/apps/)
