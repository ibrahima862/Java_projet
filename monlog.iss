[Setup]
AppName=Mon Script
AppVersion=1.0
DefaultDirName={pf}\MonScript
OutputDir=.
OutputBaseFilename=monfichier
Compression=lzma
SolidCompression=yes
PrivilegesRequired=admin

[Files]
Source: "monfic.bat";       DestDir: "{app}"; Flags: ignoreversion
Source: "monlaunch.vbs";    DestDir: "{app}"; Flags: ignoreversion
Source: "out\*";            DestDir: "{app}\out"; Flags: recursesubdirs createallsubdirs ignoreversion

[Icons]
Name: "{group}\Mon Script"; Filename: "{app}\monfic.bat"

[Run]
Filename: "wscript.exe"; Parameters: """{app}\monlaunch.vbs"""; Flags: runhidden
