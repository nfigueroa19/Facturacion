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

El call center crea citas nuevas con su ID de acceso. Los SMS se simulan y se leen con el botón "Comunicaciones" de los módulos del personal. Los textos de los SMS están en `config/contenido.json`, claves `sms.CITA_ASIGNADA` y `sms.NOVEDAD_REGISTRADA`.

El copago se paga con tarjetas de prueba y no cobra dinero: `4242 4242 4242 4242` aprueba; las demás opciones muestran un rechazo para probar el reintento.

El ingreso y la factura solo se registran el día de la cita: use la cita de `1000000001` o cree una para hoy desde el call center. Las solicitudes, los PDF, los pagos y los facturadores se guardan en PostgreSQL y siguen ahí después de reiniciar el servidor. Las sesiones se guardan en memoria: tras un reinicio hay que volver a ingresar.

Al arrancar, el servidor copia a la base los usuarios de `config/facturadores-prueba.json`. Si un usuario ya existe, actualiza su nombre y su clave; quitarlo del archivo no lo borra de la base.

## Si algo sale mal
Si el archivo queda con un error (por ejemplo, falta una coma o unas comillas), la web sigue mostrando la última versión correcta y el servidor deja el motivo en su registro. Corrija el archivo y guárdelo de nuevo.
