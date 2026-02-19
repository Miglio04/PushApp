# PushApp
*Incredibly Keen Engineers Association - I.K.E.A.*

1. Partecipanti:
- Migliaccio Matteo 909414
- Pedranzini Santiago 884850
- Piatti Riccardo 909687
- Raffaele Addamo Elia 909556

2. Modalità di debug\
   È stata implementata una modalità di debug per evitare di effettuare troppe chiamate all'API.
   Quando attiva, le chiamate all'API effettuate in ExerciseAPIDataSource vengono sostituite con una
   chiamata a ExerciseSampleDataSource che restituisce dati fittizi.
   Per disattivarla e fetchare direttamente dall'API è sufficiente modificare la variabile `DEBUG_MODE`
   e renderla `false` nel file `java/com/example/pushapp/utils/Constants.java`.\
   \
   Le chiamate a disposizione sono **3000** al mese e ad oggi, 19/02/2026, ne sono rimaste circa **1500**.
   Ad ogni chiamata a ExerciseAPIDataSource.fetchExercises() vengono consumate circa 15 chiamate all'API.
   ExerciseAPIDataSource.fetchExercises() viene chiamata quando non esiste una sessione o una volta a settimana.\
   \
   Al momento **l'applicazione è impostata in debug mode**, quindi non consuma chiamate all'API per evitare di terminarle inavvertitamente.
   Se si desidera testare l'applicazione fetchando direttamente dall'API è sufficiente disattivare la debug mode.