# Cómo cambiar textos e imágenes de la prefactura

Los cambios se ven en la web sin reiniciar el servidor ni llamar al programador.

## Cambiar un texto
1. Abra `config/contenido.json` con el Bloc de notas.
2. Cambie solo el texto que está a la derecha de los dos puntos, entre comillas.
3. Guarde el archivo.

Ejemplo: `"prefactura.titulo": "Prefactura de consulta externa"`. El nombre de la izquierda (`prefactura.titulo`) no se cambia; la web lo usa para saber dónde va cada texto.

## Cambiar una imagen (logo o banner)
1. Copie la imagen nueva en `assets/imagenes/`.
2. Si reemplaza una imagen existente con el mismo nombre, ya terminó.
3. Si la imagen tiene un nombre nuevo, escriba ese nombre en la sección `"imagenes"` de `config/contenido.json`.

Reglas del nombre de archivo:
- Solo letras minúsculas sin tildes, números y guiones. Sin espacios. Ejemplo: `banner-vacunacion-2026.png`.
- Formatos permitidos: `.png`, `.jpg` o `.jpeg`.
- Tamaño máximo: 2 MB.

El servidor quita los datos ocultos de la imagen (autor, cámara, ubicación, software) antes de mostrarla.

## Cambiar los campos del formulario del paciente
Los campos están en `config/formulario-prefactura.json`. Cada campo tiene:
- `clave`: nombre interno. Solo minúsculas sin tildes, números y guiones. No cambie la clave de un campo que ya tiene datos.
- `etiqueta`: el texto que ve el paciente.
- `tipo`: `TEXTO`, `FECHA`, `CORREO` o `TELEFONO`.
- `obligatorio`: `true` o `false`. Si no se escribe, el campo es obligatorio.
- `ayuda` (opcional): texto corto bajo el campo.

Este archivo se lee al arrancar el servidor: después de cambiarlo hay que reiniciarlo. Si tiene un error, el servidor no arranca y deja el motivo en su registro.

## Datos de prueba
Todo lo que sigue es ficticio y existe solo para probar el piloto. Antes de publicar la plataforma, estos archivos se reemplazan por la agenda real y por los usuarios del hospital.

Citas (`config/citas-prueba.json`). La fecha de cada cita se cuenta desde el día en que arranca el servidor (`diasDesdeHoy`).

| Identificación | ID de acceso | Cita | Copago |
|----------------|--------------|------|--------|
| 1000000001 | PRUEBA1 | Hoy | $5.000, contributivo |
| 1000000002 | PRUEBA2 | Mañana | Sin copago, subsidiado |
| 1000000003 | PRUEBA3 | En dos días | $5.000, contributivo |

Módulos del personal: facturación (`http://localhost:5173/facturador`), call center (`/callcenter`) y digiturno (`/digiturno`). Los tres usan el mismo ingreso: usuario `facturador`, clave `prueba123`. El archivo `config/facturadores-prueba.json` guarda solo el hash de la clave.

El call center crea citas nuevas con su ID de acceso. Los SMS se simulan y se leen con el botón "Comunicaciones" de los módulos del personal. Los textos de los SMS están en `config/contenido.json`, claves `sms.CITA_ASIGNADA`, `sms.SOLICITUD_RADICADA` y `sms.NOVEDAD_REGISTRADA`.

El copago se paga en el sandbox de Wompi y no cobra dinero. Tarjeta `4242 4242 4242 4242` aprueba y `4111 1111 1111 1111` rechaza (vencimiento futuro, CVC de 3 dígitos, nombre de al menos 5 letras). Nequi `3991111111` aprueba y `3992222222` rechaza. En PSE se elige "Banco que aprueba" o "Banco que declina". El botón "Ver datos de prueba de Wompi" de la pantalla de pago repite esta lista.

El ingreso y la factura solo se registran el día de la cita: use la cita de `1000000001` o cree una para hoy desde el call center. Las solicitudes, los PDF, los pagos y los facturadores se guardan en PostgreSQL y siguen ahí después de reiniciar el servidor. Las sesiones se guardan en memoria: tras un reinicio hay que volver a ingresar.

Al arrancar, el servidor copia a la base los usuarios de `config/facturadores-prueba.json`. Si un usuario ya existe, actualiza su nombre y su clave; quitarlo del archivo no lo borra de la base.

## Catálogo de escenarios de prueba
`config/usuarios-prueba.json` reúne los usuarios del personal (`facturador`, `callcenter` y `digiturno`, clave `prueba123`), diez pacientes con un escenario cada uno (flujo feliz, copago, novedad, revisión, digiturno, anulación, facturación y cita de mañana) y tres identificaciones libres para el call center.

Solo se activa con la variable `HSLV_CATALOGO_PRUEBAS=config/usuarios-prueba.json` y la persistencia en Appwrite. Al arrancar, el servidor crea los escenarios que faltan; los que ya existen solo cambian la fecha de la cita a hoy. La pantalla de acceso muestra el botón "Datos de prueba" con todo el catálogo.

Para dejar todos los escenarios como al principio, use el botón "Restablecer datos de prueba" de los módulos del personal. Borra lo que se hizo con esas identificaciones, incluidas las citas del call center.

Si cambia el archivo, reinicie el servidor. Las identificaciones no se pueden repetir y cada celular debe tener 10 dígitos y empezar por 3.

## Si algo sale mal
Si el archivo queda con un error (por ejemplo, falta una coma o unas comillas), la web sigue mostrando la última versión correcta y el servidor deja el motivo en su registro. Corrija el archivo y guárdelo de nuevo.
