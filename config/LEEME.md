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

## Si algo sale mal
Si el archivo queda con un error (por ejemplo, falta una coma o unas comillas), la web sigue mostrando la última versión correcta y el servidor deja el motivo en su registro. Corrija el archivo y guárdelo de nuevo.
