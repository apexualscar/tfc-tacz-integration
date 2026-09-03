# Builds the portable TACZ override gunpack and zips it for distribution.
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$src = Join-Path $root 'build\tacz_tfc_progression'
$dist = Join-Path $root 'dist'

if (-not (Test-Path $src)) { throw "Missing gunpack source: $src" }

Write-Host "Cleaning dist..."
if (Test-Path $dist) { Remove-Item -Recurse -Force $dist }
New-Item -ItemType Directory -Path $dist -Force | Out-Null

Write-Host "Zipping gunpack..."
$zip = Join-Path $dist 'tacz_tfc_progression.zip'
Compress-Archive -Path (Join-Path $src '*') -DestinationPath $zip -Force

$count = (Get-ChildItem -Recurse -Path $src -Filter '*.json' | Measure-Object).Count
Write-Host "Done. $count recipe/json files packaged -> $zip"
