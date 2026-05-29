Run KSP code generation and report results.

Use this PowerShell command:
```
$env:JAVA_HOME = "D:\For_work\AS\jbr"; $env:PATH = "$env:JAVA_HOME\bin;$env:PATH"; Set-Location D:\diet_helper; .\gradlew.bat :app:kspDebugKotlin --info 2>&1 | Select-String -Pattern "error:|Error|Exception|FAILED|warning:|BUILD" | Select-Object -Last 40
```

If BUILD SUCCESSFUL — report success with no extra text.
If BUILD FAILED — show only the error lines, grouped by file if possible. Skip Gradle daemon noise.
