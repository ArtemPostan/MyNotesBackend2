param (
    [string]$Profile = "yandex"
)

$versionFile = "docker-version.txt"
$propertiesFile = "src/main/resources/application.properties"

# 1. Read current version
if (Test-Path $versionFile) {
    $currentVersion = [int](Get-Content $versionFile)
} else {
    $currentVersion = 19
}

$nextVersion = $currentVersion + 1
$tag = "v$nextVersion"
$registry = "cr.yandex/crpnratllaftm6rq2bfi/mynotes-backend"
$fullImageName = $registry + ":" + $tag

Write-Host "=== START AUTOMATIC LOCAL-BASED DEPLOY ===" -ForegroundColor Cyan
Write-Host "Target Profile: $Profile" -ForegroundColor Cyan

# 2. AUTO-SWITCH PROFILE
if (Test-Path $propertiesFile) {
    Write-Host "Config: Setting spring.profiles.active to '$Profile'..." -ForegroundColor Yellow
    (Get-Content $propertiesFile) -replace "spring.profiles.active\s*=\s*.*", "spring.profiles.active=$Profile" | Set-Content $propertiesFile
} else {
    Write-Host "WARNING: application.properties not found at $propertiesFile" -ForegroundColor Magenta
}

# 3. ЛОКАЛЬНАЯ СБОРКА MAVEN (Деплой отдельно от Докера)
Write-Host "Step 1: Recompiling Java project locally (Maven package)..." -ForegroundColor Yellow
if (Test-Path ".\mvnw.cmd") {
    .\mvnw.cmd clean package -DskipTests
} else {
    Write-Host "WARNING: mvnw.cmd not found! Trying global mvn..." -ForegroundColor Magenta
    mvn clean package -DskipTests
}

if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Spring Boot Maven build failed!" -ForegroundColor Red
    exit
}

# 4. Run Docker Build (Docker просто упаковывает готовый JAR)
Write-Host "Step 2: Building Docker image: $fullImageName" -ForegroundColor Yellow
docker build -t $fullImageName .
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Docker build failed!" -ForegroundColor Red
    exit
}

# 5. Run Docker Push
Write-Host "Step 3: Pushing image to Yandex Container Registry..." -ForegroundColor Yellow
docker push $fullImageName
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Docker push failed!" -ForegroundColor Red
    exit
}

# 6. Save new version to file on success
$nextVersion | Out-File $versionFile -Encoding ascii
Write-Host "SUCCESS: Deploy version $tag completed successfully!" -ForegroundColor Green
Write-Host "Current active profile in properties is left as: $Profile" -ForegroundColor Green