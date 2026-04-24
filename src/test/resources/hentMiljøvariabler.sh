NAIS_STATUS=$(nais device status 2>/dev/null || echo "Not connected")

if [[ "$NAIS_STATUS" != *"Connected"* ]]; then
  echo "Naisdevice er ikke tilkoblet. Start naisdevice og velg connect. Status må være grønn." >&2
  exit 1
fi

kubectl config use-context dev-gcp

AZURE_APP_CLIENT_ID="$(kubectl -n teamfamilie get secret azuread-familie-ks-barnehagelister-lokal -o json | jq -r '.data.AZURE_APP_CLIENT_ID | @base64d')"
AZURE_OPENID_CONFIG_ISSUER="$(kubectl -n teamfamilie get secret azuread-familie-ks-barnehagelister-lokal -o json | jq -r '.data.AZURE_OPENID_CONFIG_ISSUER | @base64d')"

printf "AZURE_APP_CLIENT_ID=%s;AZURE_OPENID_CONFIG_ISSUER=%s" "$AZURE_APP_CLIENT_ID" "$AZURE_OPENID_CONFIG_ISSUER"
