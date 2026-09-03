# Generates the override recipes for the TACZ x TFC progression gunpack.
#
# Reads the default TACZ gunpack recipes, applies the tier map in
# data/tier-map.json, and writes transformed copies into
# build/zz_tacz_tfc_progression/data/tacz/recipes/...
#
# Usage:
#   .\scripts\generate-recipes.ps1 -DefaultPack <path to tacz_default_gun>
#
# -DefaultPack may be omitted if the default pack is at the well-known path
# used by this dev machine; otherwise pass it explicitly.
param(
    [string]$DefaultPack = "C:\Users\New User\AppData\Roaming\PrismLauncher\instances\Auto-TerraFirmaCraft Evolved\minecraft\tacz\tacz_default_gun"
)

$ErrorActionPreference = 'Stop'

$root    = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
$mapPath = Join-Path $root 'data\tier-map.json'
$outRoot = Join-Path $root 'build\zz_tacz_tfc_progression\data\tacz\recipes'

if (-not (Test-Path $mapPath))       { throw "Missing tier map: $mapPath" }
if (-not (Test-Path $DefaultPack))   { throw "Default pack not found: $DefaultPack" }

$map = Get-Content $mapPath -Raw | ConvertFrom-Json

function Get-Material([string]$id) {
    # t1..t7 -> item id
    return $map.material_map.$id.item
}

function Resolve-SubPath([string]$category, [string]$id) {
    return (Join-Path $category "$id.json")
}

function Copy-Recipe([string]$category, [string]$id, [string]$tier) {
    $src = Join-Path $DefaultPack "data\tacz\recipes\$category\$id.json"
    if (-not (Test-Path $src)) {
        Write-Warning "Recipe not found in default pack: $category/$id.json"
        return $false
    }

    $recipe = Get-Content $src -Raw | ConvertFrom-Json
    $ironItem = Get-Material $tier
    $changed = $false

    foreach ($m in $recipe.materials) {
        $tag = $m.item.tag
        if ($tag -eq 'forge:ingots/iron') {
            $m.item = [ordered]@{ item = $ironItem }
            $changed = $true
        }
        elseif ($tag -eq 'forge:nuggets/iron') {
            $metal = ($ironItem -split '/')[-1]
            if ($ironItem -eq 'minecraft:netherite_ingot') {
                $m.item = [ordered]@{ item = 'minecraft:netherite_scrap' }
            } else {
                $m.item = [ordered]@{ item = "tfc:metal/nugget/$metal" }
            }
            $changed = $true
        }
    }

    if (-not $changed) {
        Write-Warning "No iron material to substitute in $category/$id (skipping)"
        return $false
    }

    $destDir = Join-Path $outRoot $category
    New-Item -ItemType Directory -Path $destDir -Force | Out-Null
    $dest = Join-Path $destDir "$id.json"
    $recipe | ConvertTo-Json -Depth 20 | Set-Content -Path $dest -Encoding UTF8
    return $true
}

$total = 0
foreach ($cat in @('guns','ammo','attachments')) {
    $map.$cat.PSObject.Properties | ForEach-Object {
        $dir = if ($cat -eq 'guns') { 'gun' } else { $cat }
        if (Copy-Recipe $dir $_.Name $_.Value) { $total++ }
    }
}

Write-Host "Wrote $total override recipes to $outRoot"
