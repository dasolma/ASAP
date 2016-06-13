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
