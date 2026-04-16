kubectl config use-context dev-gcp


AZURE_APP_CLIENT_ID="$(kubectl -n teamfamilie get secret azuread-familie-ks-barnehagelister-lokal -o json | jq '.data.AZURE_APP_CLIENT_ID | @base64d')"

if [ -z "$AZURE_APP_CLIENT_ID" ] || [ "$AZURE_APP_CLIENT_ID" = "null" ]; then
  echo "Klarte ikke å hente miljøvariabler. Er du pålogget naisdevice og gcloud, og har du jq?" >&2
  exit 1
fi

cat << EOF > .env
# Denne filen er generert automatisk ved å kjøre src/test/resources/hent-og-lagre-miljøvariabel.sh

AZURE_APP_CLIENT_ID='$AZURE_APP_CLIENT_ID'
EOF

echo "Skrev .env med AZURE_APP_CLIENT_ID (len=${#AZURE_APP_CLIENT_ID})"
