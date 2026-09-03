# Builds the portable TACZ override gunpack and zips it for distribution.
#
# The gunpack must enumerate BEFORE tacz_default_gun in the tacz/ directory,
# because TACZ's DelegatingPackResources resolves recipes first-wins. We use a
# leading "00_" so the folder sorts first on NTFS (alphabetical enumeration).
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$src = Join-Path $root 'build\00_tacz_tfc_progression'
$dist = Join-Path $root 'dist'

if (-not (Test-Path $src)) { throw "Missing gunpack source: $src" }

Write-Host "Cleaning dist..."
if (Test-Path $dist) { Remove-Item -Recurse -Force $dist }
New-Item -ItemType Directory -Path $dist -Force | Out-Null

Write-Host "Zipping gunpack..."
$zip = Join-Path $dist 'tacz_tfc_progression.zip'
# Wrap the folder so extraction yields 00_tacz_tfc_progression/ (not bare contents).
Compress-Archive -Path $src -DestinationPath $zip -Force

$count = (Get-ChildItem -Recurse -Path $src -Filter '*.json' | Measure-Object).Count
Write-Host "Done. $count recipe/json files packaged -> $zip"
