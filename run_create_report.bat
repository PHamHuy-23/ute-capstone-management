@echo off
chcp 65001 > nul
echo ========================================================
echo DANG TAO BAO CAO SUA LOI KY THUAT (.DOCX)...
echo ========================================================

python -c "import docx" 2>nul
if %errorlevel% equ 0 (
    echo [THONG BAO] Tim thay python-docx! Dang chay generate_report_docx.py...
    python generate_report_docx.py
) else (
    echo [THONG BAO] Khong tim thay python-docx, dang dung bo tao OpenXML chuyen dung build_docx.py...
    python -c "import build_docx; data = build_docx.build_docx_archive(); open('BAO_CAO_SUA_LOI_KY_THUAT.docx', 'wb').write(data); print('Da tao thanh cong BAO_CAO_SUA_LOI_KY_THUAT.docx!')"
)

if exist "BAO_CAO_SUA_LOI_KY_THUAT.docx" (
    echo ========================================================
    echo THANH CONG! Tep 'BAO_CAO_SUA_LOI_KY_THUAT.docx' da duoc tao tai:
    echo %~dp0BAO_CAO_SUA_LOI_KY_THUAT.docx
    echo ========================================================
) else (
    echo [LOI] Khong tao duoc file docx!
)
