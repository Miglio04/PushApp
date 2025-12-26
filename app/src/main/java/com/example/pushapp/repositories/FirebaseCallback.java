package com.example.pushapp.repositories;

/**
 * Un'interfaccia generica per gestire le callback asincrone da Firebase,
 * permettendo di restituire un risultato di successo o un'eccezione.
 * @param <T> Il tipo di dato atteso in caso di successo.
 */
public interface FirebaseCallback<T> {
    /**
     * Chiamato quando l'operazione ha successo.
     * @param result Il risultato dell'operazione.
     */
    void onSuccess(T result);

    /**
     * Chiamato quando l'operazione fallisce.
     * @param e L'eccezione che ha causato l'errore.
     */
    void onError(Exception e);
}
