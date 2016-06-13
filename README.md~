# ASAP: A Framework for Designing Gamified Models of Complex Systems

## Guia de uso

 1. Crea un nuevo proyecto para Android con un Activity vacío.
 2. Descarga la librería ASAP desde https://bitbucket.org/dasolma/asap (en build/outpus/aar)
 3. Agrega estas líneas a build.gradle

'''        
flatDir {
 dirs 'libs'
} 
'''
 4. Actualiza las dependencias agregando la libería de ASAP y google play services:

'''
dependencies {
    compile(name:'asaplib-release', ext:'aar')
    compile 'com.android.support:appcompat-v7:23.1.1'
    compile 'com.google.android.gms:play-services:6.5.+'
}
'''

  5. Crea las clases de gamificación y la del modelo con la mínima funcionalidad.
  6. Crea el archivo de configuración config.xml en values con el siguiente contenido:

'''
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <item name="game" type="string">dasolma.com.gameoflife.[Nombre de tu clase de gamificación]</item>
</resources>
'''

  7. Agregar la siguiente líneas para invocar el modelo (por ejemplo en el activity inicial):

'''
//Elemento del layout donde se incrustará el entorno gráfico
Factory.setGlContentId(R.id.glcontent);
        
Intent intent = new Intent(MainActivity.this, [Nombre de tu clase de gamificaición].class);
startActivity(intent);
'''
