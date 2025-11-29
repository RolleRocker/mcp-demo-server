# Integration smoke test: run the jar once and send multiple JSON-RPC requests over one stdin stream
# Exits with code 0 on success, non-zero on failure.

$jar = "target\mcp-demo-server.jar"
if (-not (Test-Path $jar)) {
    Write-Error "Jar not found at $jar. Build first with: mvn clean package -DskipTests"
    exit 2
}

# Prepare multiple JSON-RPC requests (one per line). This simulates a persistent stdio session.
$requests = @'
{"jsonrpc":"2.0","id":1,"method":"tools/call","params":{"name":"create_note","arguments":{"title":"Integration","content":"integration note"}}}
{"jsonrpc":"2.0","id":2,"method":"tools/call","params":{"name":"list_notes","arguments":{}}}
{"jsonrpc":"2.0","id":3,"method":"tools/call","params":{"name":"calculate","arguments":{"operation":"add","a":2,"b":3}}}
{"jsonrpc":"2.0","id":4,"method":"tools/call","params":{"name":"write_file","arguments":{"file_path":"smoke_test_dir/integration.txt","content":"integration file"}}}
{"jsonrpc":"2.0","id":5,"method":"tools/call","params":{"name":"read_file","arguments":{"file_path":"smoke_test_dir/integration.txt"}}}
{"jsonrpc":"2.0","id":6,"method":"tools/call","params":{"name":"list_directory","arguments":{"directory_path":"smoke_test_dir"}}}
{"jsonrpc":"2.0","id":7,"method":"resources/list","params":{}} 
{"jsonrpc":"2.0","id":8,"method":"resources/read","params":{"uri":"demo://info"}} 
{"jsonrpc":"2.0","id":9,"method":"prompts/list","params":{}} 
{"jsonrpc":"2.0","id":10,"method":"prompts/get","params":{"name":"helpful_assistant","arguments":{"task":"Integration test"}}}
'@

# Write requests to a temporary input file
$inputFile = [System.IO.Path]::GetTempFileName()
$requests | Out-File -FilePath $inputFile -Encoding utf8

# Prepare temp files for output capture
$outFile = [System.IO.Path]::GetTempFileName()
$errFile = [System.IO.Path]::GetTempFileName()

Write-Host "Running integration test against $jar"

# Start java and redirect stdin/stdout/stderr
$startInfo = New-Object System.Diagnostics.ProcessStartInfo
$startInfo.FileName = "java"
$startInfo.Arguments = "-jar `"$jar`""
$startInfo.RedirectStandardInput = $true
$startInfo.RedirectStandardOutput = $true
$startInfo.RedirectStandardError = $true
$startInfo.UseShellExecute = $false
$proc = New-Object System.Diagnostics.Process
$proc.StartInfo = $startInfo
$proc.Start() | Out-Null

# Feed the requests and close stdin
[System.IO.StreamWriter] $writer = $proc.StandardInput
Get-Content -Path $inputFile | ForEach-Object { $writer.WriteLine($_) }
$writer.Close()

# Read stdout and stderr until process exits
[string] $stdout = $proc.StandardOutput.ReadToEnd()
[string] $stderr = $proc.StandardError.ReadToEnd()
$proc.WaitForExit()

Write-Host "---- STDERR ----"
Write-Host $stderr
Write-Host "---- STDOUT ----"
Write-Host $stdout

# Basic checks: ensure note creation acknowledged and list_notes contains the created title, and calculate result is present
$passed = $true
if ($stdout -notmatch "Note created successfully") { Write-Host "Missing note creation confirmation"; $passed = $false }
if ($stdout -notmatch "Integration") { Write-Host "Created note not found in list output"; $passed = $false }
if ($stdout -notmatch "Result:.*5") { Write-Host "Calculate result not found"; $passed = $false }

# File operations assertions
if ($stdout -notmatch "File written successfully: smoke_test_dir/integration.txt") { Write-Host "Write file confirmation missing"; $passed = $false }
if ($stdout -notmatch "File contents of smoke_test_dir/integration.txt") { Write-Host "Read file contents missing"; $passed = $false }
if ($stdout -notmatch "Contents of smoke_test_dir") { Write-Host "Directory listing missing"; $passed = $false }

# Resources and prompts
if ($stdout -notmatch "demo://info") { Write-Host "Resources list missing demo://info"; $passed = $false }
if ($stdout -notmatch "MCP Demo Server") { Write-Host "Resources read did not return server info"; $passed = $false }
if ($stdout -notmatch "helpful_assistant") { Write-Host "Prompts list missing helpful_assistant"; $passed = $false }
if ($stdout -notmatch "Run smoke tests|Integration test") { Write-Host "Prompts get did not include the provided task"; $passed = $false }

if ($passed) { Write-Host "Integration test passed"; exit 0 } else { Write-Host "Integration test failed"; exit 1 }
