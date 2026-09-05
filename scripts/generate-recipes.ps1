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
$blockSrc = Join-Path $root 'data\block-recipes'
$staticSrc = Join-Path $root 'src\main\resources\data\tacz_tfc_integration\static_recipes'

if (-not (Test-Path $mapPath))     { throw "Missing tier map: $mapPath" }
if (-not (Test-Path $DefaultPack)) { throw "Default pack not found: $DefaultPack" }

$map = Get-Content $mapPath -Raw | ConvertFrom-Json

function Get-Material([string]$id) {
    # t1..t7 -> item id
    return $map.material_map.$id.item
}

function Resolve-Sub([string]$category, [string]$id) {
    # Mirrors RecipeTransformer: returns @{ tag = ... } or @{ item = ... },
    # or $null if no substitution applies.
    switch ($id) {
        'forge:gems/amethyst'        { return @{ tag = 'tacz_tfc_integration:cut_gems' } }
        'forge:gems/quartz'          { return @{ item = 'tfc:metal/ingot/weak_steel' } }
        'forge:rods/blaze'           { return @{ item = 'tfc:metal/rod/red_steel' } }
        'forge:ingots/netherite'     { return @{ item = 'tfc:metal/ingot/blue_steel' } }
        'forge:ores/netherite_scrap' { return @{ item = 'tfc:metal/ingot/unknown' } }
    }
    if ($category -eq 'guns' -and $id -eq 'forge:gems/lapis') {
        return @{ item = 'tfc:metal/ingot/nickel' }
    }
    if ($category -eq 'ammo') {
        switch ($id) {
            'forge:ingots/copper' { return @{ item = 'tfc:metal/ingot/brass' } }
            'forge:gems/lapis'    { return @{ item = 'tfc:metal/ingot/bismuth' } }
        }
    }
    if ($category -eq 'attachments') {
        switch ($id) {
            'forge:gems/lapis'          { return @{ item = 'tfc:metal/ingot/sterling_silver' } }
            'minecraft:crying_obsidian' { return @{ item = 'tfc:metal/ingot/black_bronze' } }
            'minecraft:ancient_debris'  { return @{ item = 'tfc:metal/ingot/unknown' } }
        }
    }
    return $null
}

function Apply-Sub($itemObj, $sub) {
    if ($sub -eq $null) { return $false }
    $itemObj.PSObject.Properties.Remove('tag')
    $itemObj.PSObject.Properties.Remove('item')
    if ($sub.ContainsKey('tag')) { $itemObj | Add-Member -NotePropertyName tag -NotePropertyValue $sub.tag }
    else                         { $itemObj | Add-Member -NotePropertyName item -NotePropertyValue $sub.item }
    return $true
}

function Copy-Recipe([string]$dir, [string]$category, [string]$id, [string]$tier) {
    $src = Join-Path $DefaultPack "data\tacz\recipes\$dir\$id.json"
    if (-not (Test-Path $src)) {
        Write-Warning "Recipe not found in default pack: $dir/$id.json"
        return $false
    }

    $recipe = Get-Content $src -Raw | ConvertFrom-Json
    $ironItem = Get-Material $tier
    $ironMetal = ($ironItem -split '/')[-1]
    $changed = $false
    $netherite = $ironItem -eq 'minecraft:netherite_ingot'

    foreach ($m in $recipe.materials) {
        $itemObj = $m.item
        if ($null -eq $itemObj) { continue }
        $tag = $itemObj.tag
        $item = $itemObj.item

        $sub = $null
        if ($tag -eq 'forge:ingots/iron') {
            $itemObj | Add-Member -NotePropertyName item -NotePropertyValue $ironItem -Force
            $itemObj.PSObject.Properties.Remove('tag')
            $changed = $true
            continue
        }
        if ($tag -eq 'forge:nuggets/iron') {
            if ($netherite) {
                $itemObj | Add-Member -NotePropertyName item -NotePropertyValue 'minecraft:netherite_scrap' -Force
            } else {
                $itemObj | Add-Member -NotePropertyName item -NotePropertyValue "tfc:metal/nugget/$ironMetal" -Force
            }
            $itemObj.PSObject.Properties.Remove('tag')
            if ($m.PSObject.Properties.Name -contains 'count') {
                $m.count = [Math]::Max(1, [Math]::Round($m.count / 9.0))
            }
            $changed = $true
            continue
        }
        if ($tag) { $sub = Resolve-Sub $category $tag }
        elseif ($item) { $sub = Resolve-Sub $category $item }
        if ($sub) {
            $itemObj.PSObject.Properties.Remove('tag')
            $itemObj.PSObject.Properties.Remove('item')
            if ($sub.ContainsKey('tag')) { $itemObj | Add-Member -NotePropertyName tag -NotePropertyValue $sub.tag }
            else                         { $itemObj | Add-Member -NotePropertyName item -NotePropertyValue $sub.item }
            $changed = $true
        }
    }

    if (-not $changed) {
        Write-Warning "No substitutable material in $dir/$id (skipping)"
        return $false
    }

    $destDir = Join-Path $outRoot $dir
    New-Item -ItemType Directory -Path $destDir -Force | Out-Null
    $dest = Join-Path $destDir "$id.json"
    $recipe | ConvertTo-Json -Depth 20 | Set-Content -Path $dest -Encoding UTF8
    return $true
}

$total = 0
foreach ($cat in @('guns','ammo','attachments')) {
    $dir = if ($cat -eq 'guns') { 'gun' } else { $cat }
    $map.$cat.PSObject.Properties | ForEach-Object {
        $src = Join-Path $DefaultPack "data\tacz\recipes\$dir\$($_.Name).json"
        if (Test-Path $src) {
            if (Copy-Recipe $dir $cat $_.Name $_.Value) { $total++ }
        } else {
            # Recipes with no default in the TACZ pack come from bundled resources.
            $bundled = Join-Path $staticSrc "$dir\$($_.Name).json"
            if (Test-Path $bundled) {
                $destDir = Join-Path $outRoot $dir
                New-Item -ItemType Directory -Path $destDir -Force | Out-Null
                Copy-Item $bundled (Join-Path $destDir "$($_.Name).json") -Force
                $total++
            } else {
                Write-Warning "No default or bundled recipe for $dir/$($_.Name)"
            }
        }
    }
}

# Copy the static vanilla shaped-recipes (crafting tables / ammo box / target)
# that live in the TACZ mod jar, re-tiered to wrought iron. These go in the
# recipes ROOT (vanilla RecipeManager path), unlike gun/ammo/attachments.
if (-not (Test-Path $blockSrc)) { throw "Missing block recipes: $blockSrc" }
foreach ($bf in (Get-ChildItem $blockSrc -Filter '*.json')) {
    Copy-Item $bf.FullName (Join-Path $outRoot $bf.Name) -Force
    $total++
}

Write-Host "Wrote $total override recipes to $outRoot"
