# Java Café - Point of Sale

A simple Java Swing application for managing a Point of Sale (POS) system.

## How to Compile and Run

1. Open your terminal (or Command Prompt) in this folder.
2. Run the provided script:
   ```bash
   .\compile_and_run.bat
   ```

### Manual Method
If you don't want to use the script, you can do it manually:

1. **Compile:**
   ```bash
   javac -d bin src\javacafe\*.java src\javacafe\model\*.java src\javacafe\gui\*.java src\javacafe\exceptions\*.java
   ```
2. **Run:**
   ```bash
   java -cp bin javacafe.Main
   ```
