@echo off
chcp 65001 > nul
cls

:: Проверяем, лежит ли файл java.java в этой же папке
if exist "java.java" (
    echo [СИСТЕМА] Вы запускаете игру 2D...
    
    :: Прямой запуск исходного файла java.java без компиляции
    java "java.java"
    
    goto end
) else (
    echo Завершить
    goto end
)

:end
pause

