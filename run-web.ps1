$Root = $PSScriptRoot
$Deps = Join-Path $Root ".deps"
$Classes = Join-Path $Root "target\classes"
$JacksonVer = "2.17.2"
$Base = "https://repo1.maven.org/maven2/com/fasterxml/jackson"

New-Item -ItemType Directory -Force -Path $Deps, $Classes | Out-Null

$jars = @(
    "$Base/core/jackson-core/$JacksonVer/jackson-core-$JacksonVer.jar",
    "$Base/core/jackson-databind/$JacksonVer/jackson-databind-$JacksonVer.jar",
    "$Base/core/jackson-annotations/$JacksonVer/jackson-annotations-$JacksonVer.jar"
)

foreach ($url in $jars) {
    $file = Join-Path $Deps (Split-Path $url -Leaf)
    if (-not (Test-Path $file)) {
        Write-Host "Downloading $url ..."
        Invoke-WebRequest -Uri $url -OutFile $file
    }
}

$sources = Get-ChildItem -Path (Join-Path $Root "src\main\java") -Filter "*.java" -Recurse | ForEach-Object { $_.FullName }
$cp = (Get-ChildItem $Deps -Filter "*.jar" | ForEach-Object { $_.FullName }) -join ";"

Write-Host "Compiling..."
& javac -encoding UTF-8 -cp $cp -d $Classes @sources
if ($LASTEXITCODE -ne 0) { exit 1 }

Write-Host "Copying resources..."
Copy-Item -Path (Join-Path $Root "src\main\resources\*") -Destination $Classes -Recurse -Force

Write-Host "Starting LinguaPulse Web at http://localhost:8090 ..."
Start-Process "http://localhost:8090"
& java -cp "$Classes;$cp" org.linguapulse.LinguaPulseWebApp
