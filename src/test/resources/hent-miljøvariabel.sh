kubectl config use-context dev-gcp

echo $(kubectl -n teamfamilie get secret azuread-familie-ks-barnehagelister-lokal -o json | jq '.data.AZURE_APP_CLIENT_ID | @base64d')