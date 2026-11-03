[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [string]$Path
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
$root = [System.IO.Path]::GetFullPath([System.IO.Path]::Combine($PSScriptRoot, '..', '..'))
$source = [System.IO.Path]::GetFullPath([System.IO.Path]::Combine($root, $Path))
if (-not $source.StartsWith($root + [System.IO.Path]::DirectorySeparatorChar, [System.StringComparison]::OrdinalIgnoreCase)) {
    throw "Backup source is outside the project: $source"
}
if ($source.Substring($root.Length).Contains(':')) {
    throw "Backup source contains an alternate data stream: $source"
}
$item = Get-Item -LiteralPath $source -Force
if ($item.PSIsContainer) {
    throw "Backup source must be a file: $source"
}
$directory = $source
while ($directory.Length -ge $root.Length) {
    if (((Get-Item -LiteralPath $directory -Force).Attributes -band [System.IO.FileAttributes]::ReparsePoint) -ne 0) {
        throw "Backup source traverses a link or junction: $directory"
    }
    if ($directory -eq $root) {
        break
    }
    $directory = [System.IO.Path]::GetDirectoryName($directory)
}
$time = Get-Date -Format 'yyyyMMddTHHmmssfffffff'
$destination = [System.IO.Path]::Combine($item.DirectoryName, '.' + $item.Name + '.' + $time + '.bak')
# File.Move refuses an existing destination, including a collision after validation.
[System.IO.File]::Move($source, $destination)
Write-Output $destination
