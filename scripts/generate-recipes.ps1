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
$packRoot = Join-Path $root 'build\zz_tacz_tfc_progression'
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
        'forge:gems/amethyst'        { return @{ item = 'tfc:gem/amethyst' } }
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
            'minecraft:end_crystal'     { return @{ item = 'minecraft:gunpowder' } }
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

# TACZ gun data files carry // and /* */ comments; strip them before ConvertFrom-Json.
function Remove-JsonComments([string]$text) {
    $sb = New-Object System.Text.StringBuilder
    $inString = $false; $inLine = $false; $inBlock = $false
    for ($i = 0; $i -lt $text.Length; $i++) {
        $c = $text[$i]
        $next = if ($i + 1 -lt $text.Length) { $text[$i + 1] } else { [char]0 }
        if ($inLine) {
            if ($c -eq "`n") { $inLine = $false; [void]$sb.Append($c) }
            continue
        }
        if ($inBlock) {
            if ($c -eq '*' -and $next -eq '/') { $inBlock = $false; $i++ }
            continue
        }
        if ($inString) {
            [void]$sb.Append($c)
            if ($c -eq '\' -and $i + 1 -lt $text.Length) { [void]$sb.Append($text[$i + 1]); $i++ }
            elseif ($c -eq '"') { $inString = $false }
            continue
        }
        if ($c -eq '"') { $inString = $true; [void]$sb.Append($c); continue }
        if ($c -eq '/' -and $next -eq '/') { $inLine = $true; $i++; continue }
        if ($c -eq '/' -and $next -eq '*') { $inBlock = $true; $i++; continue }
        [void]$sb.Append($c)
    }
    return $sb.ToString()
}

$damageScale = @{ t1 = 0.30; t2 = 0.44; t3 = 0.58; t4 = 0.72; t5 = 0.86; t6 = 1.00 }
$round1 = { param($v) [Math]::Round([double]$v * 10) / 10.0 }

function Copy-GunData([string]$gunId, [string]$tier) {
    $src = Join-Path $DefaultPack "data\tacz\data\guns\$gunId`_data.json"
    if (-not (Test-Path $src)) {
        Write-Warning "Gun data not found: $gunId"
        return $false
    }
    $clean = Remove-JsonComments ((Get-Content $src -Raw) -replace "`r`n", "`n")
    $data = $clean | ConvertFrom-Json

    $m = if ($damageScale.ContainsKey($tier)) { $damageScale[$tier] } else { 1.0 }
    if ($data.ammo -eq 'tacz:12g' -and $null -ne $data.bullet -and $null -ne $data.bullet.damage) {
        $floor = 16.0 / [double]$data.bullet.damage
        if ($floor -gt $m) { $m = $floor }
    }

    if ($null -ne $data.bullet -and $null -ne $data.bullet.damage) {
        $data.bullet.damage = & $round1 ([double]$data.bullet.damage * $m)
    }
    if ($null -ne $data.bullet.extra_damage -and $data.bullet.extra_damage.damage_adjust) {
        foreach ($adj in $data.bullet.extra_damage.damage_adjust) { $adj.damage = & $round1 ([double]$adj.damage * $m) }
    }
    if ($null -ne $data.bullet.explosion -and $null -ne $data.bullet.explosion.damage) {
        $data.bullet.explosion.damage = & $round1 ([double]$data.bullet.explosion.damage * $m)
    }
    if ($null -ne $data.fire_mode_adjust) {
        foreach ($prop in ($data.fire_mode_adjust.PSObject.Properties)) {
            if ($prop.Value -and $prop.Value.PSObject.Properties.Name -contains 'damage') {
                $prop.Value.damage = & $round1 ([double]$prop.Value.damage * $m)
            }
        }
    }
    if ($null -ne $data.melee -and $null -ne $data.melee.default -and $null -ne $data.melee.default.damage) {
        $data.melee.default.damage = & $round1 ([double]$data.melee.default.damage * $m)
    }

    $destDir = Join-Path $packRoot 'data\tacz\data\guns'
    New-Item -ItemType Directory -Path $destDir -Force | Out-Null
    $data | ConvertTo-Json -Depth 20 | Set-Content -Path (Join-Path $destDir "$gunId`_data.json") -Encoding UTF8
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

    $ammoLadder = @{ t1 = 0.50; t2 = 0.67; t3 = 0.83; t4 = 1.00 }
    if ($category -eq 'ammo' -and $ammoLadder.ContainsKey($tier)) {
        $factor = $ammoLadder[$tier]
        if ($factor -lt 1.0) {
            foreach ($m in $recipe.materials) {
                if ($null -ne $m -and $m.PSObject.Properties.Name -contains 'count') {
                    $m.count = [Math]::Max(1, [Math]::Round($m.count * $factor))
                }
            }
        }
    }

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
        elseif ($item) {
            if ($item -eq 'minecraft:end_crystal') {
                if ($m.PSObject.Properties.Name -contains 'count') { $m.count = 128 }
                else { $m | Add-Member -NotePropertyName count -NotePropertyValue 128 }
            }
            $sub = Resolve-Sub $category $item
        }
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

$gunCount = 0
foreach ($g in $map.guns.PSObject.Properties) {
    if (Copy-GunData $g.Name $g.Value) { $gunCount++ }
}
Write-Host "Wrote $gunCount gun data overrides"

# Copy the static vanilla shaped-recipes (crafting tables / ammo box / target)
# that live in the TACZ mod jar, re-tiered to wrought iron. These go in the
# recipes ROOT (vanilla RecipeManager path), unlike gun/ammo/attachments.
if (-not (Test-Path $blockSrc)) { throw "Missing block recipes: $blockSrc" }
foreach ($bf in (Get-ChildItem $blockSrc -Filter '*.json')) {
    Copy-Item $bf.FullName (Join-Path $outRoot $bf.Name) -Force
    $total++
}

Write-Host "Wrote $total override recipes to $outRoot"
