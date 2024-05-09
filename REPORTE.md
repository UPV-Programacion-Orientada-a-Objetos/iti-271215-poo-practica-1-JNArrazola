# **Bilioteca SQL**
> Objetivo: Crear una biblioteca en java que sea capaz de ejecutar comandos SQL.

## ¿Cómo funciona?
### Primera fase: Identificar la sentencia.
Como intentamos replicar SQL, el cual es un lenguaje formal, es lógico darnos cuenta que una sentencia *select* siempre iniciará con la palabra reservada `select`, aparte de eso debe tener contener también la palabra reservada `from`. Todas estas cosas nos dan indicio de lo que es y lo que no es una correcta sentencia **sql**, de esta manera, la primera fase del código se basa en diferenciar.
```java
public static String parseQuery(String query) throws Exception {
        String brokeStr[] = query.split(" ");
        try {
            if (brokeStr.length <= 1 || !parenthesisCheck(query))
                throw new RuntimeException("Query incompleta");
            if (brokeStr[0].equalsIgnoreCase("USE")) {
                return FileManagement.useDatabase(query, brokeStr);
            } else if (brokeStr[0].equalsIgnoreCase("UPDATE")) {
                return update(query);
            } else if (brokeStr[0].equalsIgnoreCase("DELETE") && brokeStr[1].equalsIgnoreCase("FROM")) {
                return deleteFrom(query);
            } else if (brokeStr[0].equalsIgnoreCase("SELECT")) {
                return select(query);
            } else if (brokeStr[0].equalsIgnoreCase("INSERT") && brokeStr[1].equalsIgnoreCase("INTO")) {
                return (insertInto(query));
            } else if (brokeStr[0].equalsIgnoreCase("CREATE") && brokeStr[1].equalsIgnoreCase("TABLE")) {
                return createTable(query);
            } else if (brokeStr[0].equalsIgnoreCase("SHOW") && brokeStr[1].equalsIgnoreCase("TABLES")) {
                return showTables(query, brokeStr);
            } else if (brokeStr[0].equalsIgnoreCase("DROP") && brokeStr[1].equalsIgnoreCase("TABLE")) {
                return dropTable(query, brokeStr);
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }

        return "No se reconoció la sentencia";
    }
```
### Segunda fase: Trabajar la sentencia.
En realidad esto sería demasiado extenso, así que explicaré brevemente como funciona cada sentencia:

#### Cosas en común.
En todas las sentencias que se requiere consultar una tabla es necesario verificar que exista un path.
```java
if (FileManagement.getDatabasePath() == null)
    throw new NullPointerException("No hay path designado");
```
Por otro lado, siempre verifico que tengamos el mínimo número de palabras necesarias para hacer la sentencia válida, en el sentido que, por ejemplo, para un **show tables**, se necesita de al menos dos:
~~~java
if (brokeStr.length > 2)
    throw new Exception("Sintaxis incorrecta");
~~~
#### Show tables
Se listan todos los archivos `.csv` en el directorio actual, ignorando por supuesto la extensión, pues está no forma parte del nombre.
~~~java
try {
    if (files.length == 0)
            return "No hay archivos para listar";
    } catch (NullPointerException e) {
        throw new NullPointerException("El directorio no se encontró");
    }

    for (File file : files) {
        String name = file.getName();
        if (name.contains("aux"))
            continue;
        if (file.getName().contains(".csv") && !file.getName().contains("~"))
            System.out.println("* " + name.substring(0, name.indexOf(".csv")));
    }
    return "Listado éxitoso";
~~~

#### Create table
En sentencias como `Create Table`, la forma de solucionar el problema siempre sigue los mismos pasos, identificar donde esta el nombre de la tabla, pues primero verifico que si exista, de otra forma es inútil intentar realizar cualquier acción, aunque 
