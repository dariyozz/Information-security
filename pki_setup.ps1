$openssl = "C:\Program Files\Git\usr\bin\openssl.exe"
$pkiDir = "pki"
$resourcesDir = "src\main\resources"

# Create directories
if (-not (Test-Path $pkiDir)) {
    New-Item -ItemType Directory -Path $pkiDir | Out-Null
}

Write-Host "Generating FINKI CA (Root)..."
& $openssl req -x509 -new -nodes -keyout "$pkiDir/finki_ca.key" -sha256 -days 3650 -out "$pkiDir/finki_ca.crt" -subj "/C=MK/ST=Skopje/O=FINKI/CN=FINKI CA"


Write-Host "Generating Student CA (Intermediate 1)..."
& $openssl req -new -nodes -keyout "$pkiDir/student_ca.key" -out "$pkiDir/student_ca.csr" -subj "/C=MK/ST=Skopje/O=FINKI/OU=Student/CN=Student CA"
& $openssl x509 -req -in "$pkiDir/student_ca.csr" -CA "$pkiDir/finki_ca.crt" -CAkey "$pkiDir/finki_ca.key" -CAcreateserial -out "$pkiDir/student_ca.crt" -days 1825 -sha256

Write-Host "Generating Ass CA (Intermediate 1)..."
& $openssl req -new -nodes -keyout "$pkiDir/ass_ca.key" -out "$pkiDir/ass_ca.csr" -subj "/C=MK/ST=Skopje/O=FINKI/OU=Ass/CN=Ass CA"
& $openssl x509 -req -in "$pkiDir/ass_ca.csr" -CA "$pkiDir/finki_ca.crt" -CAkey "$pkiDir/finki_ca.key" -CAcreateserial -out "$pkiDir/ass_ca.crt" -days 1825 -sha256

Write-Host "Generating IB CA (Intermediate 1)..."
& $openssl req -new -nodes -keyout "$pkiDir/ib_ca.key" -out "$pkiDir/ib_ca.csr" -subj "/C=MK/ST=Skopje/O=FINKI/OU=IB/CN=IB CA"
& $openssl x509 -req -in "$pkiDir/ib_ca.csr" -CA "$pkiDir/finki_ca.crt" -CAkey "$pkiDir/finki_ca.key" -CAcreateserial -out "$pkiDir/ib_ca.crt" -days 1825 -sha256


Write-Host "Generating Lab CA (Intermediate 2)..."
& $openssl req -new -nodes -keyout "$pkiDir/lab_ca.key" -out "$pkiDir/lab_ca.csr" -subj "/C=MK/ST=Skopje/O=FINKI/OU=IB/CN=Lab CA"
& $openssl x509 -req -in "$pkiDir/lab_ca.csr" -CA "$pkiDir/ib_ca.crt" -CAkey "$pkiDir/ib_ca.key" -CAcreateserial -out "$pkiDir/lab_ca.crt" -days 1095 -sha256


Write-Host "Generating Server Certificate..."
& $openssl req -new -nodes -keyout "$pkiDir/server.key" -out "$pkiDir/server.csr" -subj "/C=MK/ST=Skopje/O=FINKI/CN=localhost"
& $openssl x509 -req -in "$pkiDir/server.csr" -CA "$pkiDir/lab_ca.crt" -CAkey "$pkiDir/lab_ca.key" -CAcreateserial -out "$pkiDir/server.crt" -days 365 -sha256

Write-Host "Generating Client Certificate (222015)..."
& $openssl req -new -nodes -keyout "$pkiDir/client_222015.key" -out "$pkiDir/client_222015.csr" -subj "/C=MK/ST=Skopje/O=FINKI/CN=222015"
& $openssl x509 -req -in "$pkiDir/client_222015.csr" -CA "$pkiDir/lab_ca.crt" -CAkey "$pkiDir/lab_ca.key" -CAcreateserial -out "$pkiDir/client_222015.crt" -days 365 -sha256

Write-Host "Generating Student/Ass Certificates..."
# Student 1
& $openssl req -new -nodes -keyout "$pkiDir/student1.key" -out "$pkiDir/student1.csr" -subj "/C=MK/ST=Skopje/O=FINKI/OU=Student/CN=student1@students.finki.ukim.mk"
& $openssl x509 -req -in "$pkiDir/student1.csr" -CA "$pkiDir/student_ca.crt" -CAkey "$pkiDir/student_ca.key" -CAcreateserial -out "$pkiDir/student1.crt" -days 365 -sha256

# Student 2
& $openssl req -new -nodes -keyout "$pkiDir/student2.key" -out "$pkiDir/student2.csr" -subj "/C=MK/ST=Skopje/O=FINKI/OU=Student/CN=student2@students.finki.ukim.mk"
& $openssl x509 -req -in "$pkiDir/student2.csr" -CA "$pkiDir/student_ca.crt" -CAkey "$pkiDir/student_ca.key" -CAcreateserial -out "$pkiDir/student2.crt" -days 365 -sha256

# Ass 1
& $openssl req -new -nodes -keyout "$pkiDir/ass1.key" -out "$pkiDir/ass1.csr" -subj "/C=MK/ST=Skopje/O=FINKI/OU=Ass/CN=ass1@finki.ukim.mk"
& $openssl x509 -req -in "$pkiDir/ass1.csr" -CA "$pkiDir/ass_ca.crt" -CAkey "$pkiDir/ass_ca.key" -CAcreateserial -out "$pkiDir/ass1.crt" -days 365 -sha256

# Ass 2
& $openssl req -new -nodes -keyout "$pkiDir/ass2.key" -out "$pkiDir/ass2.csr" -subj "/C=MK/ST=Skopje/O=FINKI/OU=Ass/CN=ass2@finki.ukim.mk"
& $openssl x509 -req -in "$pkiDir/ass2.csr" -CA "$pkiDir/ass_ca.crt" -CAkey "$pkiDir/ass_ca.key" -CAcreateserial -out "$pkiDir/ass2.crt" -days 365 -sha256


Write-Host "Creating PKCS12 Keystore for Server..."
# Bundle the server cert, key, and the certificate chain for the keystore
& $openssl pkcs12 -export -in "$pkiDir/server.crt" -inkey "$pkiDir/server.key" -certfile "$pkiDir/lab_ca.crt" -out "$resourcesDir/keystore.p12" -name server -passout pass:changeit

Write-Host "Creating PKCS12 for Client (Optional, for browser import)..."
& $openssl pkcs12 -export -in "$pkiDir/client_222015.crt" -inkey "$pkiDir/client_222015.key" -certfile "$pkiDir/lab_ca.crt" -out "$pkiDir/client_222015.p12" -name client -passout pass:changeit

Write-Host "PKI Generation Complete."
