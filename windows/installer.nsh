!include nsDialogs.nsh
Var DesktopChoice
Var DesktopCheckbox
!macro customPageAfterChangeDir
  Page custom DesktopShortcutPage DesktopShortcutLeave
!macroend
Function DesktopShortcutPage
  !insertmacro MUI_HEADER_TEXT "Desktop shortcut" "Choose how to access Notepad 98."
  nsDialogs::Create 1018
  Pop $0
  ${NSD_CreateCheckbox} 0 12u 100% 20u "Create a desktop shortcut"
  Pop $DesktopCheckbox
  ${NSD_Check} $DesktopCheckbox
  nsDialogs::Show
FunctionEnd
Function DesktopShortcutLeave
  ${NSD_GetState} $DesktopCheckbox $DesktopChoice
FunctionEnd
!macro customInstall
  ; Silent installs retain the existing shortcut behavior. Interactive installs honor the checkbox.
  ${IfNot} ${Silent}
    ${If} $DesktopChoice == ${BST_CHECKED}
      CreateShortCut "$DESKTOP\Notepad 98.lnk" "$appExe" "" "$appExe" 0
    ${Else}
      Delete "$DESKTOP\Notepad 98.lnk"
    ${EndIf}
  ${EndIf}
!macroend
