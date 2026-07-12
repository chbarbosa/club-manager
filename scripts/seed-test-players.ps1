param(
    [string] $BaseUrl = "http://localhost:8080",
    [string] $Username = "admin",
    [string] $Password = "admin123",
    [switch] $DryRun
)

$ErrorActionPreference = "Stop"

function New-PlayerSeed {
    param(
        [string] $Name,
        [string] $Birthdate,
        [string[]] $Positions,
        [string] $RegistrationNumber
    )

    [pscustomobject]@{
        name = $Name
        birthCountry = "Brazil"
        livingCountry = "Brazil"
        birthdate = $Birthdate
        teamCategory = "MASCULINE"
        positions = $Positions
        registrationNumber = $RegistrationNumber
        memberSince = "2026-01-01"
    }
}

function Get-TestPlayers {
    $players = @()

    for ($i = 1; $i -le 20; $i++) {
        if ($i -le 2) {
            $positions = @("GOALKEEPER")
        } elseif ($i -le 8) {
            $positions = @("DEFENSE")
        } elseif ($i -le 14) {
            $positions = @("MIDFIELD")
        } else {
            $positions = @("ATTACK")
        }

        $players += New-PlayerSeed `
            -Name ("U15 Test Player {0:00}" -f $i) `
            -Birthdate "2010-08-15" `
            -Positions $positions `
            -RegistrationNumber ("DEV-U15-{0:00}" -f $i)
    }

    $players += New-PlayerSeed -Name "U16 Filter Test Player" -Birthdate "2009-08-15" -Positions @("DEFENSE") -RegistrationNumber "DEV-FILTER-U16"
    $players += New-PlayerSeed -Name "U17 Filter Test Player" -Birthdate "2008-08-15" -Positions @("MIDFIELD") -RegistrationNumber "DEV-FILTER-U17"
    $players += New-PlayerSeed -Name "U18 Filter Test Player" -Birthdate "2007-08-15" -Positions @("ATTACK") -RegistrationNumber "DEV-FILTER-U18"
    $players += New-PlayerSeed -Name "U19 Filter Test Player" -Birthdate "2006-08-15" -Positions @("GOALKEEPER") -RegistrationNumber "DEV-FILTER-U19"
    $players += New-PlayerSeed -Name "U20 Filter Test Player" -Birthdate "2005-08-15" -Positions @("DEFENSE", "MIDFIELD") -RegistrationNumber "DEV-FILTER-U20"

    return $players
}

function Get-AllPlayers {
    param([hashtable] $Headers)

    $allPlayers = @()
    $page = 0
    $totalPages = 1

    while ($page -lt $totalPages) {
        $response = Invoke-RestMethod `
            -Method Get `
            -Uri "$BaseUrl/api/v1/players?page=$page&size=200&includeInactive=true" `
            -Headers $Headers

        $allPlayers += @($response.content)
        $totalPages = $response.totalPages
        $page++
    }

    return $allPlayers
}

$loginBody = @{
    username = $Username
    password = $Password
} | ConvertTo-Json

$login = Invoke-RestMethod `
    -Method Post `
    -Uri "$BaseUrl/api/v1/auth/login" `
    -ContentType "application/json" `
    -Body $loginBody

$headers = @{ Authorization = "Bearer $($login.token)" }
$existingPlayers = Get-AllPlayers -Headers $headers
$seedPlayers = Get-TestPlayers

$created = 0
$skipped = 0

foreach ($player in $seedPlayers) {
    $exists = @($existingPlayers | Where-Object {
        $_.registrationNumber -eq $player.registrationNumber -or $_.name -eq $player.name
    }).Count -gt 0

    if ($exists) {
        Write-Host "Skip existing player: $($player.name)"
        $skipped++
        continue
    }

    if ($DryRun) {
        Write-Host "Would create player: $($player.name)"
        continue
    }

    $body = $player | ConvertTo-Json -Depth 5
    Invoke-RestMethod `
        -Method Post `
        -Uri "$BaseUrl/api/v1/players" `
        -Headers $headers `
        -ContentType "application/json" `
        -Body $body | Out-Null

    Write-Host "Created player: $($player.name)"
    $created++
}

Write-Host "Seed complete. Created=$created Skipped=$skipped DryRun=$($DryRun.IsPresent)"
