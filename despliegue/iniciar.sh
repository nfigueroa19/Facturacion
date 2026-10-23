#!/bin/sh
# Render guarda los Secret Files como texto: el almacén PKCS12 llega en base64 y se decodifica al arrancar.
# El .p12 sigue cifrado; su clave llega aparte en HSLV_SECRETOS_CLAVE_ARCHIVO.
set -eu
if [ -n "${HSLV_SECRETOS_ALMACEN_BASE64:-}" ]; then
    umask 077
    base64 -d "$HSLV_SECRETOS_ALMACEN_BASE64" > /tmp/prefactura.p12
    export HSLV_SECRETOS_ALMACEN=/tmp/prefactura.p12
fi
exec ./bin/prefactura-consulta-externa
