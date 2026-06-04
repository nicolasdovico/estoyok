Para replicar la metodología de este proyecto en uno nuevo, debes implementar el sistema de gestión por agentes  (Ralph) y la estructura de documentación que permite la autonomía del desarrollo. A continuación, se detalla la guía técnica paso a paso.

  1. Archivos Core (Copiar tal cual)


  Estos archivos contienen la lógica del motor de ejecución y no requieren modificaciones, a menos que desees cambiar el comportamiento del agente.


   * ralph.sh: Es el script que orquesta las llamadas a la IA y gestiona el bucle de ejecución. Asegúrate de darle permisos de ejecución (chmod +x ralph.sh).
   * hooks/verify_state.sh: (Si existe en tu estructura) Utilizado por el agente para verificar la integridad del
     entorno antes y después de cada iteración.

  2. Infraestructura de Control (Copiar y Limpiar)


  Estos archivos mantienen el estado y el historial. Debes copiarlos pero vaciar su contenido para empezar de cero.


   * ralph-log.md: Archivo donde el agente registra sus pensamientos, acciones y resultados de cada iteración.
   * docs/progress.txt: Resumen de alto nivel del progreso del proyecto. El agente lo actualiza automáticamente.
   * iteraciones/: Carpeta que almacena los prompts y respuestas de cada ciclo. Debe estar vacía al inicio.

  3. Definición de Reglas y Mandatos (Personalizar)

  Este es el archivo más crítico. Define la "personalidad" y los límites del agente.


   * GEMINI.md:
       * Mandatos de Seguridad: Copia las reglas de protección de credenciales y manejo de GIT.
       * Estándares de Ingeniería: Define aquí si usarás un stack específico (ej: Python/FastAPI en lugar de
         PHP/Laravel).
       * Flujo de Trabajo: Copia la sección de Investigación -> Estrategia -> Ejecución.
       * Contexto del Proyecto: Cambia la descripción general para que el agente sepa qué está construyendo (ej:
         "Eres un experto en sistemas de inventario").


  4. Planificación y Roadmap (Generar Nuevo)

  Estos archivos deben crearse desde cero con la información de tu nuevo proyecto.


   * README.md: Descripción técnica básica, stack tecnológico y guía de instalación inicial.
   * docs/prd-v1.md: Documento de Requerimientos del Producto. Define qué debe hacer la aplicación, los usuarios y las funcionalidades principales.
   * docs/tasks.md: Lista de tareas pendientes. Puedes empezar con tareas de alto nivel (ej: "Configurar
     estructura base del backend").
   * docs/status.md: Estado actual de los módulos del sistema.


  5. Configuración del Entorno de IA

  Para que el agente funcione fuera de este entorno específico, necesitas:


      1. Variables de Entorno: Un archivo .env en la raíz (generalmente ignorado por git) que contenga la API Key necesaria para que ralph.sh pueda comunicarse con el modelo de lenguaje.
      2. Permisos de Herramientas: Si el nuevo proyecto requiere herramientas específicas (como compiladores o CLI de bases de datos), asegúrate de que estén instaladas en el sistema donde correrá Ralph.

  Pasos para iniciar el nuevo proyecto


   1. Inicialización: Crea la carpeta del proyecto y ejecuta git init.
   2. Estructura de Carpetas: Crea manualmente docs/, iteraciones/, backend/ y frontend/ (según aplique).
   3. Transferencia de Scripts: Copia ralph.sh y GEMINI.md.
   4. Definición Inicial: Escribe el docs/prd-v1.md y las primeras 5 tareas en docs/tasks.md.
   5. Primer Ciclo: Ejecuta ./ralph.sh y dale la primera instrucción directa, por ejemplo: "Analiza el PRD y
      propón la estructura de base de datos en un nuevo archivo docs/design.md".

  Recomendaciones de la Metodología


   * Bucle de Tareas: Nunca pidas al agente que haga "todo el proyecto". Pídele que lea docs/tasks.md, elija la tarea prioritaria y la ejecute.
   * Validación Estricta: Mantén en GEMINI.md la instrucción de que ninguna tarea se considera terminada sin pruebas (tests) que la respalden.
   * Logs: Revisa periódicamente ralph-log.md para entender si el agente está teniendo problemas de lógica o bucles infinitos de error.