kubectl config use-context dev-gcp

AZURE_APP_CLIENT_ID="$(kubectl -n teamfamilie get secret azuread-familie-ks-barnehagelister-lokal -o json | jq '.data.AZURE_APP_CLIENT_ID | @base64d')"
AZURE_OPENID_CONFIG_ISSUER="$(kubectl -n teamfamilie get secret azuread-familie-ks-barnehagelister-lokal -o json | jq '.data.AZURE_OPENID_CONFIG_ISSUER | @base64d')"

if [ -z "$AZURE_APP_CLIENT_ID" ] || [ "$AZURE_APP_CLIENT_ID" = "null" ]; then
  echo "Klarte ikke å hente miljøvariabler. Er du pålogget naisdevice og gcloud, og har du jq?" >&2
  exit 1
fi

if [ -z "$AZURE_OPENID_CONFIG_ISSUER" ] || [ "$AZURE_OPENID_CONFIG_ISSUER" = "null" ]; then
  echo "Klarte ikke å hente AZURE_OPENID_CONFIG_ISSUER. Er du pålogget naisdevice og gcloud, og har du jq?" >&2
  exit 1
fi

cat << EOF > .env
# Denne filen er generert automatisk ved å kjøre hent-og-lagre-miljøvariabler.sh

AZURE_APP_CLIENT_ID='$AZURE_APP_CLIENT_ID'
AZURE_OPENID_CONFIG_ISSUER='$AZURE_OPENID_CONFIG_ISSUER'
EOF

echo "Skrev .env med AZURE_APP_CLIENT_ID (len=${#AZURE_APP_CLIENT_ID}) og AZURE_OPENID_CONFIG_ISSUER (len=${#AZURE_OPENID_CONFIG_ISSUER})"
