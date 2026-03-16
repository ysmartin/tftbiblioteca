#!/bin/sh

echo
echo "1. Listado manual del catalogo"
curl http://localhost:8090/biblioteca/obras

echo
echo "2. Filtro manual por autor"
curl "http://localhost:8090/biblioteca/obras?autor=Borges"

echo
echo "3. Alta manual de una obra"
curl -H "Content-Type: application/json" \
  -d '{ "autor": "Mary Shelley", "titulo": "Frankenstein", "resumen": "Novela gotica sobre ciencia y responsabilidad.", "palabrasClave": ["gotico", "ciencia", "clasico"] }' \
  -X POST http://localhost:8090/biblioteca/obras

echo
echo "4. Subida o reemplazo de PDF en la obra 1"
curl -H "Content-Type: application/pdf" \
  --data-binary "@src/main/resources/documents/obra-demo.pdf" \
  -X PUT http://localhost:8090/biblioteca/obras/1/digital

echo
echo "5. Descarga del PDF de la obra 1"
curl -OJ http://localhost:8090/biblioteca/obras/1/digital

echo
echo "6. Spring Data REST"
curl http://localhost:8090/api/obras
