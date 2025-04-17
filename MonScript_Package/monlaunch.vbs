Set fso = CreateObject("Scripting.FileSystemObject")
Set shell = CreateObject("WScript.Shell")
scriptPath = fso.GetParentFolderName(WScript.ScriptFullName)
shell.Run Chr(34) & scriptPath & "\monfic.bat" & Chr(34), 0
