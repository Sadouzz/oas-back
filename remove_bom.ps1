$ErrorActionPreference = "Stop"
$utf8NoBom = New-Object System.Text.UTF8Encoding($false)
$files = Get-ChildItem -Path "d:\Licence\Licence 3\JBA\code\facturation\src\main\java" -Recurse -Filter *.java
foreach ($file in $files) {
    $bytes = [System.IO.File]::ReadAllBytes($file.FullName)
    if ($bytes.Length -ge 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) {
        $content = [System.IO.File]::ReadAllText($file.FullName)
        [System.IO.File]::WriteAllText($file.FullName, $content, $utf8NoBom)
        Write-Host "Removed BOM from $($file.FullName)"
    }
}
