Run unit tests and report results.

Use this PowerShell command:
```
$env:JAVA_HOME = "D:\For_work\AS\jbr"; $env:PATH = "$env:JAVA_HOME\bin;$env:PATH"; Set-Location D:\diet_helper; .\gradlew.bat :app:testDebugUnitTest 2>&1 | Select-String -Pattern "PASSED|FAILED|ERROR|tests|BUILD" | Select-Object -Last 40
```

Report: how many tests passed/failed. If failures — show test name, expected vs actual. Skip Gradle daemon noise.
