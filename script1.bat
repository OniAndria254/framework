set src_dir=C:/e-bossy/S4/Web Dyn/framework/framework/src
set temp_src=C:/e-bossy/S4/Web Dyn/framework/framework/temp-src

set bin_dir=C:/e-bossy/S4/Web Dyn/framework/framework/bin
set lib_dir=C:/e-bossy/S4/Web Dyn/hello-spring/lib
set work_dir=C:/e-bossy/S4/Web Dyn/framework/framework

mkdir "%temp_dir%"
mkdir "%temp_src%"

FOR /R "%src_dir%" %%F IN (*.java) DO (
    copy "%%F" "%temp_src%"
)

cd "%temp_src%"
javac -sourcepath "%temp_src%" -d "%bin_dir%" *.java

jar -cvf "%work_dir%/myServlet.jar" -C "%bin_dir%" . 