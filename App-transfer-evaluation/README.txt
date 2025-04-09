# 📦 TransferApp

Aplicación de escritorio desarrollada en Java para cargar, filtrar y procesar archivos Excel o CSV de forma automática, mostrando además información del sistema en tiempo real.

---

## 🚀 ¿Qué hace?

- Permite seleccionar un archivo Excel o CSV desde el equipo.
- Valida qué registros están completos.
- Copia el archivo original a una carpeta interna.
- Genera un nuevo archivo con la mitad de los registros válidos.
- Muestra el progreso en una barra y los resultados finales.
- Muestra información del sistema operativo, CPU, RAM, disco y red.

---

## 📁 Estructura del proyecto

```
App-transfer-evaluation/
├── TransferApp.jar                → Ejecutable con dependencias incluidas
├── start_windows.bat             → Script para ejecutar en Windows
├── start_linux.sh                → Script para ejecutar en Linux/macOS
├── README.md                     → Este archivo
```

---

## ⚙️ Requisitos

- Java 17 o superior (JDK instalado)
- Sistema operativo compatible con Java (Windows, Linux, macOS)

---

## 🪟 Cómo ejecutar en Windows

### Opción 1: Doble clic

Solo abre el archivo `TransferApp.jar` con doble clic.

### Opción 2: Terminal

Abre CMD o Git Bash en la carpeta y escribe:

```bash
java -jar TransferApp.jar
```

---

## 🐧 Cómo ejecutar en Linux / macOS

Abre una terminal y da permisos de ejecución:

```bash
chmod +x start_linux.sh
./start_linux.sh
```

También puedes ejecutar el `.jar` directamente:

```bash
java -jar TransferApp.jar
```

---

## 📂 Carpetas generadas automáticamente

- `archivos_cargados/` → donde se almacena una copia del archivo original cargado
- `archivos_procesados/` → donde se guardan los archivos generados con los registros válidos

---

## 📄 Explicación técnica del proyecto

### 🔧 Estructura general

Proyecto Java Swing con lógica separada en paquetes `gui`, `service`, `repository`.

---

### 📁 com.transfer.evaluation.gui

**MainWindow.java**
- Ventana principal con botones:
  - Cargar archivo
  - Iniciar transferencia
  - Eliminar archivos generados
- Muestra progreso y resultados.
- Invoca servicios de lógica de negocio.

**SystemInfoGUI.java**
- Panel con datos del sistema.
- Se actualiza automáticamente en segundo plano.

---

### 📁 com.transfer.evaluation.service

**ExcelTransferService.java**
- Lógica principal del proyecto:
  - `copiarArchivo()`: guarda archivo cargado.
  - `procesarArchivo()`: determina si es CSV o Excel.
  - `procesarExcel()`: extrae filas válidas de un Excel.
  - `procesarCSV()`: lo mismo, pero desde texto plano.
  - `esRegistroCompleto()`: comprueba que todas las celdas estén completas.
  - `escribirNuevoCSV()` / `escribirNuevoExcel()`: genera archivos de salida.

**TransferResult.java**
- Objeto de transferencia (`DTO`) con:
  - Registros leídos
  - Registros creados

---

### 📁 Otros

**Main.java**
- Punto de entrada. Solo invoca `MainWindow.mostrar()`.

---

### 🛡️ Validaciones incluidas

- Archivos CSV mal formateados (líneas vacías, BOM, etc.)
- Archivos sin encabezado
- Registro incompleto → se descarta
- Archivo de salida sin encabezado → se evita error

---

## 📌 Consideraciones finales

- `JFileChooser` usa el explorador nativo según sistema operativo.
- Multiplataforma: funciona en Windows, Linux y macOS.
- Código 100% portable: empaquetado con dependencias.

---