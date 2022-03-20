@echo off
set FontForge="C:\Program Files (x86)\FontForgeBuilds\run_fontforge.exe"

if exist %FontForge% goto do_compile
goto ff_notfound

:do_compile
%FontForge% -c 'Open(^$1); Generate(^$2)' "vshbtn.svg" "vshbtn.ttf"
goto f_exit

:ff_notfound
echo Cannot find FontForge at "%FontForge%", please edit the batch file and 
echo change ff path or move your ff installation to this path
goto f_exit

:f_exit
pause