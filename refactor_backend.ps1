$ErrorActionPreference = "Stop"

$files = Get-ChildItem -Path "d:\Licence\Licence 3\JBA\code\facturation\src\main\java" -Recurse -Filter *.java
foreach ($file in $files) {
    $content = [System.IO.File]::ReadAllText($file.FullName)
    $newContent = $content -creplace 'FicheAtelier', 'OrdreReparation' `
                           -creplace 'ficheAtelier', 'ordreReparation' `
                           -creplace 'FichesAtelier', 'OrdresReparation' `
                           -creplace 'fichesAtelier', 'ordresReparation' `
                           -creplace 'fiches_atelier', 'ordres_reparation' `
                           -creplace 'fiche_atelier', 'ordre_reparation' `
                           -creplace 'StatutFiche', 'StatutOrdreReparation' `
                           -creplace 'fiches-atelier', 'ordres-reparation' `
                           -creplace 'fiche-atelier', 'ordre-reparation'
    
    if ($content -cne $newContent) {
        [System.IO.File]::WriteAllText($file.FullName, $newContent, [System.Text.Encoding]::UTF8)
        Write-Host "Updated $($file.FullName)"
    }
}
