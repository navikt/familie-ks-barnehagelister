kubectl config use-context dev-gcp

AZURE_APP_CLIENT_ID=$(kubectl -n teamfamilie get secret azuread-familie-ks-barnehagelister-lokal -o json | jq '.data | map_values(@base64d)')