[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-24ddc0f5d75046c5622901739e7c5dd533143b0c8e959d652212380cedb1ea36.svg)](https://classroom.github.com/a/njY4ZnhJ)
# Misión Principal - Actividad Práctica 1

## Librería para la manejo de sentencias SQL en archivos SCV.

Se necesita un sistema en Java que sea capaz de interpretar las sentencias `CREATE TABLE`, `INSERT`, `UPDATE`, `SELECT`, `DELETE`, así como otros comandos SQL utilizando archivos CSV para las consultas de datos. Se entenderá que cada *Tabla* corresponderá a un archivo CSV en la ruta de trabajo (**Base de Datos**), en donde cada campo/atributo de la tabla corresponderá a una columna del archivo CSV.


A continuación se describirán cada una de las sentencias.


  1. Se deberá establecer una ruta de trabajo, el cual será el directorio desde donde se leerán y/o crearán los archivos. El nombre de la carpeta corresponderá al nombre de la base de datos. Lo anterior mediante el comando `USE $PATH$` donde *$PATH$* será la ruta relativa o obsoluta de la carpeta de trabajo.
  2. Una vez establecida la ruta de trabajo, se deberá poder mostrar el listado de las *Tablas* (archivos CSV en la ruta de trabajo) mediante el comando `SHOW TABLES`.
  3. El comando `CREATE TABLE` deberá ser capáz de crear un nuevo archivo, respetando la sintaxis de SQL para la creación de los campos. Por ejemplo:
  >```sql
  > CREATE TABLE Alumnos (
  >   id INT NOT NULL PRIMARY KEY,
  >    nombre VARCHAR(20) NOT NULL,
  >    app VARCHAR(20) NOT NULL,
  >    apm VARCHAR(20) NOT NULL,
  >    edad INT NULL
  >);
  >```
  dará como resultado el archivo `$PATH$/Alumnos.csv` con las columnas:
  > `id,nombre,app,apm,edad`.

  4. El comando `DROP TABLE` deberá ser capáz de eliminar el archivo de la carpeta. **Se deberá preguntar si realmente quiere borrar el archivo**.
  5. El comando `INSERT` deberá ser capáz de insertar una nueva línea en el archivo/tabla. Se deberá respetar la sintaxis de SQL. Por ejemplo: `INSERT INTO table_name (column1, column2,column3, ...) VALUES (value1, value2, value3, ...);`.
  6. El comando `DELETE` deberá ser capáz de borrar una fila o un conjunto de filas respetando la sección `WHERE` de la sintaxis de SQL (`DELETE FROM table_name WHERE condition;`). 
  > **NOTA:**
  > Se deberá respetar las búsquedas complejas utilizando los comandos `AND` Y `OR`. Por ejemplo: `DELETE FROM Alumnos WHERE (app='González' AND apm <> 'Hernández') OR id=25;`; Sólo borraría a los alumnos con apellidos *González Hernández* o aquél con *id=25*. 

  7. El comando `UPDATE` actualizará las columnas de una fila o un conjunto de filas. Se deberá respetar la sintaxis de SQL. `UPDATE table_name SET column1 = value1, column2 = value2, ... WHERE condition;`. **NOTA: la sección de `WHERE condition` respeta el mismo formato del comando `DELETE`.
  8. El comando `SELECT` obtendrá el conjunto de datos desde la tabla/archivo respetando la sintaxys de SQL `SELECT [*] | select_expr [, select_expr] ... [as alias] [FROM  table_reference [WHERE where_condition]`.


## Entregables.

A continuación se describe el conjunto de entregables:

  1. Librería en archivo `JAR` auto ejecutable.
  2. Código fuente, éste se mantendrá en el respositorio.
  3. Como parte del repositorio se deberán programar todas las pruebas unitarias necesarias para probar el funcionamiento del sistema.
  4. Se deberá realizar un reporte que contenga la explicación del funcionamiento del sistema, así como la explicación a detalle de las pruebas unitarias realizadas. **EL REPORTE SE SUBIRÁ EN FORMATO PDF EN CLASSROOM*.

