[Setup]
AppName=Mon Script
AppVersion=1.0
DefaultDirName={pf}\MonScript
OutputDir=.
OutputBaseFilename=Install_MonScript
Compression=lzma
SolidCompression=yes
PrivilegesRequired=admin

[Files]
Source: "monfic.bat";        DestDir: "{app}"; Flags: ignoreversion
Source: "monlaunch.vbs";     DestDir: "{app}"; Flags: ignoreversion
Source: "noteFac.ico";      DestDir: "{app}"; Flags: ignoreversion

[Icons]
Name: "{group}\Mon Script"; Filename: "wscript.exe"; WorkingDir: "{app}";Parameters: """{app}\monlaunch.vbs""";IconFilename: "{app}\noteFac.ico"



[Run]
Filename: "wscript.exe"; Parameters: """{app}\monlaunch.vbs"""; Flags: runhidden waituntilterminated


