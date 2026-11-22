#requires -version 5.1
# Configuração inicial do PGSharp consolidado. Requer gh autenticado, JDK e uma cópia local legítima do APK-base.
# Execute localmente: powershell -ExecutionPolicy Bypass -File .\scripts\primeira_compilacao.ps1 -ApkPath "C:\caminho\original.apk" -BaseApkUrl "https://URL-PRIVADA-AUTORIZADA"
[CmdletBinding()]
param([Parameter(Mandatory=$true)] [string]$ApkPath, [string]$BaseApkUrl)
$ErrorActionPreference = 'Stop'
$public = 'LucianoToscano/PGSharp'
$sha256 = 'db38fc01e0148e56a707fca5b08163f2171b2268a667a76428124dc2b069cd83'
$alias = 'pgspessoal'
$baseDir = Join-Path $env:USERPROFILE 'PGSharp-Unificado'
$keystore = Join-Path $baseDir 'pgspessoal.jks'

function Check-Exit([string]$step) { if($LASTEXITCODE -ne 0) { throw "$step: falha ($LASTEXITCODE)" } }
function Read-Hidden([string]$message) {
    $s = Read-Host $message -AsSecureString
    $ptr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($s)
    try { return [Runtime.InteropServices.Marshal]::PtrToStringBSTR($ptr) }
    finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ptr) }
}
function Set-GhSecret([string]$name, [string]$value) {
    # Sem segredo como argumento do processo ou em arquivos temporários.
    $p = New-Object Diagnostics.ProcessStartInfo
    $p.FileName = (Get-Command gh -ErrorAction Stop).Source
    $p.Arguments = "secret set $name -R $public"
    $p.UseShellExecute = $false
    $p.RedirectStandardInput = $true
    $p.RedirectStandardError = $true
    $p.RedirectStandardOutput = $true
    $p.CreateNoWindow = $true
    $proc = [Diagnostics.Process]::Start($p)
    try {
        $proc.StandardInput.Write($value)
        $proc.StandardInput.Close()
        $err = $proc.StandardError.ReadToEnd()
        $proc.StandardOutput.ReadToEnd() | Out-Null
        $proc.WaitForExit()
        if($proc.ExitCode -ne 0) { throw "Erro ao cadastrar $name: $err" }
    } finally { $proc.Dispose() }
    Write-Host "[OK] $name configurado."
}

try {
    $f = Get-Item -LiteralPath $ApkPath -ErrorAction Stop
    if($f.PSIsContainer) { throw 'Informe um arquivo APK.' }
    if((Get-FileHash $f.FullName -Algorithm SHA256).Hash.ToLowerInvariant() -ne $sha256) {
        throw 'SHA256 diferente da base auditada. Não envie esse APK.'
    }
    foreach($tool in @('gh','keytool')) {
        if(-not (Get-Command $tool -ErrorAction SilentlyContinue)) { throw "Instale $tool." }
    }
    & gh auth status
    Check-Exit 'Autenticação'
    $v = & gh repo view $public --json isPrivate -q '.isPrivate'
    Check-Exit 'Visibilidade pública'
    if($v.Trim() -ne 'false') { throw 'Repositório público inesperado.' }

    # A cópia original não deve ser publicada em release ou commit público.
    # URL com autorização temporária é guardada apenas como GitHub Actions Secret.
    if([string]::IsNullOrWhiteSpace($BaseApkUrl)) {
        Write-Host 'Informe uma URL HTTPS privada/autorizada, acessível ao runner por tempo limitado.'
        $BaseApkUrl = Read-Hidden 'URL HTTPS do APK-base original'
    }
    if([string]::IsNullOrWhiteSpace($BaseApkUrl) -or $BaseApkUrl -notmatch '^https://') {
        throw 'A URL autorizada do APK-base deve começar com https://.'
    }
    Set-GhSecret 'PGSHARP_BASE_APK_URL' $BaseApkUrl
    Write-Host '[OK] Referência privada do APK-base configurada como secret, sem upload público.'

    New-Item -ItemType Directory -Path $baseDir -Force | Out-Null
    $password = Read-Hidden 'Senha da chave de assinatura (mínimo 12 caracteres)'
    if($password.Length -lt 12) { throw 'Senha de assinatura insuficiente.' }
    $env:PGSHARP_SIGNING_PASSWORD = $password
    if(-not (Test-Path $keystore)) {
        & keytool -genkeypair -keystore $keystore -storetype PKCS12 -alias $alias -keyalg RSA -keysize 3072 -validity 10000 -dname 'CN=PGSharp Pessoal,O=Uso Particular,C=BR' -storepass:env PGSHARP_SIGNING_PASSWORD -keypass:env PGSHARP_SIGNING_PASSWORD -noprompt
        Check-Exit 'Criação do keystore'
    } else {
        & keytool -list -keystore $keystore -alias $alias -storepass:env PGSHARP_SIGNING_PASSWORD | Out-Null
        Check-Exit 'Validação do keystore existente'
    }
    Set-GhSecret 'PGSHARP_SIGNING_KEYSTORE_B64' ([Convert]::ToBase64String([IO.File]::ReadAllBytes($keystore)))
    Set-GhSecret 'PGSHARP_SIGNING_PASSWORD' $password
    Set-GhSecret 'PGSHARP_SIGNING_ALIAS' $alias

    $archivePass = Read-Hidden 'Senha para abrir o APK criptografado (mínimo 12 caracteres)'
    if($archivePass.Length -lt 12) { throw 'Senha do artefato insuficiente.' }
    Set-GhSecret 'PGSHARP_ARTIFACT_PASSWORD' $archivePass
    & gh workflow run gerar-apk.yml -R $public -f 'modo=apk'
    Check-Exit 'Disparo de compilação'
    Write-Host "Build solicitado: https://github.com/$public/actions/workflows/gerar-apk.yml"
    Write-Host "Guarde $keystore e as senhas. Não desinstale seu aplicativo antes dos testes."
} finally {
    Remove-Item Env:PGSHARP_SIGNING_PASSWORD -ErrorAction SilentlyContinue
    Remove-Variable password,archivePass,BaseApkUrl -ErrorAction SilentlyContinue
}
