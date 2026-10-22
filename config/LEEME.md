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

Panel del facturador (`http://localhost:5173/facturador`): usuario `facturador`, clave `prueba123`. El archivo `config/facturadores-prueba.json` guarda solo el hash de la clave.

El pago del copago es simulado: aprueba siempre y no cobra dinero. Las solicitudes, los pagos y las sesiones se guardan en memoria y se pierden al reiniciar el servidor; los PDF quedan en `datos/soportes/`, que no se sube al repositorio.

## Si algo sale mal
Si el archivo queda con un error (por ejemplo, falta una coma o unas comillas), la web sigue mostrando la última versión correcta y el servidor deja el motivo en su registro. Corrija el archivo y guárdelo de nuevo.
